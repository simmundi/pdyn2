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
import net.snowyhollows.bento.soft.SoftEnumMap;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.io.File;

/**
 * A stateless service that provides a distribution of outcomes and duration of any given stage of disease,
 * taking into account age and sex of a person.
 *
 */
public class SimpleDiseaseStageTransitionsServiceImpl implements DiseaseStageTransitionsService {
    private final SoftEnumMap<Load, LoadDiseaseStageTransitions> loadSpecificTransitions;
    private final WorkDir workDir;

    @WithFactory
    public SimpleDiseaseStageTransitionsServiceImpl(String baseTransitionsFilename,
                                                    Loads loads,
                                                    LoadDiseaseStageTransitionsReader reader, WorkDir workDir) {
        loadSpecificTransitions = new SoftEnumMap<>(loads);
        this.workDir = workDir;
        for (Load load : loads.values()) {
            String fileName = baseTransitionsFilename + load.name();
            if (workDir.exists(new File(fileName))) {
                loadSpecificTransitions.put(load, reader.readFromFile(fileName, load));
            }
        }
    }

    @Override
    public int durationOf(Load load, Stage stage, int age) {
        return loadSpecificTransitions.get(load).durationOf(stage, age);
    }

    @Override
    public Stage outcomeOf(Stage stage,
                           Entity person,
                           Load load,
                           double random) {
        return loadSpecificTransitions.get(load).outcomeOf(stage, person, random);
    }

}
