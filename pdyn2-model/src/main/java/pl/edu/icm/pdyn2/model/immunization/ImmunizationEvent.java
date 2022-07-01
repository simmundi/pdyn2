package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImmunizationEvent {
    private int day;
    private Load load;
    private int diseaseHistory;

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

}
