package pl.edu.icm.pdyn2.model.behaviour;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Behaviour {
    private int dayOfLastChange;
    private BehaviourType type = BehaviourType.DORMANT;

    public void transitionTo(BehaviourType type, int day) {
        this.type = type;
        this.dayOfLastChange = day;
    }

    public int getDayOfLastChange() {
        return dayOfLastChange;
    }

    void setDayOfLastChange(int dayOfLastChange) {
        this.dayOfLastChange = dayOfLastChange;
    }

    public BehaviourType getType() {
        return type;
    }

    void setType(BehaviourType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Behaviour{" +
                "dayOfLastChange=" + dayOfLastChange +
                ", type=" + type +
                '}';
    }
}
