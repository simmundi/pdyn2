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
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.*;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.query.SelectorFromQueryService;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.util.Status;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class AreaClusteredSelectors {
    private final int targetChunkSize = 25_000;
    private final int chunkWidth = 6;
    private final int chunkHeight = 6;
    private final EngineConfiguration engineConfiguration;
    private final SelectorFromQueryService selectorFromQueryService;
    private final CommuneManager communeManager;
    private RandomAccessSelector personInGridSelector;
    private RandomAccessSelector householdInGridSelector;
    private RandomAccessSelector personInCommuneSelector;
    private RandomAccessSelector householdInCommuneSelector;

    @WithFactory
    public AreaClusteredSelectors(EngineConfiguration engineConfiguration,
                                  SelectorFromQueryService selectorFromQueryService,
                                  CommuneManager communeManager) {
        this.engineConfiguration = engineConfiguration;
        this.selectorFromQueryService = selectorFromQueryService;
        this.communeManager = communeManager;
        engineConfiguration.addComponentClasses(Person.class, Household.class, Location.class);
    }

    public synchronized RandomAccessSelector householdSelector() {
        if (householdInGridSelector == null) {
            createSelectors();
        }
        return householdInGridSelector;
    }

    public synchronized Selector personSelector() {
        if (personInGridSelector == null) {
            createSelectors();
        }
        return personInGridSelector;
    }

    public synchronized Selector peopleByTerytPrefixSelector(Collection<String> prefixes) {
        if (personInCommuneSelector == null) {
            createSelectors();
        }
        return new Selector() {
            @Override
            public Stream<Chunk> chunks() {
                return personInCommuneSelector.chunks()
                        .filter(chunk -> prefixes.stream()
                                .anyMatch(t -> chunk.getChunkInfo().getLabel().startsWith(t))
                        );
            }

            @Override
            public int estimatedChunkSize() {
                return targetChunkSize;
            }
        };
    }

    public synchronized Selector householdsByTerytPrefixSelector(Collection<String> prefixes) {
        if (householdInCommuneSelector == null) {
            createSelectors();
        }
        return new Selector() {
            @Override
            public Stream<Chunk> chunks() {
                return householdInCommuneSelector.chunks()
                        .filter(chunk -> prefixes.stream()
                                .anyMatch(t -> chunk.getChunkInfo().getLabel().startsWith(t))
                        );
            }

            @Override
            public int estimatedChunkSize() {
                return targetChunkSize;
            }
        };
    }

    private void createSelectors() {
        HouseholdMapper householdMapper = (HouseholdMapper) engineConfiguration.getEngine().getMapperSet().classToMapper(Household.class);
        LocationMapper locationMapper = (LocationMapper) engineConfiguration.getEngine().getMapperSet().classToMapper(Location.class);

        Status status = Status.of("Building area clustered selectors", 1_000_000);
        Map<SelectorType, Integer> tagClassifiersWithInitialSizes = Map.of(
                SelectorType.HOUSEHOLD_IN_GRID, 512,
                SelectorType.HOUSEHOLD_IN_COMMUNE, 2048,
                SelectorType.PERSON_IN_GRID, 4094,
                SelectorType.PERSON_IN_COMMUNE, 16384
        );

        var builtSelectors = selectorFromQueryService.fixedMultipleSelectorsFromRawQueryInParallel(
                tagClassifiersWithInitialSizes,
                (id, result, label) -> {
                    if (householdMapper.isPresent(id)) {
                        int locationE = locationMapper.getE(id);
                        int locationN = locationMapper.getN(id);
                        var kgc = KilometerGridCell.fromPl1992ENMeters(locationE, locationN);
                        var gridTag = getGridTagForLocation(kgc);
                        var communeTag = getCommuneTagForLocation(kgc);
                        result.add(id, gridTag, SelectorType.HOUSEHOLD_IN_GRID);
                        result.add(id, communeTag, SelectorType.HOUSEHOLD_IN_COMMUNE);

                        householdMapper.getMembers(id, (index, value) -> {
                            result.add(value, gridTag, SelectorType.PERSON_IN_GRID);
                            result.add(value, communeTag, SelectorType.PERSON_IN_COMMUNE);
                        });
                        status.tick();
                    }
                });

        householdInGridSelector = builtSelectors.get(SelectorType.HOUSEHOLD_IN_GRID);
        householdInCommuneSelector = builtSelectors.get(SelectorType.HOUSEHOLD_IN_COMMUNE);
        personInGridSelector = builtSelectors.get(SelectorType.PERSON_IN_GRID);
        personInCommuneSelector = builtSelectors.get(SelectorType.PERSON_IN_COMMUNE);
        status.done();
    }

    private String getCommuneTagForLocation(KilometerGridCell kgc) {
        return communeManager.communeAt(kgc).getTeryt();
    }

    private String getGridTagForLocation(KilometerGridCell kgc) {
        var col = kgc.getLegacyPdynCol();
        var row = kgc.getLegacyPdynRow();
        return "(" +
                (col / chunkWidth) * chunkWidth + "-" + (col / chunkWidth + 1) * chunkWidth +
                "," +
                (row / chunkHeight) * chunkHeight + "-" + (row / chunkHeight + 1) * chunkHeight +
                ")";
    }

    private enum SelectorType {
        PERSON_IN_GRID,
        HOUSEHOLD_IN_GRID,
        PERSON_IN_COMMUNE,
        HOUSEHOLD_IN_COMMUNE
    }
}
