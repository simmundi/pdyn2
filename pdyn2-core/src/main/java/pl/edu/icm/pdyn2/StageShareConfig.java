package pl.edu.icm.pdyn2;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;

public class StageShareConfig {
    private final float[] influenceOfStage = new float[Stage.values().length];

    @WithFactory
    StageShareConfig(float asymptomaticInfluenceShare, float symptomaticInfluenceShare) {
        influenceOfStage[Stage.INFECTIOUS_ASYMPTOMATIC.ordinal()] = asymptomaticInfluenceShare;
        influenceOfStage[Stage.INFECTIOUS_SYMPTOMATIC.ordinal()] = symptomaticInfluenceShare;
    }

    public float getInfluenceOf(Stage stage) {
        return influenceOfStage[stage.ordinal()];
    }
}
