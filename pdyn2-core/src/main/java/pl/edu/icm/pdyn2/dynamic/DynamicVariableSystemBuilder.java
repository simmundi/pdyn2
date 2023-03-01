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

package pl.edu.icm.pdyn2.dynamic;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.simulation.EpidemicConfig;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.time.LocalDate;

import static pl.edu.icm.pdyn2.dynamic.DynamicVariablesService.DynamicVariable.*;

public class DynamicVariableSystemBuilder {

    private final DynamicVariablesService service;
    private final SimulationTimer timer;
    private final EpidemicConfig config;

    @WithFactory
    public DynamicVariableSystemBuilder(DynamicVariablesService dynamicVariablesService,
                                        SimulationTimer timer,
                                        EpidemicConfig config) {
        this.service = dynamicVariablesService;
        this.timer = timer;
        this.config = config;
    }

    public EntitySystem buildDynamicVariableSystem() {
        return (session) -> {
            LocalDate day = timer.getCurrentDate();
            float household = service.getFloatForDate(HOUSEHOLD_W, day);
            float workplace = service.getFloatForDate(WORKPLACE_W, day);
            float kindergarten = service.getFloatForDate(KINDERGARTEN_W, day);
            float school = service.getFloatForDate(SCHOOL_W, day);
            float university = service.getFloatForDate(UNIVERSITY_W, day);
            float bigUniversity = service.getFloatForDate(BIG_UNIVERSITY_W, day);
            float street = service.getFloatForDate(STREET_W, day);
            float isolation = service.getFloatForDate(ISOLATION_P, day);

            config.household(household)
                    .workplace(workplace)
                    .kindergarten(kindergarten)
                    .school(school)
                    .university(university)
                    .bigUniversity(bigUniversity)
                    .street(street)
                    .isolation(isolation);
        };
    }
}
