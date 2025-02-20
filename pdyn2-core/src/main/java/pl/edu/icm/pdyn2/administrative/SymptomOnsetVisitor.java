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

package pl.edu.icm.pdyn2.administrative;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.pdyn2.isolation.IsolationService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * Checks whether agent is in the first day of their symptoms and
 * executes logic for self-isolation and performing an official test.
 *
 */
public class SymptomOnsetVisitor {

    private final TestingService testingService;
    private final IsolationService isolationService;
    private final SimulationClock simulationClock;
    private final Stages stages;
    private final Stage symptomOnsetStage;

    @WithFactory
    public SymptomOnsetVisitor(TestingService testingService,
                               IsolationService isolationService,
                               SimulationClock simulationClock,
                               @ByName("pdyn2.behavior.testing.symptoms-onset") String symptomOnsetStage,
                               Stages stages) {
        this.testingService = testingService;
        this.isolationService = isolationService;
        this.simulationClock = simulationClock;
        this.stages = stages;
        this.symptomOnsetStage = this.stages.getByName(symptomOnsetStage);
    }

    public void visit(RandomGenerator random, Entity agent) {
        HealthStatus healthStatus = agent.get(HealthStatus.class);

        if (healthStatus == null) {
            return;
        }

        if (healthStatus.getStage() != symptomOnsetStage || healthStatus.getDayOfLastChange() != simulationClock.getDaysPassed()) {
            return;
        }

        // This is the first day of symptoms
        isolationService.maybeIsolateAgent(random.nextFloat(), agent);
        testingService.maybeTestAgent(random.nextFloat(), agent);
    }
}
