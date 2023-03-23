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

package pl.edu.icm.pdyn2.clock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

class SimulationClockTest {

    @Test
    @DisplayName("Should initialize with the current date")
    void construct() {
        // given & execute
        SimulationClock simulationClock = new SimulationClock("");

        // assert
        Assertions.assertThat(simulationClock.getCurrentDate())
                .isToday();
    }

    @Test
    @DisplayName("Should initialize with the given date")
    void construct__given_date() {
        // given & execute
        SimulationClock simulationClock = new SimulationClock("1977-04-01");

        // assert
        Assertions.assertThat(simulationClock.getCurrentDate())
                .isEqualTo(LocalDate.of(1977, Month.APRIL, 1));
    }

    @Test
    @DisplayName("Should advance the time by the given number of days")
    void getCurrentDate() {
        // given
        SimulationClock simulationClock = new SimulationClock("1977-04-01");

        // execute
        for (int i = 0; i < 30; i++) {
            simulationClock.advanceOneDay();
        }

        // assert
        Assertions.assertThat(simulationClock.getCurrentDate())
                .isEqualTo(LocalDate.of(1977, Month.MAY, 1));

    }
}
