package pl.edu.icm.pdyn2.impact;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.StageShareConfig;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.context.ContextFractionService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static pl.edu.icm.pdyn2.ComponentCreator.behaviour;
import static pl.edu.icm.pdyn2.ComponentCreator.health;
import static pl.edu.icm.pdyn2.ComponentCreator.impact;
import static pl.edu.icm.pdyn2.ComponentCreator.person;

@ExtendWith(MockitoExtension.class)
class AgentImpactServiceTest {
    @Mock
    ContextsService contextsService;
    @Mock
    ContextFractionService contextFractionService;
    @Mock
    StatsService statsService;
    @Mock
    StageShareConfig stageShareConfig;
    @InjectMocks
    AgentImpactService agentImpactService;

    private EntityMocker entityMocker = new EntityMocker(null);

    @Test
    @DisplayName("Should not update impact if nothing has changed")
    void updateImpact__no_change() {
        // given
        Entity entity = entityMocker.entity(
                impact(BehaviourType.ROUTINE, Load.WILD, Stage.INFECTIOUS_ASYMPTOMATIC),
                behaviour(BehaviourType.ROUTINE),
                health(Load.WILD, Stage.INFECTIOUS_ASYMPTOMATIC),
                person(34, Person.Sex.M));

        // execute
        agentImpactService.updateImpact(entity);

        // assert
        verifyNoInteractions(statsService);
    }

    @Test
    @DisplayName("Should update impact if something has changed")
    void updateImpact() {
        // given
        Entity entity = entityMocker.entity(
                impact(BehaviourType.ROUTINE, Load.WILD, Stage.INFECTIOUS_ASYMPTOMATIC),
                behaviour(BehaviourType.ROUTINE),
                health(Load.WILD, Stage.INFECTIOUS_SYMPTOMATIC),
                person(34, Person.Sex.M));
        Context uni = ComponentCreator.context(ContextType.BIG_UNIVERSITY, 123, Map.of(Load.WILD, 0.1f));
        Context street = ComponentCreator.context(ContextType.STREET_20, 10.5f, Map.of(Load.WILD, 1.05f));

        when(contextFractionService.calculateInfluenceFractionFor(entity.get(Person.class), uni)).thenReturn(1f);
        when(contextFractionService.calculateInfluenceFractionFor(entity.get(Person.class), street)).thenReturn(0.5f);
        when(stageShareConfig.getInfluenceOf(Stage.INFECTIOUS_ASYMPTOMATIC)).thenReturn(0.1f);
        when(stageShareConfig.getInfluenceOf(Stage.INFECTIOUS_SYMPTOMATIC)).thenReturn(1f);
        when(contextsService.findActiveContextsForAgent(entity, entity.get(Impact.class))).thenReturn(
                Stream.of(uni, street), Stream.of(uni, street));

        // execute
        agentImpactService.updateImpact(entity);

        // assert
        assertThat(uni.getContaminations()).hasSize(1);
        assertThat(uni.getAgentCount()).isEqualTo(123);
        assertThat(uni.getContaminationByLoad(Load.WILD).getLevel()).isEqualTo(1f);

        assertThat(street.getContaminations()).hasSize(1);
        assertThat(street.getAgentCount()).isEqualTo(10.5f);
        assertThat(street.getContaminationByLoad(Load.WILD).getLevel()).isEqualTo(1.5f);
    }
}
