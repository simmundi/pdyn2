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

package pl.edu.icm.pdyn2.vaccination;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.*;

public class VaccinationFromCsvSystemBuilder {
    private final VaccinationFromCsvLoader loader;
    private final SimulationTimer simulationTimer;
    private final Map<Integer, Set<VaccinationRecord>> vaccinationRecords = new HashMap<>();
    private final VaccinationService vaccinationService;
    private final Stages stages;


    @WithFactory
    public VaccinationFromCsvSystemBuilder(VaccinationFromCsvLoader vaccinationFromCsvLoader,
                                           SimulationTimer simulationTimer,
                                           VaccinationService vaccinationService,
                                           Stages stages) {
        this.loader = vaccinationFromCsvLoader;
        this.simulationTimer = simulationTimer;
        this.vaccinationService = vaccinationService;
        this.stages = stages;
    }

    public void load() {
        var loadingStatus = Status.of("Loading vaccination records from file");
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loader.forEach(recordFromCsv -> vaccinationRecords.compute(recordFromCsv.getDay(), (day, records) -> {
            var record = new VaccinationRecord();
            record.setLoad(recordFromCsv.getLoad());
            record.setMinAge(recordFromCsv.getMinAge());
            record.setMaxAge(recordFromCsv.getMaxAge());
            record.setVaccineCount(recordFromCsv.getVaccineCount());
            record.setTeryts(List.of(recordFromCsv.getTeryts().split(", ")));
            if (records == null) {
                var hs = new HashSet<VaccinationRecord>();
                hs.add(record);
                return hs;
            } else {
                records.add(record);
                return records;
            }
        }));
        loadingStatus.done();
    }

    public EntitySystem buildVaccinationSystem() {
        if (vaccinationRecords.isEmpty())
            load();
        return sessionFactory -> {
            var currentDay = simulationTimer.getDaysPassed();
            if (vaccinationRecords.isEmpty()) {
                throw new IllegalStateException("No vaccination records");
            }
            if (!vaccinationRecords.containsKey(currentDay)) {
                return;
            }
            Status status = Status.of("Vaccinating");
            var session = sessionFactory.create();
            for (VaccinationRecord record : vaccinationRecords.get(currentDay)) {
                var vaccinationEvent = new ImmunizationEvent();
                vaccinationEvent.setDay(currentDay);
                vaccinationEvent.setLoad(record.getLoad());
                vaccinationService.vaccinate(
                        session,
                        vaccinationEvent,
                        record.getVaccineCount(),
                        record.getTeryts(),
                        status,
                        HealthStatus.class,
                        hs -> hs.getStage() == stages.HEALTHY,
                        false,
                        Behaviour.class,
                        b -> b.getType() == BehaviourType.ROUTINE,
                        false,
                        Person.class,
                        person -> person.getAge() >= record.getMinAge() && person.getAge() <= record.getMaxAge(),
                        false,
                        Immunization.class,
                        immunization -> immunization.getEvents().stream()
                                .noneMatch(immunizationEvent ->
                                        immunizationEvent.getLoad() == record.getLoad()),
                        true
                );
            }
            session.close();
            status.done();
        };
    }
}
