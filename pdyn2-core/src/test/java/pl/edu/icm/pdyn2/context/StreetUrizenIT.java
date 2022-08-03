package pl.edu.icm.pdyn2.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.*;


@ExtendWith(MockitoExtension.class)
class StreetUrizenIT {
    private EngineConfiguration engineConfiguration;
    private Selectors selectors;
    private final ArrayList<Location> locations = new ArrayList<>(List.of(
            new Location(10500, 20500),
            new Location(10550, 20500),
            new Location(54500, 23500)));

    @BeforeEach
    void before() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        selectors = new Selectors(engineConfiguration);
        engineConfiguration.addComponentClasses(Area.class, Location.class, Context.class);
        engineConfiguration.getEngine().execute(ctx -> {
            Session session = ctx.create();
            locations.forEach(location -> {
                var entity = session.createEntity();
                entity.add(location);
            });
            session.close();
        });
    }

    @Test
    @Disabled("Should be turned into a unit test")
    void buildStreets() {
        //given
        StreetUrizen streetUrizen = new StreetUrizen(engineConfiguration, selectors);
        //execute
        streetUrizen.buildStreets();
        var areaList = engineConfiguration.getEngine()
                .streamDetached()
                .filter(entity -> entity.get(Area.class) != null)
                .map(entity -> entity.get(Area.class))
                .sorted(Comparator.comparingInt(Area::getE)) //need to sort because using set in StreetBuilder.buildStreets
                .collect(Collectors.toList());
        locations.sort(Comparator.comparingInt(Location::getE)); //it's sorted when creating, just to see connections with areaList
        //assert
        assertThat(areaList.size()).isEqualTo(2);
        assertThat(KilometerGridCell.fromArea(areaList.get(0))).isEqualTo(KilometerGridCell.fromLocation(locations.get(0)));
        assertThat(KilometerGridCell.fromArea(areaList.get(0))).isEqualTo(KilometerGridCell.fromLocation(locations.get(1)));
        assertThat(KilometerGridCell.fromArea(areaList.get(1))).isEqualTo(KilometerGridCell.fromLocation(locations.get(2)));
    }
}
