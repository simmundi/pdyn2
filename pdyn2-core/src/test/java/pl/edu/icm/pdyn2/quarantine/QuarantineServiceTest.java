package pl.edu.icm.pdyn2.quarantine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static pl.edu.icm.pdyn2.ComponentCreator.behaviour;
import static pl.edu.icm.pdyn2.model.behaviour.BehaviourType.QUARANTINE;

@ExtendWith(MockitoExtension.class)
class QuarantineServiceTest {

    @Mock
    StatsService statsService;

    @Mock
    AgentStateService agentStateService;

    @Mock
    SimulationTimer simulationTimer;

    @Mock
    QuarantineConfig quarantineConfig;

    @InjectMocks
    QuarantineService quarantineService;

    private EntityMocker entityMocker = new EntityMocker(null);

    @Test
    @DisplayName("Should end quarantine after 10 days")
    void maybeEndQuarantine() {
        // given
        Entity justStarted = entityMocker.entity(behaviour(QUARANTINE, 7));
        Entity shouldEnd = entityMocker.entity(behaviour(QUARANTINE, 1));
        Mockito.when(quarantineConfig.getQuarantineLengthDays()).thenReturn(10);
        Mockito.when(simulationTimer.getDaysPassed()).thenReturn(12);

        // execute
        quarantineService.maybeEndQuarantine(justStarted);
        quarantineService.maybeEndQuarantine(shouldEnd);

        // assert
        Mockito.verify(agentStateService, never()).endQuarantine(justStarted);
        Mockito.verify(agentStateService, times(1)).endQuarantine(shouldEnd);
        Mockito.verify(statsService, times(1)).tickUnquarantined();
    }
}
