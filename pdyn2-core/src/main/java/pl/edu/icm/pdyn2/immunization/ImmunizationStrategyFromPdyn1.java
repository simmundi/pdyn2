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

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.io.IOException;

public class ImmunizationStrategyFromPdyn1 implements ImmunizationStrategy {
    private final ImmunizationFromCsvProvider immunizationFromCsvProvider;

    @WithFactory
    public ImmunizationStrategyFromPdyn1(ImmunizationFromCsvProvider immunizationFromCsvProvider) {
        this.immunizationFromCsvProvider = immunizationFromCsvProvider;
        try {
            immunizationFromCsvProvider.load();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public float getImmunizationCoefficient(Immunization immunization,
                                            ImmunizationStage immunizationStage,
                                            Load load,
                                            int day) {
        float coefficient = 0;
        if (immunization == null) return coefficient;

        for (ImmunizationEvent event : immunization.getEvents()) {
            var immunizationLoad = event.getLoad();
            var days = day - event.getDay();
            coefficient = Float.max(coefficient, (float) (immunizationFromCsvProvider.getCrossImmunity(immunizationLoad, load, immunizationStage) *
                    immunizationFromCsvProvider.getSFunction(immunizationLoad, immunizationStage, days)));
        }
        return coefficient;
    }
}
