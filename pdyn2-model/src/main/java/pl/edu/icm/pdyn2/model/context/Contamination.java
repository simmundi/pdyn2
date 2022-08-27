package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.NotMapped;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresSetup;

import static pl.edu.icm.pdyn2.model.context.Integerizer.toFloat;
import static pl.edu.icm.pdyn2.model.context.Integerizer.toInt;

@WithMapper
public final class Contamination implements RequiresSetup {
    private Load load;
    private int level;
    @NotMapped private int originalLevel;

    public Load getLoad() {
        return load;
    }

    public float getLevel() {
        return toFloat(level);
    }

    float changeLevel(float integerizedDelta) {
        return toFloat(changeIntegerizedLevel(toInt(integerizedDelta)));
    }

    @Override
    public void setup() {
        this.originalLevel = level;
    }

    @Override
    public String toString() {
        return "Contamination{" +
                "load=" + load +
                ", level=" + toFloat(level) +
                '}';
    }

    void setLevel(float level) {
        this.level = toInt(level);
    }

    void setLoad(Load load) {
        this.load = load;
    }

    int changeIntegerizedLevel(int integerizedDelta) {
        this.level = Math.max(0, this.level + integerizedDelta);
        return level;
    }

    int getTotalIntegerizedLevelChange() { return this.level - this.originalLevel; }

}
