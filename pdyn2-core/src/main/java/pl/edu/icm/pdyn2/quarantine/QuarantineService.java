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

package pl.edu.icm.pdyn2.quarantine;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

public final class QuarantineService {
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final QuarantineConfig quarantineConfig;
    private final SimulationTimer simulationTimer;

    @WithFactory
    public QuarantineService(StatsService statsService,
                             AgentStateService agentStateService,
                             QuarantineConfig quarantineConfig,
                             SimulationTimer simulationTimer) {
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.quarantineConfig = quarantineConfig;
        this.simulationTimer = simulationTimer;
    }

    public void maybeEndQuarantine(Entity agentEntity) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        if (behaviour.getType() != BehaviourType.QUARANTINE) {
            return;
        }
        int howLong = simulationTimer.getDaysPassed() - behaviour.getDayOfLastChange();
        if (howLong > quarantineConfig.getQuarantineLengthDays()) {
            agentStateService.endQuarantine(agentEntity);
            statsService.tickUnquarantined();
        }
    }
}
