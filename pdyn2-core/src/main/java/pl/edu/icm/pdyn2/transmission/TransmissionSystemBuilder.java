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

package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.sampleSpace.SoftEnumSampleSpace;

public class TransmissionSystemBuilder {
    private final TransmissionService transmissionService;
    private final AgentStateService agentStateService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final StatsService statsService;

    private final RandomForChunkProvider randomForChunkProvider;
    private final Loads loads;
    private final Stages stages;

    @WithFactory
    public TransmissionSystemBuilder(TransmissionService transmissionService,
                                     AgentStateService agentStateService,
                                     AreaClusteredSelectors areaClusteredSelectors,
                                     Selectors selectors,
                                     StatsService statsService,
                                     RandomProvider randomProvider, Loads loads, Stages stages) {
        this.transmissionService = transmissionService;
        this.agentStateService = agentStateService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.statsService = statsService;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(TransmissionSystemBuilder.class);
        this.loads = loads;
        this.stages = stages;
    }

    public EntitySystem buildTransmissionSystem() {
        return EntityIterator.select(selectors.filtered(
                        areaClusteredSelectors.personSelector(),
                        selectors.hasComponents(Inhabitant.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, agent) -> {
                    if (!transmissionService.consideredForInfection(agent)) {
                        return;
                    }

                    SoftEnumSampleSpace<Load> exposurePerLoad =
                            new SoftEnumSampleSpace<>(loads);
                    EnumSampleSpace<ContextInfectivityClass> exposurePerContext = new EnumSampleSpace<>(ContextInfectivityClass.class);
                    transmissionService.gatherExposurePerLoadAndContextForAgent(
                            exposurePerLoad,
                            exposurePerContext,
                            agent);
                    float totalExposure = exposurePerLoad.sumOfProbabilities(); // TODO sumValues?

                    if (totalExposure > 0) {
                        var probability = transmissionService.exposureToProbability(totalExposure);
                        var chosenLoad = transmissionService.selectLoad(exposurePerLoad, random.nextDouble());
                        var adjustedProbability = transmissionService.adjustProbabilityWithImmunity(probability, chosenLoad, agent);

                        if (adjustedProbability > 0 && random.nextDouble() <= adjustedProbability) {
                            agentStateService.infect(agent, chosenLoad);
                            agentStateService.addSourcesDistribution(agent, exposurePerContext);
                            statsService.tickStageChange(stages.LATENT);
                        }
                    }
                });
    }
}
