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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DiseaseStageTransitionsServiceTest {
    @Mock
    LoadDiseaseStageTransitionsReader reader;
    @Mock
    LoadDiseaseStageTransitions transitions;

    @Test
    @DisplayName("Should parse file names and load tables for correct loads")
    void construct() {
        // execute
        new DiseaseStageTransitionsService("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                reader);

        // assert
        verify(reader).readFromFile(endsWith("wild"), eq(Load.WILD));
        verify(reader).readFromFile(endsWith("alpha"), eq(Load.ALPHA));
        verify(reader).readFromFile(endsWith("delta"), eq(Load.DELTA));
        verify(reader).readFromFile(endsWith("omicron"), eq(Load.OMICRON));
        verify(reader).readFromFile(endsWith("ba2"), eq(Load.BA2));
    }


    @Test
    @DisplayName("Should delegate durationOf to correct LoadDiseaseStageTransition instance")
    void durationOf() {
        // given
        when(reader.readFromFile(any(), any())).thenReturn(null);
        when(reader.readFromFile(any(), eq(Load.OMICRON))).thenReturn(transitions);
        DiseaseStageTransitionsService service =  new DiseaseStageTransitionsService("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                reader);

        // execute
        service.durationOf(Load.OMICRON, Stage.HEALTHY, 23);

        // assert
        verify(transitions).durationOf(Stage.HEALTHY, 23);
    }

    @Test
    @DisplayName("Should delegate outcomeOf to correct LoadDiseaseStageTransition instance")
    void outcomeOf() {
        // given
        when(reader.readFromFile(any(), any())).thenReturn(null);
        when(reader.readFromFile(any(), eq(Load.WILD))).thenReturn(transitions);
        DiseaseStageTransitionsService service =         new DiseaseStageTransitionsService("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                reader);

        // execute
        service.durationOf(Load.WILD, Stage.INFECTIOUS_ASYMPTOMATIC, 11);

        // assert
        verify(transitions).durationOf(Stage.INFECTIOUS_ASYMPTOMATIC, 11);
    }
}
