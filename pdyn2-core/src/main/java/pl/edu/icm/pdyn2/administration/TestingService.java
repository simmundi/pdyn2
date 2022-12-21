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

package pl.edu.icm.pdyn2.administration;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.administration.Record;
import pl.edu.icm.pdyn2.model.administration.RecordType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

public final class TestingService {
    private final SimulationTimer simulationTimer;
    private final RandomGenerator randomGenerator;
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final TestingConfig testingConfig;

    @WithFactory
    public TestingService(SimulationTimer simulationTimer,
                          RandomProvider randomProvider,
                          StatsService statsService,
                          AgentStateService agentStateService, TestingConfig testingConfig) {
        this.simulationTimer = simulationTimer;
        this.randomGenerator = randomProvider.getRandomGenerator(TestingService.class);
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.testingConfig = testingConfig;
    }

    public void maybeTestAgent(Entity agentEntity) {
        maybeTestAgentOnDay(agentEntity, 0);
    }

    public void maybeTestAgentOnDay(Entity agentEntity, int daysFromTest) {
        Preconditions.checkArgument(daysFromTest >= 0, "Cannot test agent at a future date. daysFromTest should be >= 0: %s", daysFromTest);
        float baseProbabilityOfTest = testingConfig.getBaseProbabilityOfTest();

        if (baseProbabilityOfTest > 0 && randomGenerator.nextFloat() < baseProbabilityOfTest) {
            testAgent(agentEntity, simulationTimer.getDaysPassed() - daysFromTest);
        }
    }

    private void testAgent(Entity agentEntity, int dayOfTest) {
        HealthStatus healthStatus = agentEntity.get(HealthStatus.class);
        if (healthStatus.getStage().isSick()) {
            MedicalHistory medicalHistory = agentEntity.getOrCreate(MedicalHistory.class);
            medicalHistory.getRecords().add(record(dayOfTest));
            statsService.tickTestedPositive();

            Entity homeContextEntity = agentEntity.get(Inhabitant.class).getHomeContext();
            for (Entity member : homeContextEntity.get(Household.class).getMembers()) {
                agentStateService.beginQuarantineOnDay(member, dayOfTest);
                statsService.tickQuarantined();
            }
        }
    }

    private Record record(int dayOfTest) {
        Record record = new Record();
        record.setDay(dayOfTest);
        record.setType(RecordType.POSITIVE_TEST);
        return record;
    }
}
