package pl.edu.icm.pdyn2.administration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.edu.icm.pdyn2.ComponentCreator.household;
import static pl.edu.icm.pdyn2.ComponentCreator.inhabitant;

@ExtendWith(MockitoExtension.class)
class TestingServiceTest {
    public static final float BASE_PROBABILITY_OF_TEST = 0.5f;

    TestingConfig testingConfig = new TestingConfig(BASE_PROBABILITY_OF_TEST);

    @Mock
    StatsService statsService;

    @Mock
    AgentStateService agentStateService;

    @Mock
    SimulationTimer simulationTimer;

    MockRandomProvider mockRandomProvider = new MockRandomProvider();

    TestingService testingService;

    private EntityMocker entityMocker = new EntityMocker(null);

    @BeforeEach
    void before() {
        testingService = new TestingService(simulationTimer, mockRandomProvider, statsService, agentStateService, testingConfig);
    }

    @Test
    @DisplayName("Should correctly test all agents when random mocked to 0.0")
    void maybeTestAgent() {
        // given
        HealthStatus healthStatus = ComponentCreator.health(Load.WILD, Stage.INFECTIOUS_SYMPTOMATIC);
        HealthStatus healthy = ComponentCreator.health(Load.WILD, Stage.HEALTHY);
        Entity household = entityMocker.entity(household());
        Entity sickAgent = entityMocker.entity(healthStatus, inhabitant(household));
        Entity recoveredAgent = entityMocker.entity(healthy);
        Entity healthyAgent = entityMocker.entity(healthy);

        // execute
        testingService.maybeTestAgent(sickAgent);
        testingService.maybeTestAgent(healthyAgent);
        testingService.maybeTestAgent(recoveredAgent);

        // assert
        verify(statsService, times(1)).tickTestedPositive();
        assertThat(sickAgent.get(MedicalHistory.class).getRecords()).hasSize(1);
        assertThat(recoveredAgent.get(MedicalHistory.class)).isNull();
        assertThat(healthyAgent.get(MedicalHistory.class)).isNull();
    }

    @Test
    @DisplayName("Should correctly quarantine all agents from household")
    void maybeTestAgent__quarantine() {
        // given
        HealthStatus healthStatus = ComponentCreator.health(Load.WILD, Stage.INFECTIOUS_SYMPTOMATIC);
        Entity household = entityMocker.entity(household());
        Entity sickAgent = entityMocker.entity(healthStatus, inhabitant(household));
        Entity healthySpouse = entityMocker.entity(inhabitant(household));
        household.get(Household.class).getMembers().add(sickAgent);
        household.get(Household.class).getMembers().add(healthySpouse);

        // execute
        testingService.maybeTestAgent(sickAgent);

        // assert
        verify(statsService, times(2)).tickQuarantined();
        verify(agentStateService).beginQuarantineOnDay(sickAgent, 0);
        verify(agentStateService).beginQuarantineOnDay(healthySpouse, 0);
    }

    @Test
    @DisplayName("Shouldn't test an agent when random mocked to more than base probability")
    void maybeTestAgent__none() {
        // given
        when(mockRandomProvider.getRandomGenerator().nextFloat()).thenReturn(BASE_PROBABILITY_OF_TEST + 0.001f);
        HealthStatus healthStatus = ComponentCreator.health(Load.WILD, Stage.INFECTIOUS_SYMPTOMATIC);
        Entity sickAgent = entityMocker.entity(healthStatus);

        // execute
        testingService.maybeTestAgent(sickAgent);

        // assert
        verify(statsService, never()).tickTestedPositive();
        assertThat(sickAgent.get(MedicalHistory.class)).isNull();
    }
}
