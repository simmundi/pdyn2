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
    private byte workplaceInfluence;
    private byte kindergartenInfluence;
    private byte schoolInfluence;
    private byte universityInfluence;
    private byte streetInfluence;
    private byte bigUniversityInfluence;
    private byte householdInfluence;


    public ImmunizationSource() {
    }

    public byte getHouseholdInfluence() {
        return householdInfluence;
    }

    public void setHouseholdInfluence(byte householdInfluence) {
        this.householdInfluence = householdInfluence;
    }

    public byte getWorkplaceInfluence() {
        return workplaceInfluence;
    }

    public void setWorkplaceInfluence(byte workplaceInfluence) {
        this.workplaceInfluence = workplaceInfluence;
    }

    public byte getKindergartenInfluence() {
        return kindergartenInfluence;
    }

    public void setKindergartenInfluence(byte kindergartenInfluence) {
        this.kindergartenInfluence = kindergartenInfluence;
    }

    public byte getSchoolInfluence() {
        return schoolInfluence;
    }

    public void setSchoolInfluence(byte schoolInfluence) {
        this.schoolInfluence = schoolInfluence;
    }

    public byte getUniversityInfluence() {
        return universityInfluence;
    }

    public void setUniversityInfluence(byte universityInfluence) {
        this.universityInfluence = universityInfluence;
    }

    public byte getBigUniversityInfluence() {
        return bigUniversityInfluence;
    }

    public void setBigUniversityInfluence(byte bigUniversityInfluence) {
        this.bigUniversityInfluence = bigUniversityInfluence;
    }

    public byte getStreetInfluence() {
        return streetInfluence;
    }

    public void setStreetInfluence(byte streetInfluence) {
        this.streetInfluence = streetInfluence;
    }

    public void setForContextType(ContextInfectivityClasses contextInfectivityClasses, ContextInfectivityClass contextType, float probability) {
        byte byteProbability = (byte) (probability * Byte.MAX_VALUE);
        if (probability < -1 || probability > 1) {
            throw new IllegalArgumentException("Probability should be in range <-1,1>");
        }

        int contextTypeIndex = contextType.ordinal();
        if (contextTypeIndex == contextInfectivityClasses.HOUSEHOLD.ordinal()) {
            setHouseholdInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.WORKPLACE.ordinal()) {
            setWorkplaceInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.KINDERGARTEN.ordinal()) {
            setKindergartenInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.STREET.ordinal()) {
            setStreetInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.SCHOOL.ordinal()) {
            setSchoolInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.UNIVERSITY.ordinal()) {
            setUniversityInfluence(byteProbability);
        } else if (contextTypeIndex == contextInfectivityClasses.BIG_UNIVERSITY.ordinal()) {
            setBigUniversityInfluence(byteProbability);
        } else {
                throw new IllegalStateException("Unexpected value: " + contextType);
        }
    }
}
