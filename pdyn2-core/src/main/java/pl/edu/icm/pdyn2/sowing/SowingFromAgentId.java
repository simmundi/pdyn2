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

package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.util.Status;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SowingFromAgentId implements SowingService {

    private final InfectedLoaderFromAgentId infectedLoaderFromAgentId;
    private final EngineIo board;
    private final StaticSelectors staticSelectors;
    private final AgentStateService agentStateService;
    private final Loads loads;
    private final Stages stages;

    @WithFactory
    public SowingFromAgentId(InfectedLoaderFromAgentId infectedLoaderFromAgentId,
                             EngineIo board,
                             StaticSelectors staticSelectors, AgentStateService agentStateService, Loads loads, Stages stages) {
        this.infectedLoaderFromAgentId = infectedLoaderFromAgentId;
        this.board = board;
        this.staticSelectors = staticSelectors;
        this.agentStateService = agentStateService;
        this.loads = loads;
        this.stages = stages;
    }

    public void sow() {
        var status = Status.of("infecting people from file: " + infectedLoaderFromAgentId.getSowingFilename(), 10);

        List<InfectedAgentFromCsv> infectedList = null;
        try {
            infectedList = infectedLoaderFromAgentId.readInfected();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        var infectedMap = infectedList.stream().collect(Collectors.toMap(
                InfectedAgentFromCsv::getAgentId,
                Function.identity()
        ));

        AtomicInteger agentId = new AtomicInteger();

        Selector householdSelector = staticSelectors.select(staticSelectors.config().withMandatoryComponents(Household.class).build());

        board.getEngine().execute(EntityIterator.select(householdSelector).forEach(householdEntity -> {
            var household = householdEntity.get(Household.class);
            var members = household.getMembers();
            members.forEach(memberEntity -> {
                var id = agentId.getAndIncrement();
                if (infectedMap.containsKey(id)) {
                    status.tick();
                    agentStateService.infect(memberEntity, loads.WILD);
                    agentStateService.addSourcesDistribution(memberEntity,
                            new EnumSampleSpace<>(ContextInfectivityClass.class));
                    int elapsedDays = infectedMap.get(id).getElapsedDays();
                    Stage stage = stages.LATENT;
                    if (elapsedDays > 5) {
                        stage = stages.INFECTIOUS_SYMPTOMATIC;
                        elapsedDays -= 5;
                    }
                    agentStateService.progressToDiseaseStage(memberEntity, stage, elapsedDays);
                }
            });
        }));
        status.done();
    }
}
