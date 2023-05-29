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

package pl.edu.icm.pdyn2.isolation;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * This service attempts to make an agent self-isolate, taking into account any additional configuration.
 * The service does not attempt to verify if there's a reason for the agent to self-isolate, the reasons
 * for self-isolation are up to the caller, as is the ending of the isolation (currently it only ends
 * when agent becomes healthy).
 */
public final class IsolationService {
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final IsolationConfig isolationConfig;

    @WithFactory
    public IsolationService(StatsService statsService,
                            AgentStateService agentStateService,
                            IsolationConfig isolationConfig) {
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.isolationConfig = isolationConfig;
    }

    public void maybeIsolateAgent(float random, Entity agentEntity) {
        float baseProbabilityOfSelfIsolation = isolationConfig.getBaseProbabilityOfSelfIsolation();
        float selfIsolationWeight = isolationConfig.getSelfIsolationWeight();

        if (baseProbabilityOfSelfIsolation > 0) {
            if (random < baseProbabilityOfSelfIsolation * selfIsolationWeight) {
                if (agentStateService.beginIsolation(agentEntity)) {
                    statsService.tickIsolated();
                }
            }
        }
    }
}
