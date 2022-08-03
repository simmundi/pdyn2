package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.WorkDir;
import net.snowyhollows.bento.config.WorkDirFactory;
import org.mockito.Mockito;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.context.ContextsServiceFactory;
import pl.edu.icm.pdyn2.impact.AgentImpactService;
import pl.edu.icm.pdyn2.impact.AgentImpactServiceFactory;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.pdyn2.time.SimulationTimerFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.SelectorsFactory;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

public class EmptyDataForIntegrationTests {
    public Selectors selectors;
    public Bento config;
    public AgentStateService agentStateService;
    public AgentImpactService agentImpactService;
    public ContextsService contextsService;
    public SimulationTimer simulationTimer;
    public Engine engine;
    public final int cols = 100;
    public final int rows = 100;
    public final WorkDir workDir = Mockito.mock(WorkDir.class);

    public EmptyDataForIntegrationTests() {
        config = Bento.createRoot();
        config.register("gridRows", rows);
        config.register("gridColumns", cols);
        config.register("alpha", 0.5f);
        config.register("simulationStartDate", "1977-04-01");
        config.register("asymptomaticInfluenceShare", 0.1);
        config.register("symptomaticInfluenceShare", 1.0);
        config.register(WorkDirFactory.IT, workDir);
        agentStateService = config.get(AgentStateServiceFactory.IT);
        contextsService = config.get(ContextsServiceFactory.IT);
        agentImpactService = config.get(AgentImpactServiceFactory.IT);
        simulationTimer = config.get(SimulationTimerFactory.IT);
        selectors = config.get(SelectorsFactory.IT);

        EngineConfiguration engineConfig = config.get(EngineConfigurationFactory.IT);
        engineConfig.setStoreFactory(new ArrayStoreFactory());
        engineConfig.addComponentClasses(
                Person.class,
                Location.class,
                Area.class,
                HealthStatus.class,
                Context.class,
                Inhabitant.class,
                Immunization.class,
                Behaviour.class,
                Travel.class,
                Household.class,
                MedicalHistory.class,
                Impact.class);
        engine = engineConfig.getEngine();
    }
}
