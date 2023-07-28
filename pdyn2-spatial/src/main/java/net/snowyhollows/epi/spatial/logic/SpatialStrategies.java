package net.snowyhollows.epi.spatial.logic;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.epi.spatial.model.Position;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.ContextTypes;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.transmission.ContextImpactService;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.Arrays;
import java.util.stream.Stream;

public class SpatialStrategies implements ContextImpactService, ContextsService {

    private final int[] spatialContexts;
    private final double variance;
    private final int maxDistance;
    private final ContextType contextType;


    @WithFactory
    public SpatialStrategies(@ByName("spatial.size") int size, @ByName("spatial.variance") float variance, ContextTypes contextTypes) {
        this.spatialContexts = new int[size];
        this.variance = variance;
        this.maxDistance = (int) Math.ceil(Math.sqrt(-2 * variance * Math.log(0.01)));
        this.contextType = contextTypes.getByOrdinal(0);
    }

    public double weight(double distance) {
        return Math.exp(-distance * distance / (2 * variance));
    }

    public void initializeContexts(Session session) {
        for (int i = 0; i < spatialContexts.length; i++) {
            int id = session.createEntity(new Context(contextType), Position.of(i)).getId();
            spatialContexts[i] = id;
        }
    }

    @Override
    public float calculateInfluenceFractionFor(Entity agentEntity, Entity contextEntity) {
        Position position = agentEntity.get(Position.class);
        Position contextPosition = contextEntity.get(Position.class);

        double distance = position.distance(contextPosition);
        double weight = weight(distance);
        return (float) weight;
    }

    @Override
    public Stream<Entity> findActiveContextsForAgent(Entity agentEntity) {
        Behaviour behaviour = agentEntity.get(Behaviour.class);
        BehaviourType behaviourType = behaviour.getType();
        return findActiveContextsForAgent(agentEntity, behaviourType);
    }

    @Override
    public Stream<Entity> findActiveContextsForAgent(Entity agentEntity, Impact impact) {
        BehaviourType behaviourType = impact.getType();
        return findActiveContextsForAgent(agentEntity, behaviourType);
    }

    private Stream<Entity> findActiveContextsForAgent(Entity agentEntity, BehaviourType behaviourType) {
        switch (behaviourType) {
            case SELF_ISOLATION:
            case QUARANTINE:
            case DEAD:
            case HOSPITALIZED:
            case DORMANT:
                return Stream.empty();
            default:
                break;
        }

        Position position = agentEntity.get(Position.class);
        int rounded = Math.round(position.getX());
        int min = Math.max(0, rounded - maxDistance);
        int max = Math.min(spatialContexts.length, rounded + maxDistance);
        return Arrays.stream(spatialContexts, min, max).mapToObj(agentEntity.getSession()::getEntity);
    }
}
