package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.NotMapped;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresSetup;

@WithMapper
public final class Contamination implements RequiresSetup {
    private final static float PRECISION = 1000f;
    private Load load;
    private float level;
    @NotMapped private float originalLevel;

    public Contamination() {
    }

    Contamination(Load load, float level) {
        this.load = load;
        this.level = level;
    }

    public Load getLoad() {
        return load;
    }

    void setLoad(Load load) {
        this.load = load;
    }

    public float getLevel() {
        return level;
    }

    void setLevel(float level) {
        this.level = level;
    }

    float changeLevel(float delta) {
        this.level += delta;
        return this.level = Math.round(level * PRECISION) / PRECISION;
    }

    float getTotalLevelChange() { return this.level - this.originalLevel; }

    @Override
    public void setup() {
        this.originalLevel = level;
    }

    @Override
    public String toString() {
        return "Contamination{" +
                "load=" + load +
                ", level=" + level +
                '}';
    }
}
