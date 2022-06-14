package pl.edu.icm.pdyn2.quarantine;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

public final class QuarantineService {
    private final StatsService statsService;
    private final AgentStateService agentStateService;
    private final QuarantineConfig quarantineConfig;
    private final SimulationTimer simulationTimer;

    @WithFactory
    public QuarantineService(StatsService statsService,
                             AgentStateService agentStateService,
                             QuarantineConfig quarantineConfig,
                             SimulationTimer simulationTimer) {
        this.statsService = statsService;
        this.agentStateService = agentStateService;
        this.quarantineConfig = quarantineConfig;
        this.simulationTimer = simulationTimer;
    }

    public void maybeEndQuarantine(Entity agentEntity) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        if (behaviour.getType() != BehaviourType.QUARANTINE) {
            return;
        }
        int howLong = simulationTimer.getDaysPassed() - behaviour.getDayOfLastChange();
        if (howLong > quarantineConfig.getQuarantineLengthDays()) {
            agentStateService.endQuarantine(agentEntity);
            statsService.tickUnquarantined();
        }
    }
}
