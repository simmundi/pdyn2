package pl.edu.icm.pdyn2.model.progression;

import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "health_status")
public class HealthStatus {
    private Stage stage = Stage.HEALTHY;
    private Load diseaseLoad;
    private int dayOfLastChange;
    private int diseaseHistory = 0;
    private float householdSource;
    private float workplaceSource;
    private float kindergartenSource;
    private float schoolSource;
    private float universitySource;
    private float bigUniversitySource;
    private float streetSource;
    private float sowingSource;

    public HealthStatus(Load diseaseLoad) {
        this.diseaseLoad = diseaseLoad;
    }

    public HealthStatus() {
        this.diseaseLoad = Load.WILD;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        addStageToHistory(stage);
    }

    public int getElapsedDays(int currentDay) {
        return currentDay - dayOfLastChange;
    }

    void setDayOfLastChange(int dayOfLastChange) {
        this.dayOfLastChange = dayOfLastChange;
    }

    public int getDayOfLastChange() {
        return dayOfLastChange;
    }

    public void transitionTo(Stage stage, int currentDay) {
        this.stage = stage;
        this.dayOfLastChange = currentDay;
        addStageToHistory(stage);
    }

    public Load getDiseaseLoad() {
        return diseaseLoad;
    }

    public void setDiseaseLoad(Load diseaseLoad) {
        this.diseaseLoad = diseaseLoad;
    }

    public int getDiseaseHistory() {
        return diseaseHistory;
    }

    void setDiseaseHistory(int diseaseHistory) {
        this.diseaseHistory = diseaseHistory;
    }

    public void resetDiseaseHistory() {
        this.diseaseHistory = 0;
    }

    public static HealthStatus of(Load load) {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDiseaseLoad(load);
        return healthStatus;
    }

    private void addStageToHistory(Stage stage) {
        diseaseHistory += stage.getEncoding();
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

    @Override
    public String toString() {
        return "HealthStatus{" +
                "stage=" + stage +
                ", dayOfLastChange=" + dayOfLastChange +
                ", diseaseHistory=" + diseaseHistory +
                '}';
    }
}
