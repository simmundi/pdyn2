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

package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.administration.TestingConfig;
import pl.edu.icm.pdyn2.isolation.IsolationConfig;
import pl.edu.icm.pdyn2.transmission.TransmissionConfig;

public class EpidemicConfig {
    private final TransmissionConfig transmissionConfig;
    private final TestingConfig testingConfig;
    private final IsolationConfig isolationConfig;

    @WithFactory
    public EpidemicConfig(TransmissionConfig transmissionConfig,
                          TestingConfig testingConfig,
                          IsolationConfig isolationConfig) {
        this.transmissionConfig = transmissionConfig;
        this.testingConfig = testingConfig;
        this.isolationConfig = isolationConfig;
    }

    public EpidemicConfig workplace(float v) {
        transmissionConfig.setWorkplaceWeight(v);
        return this;
    }

    public EpidemicConfig household(float v) {
        transmissionConfig.setHouseholdWeight(v);
        return this;
    }

    public EpidemicConfig kindergarten(float v) {
        transmissionConfig.setKindergartenWeight(v);
        return this;
    }

    public EpidemicConfig school(float v) {
        transmissionConfig.setSchoolsWeight(v);
        return this;
    }

    public EpidemicConfig university(float v) {
        transmissionConfig.setUniversityWeight(v);
        return this;
    }

    public EpidemicConfig bigUniversity(float v) {
        transmissionConfig.setBigUniversityWeight(v);
        return this;
    }

    public EpidemicConfig street(float v) {
        transmissionConfig.setStreetWeight(v);
        return this;
    }

    public EpidemicConfig isolation(float v) {
        isolationConfig.setSelfIsolationWeight(v);
        return this;
    }

    @Override
    public String toString() {
        return String.format("Workplace: %.3f   Household: %.3f    Kindergarten: %.3f    School: %.3f    Uni: %.3f    BigUni: %.3f    Street: %.3f   Isolation: %.3f",
                transmissionConfig.getWorkplaceWeight(),
                transmissionConfig.getHouseholdWeight(),
                transmissionConfig.getKindergartenWeight(),
                transmissionConfig.getSchoolsWeight(),
                transmissionConfig.getUniversityWeight(),
                transmissionConfig.getBigUniversityWeight(),
                transmissionConfig.getStreetWeight(),
                isolationConfig.getSelfIsolationWeight());
    }
}
