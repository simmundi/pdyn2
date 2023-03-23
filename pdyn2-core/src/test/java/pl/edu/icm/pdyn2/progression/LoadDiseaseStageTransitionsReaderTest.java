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

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.progression.Stages;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadDiseaseStageTransitionsReaderTest {
    BasicConfig basicConfig = new BasicConfig();

    @Spy
    AgeRanges ageRanges = basicConfig.ageRanges;
    @Spy
    Stages stages = basicConfig.stages;

    @Mock
    WorkDir workDir;

    @InjectMocks
    LoadDiseaseStageTransitionsReader reader;

    @Test
    @DisplayName("Should create a correct LoadDiseaseStageTransitions instance")
    void readFromFile() {
        // given
        when(workDir.openForReading(any())).thenReturn(
                LoadDiseaseStageTransitionsTest.class.getResourceAsStream("/stanCzasTest.txt"));

        // execute
        LoadDiseaseStageTransitions transition = reader.readFromFile("stan_czas.txt", basicConfig.loads.WILD);

        // assert
        assertThat(transition.durationOf(basicConfig.LATENT, 7)).isPositive();
    }
}
