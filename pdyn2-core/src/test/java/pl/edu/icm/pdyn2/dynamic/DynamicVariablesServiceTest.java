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

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DynamicVariablesServiceTest {
    @Mock
    WorkDir filesystem;

    @Test
    @DisplayName("Should parse file and load trees correct variables")
    void correctness() {
        when(filesystem.openForReading(any())).thenReturn(
                DynamicVariablesService.class.getResourceAsStream("/scenario.csv"));
        DynamicVariablesService service = new DynamicVariablesService(filesystem,
                                                    "/scenario.csv");

        assertThat(service.getValueForDate(DynamicVariablesService.DynamicVariable.HOUSEHOLD_W,
                LocalDate.parse("2020-03-06"))).isEqualTo("1");
        assertThat(service.getValueForDate(DynamicVariablesService.DynamicVariable.STREET_W,
                LocalDate.parse("2020-03-06"))).isEqualTo("1");
        assertThat(service.getValueForDate(DynamicVariablesService.DynamicVariable.HOUSEHOLD_W,
                LocalDate.parse("2020-04-15"))).isEqualTo("1.03");
        assertThat(service.getValueForDate(DynamicVariablesService.DynamicVariable.STREET_W,
                LocalDate.parse("2020-04-15"))).isEqualTo("0.15");
        assertThat(service.getValueForDate(DynamicVariablesService.DynamicVariable.TERYT,
                LocalDate.parse("2020-04-15"))).isEqualTo("22,08,14");
    }
}
