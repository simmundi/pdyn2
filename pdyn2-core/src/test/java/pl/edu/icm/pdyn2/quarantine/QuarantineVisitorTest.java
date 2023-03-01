/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2.quarantine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.*;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static pl.edu.icm.pdyn2.ComponentCreator.behaviour;
import static pl.edu.icm.pdyn2.model.behaviour.BehaviourType.QUARANTINE;

@ExtendWith(MockitoExtension.class)
class QuarantineVisitorTest {

    @Mock
    StatsService statsService;

    @Mock
    AgentStateService agentStateService;

    @Mock
    SimulationTimer simulationTimer;

    @Mock
    QuarantineConfig quarantineConfig;

    @InjectMocks
    QuarantineVisitor quarantineVisitor;

    private final BasicConfig basicConfig = new BasicConfig();
    private final EntityMocker entityMocker = new EntityMocker(basicConfig, null);

    @Test
    @DisplayName("Should end quarantine after 10 days")
    void maybeEndQuarantine() {
        // given
        Entity justStarted = entityMocker.entity(behaviour(QUARANTINE, 7));
        Entity shouldEnd = entityMocker.entity(behaviour(QUARANTINE, 1));
        Mockito.when(quarantineConfig.getQuarantineLengthDays()).thenReturn(10);
        Mockito.when(simulationTimer.getDaysPassed()).thenReturn(12);

        // execute
        quarantineVisitor.maybeEndQuarantine(justStarted);
        quarantineVisitor.maybeEndQuarantine(shouldEnd);

        // assert
        Mockito.verify(agentStateService, never()).endQuarantine(justStarted);
        Mockito.verify(agentStateService, times(1)).endQuarantine(shouldEnd);
        Mockito.verify(statsService, times(1)).tickUnquarantined();
    }
}
