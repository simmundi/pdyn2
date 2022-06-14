package pl.edu.icm.pdyn2.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.trurl.ecs.EngineConfiguration;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ExtendWith(MockitoExtension.class)
class AreaIndexTest {

    @Mock
    private EngineConfiguration engineConfiguration;
    private final Area area1 = new Area((short) 74, (short) 856);
    private final Area area2 = new Area((short) 75, (short) 857);


    @Test
    void savingAndGettingComponent() {
        //given
        var areaIndex = new AreaIndex(engineConfiguration, 20, 20);

        //execute
        areaIndex.savingComponent(4321, area2);

        areaIndex.savingComponent(1234, area1);
        areaIndex.savingComponent(1, area1);
        areaIndex.savingComponent(2, area1);
        areaIndex.savingComponent(3, area1);
        areaIndex.savingComponent(4, area1);
        areaIndex.savingComponent(5, area1);

        var idArea1 = new ArrayList<Integer>();
        areaIndex.appendStreetIdsFromKilometerGridCell(KilometerGridCell.fromArea(area1), idArea1::add);
        var idArea2 = new ArrayList<Integer>();
        areaIndex.appendStreetIdsFromKilometerGridCell(KilometerGridCell.fromArea(area2), idArea2::add);

        //assert
        assertThat(idArea1.toArray()).containsExactly(1234, 1, 2, 3, 4, 5);
        assertThat(idArea2.toArray()).containsExactly(4321);
    }

}
