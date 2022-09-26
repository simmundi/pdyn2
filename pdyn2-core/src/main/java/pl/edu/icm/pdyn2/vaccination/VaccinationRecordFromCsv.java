package pl.edu.icm.pdyn2.vaccination;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class VaccinationRecordFromCsv {
    private int day;
    private String teryts;
    private int minAge;
    private int maxAge;
    private Load load;
    private int vaccineCount;

    public VaccinationRecordFromCsv() {
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getTeryts() {
        return teryts;
    }

    public void setTeryts(String teryts) {
        this.teryts = teryts;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public int getVaccineCount() {
        return vaccineCount;
    }

    public void setVaccineCount(int vaccineCount) {
        this.vaccineCount = vaccineCount;
    }
}
