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

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClasses;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.trurl.ecs.Entity;

public class TransmissionVisitor {
    private final TransmissionService transmissionService;
    private final AgentStateService agentStateService;
    private final StatsService statsService;
    private final Loads loads;
    private final Stages stages;
    private final ContextInfectivityClasses contextInfectivityClasses;
    private final Stage exposed;

    @WithFactory
    public TransmissionVisitor(TransmissionService transmissionService,
                               AgentStateService agentStateService,
                               StatsService statsService,
                               Loads loads,
                               Stages stages,
                               ContextInfectivityClasses contextInfectivityClasses,
                               @ByName("stages.exposed") String exposedStage) {
        this.transmissionService = transmissionService;
        this.agentStateService = agentStateService;
        this.statsService = statsService;
        this.loads = loads;
        this.stages = stages;
        this.contextInfectivityClasses = contextInfectivityClasses;
        this.exposed = stages.getByName(exposedStage);
    }


    public void visit(RandomGenerator random, Entity agent) {
        if (!transmissionService.consideredForInfection(agent)) {
            return;
        }

        SoftEnumDiscretePDF<Load> exposurePerLoad =
                new SoftEnumDiscretePDF<>(loads);
        SoftEnumDiscretePDF<ContextInfectivityClass> exposurePerContext = new SoftEnumDiscretePDF<>(contextInfectivityClasses);
        transmissionService.gatherExposurePerLoadAndContextForAgent(
                exposurePerLoad,
                exposurePerContext,
                agent);
        float totalExposure = exposurePerLoad.total();

        if (totalExposure > 0) {
            var baseProbability = transmissionService.exposureToProbability(totalExposure);
            for (var load : loads.viruses()) {
                double adjustedProbability = transmissionService.adjustProbabilityWithImmunity(baseProbability, load, agent);
                exposurePerLoad.set(load,
                        (float) (exposurePerLoad.get(load) * adjustedProbability / totalExposure)
                );
            }

            double jointProbability = exposurePerLoad.total();
            if (jointProbability > 0) {
                double r = random.nextDouble();
                if (r <= jointProbability) {
                    var chosenLoad = exposurePerLoad.sampleUnnormalized(r);
                    agentStateService.infect(agent, chosenLoad);
                    agentStateService.addSourcesDistribution(agent, exposurePerContext);
                    statsService.tickStageChange(exposed);
                }
            }
        }
    }
}
