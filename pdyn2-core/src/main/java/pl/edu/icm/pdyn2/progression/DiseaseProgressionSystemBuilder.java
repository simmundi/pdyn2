package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Selectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class DiseaseProgressionSystemBuilder {

    private final DiseaseStageTransitionsService diseaseStageTransitionsService;
    private final SimulationTimer simulationTimer;
    private final AgentStateService agentStateService;
    private final StatsService statsService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final RandomForChunkProvider randomForChunkProvider;

    @WithFactory
    public DiseaseProgressionSystemBuilder(DiseaseStageTransitionsService diseaseStageTransitionsService,
                                           SimulationTimer simulationTimer,
                                           AgentStateService agentStateService,
                                           StatsService statsService,
                                           AreaClusteredSelectors areaClusteredSelectors,
                                           Selectors selectors,
                                           RandomProvider randomProvider) {
        this.diseaseStageTransitionsService = diseaseStageTransitionsService;
        this.simulationTimer = simulationTimer;
        this.agentStateService = agentStateService;
        this.statsService = statsService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(DiseaseProgressionSystemBuilder.class);
    }

    public EntitySystem buildProgressionSystem() {
        return select(selectors.filtered(areaClusteredSelectors.personSelector(), selectors.hasComponents(HealthStatus.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, entity) -> {
                    HealthStatus health = entity.get(HealthStatus.class);
                    Person person = entity.get(Person.class);
                    Stage currentStage = health.getStage();

                    if (!currentStage.hasOutcomes()) {
                        statsService.tickStage(currentStage);
                        return;
                    }

                    int stageDuration = diseaseStageTransitionsService
                            .durationOf(health.getDiseaseLoad(), currentStage, person.getAge());

                    int elapsed = health.getElapsedDays(simulationTimer.getDaysPassed());
                    double p = 1.0;
                    if (currentStage.isInfectious() && elapsed <= 1) {
                        int days = new PoissonDistribution(random,
                                stageDuration,
                                PoissonDistribution.DEFAULT_EPSILON,
                                PoissonDistribution.DEFAULT_MAX_ITERATIONS).sample();
                        health.setDuration(days);
                    }

                    if (elapsed >= health.getDuration()) {
                        Stage nextStage = diseaseStageTransitionsService.outcomeOf(
                                currentStage,
                                entity,
                                health.getDiseaseLoad(),
                                random.nextDouble());
                        agentStateService.progressToDiseaseStage(entity, nextStage);
                        statsService.tickStageChange(nextStage);
                        statsService.tickStage(nextStage);
                    } else {
                        statsService.tickStage(currentStage);
                    }
                });
    }
}
