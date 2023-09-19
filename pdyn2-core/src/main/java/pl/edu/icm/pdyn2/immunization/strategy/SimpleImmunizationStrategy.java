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

package pl.edu.icm.pdyn2.immunization.strategy;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

public class SimpleImmunizationStrategy implements ImmunizationStrategy {
    private final float simpleImmunizationStrategyValue;

    @WithFactory
    public SimpleImmunizationStrategy(@ByName(value = "pdyn2.infectivity.immunization.constant.value", fallbackValue = "1") float simpleImmunizationStrategyValue) {
        this.simpleImmunizationStrategyValue = simpleImmunizationStrategyValue;
    }

    @Override
    public float getImmunizationCoefficient(Immunization immunization, ImmunizationStage immunizationStage, Load load, int day) {
        return simpleImmunizationStrategyValue;
    }
}
