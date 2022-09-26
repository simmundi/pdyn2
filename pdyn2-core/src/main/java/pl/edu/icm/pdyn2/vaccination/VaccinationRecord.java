package pl.edu.icm.pdyn2.vaccination;

import pl.edu.icm.pdyn2.model.immunization.Load;

import java.util.List;

public class VaccinationRecord {
    private List<String> teryts;
    private int minAge;
    private int maxAge;
    private Load load;
    private int vaccineCount;

    public VaccinationRecord() {
    }

    public List<String> getTeryts() {
        return teryts;
    }

    public void setTeryts(List<String> teryts) {
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
