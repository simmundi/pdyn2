package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.WorkDir;
import net.snowyhollows.bento.config.WorkDirFactory;
import org.mockito.Mockito;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.impact.AgentImpactService;
import pl.edu.icm.pdyn2.impact.AgentImpactServiceFactory;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.context.ContextsServiceFactory;
import pl.edu.icm.pdyn2.index.AreaIndexFactory;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.pdyn2.time.SimulationTimerFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.SelectorsFactory;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.ecs.util.StaticSelectorsFactory;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static pl.edu.icm.pdyn2.ComponentCreator.*;

public class ExampleDataForIntegrationTests {
    public final Session session;
    public Selectors selectors;
    public Entity agent1;
    public Entity agent2;
    public Entity agent3;
    public Entity agent4;
    public Entity agent5;
    public Entity agent6;
    public Entity agent7;
    public Entity agent8;
    public Entity agent9;
    public Entity agentA;
    public Entity school1;
    public Entity school2;
    public Entity workplace;
    public Entity householdContext1;
    public Entity householdContext2;
    public Entity householdContext3;
    public List<Entity> streetsA = new ArrayList<>();
    public List<Entity> streetsB = new ArrayList<>();
    public List<Entity> streetsC = new ArrayList<>();
    public KilometerGridCell cellA;
    public KilometerGridCell cellB;
    public KilometerGridCell cellC;
    public Bento config;
    public AgentStateService agentStateService;
    public AgentImpactService agentImpactService;
    public ContextsService contextsService;
    public SimulationTimer simulationTimer;
    public Engine engine;
    public final int cols = 100;
    public final int rows = 100;
    public List<Entity> allAgents;
    public final WorkDir workDir = Mockito.mock(WorkDir.class);

    public ExampleDataForIntegrationTests() {
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
        AreaIndex areaIndex = config.get(AreaIndexFactory.IT);
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

        AtomicReference<Session> sessionReference = new AtomicReference<>();
        engine.execute(sessionFactory -> {
            sessionReference.set(sessionFactory.create());
        });
        session = sessionReference.get();
        createStreets(session);
        session.close();

        cellA = KilometerGridCell.fromArea(area(KilometerGridCell.fromLegacyPdynCoordinates(50, 50)));
        cellB = KilometerGridCell.fromArea(area(KilometerGridCell.fromLegacyPdynCoordinates(51, 50)));
        cellC = KilometerGridCell.fromArea(area(KilometerGridCell.fromLegacyPdynCoordinates(52, 50)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cellA, (idx, id) -> streetsA.add(session.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cellB, (idx, id) -> streetsB.add(session.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cellC, (idx, id) -> streetsC.add(session.getEntity(id)));
        householdContext1 = session.createEntity(context(ContextType.HOUSEHOLD), location(cellA));
        householdContext2 = session.createEntity(context(ContextType.HOUSEHOLD), location(cellA));
        householdContext3 = session.createEntity(context(ContextType.HOUSEHOLD), location(cellC));
        school1 = session.createEntity(context(ContextType.SCHOOL));
        school2 = session.createEntity(context(ContextType.SCHOOL));
        workplace = session.createEntity(context(ContextType.WORKPLACE));
        agent1 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext1, school1), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent2 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext1, school1), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent3 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext1, school1), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent4 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext2, school1), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent5 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext2, school1), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent6 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext2, school2), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent7 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext3, school2), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent8 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext3, school2), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agent9 = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext3, school2), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        agentA = session.createEntity(person(18, Person.Sex.M),
                inhabitant(householdContext3, workplace, school2), health(Load.WILD, Stage.HEALTHY), behaviour(BehaviourType.ROUTINE));
        allAgents = List.of(agent1, agent2, agent3, agent4, agent5, agent6, agent7, agent8, agent9, agentA);
        householdContext1.add(household(List.of(agent1, agent2, agent3)));
        householdContext2.add(household(List.of(agent4, agent5, agent6)));
        householdContext3.add(household(List.of(agent7, agent8, agent9, agentA)));
    }

    public void createStreets(Session session) {
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                for (ContextType streetContext : ContextType.streetContexts()) {
                    session.createEntity(
                            context(streetContext),
                            area(KilometerGridCell.fromLegacyPdynCoordinates(i, j))
                    );
                }
            }
        }
    }
}
