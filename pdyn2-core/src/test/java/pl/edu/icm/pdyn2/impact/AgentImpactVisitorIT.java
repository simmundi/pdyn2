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

package pl.edu.icm.pdyn2.impact;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentImpactVisitorIT {
    private final static Offset<Float> VERY_CLOSE = Offset.offset(0.000001f);
    BasicConfig basicConfig = new BasicConfig();

    ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(basicConfig, false);
    AgentImpactVisitor agentImpactVisitor = data.agentImpactVisitor;
    AgentStateService agentStateService = data.agentStateService;

    @Test
    @DisplayName("Should properly count all agents in contexts")
    void countAgents() {
        // execute
        for (Entity agent : data.allAgents) {
            agentImpactVisitor.updateImpact(agent);
        }

        // assert
        assertThat(agentCount(data.streetsA)).isCloseTo(6, VERY_CLOSE);
        assertThat(agentCount(data.streetsB)).isCloseTo(10, VERY_CLOSE);
        assertThat(agentCount(data.streetsC)).isCloseTo(4, VERY_CLOSE);

        assertThat(agentCount(data.workplace)).isEqualTo(1);

        assertThat(agentCount(data.householdContext1)).isEqualTo(3);
        assertThat(agentCount(data.householdContext2)).isEqualTo(3);
        assertThat(agentCount(data.householdContext3)).isEqualTo(4);

        assertThat(agentCount(data.school1)).isEqualTo(5);
        assertThat(agentCount(data.school2)).isEqualTo(5);
    }


    @Test
    @DisplayName("Should properly account for infection of two agents")
    void twoAgentsShedding() {
        // given
        agentStateService.infect(data.agent2, basicConfig.loads.WILD);
        agentStateService.progressToDiseaseStage(data.agent2, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);

        agentStateService.infect(data.agent3, basicConfig.loads.WILD);
        agentStateService.progressToDiseaseStage(data.agent3, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);

        agentStateService.infect(data.agent4, basicConfig.OMICRON);
        agentStateService.progressToDiseaseStage(data.agent4, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);

        // execute

        for (Entity agent : data.allAgents) {
            agentImpactVisitor.updateImpact(agent);
        }

        // assert
        assertThat(agentCount(data.streetsA)).isCloseTo(6, VERY_CLOSE);
        assertThat(agentCount(data.streetsB)).isCloseTo(10, VERY_CLOSE);
        assertThat(agentCount(data.streetsC)).isCloseTo(4, VERY_CLOSE);

        assertThat(contaminationLevel(data.householdContext1, basicConfig.loads.WILD)).isEqualTo(2f);
        assertThat(contaminationLevel(data.householdContext2, basicConfig.loads.WILD)).isZero();
        assertThat(contaminationLevel(data.workplace, basicConfig.loads.WILD)).isZero();
        assertThat(contaminationLevel(data.streetsA, basicConfig.loads.WILD)).isCloseTo(2f, VERY_CLOSE);
        assertThat(contaminationLevel(data.streetsB, basicConfig.loads.WILD)).isCloseTo(2f, VERY_CLOSE);
        assertThat(contaminationLevel(data.streetsC, basicConfig.loads.WILD)).isZero();
        assertThat(contaminationLevel(data.school1, basicConfig.loads.WILD)).isEqualTo(2f);
        assertThat(contaminationLevel(data.school1, basicConfig.DELTA)).isZero();
    }

    private float agentCount(Entity contextEntity) {
        return contextEntity.get(Context.class).getAgentCount();
    }

    private float agentCount(List<Entity> contextEntities) {
        return (float) contextEntities.stream().mapToDouble(this::agentCount).sum();
    }

    private float contaminationLevel(Entity contextEntity, Load load) {
        return contextEntity.get(Context.class).getContaminationByLoad(load).getLevel();
    }

    private float contaminationLevel(List<Entity> contextEntities, Load load) {
        return (float) contextEntities.stream().mapToDouble(e -> contaminationLevel(e, load)).sum();
    }
}
