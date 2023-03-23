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
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * Visitor for ending the quarantine, based on its duration.
 * Agents are quarantined in the testing visitor (after being positively tested).
 */
public final class QuarantineVisitor {
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final QuarantineConfig quarantineConfig;
    private final SimulationClock simulationClock;

    @WithFactory
    public QuarantineVisitor(StatsService statsService,
                             AgentStateService agentStateService,
                             QuarantineConfig quarantineConfig,
                             SimulationClock simulationClock) {
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.quarantineConfig = quarantineConfig;
        this.simulationClock = simulationClock;
    }

    public void maybeEndQuarantine(Entity agentEntity) {
        Behaviour behaviour = agentEntity.get(Behaviour.class);
        if (behaviour == null || behaviour.getType() != BehaviourType.QUARANTINE) {
            return;
        }
        int howLong = simulationClock.getDaysPassed() - behaviour.getDayOfLastChange();
        if (howLong > quarantineConfig.getQuarantineLengthDays()) {
            agentStateService.endQuarantine(agentEntity);
            statsService.tickUnquarantined();
        }
    }
}
