package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSource;
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
                        var probabilities = transmissionService.exposureToProbability(exposurePerLoad, agent);
                        var totalProbability = probabilities.sumOfProbabilities();
                        double r = random.nextDouble();
                        if (totalProbability > 0 && r <= totalProbability) {
                            var chosenLoad = transmissionService.selectLoad(probabilities, r);
                            agentStateService.infect(agent, chosenLoad);
                            EnumSampleSpace<ContextType> exposurePerContext = transmissionService.gatherExposurePerContextForAgent(
                                    new EnumSampleSpace<>(ContextType.class),
                                    agent);
                            var immunizationSource = agent.getOrCreate(ImmunizationSource.class);
                            immunizationSource.setHouseholdSource(exposurePerContext.getProbability(ContextType.HOUSEHOLD));
                            immunizationSource.setWorkplaceSource(exposurePerContext.getProbability(ContextType.WORKPLACE));
                            immunizationSource.setKindergartenSource(exposurePerContext.getProbability(ContextType.KINDERGARTEN));
                            immunizationSource.setSchoolSource(exposurePerContext.getProbability(ContextType.SCHOOL));
                            immunizationSource.setUniversitySource(exposurePerContext.getProbability(ContextType.UNIVERSITY));
                            immunizationSource.setBigUniversitySource(exposurePerContext.getProbability(ContextType.BIG_UNIVERSITY));
                            immunizationSource.setStreetSource(exposurePerContext.getProbability(ContextType.STREET_00) +
                                    exposurePerContext.getProbability(ContextType.STREET_10) +
                                    exposurePerContext.getProbability(ContextType.STREET_20) +
                                    exposurePerContext.getProbability(ContextType.STREET_30) +
                                    exposurePerContext.getProbability(ContextType.STREET_40) +
                                    exposurePerContext.getProbability(ContextType.STREET_50) +
                                    exposurePerContext.getProbability(ContextType.STREET_60) +
                                    exposurePerContext.getProbability(ContextType.STREET_70) +
                                    exposurePerContext.getProbability(ContextType.STREET_80) +
                                    exposurePerContext.getProbability(ContextType.STREET_90));
                            statsService.tickStageChange(Stage.LATENT);
                        }
                    }
                });
    }
}
