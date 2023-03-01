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
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.sampleSpace.SoftEnumSampleSpace;

public class TransmissionVisitor {
    private final TransmissionService transmissionService;
    private final AgentStateService agentStateService;
    private final StatsService statsService;
    private final Loads loads;
    private final Stages stages;

    @WithFactory
    public TransmissionVisitor(TransmissionService transmissionService,
                               AgentStateService agentStateService,
                               StatsService statsService,
                               Loads loads,
                               Stages stages) {
        this.transmissionService = transmissionService;
        this.agentStateService = agentStateService;
        this.statsService = statsService;
        this.loads = loads;
        this.stages = stages;
    }


    public void visit(RandomGenerator random, Entity agent) {
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
    }
}
