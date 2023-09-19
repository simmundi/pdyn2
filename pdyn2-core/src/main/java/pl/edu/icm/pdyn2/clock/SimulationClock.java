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

import com.google.common.base.Strings;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.SessionFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Keeps track of the simulation time.
 *
 * Allows some main loop to advance the time by one day.
 */
public class SimulationClock implements EntitySystem {
    private final LocalDateTime simulationStartDate;
    private Duration simulationTime = Duration.ZERO;

    @WithFactory
    public SimulationClock(@ByName("pdyn2.simulation.start") String simulationStartDate) {
        this.simulationStartDate = Strings.isNullOrEmpty(simulationStartDate)
                ? LocalDateTime.now()
                : LocalDate.parse(simulationStartDate).atStartOfDay();
    }

    public void advanceOneDay() {
        simulationTime = simulationTime.plusDays(1);
    }

    public LocalDate getCurrentDate() {
        return simulationStartDate.plus(simulationTime).toLocalDate();
    }

    public int getDaysPassed() {
        return (int) simulationTime.toDaysPart();
    }

    @Override
    public void execute(SessionFactory session) {
        advanceOneDay();
    }
}
