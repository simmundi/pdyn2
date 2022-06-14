package pl.edu.icm.pdyn2.administration;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.administration.Record;
import pl.edu.icm.pdyn2.model.administration.RecordType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

public final class TestingService {
    private final SimulationTimer simulationTimer;
    private final RandomGenerator randomGenerator;
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final TestingConfig testingConfig;

    @WithFactory
    public TestingService(SimulationTimer simulationTimer,
                          RandomProvider randomProvider,
                          StatsService statsService,
                          AgentStateService agentStateService, TestingConfig testingConfig) {
        this.simulationTimer = simulationTimer;
        this.randomGenerator = randomProvider.getRandomGenerator(TestingService.class);
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.testingConfig = testingConfig;
    }

    public void maybeTestAgent(Entity agentEntity) {
        maybeTestAgentOnDay(agentEntity, 0);
    }

    public void maybeTestAgentOnDay(Entity agentEntity, int daysFromTest) {
        Preconditions.checkArgument(daysFromTest >= 0, "Cannot test agent at a future date. daysFromTest should be >= 0: %s", daysFromTest);
        float baseProbabilityOfTest = testingConfig.getBaseProbabilityOfTest();

        if (baseProbabilityOfTest > 0 && randomGenerator.nextFloat() < baseProbabilityOfTest) {
            testAgent(agentEntity, simulationTimer.getDaysPassed() - daysFromTest);
        }
    }

    private void testAgent(Entity agentEntity, int dayOfTest) {
        HealthStatus healthStatus = agentEntity.get(HealthStatus.class);
        if (healthStatus.getStage().isSick()) {
            MedicalHistory medicalHistory = agentEntity.getOrCreate(MedicalHistory.class);
            medicalHistory.getRecords().add(record(dayOfTest));
            statsService.tickTestedPositive();

            Entity homeContextEntity = agentEntity.get(Inhabitant.class).getHomeContext();
            for (Entity member : homeContextEntity.get(Household.class).getMembers()) {
                agentStateService.beginQuarantineOnDay(member, dayOfTest);
                statsService.tickQuarantined();
            }
        }
    }

    private Record record(int dayOfTest) {
        Record record = new Record();
        record.setDay(dayOfTest);
        record.setType(RecordType.POSITIVE_TEST);
        return record;
    }
}
