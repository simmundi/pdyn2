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

package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.EnumManagedBy;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImmunizationEvent {
    private int day;
    @EnumManagedBy(Loads.class)
    private Load load;
    private int diseaseHistory;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load type) {
        this.load = type;
    }

    public int getDiseaseHistory() {
        return diseaseHistory;
    }

    public void setDiseaseHistory(int diseaseHistory) {
        this.diseaseHistory = diseaseHistory;
    }

}
