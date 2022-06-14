package pl.edu.icm.pdyn2.context;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The service encapsulates logic for returning all the contexts that an agent is active in.
 * <p>
 * The main factor for establishing the contexts is the agent's Behaviour (or rather - its type),
 * but the information might come from different components, like Travel and Inhabitant; some of
 * the logic might be purely programmatic (e.g. households automatically pulling in their streets)
 */
public class ContextsService {

    private final AreaIndex areaIndex;

    @WithFactory
    public ContextsService(AreaIndex areaIndex) {
        this.areaIndex = areaIndex;
    }

    public Stream<Context> findActiveContextsForAgent(Entity agentEntity) {
        Behaviour behaviour = agentEntity.get(Behaviour.class);
        BehaviourType type = behaviour == null ? BehaviourType.DORMANT : behaviour.getType();
        return findActiveContextsForAgent(agentEntity, type);
    }

    public Stream<Context> findActiveContextsForAgent(Entity agentEntity, Impact impact) {
        BehaviourType type = impact.getType() == null ? BehaviourType.DORMANT : impact.getType();
        return findActiveContextsForAgent(agentEntity, type);
    }

    private Stream<Context> findActiveContextsForAgent(Entity agentEntity, BehaviourType type) {
        switch (type) {
            case DORMANT:
            case DEAD:
            case HOSPITALIZED:  // not interacting with any context
                return Stream.empty();
            case ROUTINE: {     // routine: home + any education / workplace / street contexts + their linked contexts
                List<Entity> contexts = new ArrayList(100);
                Inhabitant inhabitant = agentEntity.get(Inhabitant.class);
                contexts.add(inhabitant.getHomeContext());
                contexts.addAll(inhabitant.getContexts());
                appendHood(inhabitant.getHomeContext(), contexts);
                return contexts.stream().map(e -> e.get(Context.class));
            }
            case PRIVATE_TRAVEL: {  // target place of travel plus its linked contexts (e.g. street)
                Travel travel = agentEntity.get(Travel.class);
                List<Entity> contexts = new ArrayList(100);
                contexts.add(travel.getStayingAt());
                appendHood(travel.getStayingAt(), contexts);
                return contexts.stream().map(e -> e.get(Context.class));
            }
            case QUARANTINE:
            case SELF_ISOLATION: {  // just the home context
                Inhabitant inhabitant = agentEntity.get(Inhabitant.class);
                return Stream.of(inhabitant.getHomeContext().get(Context.class));
            }
        }

        throw new IllegalStateException("State is not supported: " + type);
    }

    private void appendHood(Entity entity, List<Entity> results) {
        Location location = entity.get(Location.class);
        if (location == null) {
            return;
        }
        Session ctx = entity.getSession();
        KilometerGridCell cell = KilometerGridCell.fromLocation(location);
        areaIndex.appendStreetIdsFromKilometerGridCell(cell, (idx, id) -> results.add(ctx.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cell.neighbourE(), (idx, id) -> results.add(ctx.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cell.neighbourW(), (idx, id) -> results.add(ctx.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cell.neighbourN(), (idx, id) -> results.add(ctx.getEntity(id)));
        areaIndex.appendStreetIdsFromKilometerGridCell(cell.neighbourS(), (idx, id) -> results.add(ctx.getEntity(id)));
    }

}
