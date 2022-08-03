package pl.edu.icm.pdyn2.context;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.util.HashSet;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class StreetUrizen {
    private final EngineConfiguration engineConfiguration;
    private final Selectors selectors;

    public final static ContextType[] STREET_CONTEXTS = ContextType.streetContexts();
    @WithFactory
    public StreetUrizen(EngineConfiguration engineConfiguration, Selectors selectors) {
        this.engineConfiguration = engineConfiguration;
        this.selectors = selectors;
    }

    public void buildStreets() {
        var statusCollecting = Status.of("Collecting streets to build", 1000000);
        HashSet<KilometerGridCell> kilometerGridCellsWithLocation = new HashSet<>();
        Engine engine = engineConfiguration.getEngine();
        engine.execute(select(selectors.allWithComponents(Location.class)).detachEntities().forEach(Location.class, (entity, location) -> {
            statusCollecting.tick();
            KilometerGridCell center = KilometerGridCell.fromLocation(location);
            kilometerGridCellsWithLocation.add(center);
            kilometerGridCellsWithLocation.add(center.neighbourE());
            kilometerGridCellsWithLocation.add(center.neighbourW());
            kilometerGridCellsWithLocation.add(center.neighbourN());
            kilometerGridCellsWithLocation.add(center.neighbourS());
        }));
        statusCollecting.done("found: " + kilometerGridCellsWithLocation.size() + " street locations");
        var statusBuilding = Status.of("Building streets", 10000);
        engine.execute(ctx -> {
            Session session = ctx.create();
            kilometerGridCellsWithLocation.stream()
                    .peek(kgc -> statusBuilding.tick())
                    .forEach(kgc -> buildStreet(session, kgc));
            session.close();
        });
        statusBuilding.done();
    }

    private void buildStreet(Session ctx, KilometerGridCell kilometerGridCell) {
        for (ContextType streetContext : STREET_CONTEXTS) {
            Entity street = ctx.createEntity();
            Area area = kilometerGridCell.toArea();
            Context context = new Context(streetContext);
            street.add(area);
            street.add(context);
        }
    }

}
