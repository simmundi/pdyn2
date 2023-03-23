/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.sampleSpace.SoftEnumSampleSpace;

public class ImmunityEffectsOutcomeModifier implements OutcomeModifier {

    private final ImmunizationStrategy immunizationStrategy;
    private final SimulationClock simulationClock;
    private final Stages stages;

    private final Stage INFECTIOUS_ASYMPTOMATIC;
    private final Stage HOSPITALIZED_NO_ICU;
    private final Stage HOSPITALIZED_PRE_ICU;

    @WithFactory
    public ImmunityEffectsOutcomeModifier(ImmunizationStrategy immunizationStrategy, SimulationClock simulationClock, Stages stages) {
        this.immunizationStrategy = immunizationStrategy;
        this.simulationClock = simulationClock;
        this.stages = stages;

        INFECTIOUS_ASYMPTOMATIC = stages.getByName("INFECTIOUS_ASYMPTOMATIC");
        HOSPITALIZED_NO_ICU = stages.getByName("HOSPITALIZED_NO_ICU");
        HOSPITALIZED_PRE_ICU = stages.getByName("HOSPITALIZED_PRE_ICU");
    }

    @Override
    public void modifyOutcomes(SoftEnumDiscretePDF<Stage> pdf, Load load, Entity person) {
        var currentDay = simulationClock.getDaysPassed();
        var immunization = person.get(Immunization.class);
        if (pdf.isNonZero(stages.INFECTIOUS_SYMPTOMATIC)) {
            var sigmaObjawowy = immunizationStrategy.getImmunizationCoefficient(immunization,
                    ImmunizationStage.SYMPTOMATIC,
                    load,
                    currentDay);
            pdf.scaleAndCompensate(stages.INFECTIOUS_SYMPTOMATIC,
                    1 - sigmaObjawowy,
                    INFECTIOUS_ASYMPTOMATIC);
        } else {
            if (pdf.isNonZero(HOSPITALIZED_NO_ICU)) {
                var sigmaBezOiom = immunizationStrategy.getImmunizationCoefficient(immunization,
                        ImmunizationStage.ASYMPTOMATIC,
                        load,
                        currentDay);
                pdf.scaleAndCompensate(HOSPITALIZED_NO_ICU,
                        1 - sigmaBezOiom,
                        stages.HEALTHY);
            }
            if (pdf.isNonZero(HOSPITALIZED_PRE_ICU)) {
                var sigmaPrzedOiom = immunizationStrategy.getImmunizationCoefficient(immunization,
                        ImmunizationStage.HOSPITALIZED_PRE_ICU,
                        load,
                        currentDay);
                pdf.scaleAndCompensate(HOSPITALIZED_PRE_ICU,
                        1 - sigmaPrzedOiom,
                        stages.HEALTHY);
            }
        }
    }
}
