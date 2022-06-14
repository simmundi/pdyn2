package pl.edu.icm.pdyn2.model.travel;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Travel {
    private short dayOfTravel;
    private Entity stayingAt;

    public short getDayOfTravel() {
        return dayOfTravel;
    }

    public void setDayOfTravel(short dayOfTravel) {
        this.dayOfTravel = dayOfTravel;
    }

    public Entity getStayingAt() {
        return stayingAt;
    }

    public void setStayingAt(Entity stayingAt) {
        this.stayingAt = stayingAt;
    }

}
