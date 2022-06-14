package pl.edu.icm.pdyn2.model.progression;

public enum Stage {
    LATENT,
    INFECTIOUS_ASYMPTOMATIC,
    INFECTIOUS_SYMPTOMATIC,
    HOSPITALIZED_NO_ICU,
    HOSPITALIZED_PRE_ICU,
    HOSPITALIZED_ICU,
    DECEASED,
    HEALTHY;

    public boolean isInfectious() {
        return this == INFECTIOUS_ASYMPTOMATIC || this == INFECTIOUS_SYMPTOMATIC;
    }

    public boolean isSick() {
        return this != DECEASED && this != HEALTHY;
    }

    public boolean hasOutcomes() {
        return this != DECEASED && this != HEALTHY;
    }

    public short getEncoding() {
        switch (this) {
            case LATENT:
                return 2;
            case INFECTIOUS_ASYMPTOMATIC:
                return 4;
            case INFECTIOUS_SYMPTOMATIC:
                return 8;
            case HOSPITALIZED_NO_ICU:
                return 32;
            case HOSPITALIZED_PRE_ICU:
                return 64;
            case HOSPITALIZED_ICU:
                return 128;
            case DECEASED:
                return 256;
            case HEALTHY:
                return 16;
        }
        throw new IllegalStateException("No encoding for " + this);
    }
}
