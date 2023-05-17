/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.progression;

import net.snowyhollows.bento.config.WorkDir;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Covid19StageTransitionsProviderTest {
    BasicConfig basicConfig = new BasicConfig();

    @Spy
    AgeRanges ageRanges = basicConfig.ageRanges;
    @Spy
    Stages stages = basicConfig.stages;

    @Mock
    WorkDir workDir;

    @InjectMocks
    Covid19StageTransitionsProvider reader;

    @Test
    @DisplayName("Should read 40 configuration points from file")
    void readFromFile() throws IOException {
        // given
        when(workDir.openForReading(any())).thenReturn(
                Covid19StageTransitionsProviderTest.class.getResourceAsStream("/stanCzasTest.txt"));
        var configFormatted = new Properties();
        configFormatted.load(Covid19StageTransitionsProviderTest.class.getResourceAsStream("/stanCzasTest.properties"));

        // execute
        Properties result = new Properties();
        reader.readFromFile("stan_czas.txt", basicConfig.WILD, q -> {
            String v = String.format("pdyn2.progression.transition.%s.%s.%s.", q.first, q.second, q.third);
            result.put(v + "duration", Integer.toString(q.fourth.getDuration()));

            if (!q.fourth.getOutcomes().isEmpty()) {
                assertThat(q.fourth.getOutcomes().isNormalized()).isTrue();
            } else {
                assertThat(q.fourth.getDuration()).isEqualTo(999999);
            }


            for (Stage target : stages.values()) {
                if (q.fourth.getOutcomes().get(target) != 0) {
                    result.put(v + target, Float.toString(q.fourth.getOutcomes().get(target)));
                }
            }
        });

        // assert
        assertThat(result).isEqualTo(configFormatted);
    }
}