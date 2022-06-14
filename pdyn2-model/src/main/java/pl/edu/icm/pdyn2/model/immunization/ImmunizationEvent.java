package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImmunizationEvent {
    private int day;
    private Load load;
    private int diseaseHistory;
    private float householdSource;
    private float workplaceSource;
    private float kindergartenSource;
    private float schoolSource;
    private float universitySource;
    private float bigUniversitySource;
    private float streetSource;
    private float sowingSource;

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

    public float getHouseholdSource() {
        return householdSource;
    }

    public void setHouseholdSource(float householdSource) {
        this.householdSource = householdSource;
    }

    public float getWorkplaceSource() {
        return workplaceSource;
    }

    public void setWorkplaceSource(float workplaceSource) {
        this.workplaceSource = workplaceSource;
    }

    public float getKindergartenSource() {
        return kindergartenSource;
    }

    public void setKindergartenSource(float kindergartenSource) {
        this.kindergartenSource = kindergartenSource;
    }

    public float getSchoolSource() {
        return schoolSource;
    }

    public void setSchoolSource(float schoolSource) {
        this.schoolSource = schoolSource;
    }

    public float getUniversitySource() {
        return universitySource;
    }

    public void setUniversitySource(float universitySource) {
        this.universitySource = universitySource;
    }

    public float getBigUniversitySource() {
        return bigUniversitySource;
    }

    public void setBigUniversitySource(float bigUniversitySource) {
        this.bigUniversitySource = bigUniversitySource;
    }

    public float getStreetSource() {
        return streetSource;
    }

    public void setStreetSource(float streetSource) {
        this.streetSource = streetSource;
    }

    public float getSowingSource() {
        return sowingSource;
    }

    public void setSowingSource(float sowingSource) {
        this.sowingSource = sowingSource;
    }

    public float getSource(ContextInfectivityClass sourceClass) {
        switch (sourceClass) {
            case HOUSEHOLD:
                return householdSource;
            case WORKPLACE:
                return workplaceSource;
            case KINDERGARTEN:
                return kindergartenSource;
            case SCHOOL:
                return schoolSource;
            case UNIVERSITY:
                return universitySource;
            case BIG_UNIVERSITY:
                return bigUniversitySource;
            case STREET:
                return streetSource;
            case SOWING:
                return sowingSource;
            default:
                throw new IllegalArgumentException("This ContextInfectivityClass is not recognized: " + sourceClass);
        }
    }

    public void setSource(ContextInfectivityClass sourceClass, float sourceValue) {
        switch (sourceClass) {
            case HOUSEHOLD:
                setHouseholdSource(sourceValue);
                break;
            case WORKPLACE:
                setWorkplaceSource(sourceValue);
                break;
            case KINDERGARTEN:
                setKindergartenSource(sourceValue);
                break;
            case SCHOOL:
                setSchoolSource(sourceValue);
                break;
            case UNIVERSITY:
                setUniversitySource(sourceValue);
                break;
            case BIG_UNIVERSITY:
                setBigUniversitySource(sourceValue);
                break;
            case STREET:
                setStreetSource(sourceValue);
                break;
            case SOWING:
                setSowingSource(sourceValue);
                break;
            default:
                throw new IllegalArgumentException("This ContextInfectivityClass is not recognized: " + sourceClass);
        }
    }

}
