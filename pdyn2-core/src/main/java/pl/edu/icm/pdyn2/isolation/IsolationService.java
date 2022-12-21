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
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

public final class IsolationService {
    private final RandomGenerator randomGenerator;
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final IsolationConfig isolationConfig;

    @WithFactory
    public IsolationService(RandomProvider randomProvider,
                            StatsService statsService,
                            AgentStateService agentStateService,
                            IsolationConfig isolationConfig) {
        this.randomGenerator = randomProvider.getRandomGenerator(IsolationService.class);
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.isolationConfig = isolationConfig;
    }

    public void maybeIsolateAgent(Entity agentEntity) {
        float baseProbabilityOfSelfIsolation = isolationConfig.getBaseProbabilityOfSelfIsolation();
        float selfIsolationWeight = isolationConfig.getSelfIsolationWeight();

        if (baseProbabilityOfSelfIsolation > 0 && agentEntity.get(HealthStatus.class).getStage() == Stage.INFECTIOUS_SYMPTOMATIC) {
            if (randomGenerator.nextFloat() < baseProbabilityOfSelfIsolation * selfIsolationWeight) {
                // recklessly stays at home
            } else {
                if (agentStateService.beginIsolation(agentEntity)) {
                    statsService.tickIsolated();
                }
            }
        }
    }

}
