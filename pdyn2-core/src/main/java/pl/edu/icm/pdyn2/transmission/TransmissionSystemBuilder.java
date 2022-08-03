package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

public class TransmissionSystemBuilder {
    private final TransmissionService transmissionService;
    private final AgentStateService agentStateService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final StatsService statsService;

    private final RandomForChunkProvider randomForChunkProvider;

    @WithFactory
    public TransmissionSystemBuilder(TransmissionService transmissionService,
                                     AgentStateService agentStateService,
                                     AreaClusteredSelectors areaClusteredSelectors,
                                     Selectors selectors,
                                     StatsService statsService,
                                     RandomProvider randomProvider) {
        this.transmissionService = transmissionService;
        this.agentStateService = agentStateService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.statsService = statsService;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(TransmissionSystemBuilder.class);
    }

    public EntitySystem buildTransmissionSystem() {
        return EntityIterator.select(selectors.filtered(
                        areaClusteredSelectors.personSelector(),
                        selectors.hasComponents(Inhabitant.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, agent) -> {
                    if (!transmissionService.consideredForInfection(agent)) {
                        return;
                    }

                    EnumSampleSpace<Load> exposurePerLoad = transmissionService.gatherExposurePerLoadForAgent(
                            new EnumSampleSpace<>(Load.class),
                            agent);
                    float totalExposure = exposurePerLoad.sumOfProbabilities(); // TODO sumValues?

                    if (totalExposure > 0) {
                        var probability = transmissionService.exposureToProbability(totalExposure);
                        var chosenLoad = transmissionService.selectLoad(exposurePerLoad, random.nextDouble());
                        var adjustedProbability = transmissionService.adjustProbabilityWithImmunity(probability, chosenLoad, agent);

                        if (adjustedProbability > 0 && random.nextDouble() <= adjustedProbability) {
                            agentStateService.infect(agent, chosenLoad);
                            statsService.tickStageChange(Stage.LATENT);
                        }
                    }
                });
    }

}
