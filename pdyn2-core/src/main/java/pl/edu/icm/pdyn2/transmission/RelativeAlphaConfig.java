package pl.edu.icm.pdyn2.transmission;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;

public class RelativeAlphaConfig {
    float alphaAlpha;
    float alphaDelta;
    float alphaOmicron;
    float alphaBA2;

    @WithFactory
    public RelativeAlphaConfig(float alphaAlpha,
                               float alphaDelta,
                               float alphaOmicron,
                               float alphaBA2) {
        this.alphaAlpha = alphaAlpha;
        this.alphaDelta = alphaDelta;
        this.alphaOmicron = alphaOmicron;
        this.alphaBA2 = alphaBA2;
    }

    public float getRelativeAlpha(Load load) {
        switch (load) {
            case ALPHA:
                return alphaAlpha;
            case DELTA:
                return alphaDelta;
            case OMICRON:
                return alphaOmicron;
            case BA2:
                return alphaBA2;
            case WILD:
                return 1.0f;
            default:
                throw new IllegalArgumentException("Invalid load: " + load +
                        " Relative alpha available for loads: ALPHA, DELTA, OMICRON, WILD");
        }
    }
}
