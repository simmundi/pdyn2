package pl.edu.icm.pdyn2.model.immunization;

import static pl.edu.icm.pdyn2.model.immunization.LoadClassification.VACCINE;
import static pl.edu.icm.pdyn2.model.immunization.LoadClassification.VIRUS;

public enum Load {
    WILD(VIRUS),
    ALPHA(VIRUS),
    DELTA(VIRUS),
    OMICRON(VIRUS),
    BA2(VIRUS),
    BA45(VIRUS),

    PFIZER(VACCINE),
    ASTRA(VACCINE),
    MODERNA(VACCINE),
    BOOSTER(VACCINE);

    public final LoadClassification classification;

    Load(LoadClassification classification) {
        this.classification = classification;
    }

    public static Load[] viruses() {
        return new Load[]{
                WILD, ALPHA, DELTA, OMICRON, BA2
        };
    }
}
