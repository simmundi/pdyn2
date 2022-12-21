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

package pl.edu.icm.pdyn2.model.context;

/**
 *  Utils for storing float values as fixed point numbers, to be
 *  used in contexts (to store contamination level and agent count).
 *
 *  Keeping three decimal places (i.e. 0.001 is represented as 1).
 *  allows us to store values up to around 500_000f, which
 *  should be enough for storing agents, since the four mostly populated
 *  square kilometers in Poland have - on the whole - less than 100 000 people
 *  in them.
 */

class Integerizer {
    private final static float PRECISION = 1000f;

    public static int toInt(float value) {
        return Math.round(value * PRECISION);
    }

    public static float toFloat(int value) {
        return (float)value / PRECISION;
    }


}
