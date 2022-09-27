package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
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

    public void setForContextType(ContextInfectivityClass contextType, float probability) {
        switch (contextType) {
            case HOUSEHOLD:
                setHouseholdInfluence(probability);
                break;
            case WORKPLACE:
                setWorkplaceInfluence(probability);
                break;
            case KINDERGARTEN:
                setKindergartenInfluence(probability);
                break;
            case SCHOOL:
                setSchoolInfluence(probability);
                break;
            case UNIVERSITY:
                setUniversityInfluence(probability);
                break;
            case BIG_UNIVERSITY:
                setBigUniversityInfluence(probability);
                break;
            case STREET:
                setStreetInfluence(probability);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + contextType);
        }
    }
}
