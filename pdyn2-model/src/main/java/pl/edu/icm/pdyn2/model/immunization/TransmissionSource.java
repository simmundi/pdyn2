package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class TransmissionSource {
    public enum SOURCES {
        HOUSEHOLD,
        WORKPLACE,
        KINDERGARTEN,
        SCHOOL,
        UNIVERSITY,
        BIG_UNIVERSITY,
        STREET
    }
    private final float probability;
    private final SOURCES source;
    public TransmissionSource() {
        source = SOURCES.HOUSEHOLD;
        probability = 0.0f;
    }
    public TransmissionSource(SOURCES source, float probability) {
        this.source = source;
        this.probability = probability;
    }

    public float getProbability() {
        return probability;
    }

    public SOURCES getSource() {
        return source;
    }
}
