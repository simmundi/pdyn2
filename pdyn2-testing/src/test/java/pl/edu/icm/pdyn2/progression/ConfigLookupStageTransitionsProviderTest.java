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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.Configurer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.lookup.ConfigLookupTransitionProvider;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfigLookupStageTransitionsProviderTest {
    BasicConfig basicConfig = new BasicConfig();

    @Test
    @DisplayName("Should read 40 configuration points from configuration")
    void readFromFile() throws IOException {
        // given
        var configFormatted = new Properties();
        configFormatted.load(ConfigLookupStageTransitionsProviderTest.class.getResourceAsStream("/stanCzasTest.properties"));

        var bento = new Configurer().loadConfigResource("/stanCzasTest.properties").getConfig();

        ConfigLookupTransitionProvider provider = new ConfigLookupTransitionProvider(basicConfig.stages, basicConfig.ageRanges, basicConfig.loads, bento);

        // execute
        Properties result = new Properties();
        provider.readTransitions(q -> {
            String v = String.format("pdyn2.progression.transition.%s.%s.%s.", q.first, q.second, q.third);
            result.put(v + "duration", Integer.toString(q.fourth.getDuration()));

            if (!q.fourth.getOutcomes().isEmpty()) {
                assertThat(q.fourth.getOutcomes().isNormalized()).isTrue();
            } else {
                assertThat(q.fourth.getDuration()).isEqualTo(999999);
            }


            for (Stage target : basicConfig.stages.values()) {
                if (q.fourth.getOutcomes().get(target) != 0) {
                    result.put(v + target, Float.toString(q.fourth.getOutcomes().get(target)));
                }
            }
        });

        // assert
        assertThat(result).isEqualTo(configFormatted);
    }
}