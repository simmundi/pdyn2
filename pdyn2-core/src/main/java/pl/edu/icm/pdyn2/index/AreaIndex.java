package pl.edu.icm.pdyn2.index;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.index.ComponentIndex;
import pl.edu.icm.trurl.store.IntSink;

import java.util.Arrays;

public class AreaIndex extends ComponentIndex<Area> {

    public static final int EMPTY_ID = Integer.MIN_VALUE;
    public static final int SLOTS = 10;

    public final int gridColumns;
    public final int gridRows;
    private final int[] positions;

    @WithFactory
    public AreaIndex(EngineConfiguration engineConfiguration, int gridColumns, int gridRows) {
        super(engineConfiguration, Area.class);
        positions = new int[gridColumns * gridRows * SLOTS];

        Arrays.fill(positions, EMPTY_ID);

        this.gridColumns = gridColumns;
        this.gridRows = gridRows;
    }

    @Override
    public void savingComponent(int id, Area newValue) {
        if (newValue == null) {
            //no support for deleting
        } else {
            KilometerGridCell gridCell = KilometerGridCell.fromArea(newValue);
            var col = gridCell.getLegacyPdynCol();
            var row = gridCell.getLegacyPdynRow();

            if (isInGrid(col, row)) {
                int baseIdx = (row * gridColumns + col) * SLOTS;

                for (int idx = baseIdx; idx < baseIdx + SLOTS; idx++) {
                    if (positions[idx] == EMPTY_ID) {
                        positions[idx] = id;
                        return;
                    }
                }

                throw new IllegalStateException("already fully occupied");
            } else {
                throw new IllegalStateException("not in grid: " + col + ", " + row);
            }
        }
    }

    public void appendStreetIdsFromKilometerGridCell(KilometerGridCell kilometerGridCell, IntSink intSink) {
        if (!isInGrid(kilometerGridCell.getLegacyPdynCol(), kilometerGridCell.getLegacyPdynRow())) {
            return;
        }

        int baseIdx = (kilometerGridCell.getLegacyPdynRow() * gridColumns
                + kilometerGridCell.getLegacyPdynCol()) * SLOTS;

        for (int i = 0; i < SLOTS; i++) {
            int streetId = positions[baseIdx + i];
            if (streetId != EMPTY_ID) {
                intSink.setInt(i, positions[baseIdx + i]);
            }
        }
    }

    private boolean isInGrid(int col, int row) {
        return col < gridColumns && row < gridRows && col >= 0 && row >= 0;
    }
}
