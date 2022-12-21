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

package pl.edu.icm.pdyn2.behaviour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.behaviour.TravelConfig;
import pl.edu.icm.pdyn2.behaviour.TravelService;
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
class TravelServiceTest {
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
    TravelService travelService;

    @Test
    @DisplayName("Should not do anything when behaviour is neither Routine or Travel")
    void createTravelSystem() {
        // given
        Behaviour quarantine = new Behaviour();
        quarantine.transitionTo(BehaviourType.QUARANTINE, 0);
        Behaviour dormant = new Behaviour();
        dormant.transitionTo(BehaviourType.DORMANT, 0);
        Behaviour dead = new Behaviour();
        dead.transitionTo(BehaviourType.DEAD, 0);

        // execute
        travelService.processTravelLogic(agent, quarantine, randomProvider.getRandomGenerator());
        travelService.processTravelLogic(agent, dormant, randomProvider.getRandomGenerator());
        travelService.processTravelLogic(agent, dead, randomProvider.getRandomGenerator());

        // assert
        verifyNoInteractions(agent);
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
        when(agent.get(Person.class)).thenReturn(person);
        when(person.getAge()).thenReturn(20);
        when(householdIndex.getInt(0.9f)).thenReturn(TARGET_ID);
        when(agent.getSession()).thenReturn(session);
        when(session.getEntity(TARGET_ID)).thenReturn(targetHousehold);

        // execute
        travelService.processTravelLogic(agent, behaviour, randomProvider.getRandomGenerator());

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

        // execute
        travelService.processTravelLogic(agent, behaviour, randomProvider.getRandomGenerator());

        // assert
        verify(agentStateService).endTravel(agent);
    }
}
