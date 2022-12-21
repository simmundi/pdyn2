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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;

public class RelativeAlphaConfig {
    float alphaAlpha;
    float alphaDelta;
    float alphaOmicron;
    float alphaBA2;
    float alphaBA45;

    @WithFactory
    public RelativeAlphaConfig(float alphaAlpha,
                               float alphaDelta,
                               float alphaOmicron,
                               float alphaBA2,
                               float alphaBA45) {
        this.alphaAlpha = alphaAlpha;
        this.alphaDelta = alphaDelta;
        this.alphaOmicron = alphaOmicron;
        this.alphaBA2 = alphaBA2;
        this.alphaBA45 = alphaBA45;
    }

    public float getRelativeAlpha(Load load) {
        switch (load) {
            case ALPHA:
                return alphaAlpha;
            case DELTA:
                return alphaDelta;
            case OMICRON:
                return alphaOmicron;
            case BA2:
                return alphaBA2;
            case BA45:
                return alphaBA45;
            case WILD:
                return 1.0f;
            default:
                throw new IllegalArgumentException("Invalid load: " + load +
                        " Relative alpha available for loads: ALPHA, DELTA, OMICRON, BA2, BA45, WILD");
        }
    }
}
