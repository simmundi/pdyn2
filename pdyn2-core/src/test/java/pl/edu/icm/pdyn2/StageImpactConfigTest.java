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

package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.transmission.StageImpactConfig;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ExtendWith(MockitoExtension.class)
class StageImpactConfigTest {

    BasicConfig basicConfig = new BasicConfig();
    @Test
    void getInfluenceOf() {
        //given
        var stageShareConfig = new StageImpactConfig(0.1f, 1.0f, basicConfig.stages);
        //execute
        for (var stage : basicConfig.stages.values()) {
            float value = 0f;

            if (stage == basicConfig.stages.getByName("INFECTIOUS_ASYMPTOMATIC")) {
                value = 0.1f;
            }
            if (stage == basicConfig.stages.INFECTIOUS_SYMPTOMATIC) {
                value = 1f;
            }

            //assert
            assertThat(stageShareConfig.getInfluenceOf(stage)).isEqualTo(value);
        }
    }
}
