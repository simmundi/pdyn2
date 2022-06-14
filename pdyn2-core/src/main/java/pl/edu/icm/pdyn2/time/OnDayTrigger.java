package pl.edu.icm.pdyn2.time;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.time.DayOfWeek;

public class OnDayTrigger {
    private final SimulationTimer simulationTimer;

    @WithFactory
    public OnDayTrigger(SimulationTimer simulationTimer) {
        this.simulationTimer = simulationTimer;
    }

    public EntitySystem onDay(int day, EntitySystem system) {
        return session -> {
            if (day == simulationTimer.getDaysPassed()) system.execute(session);
        };
    }

    public EntitySystem onDay(DayOfWeek dayOfWeek, EntitySystem system) {
        return session -> {
            if (dayOfWeek == simulationTimer.getCurrentDate().getDayOfWeek()) system.execute(session);
        };
    }

    public EntitySystem onDayOtherThan(int day, EntitySystem system) {
        return session -> {
            if (day != simulationTimer.getDaysPassed()) system.execute(session);
        };
    }

}
