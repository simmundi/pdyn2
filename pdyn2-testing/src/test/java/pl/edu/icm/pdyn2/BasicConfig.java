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

package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.mockito.Mockito;
import pl.edu.icm.pdyn2.covid19.immunization.ImmunizationStrategyFromPdyn1Rewritten;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategyFactory;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.AgeRangesFactory;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClasses;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClassesFactory;
import pl.edu.icm.pdyn2.model.context.ContextTypes;
import pl.edu.icm.pdyn2.model.context.ContextTypesFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.immunization.LoadsFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.model.progression.StagesFactory;
import pl.edu.icm.pdyn2.progression.OutcomeModifier;
import pl.edu.icm.pdyn2.progression.OutcomeModifierFactory;

import java.io.IOException;
import java.util.Map;

public class BasicConfig {

    public final Bento bento;
    public final Stages stages;
    public final Loads loads;
    public final AgeRanges ageRanges;
    public final ContextTypes contextTypes;
    public final ContextInfectivityClasses contextInfectivityClasses;
    public final OutcomeModifier outcomeModifier;
    public final ImmunizationStrategy immunizationStrategy;

    public final Load WILD;
    public final Load ALPHA;
    public final Load DELTA;
    public final Load MODERNA;
    public final Load BA1;
    public final Load PFIZER;
    public final Load BA45;
    public final Load BA2;

    public final Stage HEALTHY;
    public final Stage LATENT;
    public final Stage HOSPITALIZED_ICU;
    public final Stage INFECTIOUS_SYMPTOMATIC;
    public final Stage INFECTIOUS_ASYMPTOMATIC;
    public final Stage HOSPITALIZED_PRE_ICU;
    public final Stage HOSPITALIZED_NO_ICU;

    public final AgeRange RANGE_0_10;
    public final AgeRange RANGE_120_130;

    public BasicConfig(Map<String, ?> configExtension) {
        try {
            var bentoConfigurer = new Configurer()
                    .loadConfigResource("/ageRanges-basicConfig.properties")
                    .loadConfigResource("/contextTypes-basicConfig.properties")
                    .loadConfigResource("/loads-covid19-basicConfig.properties")
                    .loadConfigResource("/stages-covid19-basicConfig.properties")
                    .loadConfigResource("/contextInfectivityClasses-basicConfig.properties")
                    .setParam("pdyn2.infectivity.immunization.strategy", ImmunizationStrategyFromPdyn1Rewritten.class.getName())
                    .setParam("pdyn2.simulation.start", "2001-01-01");

            configExtension.forEach(bentoConfigurer::setParam);

            bento = bentoConfigurer.getConfig();
            stages = bento.get(StagesFactory.IT);
            loads = bento.get(LoadsFactory.IT);
            bento.register(ImmunizationStrategyFactory.IT, Mockito.spy(bento.get(ImmunizationStrategyFactory.IT)));
            ageRanges = bento.get(AgeRangesFactory.IT);
            contextTypes = bento.get(ContextTypesFactory.IT);
            contextInfectivityClasses = bento.get(ContextInfectivityClassesFactory.IT);
            outcomeModifier = bento.get(OutcomeModifierFactory.IT);
            immunizationStrategy = bento.get(ImmunizationStrategyFactory.IT);

            WILD = loads.getByName("WILD");
            ALPHA = loads.getByName("ALPHA");
            MODERNA = loads.getByName("MODERNA");
            DELTA = loads.getByName("DELTA");
            BA1 = loads.getByName("BA1");
            PFIZER = loads.getByName("PFIZER");
            BA45 = loads.getByName("BA45");
            BA2 = loads.getByName("BA2");

            HEALTHY = stages.getByName("HEALTHY");
            LATENT = stages.getByName("LATENT");
            HOSPITALIZED_ICU = stages.getByName("HOSPITALIZED_ICU");
            HOSPITALIZED_PRE_ICU = stages.getByName("HOSPITALIZED_PRE_ICU");
            INFECTIOUS_ASYMPTOMATIC = stages.getByName("INFECTIOUS_ASYMPTOMATIC");
            INFECTIOUS_SYMPTOMATIC = stages.getByName("INFECTIOUS_SYMPTOMATIC");
            HOSPITALIZED_NO_ICU = stages.getByName("HOSPITALIZED_NO_ICU");

            RANGE_0_10 = ageRanges.getByName("RANGE_0_10");
            RANGE_120_130 = ageRanges.getByName("RANGE_120_130");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BasicConfig() {
        this(Map.of());
    }
}
