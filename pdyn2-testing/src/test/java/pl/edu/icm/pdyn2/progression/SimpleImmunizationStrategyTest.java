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

package pl.edu.icm.pdyn2.progression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.strategy.SimpleImmunizationStrategy;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class SimpleImmunizationStrategyTest {
    final BasicConfig basicConfig = new BasicConfig();
    @Mock
    Entity agent;

    @Test
    void getImmunizationCoefficient() {
        //given
        var simpleImmunizationStrategy = new SimpleImmunizationStrategy(0.3f);
        //execute
        var coef = simpleImmunizationStrategy.getImmunizationCoefficient(agent,
                ImmunizationStage.LATENT,
                basicConfig.WILD,
                0);
        //assert
        assertThat(coef).isEqualTo(0.3f);
    }
}