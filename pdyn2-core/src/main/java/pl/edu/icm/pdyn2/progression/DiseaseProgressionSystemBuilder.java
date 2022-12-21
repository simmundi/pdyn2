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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Selectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class DiseaseProgressionSystemBuilder {

    private final DiseaseStageTransitionsService diseaseStageTransitionsService;
    private final SimulationTimer simulationTimer;
    private final AgentStateService agentStateService;
    private final StatsService statsService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final RandomForChunkProvider randomForChunkProvider;

    @WithFactory
    public DiseaseProgressionSystemBuilder(DiseaseStageTransitionsService diseaseStageTransitionsService,
                                           SimulationTimer simulationTimer,
                                           AgentStateService agentStateService,
                                           StatsService statsService,
                                           AreaClusteredSelectors areaClusteredSelectors,
                                           Selectors selectors,
                                           RandomProvider randomProvider) {
        this.diseaseStageTransitionsService = diseaseStageTransitionsService;
        this.simulationTimer = simulationTimer;
        this.agentStateService = agentStateService;
        this.statsService = statsService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(DiseaseProgressionSystemBuilder.class);
    }

    public EntitySystem buildProgressionSystem() {
        return select(selectors.filtered(areaClusteredSelectors.personSelector(), selectors.hasComponents(HealthStatus.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, entity) -> {
                    HealthStatus health = entity.get(HealthStatus.class);
                    Person person = entity.get(Person.class);
                    Stage currentStage = health.getStage();

                    if (!currentStage.hasOutcomes()) {
                        statsService.tickStage(currentStage);
                        return;
                    }

                    int elapsed = health.getElapsedDays(simulationTimer.getDaysPassed());

                    int stageDuration = diseaseStageTransitionsService
                            .durationOf(health.getDiseaseLoad(), currentStage, person.getAge());

                    if (elapsed >= stageDuration) {
                        Stage nextStage = diseaseStageTransitionsService.outcomeOf(
                                currentStage,
                                entity,
                                health.getDiseaseLoad(),
                                random.nextDouble());
                        agentStateService.progressToDiseaseStage(entity, nextStage);
                        statsService.tickStageChange(nextStage);
                        statsService.tickStage(nextStage);
                    } else {
                        statsService.tickStage(currentStage);
                    }
                });
    }
}
