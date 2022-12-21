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

package pl.edu.icm.pdyn2.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ContextsServiceTest {
    @Mock
    private AreaIndex areaIndex;
    @InjectMocks
    ContextsService contextsService;

    @Test
    @DisplayName("Should find all the contexts of a routine-following agent")
    void findActiveContextsForAgent() {
        // given
        ExampleData data = new ExampleData();

        // execute
        var result = contextsService.findActiveContextsForAgent(data.agent1).collect(Collectors.toSet());

        // assert
        assertThat(result).containsExactlyInAnyOrder(
                data.home.get(Context.class),
                data.work.get(Context.class),
                data.school.get(Context.class));
    }

    @Test
    @DisplayName("Should return zero contexts for a dead agent")
    void findActiveContextsForAgent__dead() {
        // given
        ExampleData data = new ExampleData();
        data.agent1.getOrCreate(Behaviour.class).transitionTo(BehaviourType.DEAD, 0);

        // execute
        var result = contextsService.findActiveContextsForAgent(data.agent1).collect(Collectors.toSet());

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return just home contexts for isolating agnets")
    void findActiveContextsForAgent__isolating() {
        // given
        ExampleData data = new ExampleData();
        data.agent1.get(Behaviour.class).transitionTo(BehaviourType.SELF_ISOLATION, 0);
        data.agent2.get(Behaviour.class).transitionTo(BehaviourType.QUARANTINE, 0);

        // execute
        var selfIsolating = contextsService.findActiveContextsForAgent(data.agent1).collect(Collectors.toSet());
        var quarantined = contextsService.findActiveContextsForAgent(data.agent2).collect(Collectors.toSet());

        // assert
        assertThat(selfIsolating).containsExactly(data.home.get(Context.class));
        assertThat(quarantined).containsExactly(data.home.get(Context.class));
    }

    @Test
    @DisplayName("Should return zero contexts for a hospitalized agent")
    void findActiveContextsForAgent__hospitalized() {
        // given
        ExampleData data = new ExampleData();
        data.agent1.get(Behaviour.class).transitionTo(BehaviourType.HOSPITALIZED, 0);

        // execute
        var result = contextsService.findActiveContextsForAgent(data.agent1).collect(Collectors.toSet());

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return current place of stay")
    void findActiveContextsForAgent__traveling() {
        // given
        ExampleData data = new ExampleData();
        data.agent2.get(Behaviour.class).transitionTo(BehaviourType.PRIVATE_TRAVEL, 0);
        data.agent2.add(new Travel()).setStayingAt(data.relativesHome);

        // execute
        var result = contextsService.findActiveContextsForAgent(data.agent2).collect(Collectors.toSet());

        // assert
        assertThat(result).containsExactly(data.relativesHome.get(Context.class));
    }

    static class ExampleData {
        Session session = Mockito.mock(Session.class);
        EntityMocker build = new EntityMocker(session);

        Entity home = build.entityWithId(1, ComponentCreator.context(ContextType.HOUSEHOLD),
                ComponentCreator.location(KilometerGridCell.fromPl1992ENKilometers(5, 5)));
        Entity work = build.entityWithId(2, ComponentCreator.context(ContextType.WORKPLACE));
        Entity school = build.entityWithId(3, ComponentCreator.context(ContextType.SCHOOL));
        Entity relativesHome = build.entityWithId(4, ComponentCreator.context(ContextType.HOUSEHOLD),
                ComponentCreator.location(KilometerGridCell.fromLegacyPdynCoordinates(0, 0)));

        Entity agent1 = build.entityWithId(5,
                ComponentCreator.inhabitant(home, work, school),
                ComponentCreator.behaviour(BehaviourType.ROUTINE));
        Entity agent2 = build.entityWithId(6,
                ComponentCreator.inhabitant(home, work, school),
                ComponentCreator.behaviour(BehaviourType.ROUTINE));
    }

}
