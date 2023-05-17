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

package pl.edu.icm.pdyn2.simulation;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.administrative.SymptomOnsetVisitor;
import pl.edu.icm.pdyn2.impact.AgentImpactVisitor;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.progression.DiseaseProgressionVisitor;
import pl.edu.icm.pdyn2.quarantine.EndQuarantineVisitor;
import pl.edu.icm.pdyn2.transmission.TransmissionVisitor;
import pl.edu.icm.pdyn2.travel.TravelVisitor;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.IteratingSystemBuilder;
import pl.edu.icm.trurl.ecs.util.Visit;

import static pl.edu.icm.trurl.ecs.util.Systems.sequence;
import static pl.edu.icm.trurl.ecs.util.Systems.withStatusMessage;

/**
 * Builds a system representing the whole simulation logic for the disease spread.
 * Should be composable with other simulations, disease-oriented or not, running
 * in parallel.
 * <p>
 * Does not include ticking the simulation clock or gathering statistics.
 */
public class DefaultSimulation {
    private final TransmissionVisitor transmissionVisitor;
    private final DiseaseProgressionVisitor diseaseProgressionVisitor;
    private final TravelVisitor travelVisitor;
    private final EndQuarantineVisitor endQuarantineVisitor;
    private final SymptomOnsetVisitor startsFeelingSickVisitor;
    private final AgentImpactVisitor agentImpactVisitor;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final RandomForChunkProvider randomForChunkProvider;

    @WithFactory
    public DefaultSimulation(TransmissionVisitor transmissionVisitor,
                             DiseaseProgressionVisitor diseaseProgressionVisitor,
                             TravelVisitor travelVisitor,
                             EndQuarantineVisitor endQuarantineVisitor,
                             SymptomOnsetVisitor startsFeelingSickVisitor,
                             AgentImpactVisitor agentImpactVisitor,
                             AreaClusteredSelectors areaClusteredSelectors,
                             RandomProvider randomProvider) {

        this.transmissionVisitor = transmissionVisitor;
        this.diseaseProgressionVisitor = diseaseProgressionVisitor;
        this.travelVisitor = travelVisitor;
        this.endQuarantineVisitor = endQuarantineVisitor;
        this.startsFeelingSickVisitor = startsFeelingSickVisitor;
        this.agentImpactVisitor = agentImpactVisitor;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(DefaultSimulation.class);
    }

    public EntitySystem defaultSimulationDay() {

        var allAgents =  areaClusteredSelectors.personSelector();

        var impactSystem = IteratingSystemBuilder.iteratingOverInParallel(allAgents)
                .persistingAll()
                .withoutContext()
                .perform(Visit.of(agentImpactVisitor::updateImpact))
                .build();

        var stateChangeSystem = IteratingSystemBuilder.iteratingOverInParallel(allAgents)
                .persistingAll()
                .withContext(randomForChunkProvider)
                .perform(Visit.of(transmissionVisitor::visit))
                .andPerform(Visit.of(diseaseProgressionVisitor::visit))
                .andPerform(Visit.of(travelVisitor::visit))
                .andPerform(Visit.of(endQuarantineVisitor::maybeEndQuarantine))
                .andPerform(Visit.of(startsFeelingSickVisitor::visit))
                .build();

        return sequence(
                withStatusMessage(impactSystem, "Calculates impact on contexts"),
                withStatusMessage(stateChangeSystem, "Changes agents' state"));
    }
}
