package pl.edu.icm.pdyn2.time;

import com.google.common.base.Strings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.SessionFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Keeps track of the simulation time.
 *
 * Provides any piece of logic with the current date,
 * allows some main loop to advance the time by one day.
 */
public class SimulationTimer implements EntitySystem {
    private final LocalDateTime simulationStartDate;
    private Duration simulationTime = Duration.ZERO;

    @WithFactory
    public SimulationTimer(String simulationStartDate) {
        this.simulationStartDate = Strings.isNullOrEmpty(simulationStartDate)
                ? LocalDateTime.now()
                : LocalDate.parse(simulationStartDate).atStartOfDay();
    }

    public void advanceOneDay() {
        simulationTime = simulationTime.plusDays(1);
    }

    public LocalDate getCurrentDate() {
        return simulationStartDate.plus(simulationTime).toLocalDate();
    }

    public int getDaysPassed() {
        return (int) simulationTime.toDaysPart();
    }

    @Override
    public void execute(SessionFactory session) {
        advanceOneDay();
    }
}
