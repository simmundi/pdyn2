package pl.edu.icm.pdyn2.model.impact;

import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper(namespace = "impact")
public class Impact {
    private BehaviourType type;
    private Stage stage;
    private Load load;

    public void affect(Behaviour behaviour, HealthStatus healthStatus) {
        Stage stage = healthStatus == null ? null : healthStatus.getStage();
        Load load = healthStatus == null ? null : healthStatus.getDiseaseLoad();
        BehaviourType type = behaviour == null ? null : behaviour.getType();

        setLoad(load);
        setStage(stage);
        setType(type);
    }

    void setType(BehaviourType type) {
        this.type = type;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setLoad(Load load) {
        this.load = load;
    }

    public BehaviourType getType() {
        return type;
    }

    public Stage getStage() {
        return stage;
    }

    public Load getLoad() {
        return load;
    }

    public boolean isDifferentFrom(Behaviour behaviour, HealthStatus healthStatus) {
        Stage stage = healthStatus == null ? null : healthStatus.getStage();
        Load load = healthStatus == null ? null : healthStatus.getDiseaseLoad();
        BehaviourType type = behaviour == null ? null : behaviour.getType();

        return this.type != type || this.stage != stage || this.load != load;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Impact impact = (Impact) o;
        return type == impact.type && stage == impact.stage && load == impact.load;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, stage, load);
    }
}
