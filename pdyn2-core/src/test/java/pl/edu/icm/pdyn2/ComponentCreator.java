package pl.edu.icm.pdyn2;

import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Arrays;
import java.util.Collection;

public class ComponentCreator {

    private ComponentCreator() {
    }

    public static Person person(int age, Person.Sex sex) {
        Person person = new Person();
        person.setAge(age);
        person.setSex(sex);
        return person;
    }

    public static Impact impact(BehaviourType type, Load load, Stage stage) {
        Impact impact = new Impact();
        impact.affect(
                behaviour(type),
                health(load, stage));
        return impact;
    }

    public static Area area(KilometerGridCell cell) {
        return new Area((short) cell.getE(), (short) cell.getN());
    }

    public static Area area(int e, int n) {
        return new Area((short) e, (short) n);
    }

    public static Context context(ContextType type) {
        Context context = new Context();
        context.setContextType(type);
        return context;
    }

    public static Household household(Collection<Entity> members) {
        Household household = new Household();
        household.getMembers().addAll(members);
        return household;
    }

    public static Household household(Entity... members) {
        return household(Arrays.asList(members));
    }

    public static Location location(KilometerGridCell cell) {
        Location location = new Location();
        location.setE(cell.getE() * 1000 + 500);
        location.setN(cell.getN() * 1000 + 500);
        return location;
    }

    public static Inhabitant inhabitant(Entity home, Entity... otherContexts) {
        Inhabitant inhabitant = new Inhabitant();
        inhabitant.setHomeContext(home);
        for (Entity otherContext : otherContexts) {
            inhabitant.getContexts().add(otherContext);
        }
        return inhabitant;
    }

    public static Behaviour behaviour(BehaviourType type, int startDay) {
        Behaviour behaviour = new Behaviour();
        behaviour.transitionTo(type, startDay);
        return behaviour;
    }

    public static Behaviour behaviour(BehaviourType type) {
        return behaviour(type, 0);
    }

    public static Travel travel(Entity target) {
        Travel travel = new Travel();
        travel.setStayingAt(target);
        return travel;
    }

    public static HealthStatus health(Load load, Stage stage) {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setDiseaseLoad(load);
        healthStatus.setStage(stage);
        return healthStatus;
    }
}
