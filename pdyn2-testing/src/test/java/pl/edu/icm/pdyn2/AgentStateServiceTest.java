/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSources;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.transmission.ContextImpactService;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentStateServiceTest {

    BasicConfig basicConfig = new BasicConfig();


    @Spy
    Stages stages = basicConfig.stages;

    @Mock
    SimulationClock simulationClock;

    @Mock
    ContextImpactService contextImpactService;

    AgentStateService agentStateService;

    @BeforeEach
    void before() {
        agentStateService = new AgentStateService(simulationClock, basicConfig.contextInfectivityClasses, basicConfig.stages, "LATENT", "HEALTHY", "DECEASED");
    }

    @Test
    @DisplayName("Should infect a healthy agent")
    void infect() {
        // given
        ExampleData data = new ExampleData(basicConfig);
        when(simulationClock.getDaysPassed()).thenReturn(123);

        // execute
        agentStateService.infect(data.agent2, basicConfig.ALPHA);
        var result = data.agent2.get(HealthStatus.class);

        // assert
        assertThat(result.getDiseaseLoad()).isEqualTo(basicConfig.ALPHA);
        assertThat(result.getDayOfLastChange()).isEqualTo(123);
    }

    @Test
    @DisplayName("Should initialize travel")
    void beginTravel() {
        // given
        ExampleData data = new ExampleData(basicConfig);
        when(simulationClock.getDaysPassed()).thenReturn(7);
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
        ExampleData data = new ExampleData(basicConfig);
        when(simulationClock.getDaysPassed()).thenReturn(11).thenReturn(15);
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
        ExampleData data = new ExampleData(basicConfig);
        when(simulationClock.getDaysPassed()).thenReturn(23);

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
        ExampleData data = new ExampleData(basicConfig);
        when(simulationClock.getDaysPassed()).thenReturn(23).thenReturn(27);
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
        ExampleData data = new ExampleData(basicConfig);

        // execute
        agentStateService.changeLoad(data.agent3, basicConfig.WILD);

        // assert
        var health = data.agent3.get(HealthStatus.class);
        assertThat(health.getDiseaseLoad()).isEqualTo(basicConfig.WILD);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> agentStateService.changeLoad(data.agent1, basicConfig.ALPHA))
                .withMessage("Only latent agents can have their load changed");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> agentStateService.changeLoad(data.agent3, basicConfig.MODERNA))
                .withMessage("Variant can be changed only to another virus load");
    }

    @Test
    @DisplayName("should create ImmunizationSources component")
    void addSourcesDistribution() {
        // given
        ExampleData data = new ExampleData(basicConfig);
        SoftEnumDiscretePDF<ContextInfectivityClass> sourcesDistribution = new SoftEnumDiscretePDF<>(basicConfig.contextInfectivityClasses);
        sourcesDistribution.set(basicConfig.contextInfectivityClasses.SCHOOL, 10f);
        sourcesDistribution.set(basicConfig.contextInfectivityClasses.STREET, 12f);

        // execute
        agentStateService.addSourcesDistribution(data.agent3, sourcesDistribution);

        // assert
        var sources = data.agent3.get(ImmunizationSources.class);

        assertThat(sources.getImmunizationSources().get(0).getSchoolInfluence()).isEqualTo(10f);
        assertThat(sources.getImmunizationSources().get(0).getStreetInfluence()).isEqualTo(12f);
    }

    static class ExampleData {
        EntityMocker build;

        Entity someRemoteHousehold;

        Entity agent1;
        Entity agent2;
        Entity agent3;

        public ExampleData(BasicConfig basicConfig) {
            build = new EntityMocker(basicConfig, null);
            agent1 = build.entityWithId(5,
                    ComponentCreator.health(basicConfig.DELTA, basicConfig.INFECTIOUS_SYMPTOMATIC));
            agent2 = build.entityWithId(6,
                    ComponentCreator.health(basicConfig.WILD, basicConfig.HEALTHY));
            agent3 = build.entityWithId(7,
                    ComponentCreator.health(basicConfig.ALPHA, basicConfig.LATENT));
            someRemoteHousehold = build.entityWithId(1, ComponentCreator.context(basicConfig.contextTypes.HOUSEHOLD));

        }
    }

}
