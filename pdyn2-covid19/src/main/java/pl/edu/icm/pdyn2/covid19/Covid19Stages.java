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

package pl.edu.icm.pdyn2.covid19;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;

public class Covid19Stages {
    public final Stage LATENT;
    public final Stage DECEASED;
    public final Stage HEALTHY;
    public final Stage INFECTIOUS_ASYMPTOMATIC;
    public final Stage INFECTIOUS_SYMPTOMATIC;
    public final Stage HOSPITALIZED_ICU;
    public final Stage HOSPITALIZED_PRE_ICU;
    public final Stage HOSPITALIZED_NO_ICU;

    @WithFactory
    public Covid19Stages(Stages stages) {
        LATENT = stages.getByName("LATENT");
        DECEASED = stages.getByName("DECEASED");
        HEALTHY = stages.getByName("HEALTHY");
        INFECTIOUS_SYMPTOMATIC = stages.getByName("INFECTIOUS_SYMPTOMATIC");
        INFECTIOUS_ASYMPTOMATIC = stages.getByName("INFECTIOUS_ASYMPTOMATIC");
        HOSPITALIZED_ICU = stages.getByName("HOSPITALIZED_ICU");
        HOSPITALIZED_PRE_ICU = stages.getByName("HOSPITALIZED_PRE_ICU");
        HOSPITALIZED_NO_ICU = stages.getByName("HOSPITALIZED_NO_ICU");
    }
}
