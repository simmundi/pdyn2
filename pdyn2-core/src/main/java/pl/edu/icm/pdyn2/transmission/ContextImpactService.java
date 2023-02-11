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

package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategyFromPdyn1;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategyFromPdyn1Rewritten;
import pl.edu.icm.pdyn2.immunization.SimpleImmunizationStrategy;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
@ImplementationSwitch(
        configKey = "contextImpactService",
        cases = {
                @ImplementationSwitch.When(name="basic", implementation = BasicContextImpactService.class),
        }
)
public interface ContextImpactService {
    int AGE_RANGE_COUNT = AgeRange.values().length;
    int CONTEXT_TYPE_COUNT = ContextType.values().length;

    float calculateInfluenceFractionFor(Person person, Context context);
}
