package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.context.ContextFractionService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Contamination;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentStateServiceTest {

    @Mock
    SimulationTimer simulationTimer;

    @Mock
    ContextFractionService contextFractionService;

    @InjectMocks
    AgentStateService agentStateService;

    @Test
    @DisplayName("Should infect a healthy agent")
    void infect() {
        // given
        ExampleData data = new ExampleData();
        when(simulationTimer.getDaysPassed()).thenReturn(123);

        // execute
        agentStateService.infect(data.agent2, Load.ALPHA);
        var result = data.agent2.get(HealthStatus.class);

        // assert
        assertThat(result.getDiseaseLoad()).isEqualTo(Load.ALPHA);
        assertThat(result.getDayOfLastChange()).isEqualTo(123);
    }

    @Test
    @DisplayName("Should initialize travel")
    void beginTravel() {
        // given
        ExampleData data = new ExampleData();
        when(simulationTimer.getDaysPassed()).thenReturn(7);
        agentStateService.activate(data.agent2);

        // execute
        agentStateService.beginTravel(data.agent2, data.someRemoteHousehold);

        // assert
        var result = data.agent2.get(Travel.class);
        var behaviour = data.agent2.get(Behaviour.class);
        assertThat(behaviour.getType()).isEqualTo(BehaviourType.PRIVATE_TRAVEL);
        assertThat(behaviour.getDayOfLastChange()).isEqualTo((short) 7);
        assertThat(result.getDayOfTravel()).isEqualTo((short) 7);
        assertThat(result.getStayingAt()).isEqualTo(data.someRemoteHousehold);
    }

    @Test
    @DisplayName("Should finalize travel")
    void endTravel() {
        // given
        ExampleData data = new ExampleData();
        when(simulationTimer.getDaysPassed()).thenReturn(11).thenReturn(15);
        agentStateService.activate(data.agent2);
        agentStateService.beginTravel(data.agent2, data.someRemoteHousehold);

        // execute
        agentStateService.endTravel(data.agent2);

        // assert
        var behaviour = data.agent2.get(Behaviour.class);
        assertThat(behaviour.getType()).isEqualTo(BehaviourType.ROUTINE);
        assertThat(behaviour.getDayOfLastChange()).isEqualTo((short) 15);
    }

    @Test
    @DisplayName("Should initialize quarantine")
    void beginQuarantine() {
        // given
        ExampleData data = new ExampleData();
        when(simulationTimer.getDaysPassed()).thenReturn(23);

        // execute
        agentStateService.beginQuarantine(data.agent2);

        // assert
        var behaviour = data.agent2.get(Behaviour.class);
        assertThat(behaviour.getType()).isEqualTo(BehaviourType.QUARANTINE);
        assertThat(behaviour.getDayOfLastChange()).isEqualTo((short) 23);
    }

    @Test
    @DisplayName("Should end quarantine")
    void endQuarantine() {
        // given
        ExampleData data = new ExampleData();
        when(simulationTimer.getDaysPassed()).thenReturn(23).thenReturn(27);
        agentStateService.beginQuarantine(data.agent2);

        // execute
        agentStateService.endQuarantine(data.agent2);

        // assert
        var behaviour = data.agent2.get(Behaviour.class);
        assertThat(behaviour.getType()).isEqualTo(BehaviourType.ROUTINE);
        assertThat(behaviour.getDayOfLastChange()).isEqualTo((short) 27);
    }

    @Test
    @DisplayName("Should change load")
    void changeLoad() {
        // given
        ExampleData data = new ExampleData();

        // execute
        agentStateService.changeLoad(data.agent3, Load.WILD);

        // assert
        var health = data.agent3.get(HealthStatus.class);
        assertThat(health.getDiseaseLoad()).isEqualTo(Load.WILD);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> agentStateService.changeLoad(data.agent1, Load.ALPHA))
                .withMessage("Only latent agents can have their load changed");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> agentStateService.changeLoad(data.agent3, Load.MODERNA))
                .withMessage("Variant can be changed only to another virus load");
    }

    static class ExampleData {
        EntityMocker build = new EntityMocker(null);

        Entity someRemoteHousehold = build.entityWithId(1, ComponentCreator.context(ContextType.HOUSEHOLD));

        Entity agent1 = build.entityWithId(5,
                ComponentCreator.health(Load.DELTA, Stage.INFECTIOUS_SYMPTOMATIC));
        Entity agent2 = build.entityWithId(6,
                ComponentCreator.health(Load.WILD, Stage.HEALTHY));
        Entity agent3 = build.entityWithId(7,
                ComponentCreator.health(Load.ALPHA, Stage.LATENT));

    }

}
