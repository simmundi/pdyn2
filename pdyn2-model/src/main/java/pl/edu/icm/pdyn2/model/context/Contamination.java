package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public final class Contamination {
    private Load load;
    private float level;

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public float getLevel() {
        return level;
    }

    public void setLevel(float level) {
        this.level = level;
    }

    public Contamination changeLevel(float delta) {
        this.level += delta;
        return this;
    }
}
