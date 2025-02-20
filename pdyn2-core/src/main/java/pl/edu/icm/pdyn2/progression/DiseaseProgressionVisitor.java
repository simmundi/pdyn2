/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * When visiting an entity that has health, this consults external service and the calendar
 * service and - using a random generator - eventually decides to progress the sickness to
 * the next selected stage.
 *
 * The actual change of stage is done through AgentStateService.
 */
public class DiseaseProgressionVisitor {

    private final DiseaseStageTransitionsService diseaseStageTransitionsService;
    private final SimulationClock simulationClock;
    private final AgentStateService agentStateService;
    private final StatsService statsService;

    @WithFactory
    public DiseaseProgressionVisitor(DiseaseStageTransitionsService diseaseStageTransitionsService,
                                     SimulationClock simulationClock,
                                     AgentStateService agentStateService,
                                     StatsService statsService) {
        this.diseaseStageTransitionsService = diseaseStageTransitionsService;
        this.simulationClock = simulationClock;
        this.agentStateService = agentStateService;
        this.statsService = statsService;
    }

    public void visit(RandomGenerator random, Entity agent) {
        HealthStatus health = agent.get(HealthStatus.class);
        if (health == null) return;
        Stage currentStage = health.getStage();

        if (!currentStage.isSick()) {
            statsService.tickStage(currentStage);
            return;
        }

        int elapsed = health.getElapsedDays(simulationClock.getDaysPassed());

        int stageDuration = diseaseStageTransitionsService
                .selectDurationOf(currentStage, agent, health.getDiseaseLoad(), 0);

        if (elapsed >= stageDuration) {
            Stage nextStage = diseaseStageTransitionsService.selectOutcomeOf(
                    currentStage,
                    agent,
                    health.getDiseaseLoad(),
                    random.nextDouble());
            agentStateService.progressToDiseaseStage(agent, nextStage);
            statsService.tickStageChange(nextStage);
            statsService.tickStage(nextStage);
        } else {
            statsService.tickStage(currentStage);
        }

    }
}
