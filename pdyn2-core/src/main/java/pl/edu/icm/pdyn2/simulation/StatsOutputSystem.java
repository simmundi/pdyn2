/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.simulation;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.pdyn2.transmission.TransmissionConfig;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.SessionFactory;

public class StatsOutputSystem implements EntitySystem {
    private final StatsService statsService;
    private final SimulationTimer simulationTimer;
    private final TransmissionConfig transmissionConfig;

    @WithFactory
    public StatsOutputSystem(StatsService statsService, SimulationTimer simulationTimer, TransmissionConfig transmissionConfig) {
        this.statsService = statsService;
        this.simulationTimer = simulationTimer;
        this.transmissionConfig = transmissionConfig;
    }

    @Override
    public void execute(SessionFactory sessionFactory) {
        System.out.println(simulationTimer.getCurrentDate() + " (day " + simulationTimer.getDaysPassed() + ")");
        System.out.println("Weights: " + transmissionConfig.toString());
        statsService.debugOutputStats(System.out);
        statsService.writeDayToStatsOutputFile();
        statsService.resetStats();
    }
}
