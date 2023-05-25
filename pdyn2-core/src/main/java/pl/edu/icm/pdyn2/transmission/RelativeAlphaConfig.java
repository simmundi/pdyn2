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
import pl.edu.icm.pdyn2.model.immunization.Loads;

public class RelativeAlphaConfig {
    private final Loads loads;
    private final float alphaAlpha;
    private final float alphaDelta;
    private final float alphaBA1;
    private final float alphaBA2;
    private final float alphaBA45;

    @WithFactory
    public RelativeAlphaConfig(Loads loads,
                               float alphaAlpha,
                               float alphaDelta,
                               float alphaBA1,
                               float alphaBA2,
                               float alphaBA45) {
        this.loads = loads;
        this.alphaAlpha = alphaAlpha;
        this.alphaDelta = alphaDelta;
        this.alphaBA1 = alphaBA1;
        this.alphaBA2 = alphaBA2;
        this.alphaBA45 = alphaBA45;
    }

    public float getRelativeAlpha(Load load) {
        if (load.name().equals("ALPHA")) {
            return alphaAlpha;
        }
        if (load.name().equals("DELTA")) {
            return alphaDelta;
        }
        if (load.name().equals("BA1")) {
            return alphaBA1;
        }
        if (load.name().equals("BA2")) {
            return alphaBA2;
        }
        if (load.name().equals("BA45")) {
            return alphaBA45;
        }
        if (load.name().equals("WILD")) {
            return 1.0f;
        }
        throw new IllegalArgumentException("Invalid load: " + load.name());
    }
}
