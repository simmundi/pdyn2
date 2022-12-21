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

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;

public class TravelService {
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final AgentStateService agentStateService;
    private final TravelConfig travelConfig;
    private RandomAccessSelector households;

    @WithFactory
    public TravelService(AreaClusteredSelectors areaClusteredSelectors, AgentStateService agentStateService,
                         TravelConfig travelConfig) {
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.agentStateService = agentStateService;
        this.travelConfig = travelConfig;
    }

    public void processTravelLogic(Entity e, Behaviour behaviour, RandomGenerator randomGenerator) {

        switch (behaviour.getType()) {
            case ROUTINE:
                if (randomGenerator.nextFloat() < travelConfig.getProbabilityOfTravel()) {
                    Person person = e.get(Person.class);
                    if (person.getAge() >= 17) {
                        int targetHouseholdId = getHouseholdSelector().getInt(randomGenerator.nextFloat());
                        agentStateService.beginTravel(e, e.getSession().getEntity(targetHouseholdId));
                    }
                }
                break;
            case PRIVATE_TRAVEL:
                if (randomGenerator.nextFloat() < travelConfig.getProbabilityOfEndingTravel()) {
                    agentStateService.endTravel(e);
                }
                break;
            default:
                // travel does not apply
        }
    }

    private RandomAccessSelector getHouseholdSelector() {
        if (this.households == null) {
            this.households = areaClusteredSelectors.householdSelector();
        }
        return this.households;
    }
}
