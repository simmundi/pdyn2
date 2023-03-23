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

package pl.edu.icm.pdyn2.administrative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.pdyn2.*;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static pl.edu.icm.pdyn2.ComponentCreator.household;
import static pl.edu.icm.pdyn2.ComponentCreator.inhabitant;

@ExtendWith(MockitoExtension.class)
class TestingServiceTest {
    public static final float BASE_PROBABILITY_OF_TEST = 0.5f;
    BasicConfig basicConfig = new BasicConfig();

    TestingConfig testingConfig = new TestingConfig(BASE_PROBABILITY_OF_TEST);

    @Mock
    StatsService statsService;

    @Mock
    AgentStateService agentStateService;

    @Mock
    SimulationClock simulationClock;


    TestingService testingService;

    private EntityMocker entityMocker = new EntityMocker(basicConfig, null);

    @BeforeEach
    void before() {
        testingService = new TestingService(simulationClock, statsService, agentStateService, testingConfig);
    }

    @Test
    @DisplayName("Should correctly test all agents when random equals 0.0")
    void maybeTestAgent() {
        // given
        HealthStatus healthStatus = ComponentCreator.health(basicConfig.loads.WILD, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);
        HealthStatus healthy = ComponentCreator.health(basicConfig.loads.WILD, basicConfig.stages.HEALTHY);
        Entity household = entityMocker.entity(household());
        Entity sickAgent = entityMocker.entity(healthStatus, inhabitant(household));
        Entity recoveredAgent = entityMocker.entity(healthy);
        Entity healthyAgent = entityMocker.entity(healthy);

        // execute
        testingService.maybeTestAgent(0, sickAgent);
        testingService.maybeTestAgent(0, healthyAgent);
        testingService.maybeTestAgent(0, recoveredAgent);

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
        HealthStatus healthStatus = ComponentCreator.health(basicConfig.loads.WILD, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);
        Entity household = entityMocker.entity(household());
        Entity sickAgent = entityMocker.entity(healthStatus, inhabitant(household));
        Entity healthySpouse = entityMocker.entity(inhabitant(household));
        household.get(Household.class).getMembers().add(sickAgent);
        household.get(Household.class).getMembers().add(healthySpouse);

        // execute
        testingService.maybeTestAgent(0, sickAgent);

        // assert
        verify(statsService, times(2)).tickQuarantined();
        verify(agentStateService).beginQuarantineOnDay(sickAgent, 0);
        verify(agentStateService).beginQuarantineOnDay(healthySpouse, 0);
    }

    @Test
    @DisplayName("Shouldn't test an agent when random is more than the base probability")
    void maybeTestAgent__none() {
        // given
        HealthStatus healthStatus = ComponentCreator.health(basicConfig.loads.WILD, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);
        Entity sickAgent = entityMocker.entity(healthStatus);

        // execute
        testingService.maybeTestAgent(BASE_PROBABILITY_OF_TEST + 0.001f, sickAgent);

        // assert
        verify(statsService, never()).tickTestedPositive();
        assertThat(sickAgent.get(MedicalHistory.class)).isNull();
    }
}
