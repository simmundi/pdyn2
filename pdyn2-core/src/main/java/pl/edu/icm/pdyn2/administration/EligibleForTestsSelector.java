package pl.edu.icm.pdyn2.administration;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.HealthStatusMapper;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.Selectors;

import java.util.stream.Stream;

/**
 * Selector returning agents eligible for tests, used by the testing system.
 */
public class EligibleForTestsSelector implements Selector {

    private HealthStatusMapper healthStatusMapper;
    private final SimulationTimer simulationTimer;
    private final Selectors selectors;
    private final AreaClusteredSelectors areaClusteredSelectors;

    @WithFactory
    public EligibleForTestsSelector(EngineConfiguration engineConfiguration,
                                    SimulationTimer simulationTimer,
                                    Selectors selectors,
                                    AreaClusteredSelectors areaClusteredSelectors) {
        this.simulationTimer = simulationTimer;
        this.selectors = selectors;
        this.areaClusteredSelectors = areaClusteredSelectors;
        engineConfiguration.addEngineCreationListeners(engine -> {
            healthStatusMapper = (HealthStatusMapper) engine.getMapperSet().classToMapper(HealthStatus.class);
        });
    }


    /**
     * The current logic selects agents who are in their fist day after the LATENT stage.
     */
    @Override
    public Stream<Chunk> chunks() {
        return selector().chunks();
    }

    private Selector selector() {
        int today = simulationTimer.getDaysPassed();
        return selectors.filtered(
                areaClusteredSelectors.personSelector(),
                (id -> {
                    int day = healthStatusMapper.getDayOfLastChange(id);
                    if (day == today) {
                        Stage stage = healthStatusMapper.getStage(id);
                        return stage == Stage.INFECTIOUS_ASYMPTOMATIC || stage == Stage.INFECTIOUS_SYMPTOMATIC;
                    }
                    return false;
                }));
    }
}
