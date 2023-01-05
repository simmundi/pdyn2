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
import pl.edu.icm.pdyn2.immunization.LoadService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DiseaseStageTransitionsService {
    private final Map<Load, LoadDiseaseStageTransitions> loadSpecificTransitions = new HashMap<>();

    @WithFactory
    public DiseaseStageTransitionsService(String stageTransitionsDirectory,
                                          LoadDiseaseStageTransitionsReader reader,
                                          LoadService loadService) {
        for (Load virusLoad:
             loadService.getViruses()) {
            loadSpecificTransitions.put(virusLoad,
                    reader.readFromFile(stageTransitionsDirectory + virusLoad.getStageTransitionsFilename(),
                            virusLoad));
        }
    }

    public int durationOf(Load load, Stage stage, int age) {
        return loadSpecificTransitions.get(load).durationOf(stage, age);
    }

    public Stage outcomeOf(Stage stage,
                           Entity person,
                           Load load,
                           double random) {
        return loadSpecificTransitions.get(load).outcomeOf(stage, person, random);
    }

}
