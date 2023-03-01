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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.time.SimulationTimer;

/**
 * Service for reading LoadDiseaseStageTransitions for the given load.
 */
class LoadDiseaseStageTransitionsReader {
    private final ImmunizationService immunizationService;
    private final SimulationTimer simulationTimer;
    private final WorkDir workDir;
    private final Stages stages;
    private final AgeRanges ageRanges;


    @WithFactory
    public LoadDiseaseStageTransitionsReader(ImmunizationService immunizationService, SimulationTimer simulationTimer, WorkDir workDir, Stages stages, AgeRanges ageRanges) {
        this.immunizationService = immunizationService;
        this.simulationTimer = simulationTimer;
        this.workDir = workDir;
        this.stages = stages;
        this.ageRanges = ageRanges;
    }

    public LoadDiseaseStageTransitions readFromFile(String absolutePath, Load load) {
        return new LoadDiseaseStageTransitions(
                    absolutePath,
                    immunizationService,
                    simulationTimer,
                    workDir,
                    stages,
                    ageRanges,
                    load);
    }
}
