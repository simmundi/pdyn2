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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;

import java.util.Arrays;

public class StageImpactConfig {
    private final float[] influenceOfStage;

    @WithFactory
    public StageImpactConfig(Bento bento, float asymptomaticInfluenceShare, float symptomaticInfluenceShare, Stages stages) {
        influenceOfStage  = new float[stages.values().size()];
        for (int i = 0; i < stages.values().size(); i++) {
            Stage stage = stages.values().get(i);
            if (bento.get("stage_impact." + stage.name(), "absent") != "absent") {
                influenceOfStage[i] = bento.getFloat("stage_impact." + stage.name());
            } else if (stage.infectious) {
                influenceOfStage[i] = 1;
            }
        }
    }

    public float getInfluenceOf(Stage stage) {
        return influenceOfStage[stage.ordinal()];
    }
}
