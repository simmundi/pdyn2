package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.pdyn2.transmission.TransmissionConfig;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.SessionFactory;

public class StatsOutputSystem implements EntitySystem {
    private final StatsService statsService;
    private final SimulationTimer simulationTimer;
    private final TransmissionConfig transmissionConfig;

    @WithFactory
    public StatsOutputSystem(StatsService statsService, SimulationTimer simulationTimer, TransmissionConfig transmissionConfig) {
        this.statsService = statsService;
        this.simulationTimer = simulationTimer;
        this.transmissionConfig = transmissionConfig;
    }

    @Override
    public void execute(SessionFactory sessionFactory) {
        System.out.println(simulationTimer.getCurrentDate() + " (day " + simulationTimer.getDaysPassed() + ")");
        System.out.println("Weights: " + transmissionConfig.toString());
        statsService.debugOutputStats(System.out);
        statsService.writeDayToStatsOutputFile();
        statsService.resetStats();
    }
}
