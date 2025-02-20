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

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.administration.Record;
import pl.edu.icm.pdyn2.model.administration.RecordType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.trurl.ecs.Entity;

public final class TestingService {
    private final SimulationClock simulationClock;
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final TestingConfig testingConfig;

    @WithFactory
    public TestingService(SimulationClock simulationClock,
                          StatsService statsService,
                          AgentStateService agentStateService, TestingConfig testingConfig) {
        this.simulationClock = simulationClock;
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.testingConfig = testingConfig;
    }

    public void maybeTestAgent(float random, Entity agentEntity) {
        maybeTestAgentOnDay(random, agentEntity, 0);
    }

    public void maybeTestAgentOnDay(float random, Entity agentEntity, int daysFromTest) {
        Preconditions.checkArgument(daysFromTest >= 0, "Cannot test agent at a future date. daysFromTest should be >= 0: %s", daysFromTest);
        float baseProbabilityOfTest = testingConfig.getBaseProbabilityOfTest();

        if (baseProbabilityOfTest > 0 && random < baseProbabilityOfTest) {
            testAgent(agentEntity, simulationClock.getDaysPassed() - daysFromTest);
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
