package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;

public class StageImpactConfig {
    private final float[] influenceOfStage = new float[Stage.values().length];

    @WithFactory
    public StageImpactConfig(float asymptomaticInfluenceShare, float symptomaticInfluenceShare) {
        influenceOfStage[Stage.INFECTIOUS_ASYMPTOMATIC.ordinal()] = asymptomaticInfluenceShare;
        influenceOfStage[Stage.INFECTIOUS_SYMPTOMATIC.ordinal()] = symptomaticInfluenceShare;
    }

    public float getInfluenceOf(Stage stage) {
        return influenceOfStage[stage.ordinal()];
    }
}
