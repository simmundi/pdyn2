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

package pl.edu.icm.pdyn2.model.immunization;

import static pl.edu.icm.pdyn2.model.immunization.LoadClassification.VACCINE;
import static pl.edu.icm.pdyn2.model.immunization.LoadClassification.VIRUS;

public enum Load {
    WILD(VIRUS),
    ALPHA(VIRUS),
    DELTA(VIRUS),
    OMICRON(VIRUS),
    BA2(VIRUS),
    BA45(VIRUS),

    PFIZER(VACCINE),
    ASTRA(VACCINE),
    MODERNA(VACCINE),
    BOOSTER(VACCINE);

    public final LoadClassification classification;

    Load(LoadClassification classification) {
        this.classification = classification;
    }

    public static Load[] viruses() {
        return new Load[]{
                WILD, ALPHA, DELTA, OMICRON, BA2, BA45
        };
    }
}
