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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.HouseholdMapper;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.ManuallyChunkedArraySelector;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.util.Status;

public class AreaClusteredSelectors {

    private final EngineConfiguration engineConfiguration;
    private final int targetChunkSize = 25_000;
    private final int gridColumns;
    private final int gridRows;
    private final int chunkWidth = 6;
    private final int chunkHeight = 6;
    private ManuallyChunkedArraySelector personSelector;
    private ManuallyChunkedArraySelector householdSelector;

    @WithFactory
    public AreaClusteredSelectors(EngineConfiguration engineConfiguration, int gridColumns, int gridRows) {
        this.engineConfiguration = engineConfiguration;
        this.gridColumns = gridColumns;
        this.gridRows = gridRows;
        engineConfiguration.addComponentClasses(Person.class, Household.class, Location.class);
    }

    public synchronized RandomAccessSelector householdSelector() {
        if (householdSelector == null) {
            createSelectors();
        }
        return householdSelector;
    }

    public synchronized Selector personSelector() {
        if (personSelector == null) {
            createSelectors();
        }
        return personSelector;
    }

    private void createSelectors() {
        HouseholdMapper householdMapper = (HouseholdMapper) engineConfiguration.getEngine().getMapperSet().classToMapper(Household.class);
        Mapper<Location> locationMapper = engineConfiguration.getEngine().getMapperSet().classToMapper(Location.class);
        EntityListAttribute members = engineConfiguration.getEngine().getStore().get("members");

        Multimap<KilometerGridCell, Integer> multimap = HashMultimap.create(13_000_000, 100);

        {
            Status status = Status.of("locating households", 1000_000);
            for (int id = 0; id < engineConfiguration.getEngine().getCount(); id++) {
                if (!householdMapper.isPresent(id)) {
                    continue;
                }
                status.tick();
                multimap.put(KilometerGridCell.fromLocation(locationMapper.createAndLoad(id)),
                        id);
            }
            status.done();
        }

        personSelector = new ManuallyChunkedArraySelector(39_000_000, multimap.keySet().size());
        householdSelector = new ManuallyChunkedArraySelector(15_000_000, multimap.keySet().size());
        {
            Status status = Status.of("indexing households and people", 1000_000);

            for (int col = 0; col < gridColumns; col += chunkWidth) {
                for (int row = 0; row < gridRows; row += chunkHeight) {
                    for (int e = 0; e < chunkWidth; e++) {
                        for (int n = 0; n < chunkHeight; n++) {
                            KilometerGridCell kilometerGridCell = KilometerGridCell.fromLegacyPdynCoordinates(col + e, row + n);
                            for (Integer id : multimap.get(kilometerGridCell)) {
                                householdSelector.add(id);
                                members.loadIds(id, (index, value) -> personSelector.add(value));
                                status.tick();
                            }
                        }
                    }
                    if (householdSelector.getRunningSize() >= targetChunkSize) {
                        householdSelector.endChunk();
                    }
                    if (personSelector.getRunningSize() >= targetChunkSize) {
                        personSelector.endChunk();
                    }
                }
            }
            status.done();
        }
    }
}
