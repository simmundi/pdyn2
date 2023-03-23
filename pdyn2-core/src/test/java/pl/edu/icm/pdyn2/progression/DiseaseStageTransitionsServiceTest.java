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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DiseaseStageTransitionsServiceTest {
    BasicConfig basicConfig = new BasicConfig();
    @Mock
    LoadDiseaseStageTransitionsReader reader;
    @Mock
    LoadDiseaseStageTransitions transitions;

    @Test
    @DisplayName("Should parse file names and load tables for correct loads")
    void construct() {
        // given
        when(reader.readFromFile(anyString(), any())).thenReturn(Mockito.mock(LoadDiseaseStageTransitions.class));

        // execute
        new DiseaseStageTransitionsServiceImpl("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                basicConfig.loads,
                reader);

        // assert
        verify(reader).readFromFile(endsWith("wild"), eq(basicConfig.loads.WILD));
        verify(reader).readFromFile(endsWith("alpha"), eq(basicConfig.ALPHA));
        verify(reader).readFromFile(endsWith("delta"), eq(basicConfig.DELTA));
        verify(reader).readFromFile(endsWith("omicron"), eq(basicConfig.OMICRON));
        verify(reader).readFromFile(endsWith("ba2"), eq(basicConfig.BA2));
    }


    @Test
    @DisplayName("Should delegate durationOf to correct LoadDiseaseStageTransition instance")
    void durationOf() {
        // given
        when(reader.readFromFile(any(), any())).thenReturn(mock(LoadDiseaseStageTransitions.class));
        when(reader.readFromFile(any(), eq(basicConfig.OMICRON))).thenReturn(transitions);
        DiseaseStageTransitionsService service =  new DiseaseStageTransitionsServiceImpl("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                basicConfig.loads,
                reader);

        // execute
        service.durationOf(basicConfig.OMICRON, basicConfig.stages.HEALTHY, 23);

        // assert
        verify(transitions).durationOf(basicConfig.stages.HEALTHY, 23);
    }

    @Test
    @DisplayName("Should delegate outcomeOf to correct LoadDiseaseStageTransition instance")
    void outcomeOf() {
        // given
        when(reader.readFromFile(any(), any())).thenReturn(mock(LoadDiseaseStageTransitions.class));
        when(reader.readFromFile(any(), eq(basicConfig.loads.WILD))).thenReturn(transitions);
        DiseaseStageTransitionsService service =         new DiseaseStageTransitionsServiceImpl("wild",
                "alpha",
                "delta",
                "omicron",
                "ba2",
                basicConfig.loads,
                reader);

        // execute
        service.durationOf(basicConfig.loads.WILD, basicConfig.INFECTIOUS_ASYMPTOMATIC, 11);

        // assert
        verify(transitions).durationOf(basicConfig.INFECTIOUS_ASYMPTOMATIC, 11);
    }
}
