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

package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.model.immunization.Immunization;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmunizationServiceTest {
    @Mock
    ImmunizationStrategyProvider mockprovider;
    @Mock
    ImmunizationStrategy immunizationStrategy;

    ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(false);

    @BeforeEach
    void before() {
        when(mockprovider.getImmunizationStrategy()).thenReturn(immunizationStrategy);
        when(immunizationStrategy.getImmunizationCoefficient(any(), any(), any(), anyInt())).thenReturn(0.5f);
    }

    @Test
    void getImmunizationCoefficient() {
        //given
        var immunizationService = new ImmunizationService(mockprovider);
        //execute
        var coef = immunizationService.getImmunizationCoefficient(new Immunization(),
                ImmunizationStage.LATENTNY,
                data.wild,
                0);
        //assert
        assertThat(coef).isEqualTo(0.5f);
    }
}