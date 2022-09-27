package pl.edu.icm.pdyn2;

import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSources;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityMocker {

    private final MapperSet mapperSet;
    private final Session session;
    private final Map<Integer, Entity> entities = new HashMap<>();
    private final Store store = new ArrayStore(1);
    private final AtomicInteger idSequence = new AtomicInteger();

    public EntityMocker(Session session) {
        this(session,
                Person.class,
                Location.class,
                HealthStatus.class,
                Context.class,
                Inhabitant.class,
                Immunization.class,
                MedicalHistory.class,
                Impact.class,
                Behaviour.class,
                Travel.class,
                ImmunizationSources.class);
    }

    public EntityMocker(Session session, Class<?>... componentClasses) {
        this.mapperSet = new MapperSet(new DynamicComponentAccessor(componentClasses));
        mapperSet.streamMappers().forEach(m -> m.configureAndAttach(store));
        this.session = session;
    }

    public Entity id(int id) {
        return entities.get(id);
    }

    public Entity entityWithId(int id, Object... components) {
        idSequence.getAndAccumulate(id, (a, b) -> Math.max(a, b));
        return entity(components);
    }

    public Entity entity(Object... components) {
        int id = idSequence.getAndIncrement();
        Entity entity = new Entity(mapperSet, session, id);
        entities.put(id, entity);
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

}
