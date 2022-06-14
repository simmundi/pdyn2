package pl.edu.icm.pdyn2.progression;

import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

class TransitionOracle {
    private Stage stage;
    private final EnumSampleSpace<Stage> outcomes = new EnumSampleSpace<>(Stage.class);
    private int duration;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void addProbableOutcome(float probability, Stage outcome) {
        outcomes.changeOutcome(outcome, probability);
    }

    public EnumSampleSpace<Stage> getOutcomes() {
        return outcomes;
    }
}
