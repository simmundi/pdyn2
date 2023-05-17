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

package pl.edu.icm.pdyn2.travel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelVisitorTest {
    @Mock
    AreaClusteredSelectors areaClusteredSelectors;
    @Mock
    RandomAccessSelector householdIndex;
    @Mock
    Session session;
    @Mock
    AgentStateService agentStateService;
    @Mock
    Person person;
    @Mock
    Entity agent;
    static final int TARGET_ID = 123;
    @Mock
    Entity targetHousehold;
    @Spy
    MockRandomProvider randomProvider = new MockRandomProvider();
    @Spy
    TravelConfig travelConfig = new TravelConfig(0.5f, 1);

    @InjectMocks
    TravelVisitor travelVisitor;

    @Test
    @DisplayName("Should not do anything when behaviour is neither Routine or Travel")
    void createTravelSystem() {
        // given
        Behaviour quarantine = new Behaviour();
        quarantine.transitionTo(BehaviourType.QUARANTINE, 0);
        Behaviour selfIsolating = new Behaviour();
        selfIsolating.transitionTo(BehaviourType.SELF_ISOLATION, 0);
        Behaviour dead = new Behaviour();
        dead.transitionTo(BehaviourType.DEAD, 0);

        Entity quarantiedAgent = Mockito.mock(Entity.class);
        Entity isolatingAgent = Mockito.mock(Entity.class);
        Entity deadAgent = Mockito.mock(Entity.class);
        Mockito.when(quarantiedAgent.get(Behaviour.class)).thenReturn(quarantine);
        Mockito.when(isolatingAgent.get(Behaviour.class)).thenReturn(selfIsolating);
        Mockito.when(deadAgent.get(Behaviour.class)).thenReturn(dead);

        // execute
        travelVisitor.visit(randomProvider.getRandomGenerator(), quarantiedAgent);
        travelVisitor.visit(randomProvider.getRandomGenerator(), isolatingAgent);
        travelVisitor.visit(randomProvider.getRandomGenerator(), deadAgent);

        // assert
        verifyNoInteractions(agentStateService);
        verifyNoInteractions(randomProvider.getRandomGenerator());
        verifyNoInteractions(householdIndex);
    }

    @Test
    @DisplayName("Should send an agent on a travel")
    void createTravelSystem__travel() {
        // given
        Behaviour behaviour = new Behaviour();
        behaviour.transitionTo(BehaviourType.ROUTINE, 0);
        when(areaClusteredSelectors.householdSelector()).thenReturn(householdIndex);
        when(randomProvider.getRandomGenerator().nextFloat()).thenReturn(0f).thenReturn(0.9f);
        when(agent.get(Behaviour.class)).thenReturn(behaviour);
        when(agent.get(Person.class)).thenReturn(person);
        when(person.getAge()).thenReturn(20);
        when(householdIndex.getInt(0.9f)).thenReturn(TARGET_ID);
        when(agent.getSession()).thenReturn(session);
        when(session.getEntity(TARGET_ID)).thenReturn(targetHousehold);

        // execute
        travelVisitor.visit(randomProvider.getRandomGenerator(), agent);

        // assert
        verify(agentStateService).beginTravel(agent, targetHousehold);
    }

    @Test
    @DisplayName("Should end a travel")
    void createTravelSystem__end_travel() {
        // given
        Behaviour behaviour = new Behaviour();
        behaviour.transitionTo(BehaviourType.PRIVATE_TRAVEL, 0);
        when(randomProvider.getRandomGenerator().nextFloat()).thenReturn(0f);
        when(agent.get(Behaviour.class)).thenReturn(behaviour);

        // execute
        travelVisitor.visit(randomProvider.getRandomGenerator(), agent);

        // assert
        verify(agentStateService).endTravel(agent);
    }
}
