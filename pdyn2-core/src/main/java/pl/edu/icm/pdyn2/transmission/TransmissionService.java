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
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.pdyn2.model.context.Contamination;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * Logic for transmission of the disease.
 * <p>
 * The point of view is that of a healthy agent who might be infected
 * by one of the visited contexts.
 */
public class TransmissionService {
    private final TransmissionConfig transmissionConfig;
    private final ContextsService contextsService;
    private final SimulationClock simulationClock;
    private final Stages stages;
    private final ImmunizationStrategy immunizationStrategy;
    private final SoftEnumDiscretePDF<Load> relativeAlpha;
    private final Stage susceptibleStage;

    @WithFactory
    public TransmissionService(ContextsService contextsService,
                               TransmissionConfig transmissionConfig,
                               SimulationClock simulationClock,
                               Loads loads,
                               Stages stages,
                               @ByName("stages.susceptible") String susceptibleStageName,
                               ImmunizationStrategy immunizationStrategy) {
        this.relativeAlpha = new SoftEnumDiscretePDF<>(loads);
        this.contextsService = contextsService;
        this.transmissionConfig = transmissionConfig;
        this.simulationClock = simulationClock;
        this.stages = stages;
        this.susceptibleStage = stages.getByName(susceptibleStageName);
        this.immunizationStrategy = immunizationStrategy;
        for (Load currentLoad : loads.viruses()) {
            relativeAlpha.set(currentLoad, currentLoad.virulence);
        }
    }

    /**
     * Agents are only considered for infection if they are healthy.
     *
     * @param agent
     * @return true, if the agent is healthy
     */
    public boolean consideredForInfection(Entity agent) {
        HealthStatus healthStatus = agent.get(HealthStatus.class);
        return healthStatus != null && healthStatus.getStage() == susceptibleStage;
    }


    /**
     * Updates the given infectivity object using contexts which are active for the given agent
     * (i.e. a DEAD agent will not interact with any contexts, QUARANTINED agent will interact
     * only with their home context, ROUTINE agent will interact with schools / work / streets etc.
     *
     * @param infectivity map from Load type (e.g. ALPHA) to float representing the "total viral load"
     * @param entity      the agent entity
     * @return
     */
    public void gatherExposurePerLoadAndContextForAgent(SoftEnumDiscretePDF<Load> infectivity,
                                                        SoftEnumDiscretePDF<ContextInfectivityClass> infectionSourcePDF,
                                                        Entity entity) {
        var contexts = contextsService.findActiveContextsForAgent(entity);
        contexts.forEach(context -> {
            addContextInfectivity(context, infectivity);
            updateSources(context, infectionSourcePDF);
        });
        infectivity.multiply(relativeAlpha);
    }


    /**
     * Calculates the basic formula for probability of getting sick based on the exposure
     *
     * @param totalExposure
     */
    public double exposureToProbability(float totalExposure) {
        return 1 - Math.exp(-transmissionConfig.getAlpha() * totalExposure);
    }

    /**
     * Given the exposure per load, selects (based on a random number given) one of the loads
     *
     * @param exposurePerLoad as gathered by this#gatherExposurePerLoadForAgent()
     * @param randomDouble    number between 0 to 1
     * @return
     */
    public Load selectLoad(SoftEnumDiscretePDF<Load> exposurePerLoad, double randomDouble) {
        // this should never choose "default", because for valid randomDouble (less than 1) the value
        // will always be below total.
        return exposurePerLoad.sampleUnnormalized(randomDouble * exposurePerLoad.total());
    }

    /**
     * Given a load, total base probability of getting sick and an agent, returns the probability
     * adjusted for agent's immunization history, for the current date (from simulationTimer)
     *
     * @param probability
     * @param chosenLoad
     * @param agent
     * @return
     */
    public double adjustProbabilityWithImmunity(double probability, Load chosenLoad, Entity agent) {
        Immunization immunization = agent.get(Immunization.class);
        double coefficient = immunization == null ? 1 : (1 - immunizationStrategy.getImmunizationCoefficient(immunization,
                ImmunizationStage.LATENT,
                chosenLoad,
                simulationClock.getDaysPassed()));
        return probability * coefficient;
    }

    private void addContextInfectivity(Context context, SoftEnumDiscretePDF<Load> infectivity) {
        var weight = transmissionConfig.getTotalWeightForContextType(context.getContextType());
        var agentCount = context.getAgentCount();
        for (Contamination contamination : context.getContaminations()) {
            var loadInContext = contamination.getLoad();
            var levelInContext = contamination.getLevel() * weight / agentCount;
            infectivity.shift(loadInContext, levelInContext);
        }
    }

    private void updateSources(Context context, SoftEnumDiscretePDF<ContextInfectivityClass> infectionSources) {
        var weight = transmissionConfig.getTotalWeightForContextType(context.getContextType());
        var agentCount = context.getAgentCount();
        for (Contamination contamination : context.getContaminations()) {
            var loadInContext = contamination.getLoad();
            var levelInContext = contamination.getLevel() * weight / agentCount;
            infectionSources
                    .shift(context.getContextType().getInfectivityClass(),
                            levelInContext * relativeAlpha.get(loadInContext));
        }
    }
}
