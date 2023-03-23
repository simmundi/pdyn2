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

import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClasses;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImmunizationSource {
    private float workplaceInfluence;
    private float kindergartenInfluence;
    private float schoolInfluence;
    private float universityInfluence;
    private float streetInfluence;
    private float bigUniversityInfluence;
    private float householdInfluence;


    public ImmunizationSource() {
    }

    public float getHouseholdInfluence() {
        return householdInfluence;
    }

    public void setHouseholdInfluence(float householdInfluence) {
        this.householdInfluence = householdInfluence;
    }

    public float getWorkplaceInfluence() {
        return workplaceInfluence;
    }

    public void setWorkplaceInfluence(float workplaceInfluence) {
        this.workplaceInfluence = workplaceInfluence;
    }

    public float getKindergartenInfluence() {
        return kindergartenInfluence;
    }

    public void setKindergartenInfluence(float kindergartenInfluence) {
        this.kindergartenInfluence = kindergartenInfluence;
    }

    public float getSchoolInfluence() {
        return schoolInfluence;
    }

    public void setSchoolInfluence(float schoolInfluence) {
        this.schoolInfluence = schoolInfluence;
    }

    public float getUniversityInfluence() {
        return universityInfluence;
    }

    public void setUniversityInfluence(float universityInfluence) {
        this.universityInfluence = universityInfluence;
    }

    public float getBigUniversityInfluence() {
        return bigUniversityInfluence;
    }

    public void setBigUniversityInfluence(float bigUniversityInfluence) {
        this.bigUniversityInfluence = bigUniversityInfluence;
    }

    public float getStreetInfluence() {
        return streetInfluence;
    }

    public void setStreetInfluence(float streetInfluence) {
        this.streetInfluence = streetInfluence;
    }

    public void setForContextType(ContextInfectivityClasses contextInfectivityClasses, ContextInfectivityClass contextType, float probability) {
        int contextTypeIndex = contextType.ordinal();
        if (contextTypeIndex == contextInfectivityClasses.HOUSEHOLD.ordinal()) {
            setHouseholdInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.WORKPLACE.ordinal()) {
            setWorkplaceInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.KINDERGARTEN.ordinal()) {
            setKindergartenInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.STREET.ordinal()) {
            setStreetInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.SCHOOL.ordinal()) {
            setSchoolInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.UNIVERSITY.ordinal()) {
            setUniversityInfluence(probability);
        } else if (contextTypeIndex == contextInfectivityClasses.BIG_UNIVERSITY.ordinal()) {
            setBigUniversityInfluence(probability);
        } else {
                throw new IllegalStateException("Unexpected value: " + contextType);
        }
    }
}
