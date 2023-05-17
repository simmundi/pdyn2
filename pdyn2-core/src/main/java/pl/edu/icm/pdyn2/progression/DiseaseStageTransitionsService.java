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

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.lookup.LookupStageTransitionsService;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * A stateless service that allows sampling outcomes and durations of any given stage of a given disease (load),
 * taking into account parameters of an actual person (like age or sex).
 *
 * The default implementation uses a lookup table in the form of (load, age range, stage) -> (discrete PDF of outcomes, duration in days)
 */
@ImplementationSwitch(
        configKey = "diseaseStageTransitionService",
        cases = {
                @ImplementationSwitch.When(name="lookup", implementation = LookupStageTransitionsService.class, useByDefault = true),
        }
)
public interface DiseaseStageTransitionsService {
    int selectDurationOf(Stage stage,
                         Entity person,
                         Load load,
                         double random);

    Stage selectOutcomeOf(Stage stage,
                          Entity person,
                          Load load,
                          double random);
}
