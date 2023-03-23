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

package pl.edu.icm.pdyn2.impact;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.transmission.ContextImpactService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.transmission.StageImpactConfig;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * If agents impact has changed from the last time it was checked,
 * it is replaced (by withdrawing the old impact and applying the new)
 */
public class AgentImpactVisitor {
    private final ContextsService contextsService;
    private final ContextImpactService contextImpactService;
    private final StatsService statsService;
    private final StageImpactConfig stageImpactConfig;
    private final Stages stages;
    private final static int REMOVE = -1;
    private final static int ADD = 1;

    @WithFactory
    public AgentImpactVisitor(ContextsService contextsService,
                              ContextImpactService contextImpactService,
                              StatsService statsService,
                              StageImpactConfig stageImpactConfig,
                              Stages stages) {
        this.contextsService = contextsService;
        this.contextImpactService = contextImpactService;
        this.statsService = statsService;
        this.stageImpactConfig = stageImpactConfig;
        this.stages = stages;
    }

    public void updateImpact(Entity agentEntity) {
        Behaviour behaviour = agentEntity.get(Behaviour.class);

        if (behaviour == null) {
            return;
        }

        Impact impact = agentEntity.getOrCreate(Impact.class);
        HealthStatus disease = agentEntity.get(HealthStatus.class);

        if (impact.isDifferentFrom(behaviour, disease)) {
            Person person = agentEntity.get(Person.class);
            applyAgentInfluence(agentEntity, impact, person, REMOVE);
            impact.affect(behaviour, disease);
            applyAgentInfluence(agentEntity, impact, person, ADD);
            statsService.tickChangedImpact();
        }
    }

    private void applyAgentInfluence(Entity agentEntity, Impact impact, Person person, int sign) {
        Stage currentStage = impact.getStage() == null ? stages.HEALTHY : impact.getStage();
        Load load = currentStage.isInfectious() ? impact.getLoad() : null;

        float activityDelta = sign;
        float infectionDelta = stageImpactConfig.getInfluenceOf(currentStage) * sign;

        contextsService.findActiveContextsForAgent(agentEntity, impact).forEach(c -> {
            float influenceFraction = contextImpactService.calculateInfluenceFractionFor(person, c);
            c.updateAgentCount(activityDelta * influenceFraction);
            if (load != null) {
                float scaledInfectionDelta = infectionDelta * influenceFraction;
                c.changeContaminationLevel(load, scaledInfectionDelta);
            }
        });
    }
}
