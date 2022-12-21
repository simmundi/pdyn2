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

package pl.edu.icm.pdyn2.variantsowing;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.*;

public class VariantSowingFromCsvSystemBuilder {
    private final VariantSowingFromCsvLoader loader;
    private final VariantSowingService variantSowingService;
    private final SimulationTimer simulationTimer;
    private final Map<Integer, Set<VariantSowingRecord>> sowingRecords = new HashMap<>();


    @WithFactory
    public VariantSowingFromCsvSystemBuilder(VariantSowingFromCsvLoader variantSowingFromCsvLoader,
                                             VariantSowingService variantSowingService,
                                             SimulationTimer simulationTimer) {
        this.loader = variantSowingFromCsvLoader;
        this.variantSowingService = variantSowingService;
        this.simulationTimer = simulationTimer;
    }

    public void load() {
        var loadingStatus = Status.of("Loading variant sowing records from file");
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loader.forEach(recordFromCsv -> sowingRecords.compute(recordFromCsv.getDay(), (day, records) -> {
            var record = new VariantSowingRecord();
            record.setLoad(recordFromCsv.getLoad());
            record.setMinAge(recordFromCsv.getMinAge());
            record.setMaxAge(recordFromCsv.getMaxAge());
            record.setSowingCount(recordFromCsv.getSowingCount());
            record.setTeryts(List.of(recordFromCsv.getTeryts().split(", ")));
            if (records == null) {
                var hs = new HashSet<VariantSowingRecord>();
                hs.add(record);
                return hs;
            } else {
                records.add(record);
                return records;
            }
        }));
        loadingStatus.done();
    }

    public EntitySystem buildVariantSowingSystem() {
        if (sowingRecords.isEmpty())
            load();
        return sessionFactory -> {
            var currentDay = simulationTimer.getDaysPassed();
            if (sowingRecords.isEmpty()) {
                throw new IllegalStateException("No variant sowing records");
            }
            if (!sowingRecords.containsKey(currentDay)) {
                return;
            }
            var status = Status.of("Variant sowing");
            var session = sessionFactory.create();
            for (VariantSowingRecord record : sowingRecords.get(currentDay)) {
                variantSowingService.sowVariant(session, record.getLoad(), record.getSowingCount(), record.getTeryts(), status,
                        HealthStatus.class, hs -> hs.getStage() == Stage.LATENT && hs.getDiseaseLoad() != record.getLoad(), false,
                        Person.class, person -> person.getAge() >= record.getMinAge() && person.getAge() <= record.getMaxAge(), false);
            }
            session.close();
            status.done();
        };
    }
}
