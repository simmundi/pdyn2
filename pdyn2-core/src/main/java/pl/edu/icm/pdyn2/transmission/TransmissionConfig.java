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

package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClasses;
import pl.edu.icm.pdyn2.model.context.ContextType;

public class TransmissionConfig {
    final float alpha;

    float householdWeight;
    float workplaceWeight;
    float schoolsWeight;
    float kindergartenWeight;
    float universityWeight;
    float bigUniversityWeight;
    float streetWeight;

    final float baseHouseholdWeight;
    final float baseWorkplaceWeight;
    final float baseSchoolsWeight;
    final float baseKindergartenWeight;
    final float baseUniversityWeight;
    final float baseBigUniversityWeight;
    final float baseStreetWeight;
    final ContextInfectivityClasses contextInfectivityClasses;

    @WithFactory
    public TransmissionConfig(float alpha,
                              float baseHouseholdWeight,
                              float baseWorkplaceWeight,
                              float baseSchoolsWeight,
                              float baseKindergartenWeight,
                              float baseUniversityWeight,
                              float baseBigUniversityWeight,
                              float baseStreetWeight, ContextInfectivityClasses contextInfectivityClasses) {
        this.alpha = alpha;
        this.baseHouseholdWeight = baseHouseholdWeight;
        this.baseWorkplaceWeight = baseWorkplaceWeight;
        this.baseSchoolsWeight = baseSchoolsWeight;
        this.baseKindergartenWeight = baseKindergartenWeight;
        this.baseUniversityWeight = baseUniversityWeight;
        this.baseBigUniversityWeight = baseBigUniversityWeight;
        this.baseStreetWeight = baseStreetWeight;
        this.contextInfectivityClasses = contextInfectivityClasses;
    }

    public float getTotalWeightForContextType(ContextType type) {
        ContextInfectivityClass infectivityClass = type.getInfectivityClass();
        if (infectivityClass == contextInfectivityClasses.HOUSEHOLD) {
            return getHouseholdWeight() * baseHouseholdWeight;
        } else if (infectivityClass == contextInfectivityClasses.WORKPLACE) {
            return getWorkplaceWeight() * baseWorkplaceWeight;
        } else if (infectivityClass == contextInfectivityClasses.SCHOOL) {
            return getSchoolsWeight() * baseSchoolsWeight;
        } else if (infectivityClass == contextInfectivityClasses.UNIVERSITY) {
            return getUniversityWeight() * baseUniversityWeight;
        } else if (infectivityClass == contextInfectivityClasses.BIG_UNIVERSITY) {
            return getBigUniversityWeight() * baseBigUniversityWeight;
        } else if (infectivityClass == contextInfectivityClasses.STREET) {
            return getStreetWeight() * baseStreetWeight;
        } else if (infectivityClass == contextInfectivityClasses.KINDERGARTEN) {
            return getKindergartenWeight() * baseKindergartenWeight;
        } else {
            throw new IllegalArgumentException("Unknown infectivity for context type: " + type);
        }
    }

    public float getAlpha() {
        return alpha;
    }

    public float getHouseholdWeight() {
        return householdWeight;
    }

    public float getWorkplaceWeight() {
        return workplaceWeight;
    }

    public float getKindergartenWeight() {
        return kindergartenWeight;
    }

    public float getSchoolsWeight() {
        return schoolsWeight;
    }

    public float getUniversityWeight() {
        return universityWeight;
    }

    public float getBigUniversityWeight() {
        return bigUniversityWeight;
    }

    public float getStreetWeight() {
        return streetWeight;
    }

    public void setHouseholdWeight(float householdWeight) {
        this.householdWeight = householdWeight;
    }

    public void setWorkplaceWeight(float workplaceWeight) {
        this.workplaceWeight = workplaceWeight;
    }

    public void setSchoolsWeight(float schoolsWeight) {
        this.schoolsWeight = schoolsWeight;
    }

    public void setKindergartenWeight(float kindergartenWeight) {
        this.kindergartenWeight = kindergartenWeight;
    }

    public void setUniversityWeight(float universityWeight) {
        this.universityWeight = universityWeight;
    }

    public void setBigUniversityWeight(float bigUniversityWeight) {
        this.bigUniversityWeight = bigUniversityWeight;
    }

    public void setStreetWeight(float streetWeight) {
        this.streetWeight = streetWeight;
    }
}
