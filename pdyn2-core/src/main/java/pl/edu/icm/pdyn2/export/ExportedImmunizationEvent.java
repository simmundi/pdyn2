package pl.edu.icm.pdyn2.export;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ExportedImmunizationEvent {
    private int id;
    private int startDay;
    private int diseaseHistory;

    private float householdSource;
    private float workplaceSource;
    private float kindergartenSource;
    private float schoolSource;
    private float universitySource;
    private float bigUniversitySource;
    private float streetSource;
    private float sowingSource;

    private String diseaseLoad;
    private String vaccineLoad;
    private String testedValue;

    public ExportedImmunizationEvent() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
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

    public String getDiseaseLoad() {
        return diseaseLoad;
    }

    public void setDiseaseLoad(String diseaseLoad) {
        this.diseaseLoad = diseaseLoad;
    }

    public String getVaccineLoad() {
        return vaccineLoad;
    }

    public void setVaccineLoad(String vaccineLoad) {
        this.vaccineLoad = vaccineLoad;
    }

    public int getDiseaseHistory() {
        return diseaseHistory;
    }

    public void setDiseaseHistory(int diseaseHistory) {
        this.diseaseHistory = diseaseHistory;
    }

    public String getTestedValue() {
        return testedValue;
    }

    public void setTestedValue(String testedValue) {
        this.testedValue = testedValue;
    }
}
