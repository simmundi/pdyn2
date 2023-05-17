/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.immunization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmunizationStrategyFromPdyn1RewrittenTest {

    final BasicConfig basicConfig = new BasicConfig();
    @Mock
    Immunization immunization1;
    @Mock
    Immunization immunization2;
    ImmunizationEvent immunizationEvent1 = new ImmunizationEvent();
    ImmunizationEvent immunizationEvent2 = new ImmunizationEvent();

    @BeforeEach
    void setup() {
        immunizationEvent1.setLoad(basicConfig.ALPHA);
        immunizationEvent1.setDay(1);
        immunizationEvent2.setLoad(basicConfig.BA45);
        immunizationEvent2.setDay(59);
        when(immunization1.getEvents()).thenReturn(List.of(immunizationEvent1, immunizationEvent2));
        when(immunization2.getEvents()).thenReturn(List.of(immunizationEvent1));
    }

    @Test
    void getImmunizationCoefficient() {
        //given
        var immunizationStrategy = new ImmunizationStrategyFromPdyn1Rewritten(basicConfig.loads);
        //execute
        var coef1 = immunizationStrategy.getImmunizationCoefficient(immunization1, ImmunizationStage.LATENT, basicConfig.OMICRON, 73);
        var coef2 = immunizationStrategy.getImmunizationCoefficient(immunization2, ImmunizationStage.LATENT, basicConfig.OMICRON, 73);
        //assert
        assertThat(coef1).isEqualTo(0.9f);
        assertThat(coef2).isEqualTo(0.76f);
    }
}