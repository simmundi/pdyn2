package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
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

    @WithFactory
    public TransmissionConfig(float alpha,
                              float baseHouseholdWeight,
                              float baseWorkplaceWeight,
                              float baseSchoolsWeight,
                              float baseKindergartenWeight,
                              float baseUniversityWeight,
                              float baseBigUniversityWeight,
                              float baseStreetWeight) {
        this.alpha = alpha;
        this.baseHouseholdWeight = baseHouseholdWeight;
        this.baseWorkplaceWeight = baseWorkplaceWeight;
        this.baseSchoolsWeight = baseSchoolsWeight;
        this.baseKindergartenWeight = baseKindergartenWeight;
        this.baseUniversityWeight = baseUniversityWeight;
        this.baseBigUniversityWeight = baseBigUniversityWeight;
        this.baseStreetWeight = baseStreetWeight;
    }

    public float getTotalWeightForContextType(ContextType type) {
        ContextInfectivityClass infectivityClass = type.getInfectivityClass();
        switch (infectivityClass) {
            case HOUSEHOLD:
                return getHouseholdWeight() * baseHouseholdWeight;
            case WORKPLACE:
                return getWorkplaceWeight() * baseWorkplaceWeight;
            case SCHOOL:
                return getSchoolsWeight() * baseSchoolsWeight;
            case UNIVERSITY:
                return getUniversityWeight() * baseUniversityWeight;
            case BIG_UNIVERSITY:
                return getBigUniversityWeight() * baseBigUniversityWeight;
            case STREET:
                return getStreetWeight() * baseStreetWeight;
            case KINDERGARTEN:
                return getKindergartenWeight() * baseKindergartenWeight;
            default:
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

    public float getSchoolsWeight() { return schoolsWeight; }

    public float getUniversityWeight() { return universityWeight; }

    public float getBigUniversityWeight() { return bigUniversityWeight; }

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
