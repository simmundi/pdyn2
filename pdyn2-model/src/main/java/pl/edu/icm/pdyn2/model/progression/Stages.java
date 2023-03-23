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

package pl.edu.icm.pdyn2.model.progression;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

public class Stages extends SoftEnumManager<Stage> {
    public final Stage HEALTHY;
    public final Stage INFECTIOUS_SYMPTOMATIC;
    public final Stage DECEASED;

    @WithFactory
    public Stages(Bento bento) {
        super(bento, "stages", StageFactory.IT);
        INFECTIOUS_SYMPTOMATIC = getByName("INFECTIOUS_SYMPTOMATIC");
        DECEASED = getByName("DECEASED");
        HEALTHY = getByName("HEALTHY");
    }

    @Override
    public Stage[] emptyArray() {
        return new Stage[0];
    }
}
