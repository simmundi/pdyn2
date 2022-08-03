package pl.edu.icm.pdyn2.impact;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class AgentImpactServiceIT {
    private final static Offset<Float> VERY_CLOSE = Offset.offset(0.000001f);

    ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests();
    AgentImpactService agentImpactService = data.agentImpactService;
    AgentStateService agentStateService = data.agentStateService;

    @Test
    @DisplayName("Should properly count all agents in contexts")
    void countAgents() {
        // execute
        for (Entity agent : data.allAgents) {
            agentImpactService.updateImpact(agent);
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
        agentStateService.infect(data.agent2, Load.WILD);
        agentStateService.progressToDiseaseStage(data.agent2, Stage.INFECTIOUS_SYMPTOMATIC);

        agentStateService.infect(data.agent3, Load.WILD);
        agentStateService.progressToDiseaseStage(data.agent3, Stage.INFECTIOUS_SYMPTOMATIC);

        agentStateService.infect(data.agent4, Load.OMICRON);
        agentStateService.progressToDiseaseStage(data.agent4, Stage.INFECTIOUS_SYMPTOMATIC);

        // execute

        for (Entity agent : data.allAgents) {
            agentImpactService.updateImpact(agent);
        }

        // assert
        assertThat(agentCount(data.streetsA)).isCloseTo(6, VERY_CLOSE);
        assertThat(agentCount(data.streetsB)).isCloseTo(10, VERY_CLOSE);
        assertThat(agentCount(data.streetsC)).isCloseTo(4, VERY_CLOSE);

        assertThat(contaminationLevel(data.householdContext1, Load.WILD)).isEqualTo(2f);
        assertThat(contaminationLevel(data.householdContext2, Load.WILD)).isZero();
        assertThat(contaminationLevel(data.workplace, Load.WILD)).isZero();
        assertThat(contaminationLevel(data.streetsA, Load.WILD)).isCloseTo(2f, VERY_CLOSE);
        assertThat(contaminationLevel(data.streetsB, Load.WILD)).isCloseTo(2f, VERY_CLOSE);
        assertThat(contaminationLevel(data.streetsC, Load.WILD)).isZero();
        assertThat(contaminationLevel(data.school1, Load.WILD)).isEqualTo(2f);
        assertThat(contaminationLevel(data.school1, Load.DELTA)).isZero();
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
