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

package pl.edu.icm.pdyn2.model;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import pl.edu.icm.pdyn2.model.context.ContextTypes;
import pl.edu.icm.pdyn2.model.context.ContextTypesFactory;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.immunization.LoadsFactory;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.model.progression.StagesFactory;

import java.io.IOException;

public class BasicConfig {

    public final Bento bento;
    public final Stages stages;
    public final Loads loads;
    public final AgeRanges ageRanges;
    public final ContextTypes contextTypes;

    public BasicConfig() {
        try {
            bento = new Configurer()
                    .loadConfigResource("/ageRanges.properties")
                    .loadConfigResource("/contextTypes.properties")
                    .loadConfigResource("/loads.properties")
                    .loadConfigResource("/stages.properties")
                    .getConfig();
            stages = bento.get(StagesFactory.IT);
            loads = bento.get(LoadsFactory.IT);
            ageRanges = bento.get(AgeRangesFactory.IT);
            contextTypes = bento.get(ContextTypesFactory.IT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
