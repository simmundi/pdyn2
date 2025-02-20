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

package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import net.snowyhollows.bento.annotation.ImplementationSwitch.When;
import pl.edu.icm.pdyn2.immunization.strategy.SimpleImmunizationStrategy;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.Entity;

@ImplementationSwitch(
        configKey = "pdyn2.infectivity.immunization.strategy",
        cases = {
                @When(name="constant", implementation = SimpleImmunizationStrategy.class, useByDefault = true)
        }
)
public interface ImmunizationStrategy {
    float getImmunizationCoefficient(Entity agent,
                                     ImmunizationStage immunizationStage,
                                     Load load,
                                     int day);
}
