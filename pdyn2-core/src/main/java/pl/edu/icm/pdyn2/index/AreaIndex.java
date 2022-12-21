/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

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
