package pl.edu.icm.pdyn2.isolation;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

public final class IsolationService {
    private final RandomGenerator randomGenerator;
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final IsolationConfig isolationConfig;

    @WithFactory
    public IsolationService(RandomProvider randomProvider,
                            StatsService statsService,
                            AgentStateService agentStateService,
                            IsolationConfig isolationConfig) {
        this.randomGenerator = randomProvider.getRandomGenerator(IsolationService.class);
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.isolationConfig = isolationConfig;
    }

    public void maybeIsolateAgent(Entity agentEntity) {
        float baseProbabilityOfSelfIsolation = isolationConfig.getBaseProbabilityOfSelfIsolation();
        float selfIsolationWeight = isolationConfig.getSelfIsolationWeight();

        if (baseProbabilityOfSelfIsolation > 0 && agentEntity.get(HealthStatus.class).getStage() == Stage.INFECTIOUS_SYMPTOMATIC) {
            if (randomGenerator.nextFloat() < baseProbabilityOfSelfIsolation * selfIsolationWeight) {
                // recklessly stays at home
            } else {
                if (agentStateService.beginIsolation(agentEntity)) {
                    statsService.tickIsolated();
                }
            }
        }
    }

}
