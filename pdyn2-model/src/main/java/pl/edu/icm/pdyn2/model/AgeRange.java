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

package pl.edu.icm.pdyn2.model;

public enum AgeRange {
    RANGE_0_10(0, 10),
    RANGE_10_20(10, 20),
    RANGE_20_30(20, 30),
    RANGE_30_40(30, 40),
    RANGE_40_50(40, 50),
    RANGE_50_60(50, 60),
    RANGE_60_70(60, 70),
    RANGE_70_80(70, 80),
    RANGE_80_90(80, 90),
    RANGE_90_100(90, 100),
    RANGE_100_110(100, 110),
    RANGE_110_120(110, 120),
    RANGE_120_130(120, 130);

    public final int ageFrom;
    public final int ageTo;

    AgeRange(int ageFrom, int ageTo) {
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
    }


    public static AgeRange ofRange(int ageFrom, int ageTo) {
        for (AgeRange value : values()) {
            if (value.ageFrom == ageFrom && value.ageTo == ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported age range: " + ageFrom + "-" + ageTo);
    }

    public static AgeRange of(int age) {
        for (AgeRange value : values()) {
            if (age >= value.ageFrom && age < value.ageTo) {
                return value;
            }
        }
        throw new IllegalArgumentException("Age outside of any range: " + age);
    }
}
