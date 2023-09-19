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
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

public class AgeRanges extends SoftEnumManager<AgeRange> {

    @WithFactory
    public AgeRanges(Bento bento) {
        super(bento, "pdyn2.infectivity.progression.lookup.age-ranges", AgeRangeFactory.IT);
    }

    @Override
    public AgeRange[] emptyArray() {
        return new AgeRange[0];
    }

    public AgeRange ofRange(int ageFrom, int ageTo) {
        for (AgeRange value : values()) {
            if (value.ageFrom == ageFrom && value.ageTo == ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported age range: " + ageFrom + "-" + ageTo);
    }

    public AgeRange of(int age) {
        for (AgeRange value : values()) {
            if (age >= value.ageFrom && age < value.ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Age outside of any range: " + age);
    }
}
