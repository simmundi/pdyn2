package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.context.*;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

public class TransmissionSystemBuilder {
    private final TransmissionConfig transmissionConfig;
    private final SimulationTimer simulationTimer;
    private final ImmunizationService immunizationService;
    private final ContextsService contextsService;
    private final AgentStateService agentStateService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final StatsService statsService;
    private final EnumSampleSpace<Load> relativeAlpha = new EnumSampleSpace<>(Load.class);
    private final RandomForChunkProvider randomForChunkProvider;
    private final RelativeAlphaConfig relativeAlphaConfig;

    @WithFactory
    public TransmissionSystemBuilder(ContextsService contextsService,
                                     TransmissionConfig transmissionConfig,
                                     SimulationTimer simulationTimer,
                                     ImmunizationService immunizationService,
                                     RelativeAlphaConfig relativeAlphaConfig,
                                     AgentStateService agentStateService,
                                     AreaClusteredSelectors areaClusteredSelectors,
                                     Selectors selectors,
                                     StatsService statsService,
                                     RandomProvider randomProvider) {
        this.transmissionConfig = transmissionConfig;
        this.simulationTimer = simulationTimer;
        this.immunizationService = immunizationService;
        this.contextsService = contextsService;
        this.agentStateService = agentStateService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.statsService = statsService;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(TransmissionSystemBuilder.class);
        this.relativeAlphaConfig = relativeAlphaConfig;
        for (Load currentLoad : Load.viruses()) {
            relativeAlpha.changeOutcome(currentLoad, relativeAlphaConfig.getRelativeAlpha(currentLoad));
        }
    }

    public EntitySystem buildTransmissionSystem() {
        return EntityIterator.select(selectors.filtered(
                        areaClusteredSelectors.personSelector(),
                        selectors.hasComponents(Inhabitant.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, entity) -> {
                    HealthStatus healthStatus = entity.get(HealthStatus.class);
                    if (healthStatus.getStage() != Stage.HEALTHY) {
                        // can't be infected if already infected. Ignore.
                        return;
                    }
                    Immunization immunization = entity.get(Immunization.class);

                    EnumSampleSpace<ContextInfectivityClass> source = new EnumSampleSpace<>(ContextInfectivityClass.class);
                    EnumSampleSpace<Load> infectivity = new EnumSampleSpace<>(Load.class);
                    var contexts = contextsService.findActiveContextsForAgent(entity);
                    contexts.forEach(context -> {
                        addContextInfectivity(context, infectivity);
                        addSourceContext(context, source);
                    });
                    infectivity.multiply(relativeAlpha);

                    float totalLevel = infectivity.sumOfProbabilities();
                    if (totalLevel > 0) {
                        var probability = 1 - Math.exp(-transmissionConfig.getAlpha() * totalLevel);
                        source.normalize();
                        infectivity.normalize();
                        var chosenLoad = infectivity.sample(random.nextDouble());
                        if (immunization != null) {
                            probability *= (1 - immunizationService.getImmunizationCoefficient(immunization,
                                    ImmunizationStage.LATENTNY,
                                    chosenLoad,
                                    simulationTimer.getDaysPassed()));
                        }
                        if (probability > 0 && random.nextDouble() <= probability) {
                            agentStateService.infect(entity, chosenLoad, source);
                            statsService.tickStageChange(Stage.LATENT);
                        }
                    }
                });
    }

    private void addContextInfectivity(Context context, EnumSampleSpace<Load> infectivity) {
        var weight = transmissionConfig.getTotalWeightForContextType(context.getContextType());
        var agentCount = context.getAgentCount();
        for (Contamination contamination : context.getContaminations()) {
            var loadInContext = contamination.getLoad();
            var levelInContext = contamination.getLevel() * weight / agentCount;
            infectivity.increaseOutcome(loadInContext, levelInContext);
        }
    }

    private void addSourceContext(Context context, EnumSampleSpace<ContextInfectivityClass> sourceContexts) {
        var weight = transmissionConfig.getTotalWeightForContextType(context.getContextType());
        var agentCount = context.getAgentCount();
        for (Contamination contamination : context.getContaminations()) {
            var loadInContext = contamination.getLoad();
            var levelInContext = contamination.getLevel() * weight / agentCount;
            var multipliedLevel = levelInContext * relativeAlphaConfig.getRelativeAlpha(loadInContext);
            sourceContexts.increaseOutcome(context.getContextType().getInfectivityClass(), multipliedLevel);
        }
    }
}
