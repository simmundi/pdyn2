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

package pl.edu.icm.pdyn2.progression.lookup;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.util.Quadruple;

import java.util.function.Consumer;
@ImplementationSwitch(
        configKey = "lookupTransitionsProvider",
        cases = {
                @ImplementationSwitch.When(name="config", implementation = ConfigLookupTransitionProvider.class, useByDefault = true),
        }
)
public interface LookupTransitionsProvider {
    void readTransitions(Consumer<Quadruple<Load, AgeRange, Stage, TransitionDescriptor>> consumer);
}
