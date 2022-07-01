package pl.edu.icm.pdyn2.model.progression;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "health_status")
public class HealthStatus {
    private Stage stage = Stage.HEALTHY;
    private Load diseaseLoad;
    private int dayOfLastChange;
    private int diseaseHistory = 0;

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

    @Override
    public String toString() {
        return "HealthStatus{" +
                "stage=" + stage +
                ", dayOfLastChange=" + dayOfLastChange +
                ", diseaseHistory=" + diseaseHistory +
                '}';
    }
}
