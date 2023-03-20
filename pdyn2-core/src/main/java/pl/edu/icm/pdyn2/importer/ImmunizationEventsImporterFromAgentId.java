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

package pl.edu.icm.pdyn2.importer;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.util.IteratingSystemBuilder;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.Visit;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.util.Status;

import java.util.*;
import java.util.stream.Collectors;

public class ImmunizationEventsImporterFromAgentId {
    private final ImmunizationEventsLoaderFromAgentId loader;
    private final AgentIdMappingLoader mappingLoader;
    private final EngineIo board;
    private final Selectors selectors;
    private final AgentStateService agentStateService;
    private final SimulationTimer simulationTimer;
    private final DiseaseStageTransitionsService transitionsService;
    private final Loads loads;
    private final Stages stages;

    @WithFactory
    public ImmunizationEventsImporterFromAgentId(ImmunizationEventsLoaderFromAgentId immunizationEventsLoaderFromAgentId,
                                                 AgentIdMappingLoader mappingLoader,
                                                 EngineIo board,
                                                 Selectors selectors,
                                                 AgentStateService agentStateService,
                                                 SimulationTimer simulationTimer,
                                                 DiseaseStageTransitionsService transitionsService,
                                                 Loads loads,
                                                 Stages stages) {
        this.loader = immunizationEventsLoaderFromAgentId;
        this.mappingLoader = mappingLoader;
        this.board = board;
        this.selectors = selectors;
        this.agentStateService = agentStateService;
        this.simulationTimer = simulationTimer;
        this.transitionsService = transitionsService;
        this.loads = loads;
        this.stages = stages;
    }

    public void importEvents(String eventsFilename, String idsFilename, int durationOfPreviousSimulation) {

        Map<Integer, List<ImmunizationEvent>> immunizationMap = new HashMap<>();
        Map<Integer, Integer> idsMap = new HashMap<>();
        var currentDay = simulationTimer.getDaysPassed();

        mappingLoader.forEach(idsFilename, idsMapping -> idsMap.put(idsMapping.getPdyn2Id(), idsMapping.getPdyn1Id()));
        loader.forEach(eventsFilename, importedEvent -> {
            var event = new ImmunizationEvent();
            var load = load(importedEvent.getOdmiana_wirusa(), importedEvent.getOdmiana_szczepionki());
            event.setLoad(load);
            if (load.classification == LoadClassification.VIRUS) {
                event.setDiseaseHistory(importedEvent.getHistoria_stanow());
            }
            event.setDay(currentDay - durationOfPreviousSimulation + importedEvent.getDzien_zakazenia());
            immunizationMap.computeIfAbsent(importedEvent.getId(), k -> new ArrayList<>()).add(event);
        });
        var status = Status.of("applying immunization history to agents", loader.getCapacity() / 10 + 1);
        board.getEngine().execute(IteratingSystemBuilder.iteratingOver(
                        selectors.allWithComponents(Household.class))
                .persistingAll()
                .withoutContext()
                .perform(Visit.of((entity -> {
                    var members = entity.get(Household.class).getMembers();
                    members.forEach(memberEntity -> {
                        var pdyn2Id = memberEntity.getId();
                        var pdyn1Id = Optional.ofNullable(idsMap.get(pdyn2Id))
                                .orElseThrow(() -> new IllegalStateException("Could not find id mapping for pdyn2Id=" + pdyn2Id));

                        if (immunizationMap.containsKey(pdyn1Id)) {
                            var age = memberEntity.get(Person.class).getAge();
                            for (ImmunizationEvent event : immunizationMap.get(pdyn1Id)) {
                                event.setDay(
                                        endDayFromDiseaseHistory(event.getLoad(), event.getDiseaseHistory(), age, event.getDay()));
                                agentStateService.addImmunizationEvent(memberEntity, event);
                                if (event.getLoad().classification == LoadClassification.VIRUS) {
                                    agentStateService.addSourcesDistribution(memberEntity,
                                            new EnumSampleSpace<>(ContextInfectivityClass.class));
                                }
                                status.tick();
                            }
                        }
                    });
                }))).build());
        status.done();
    }

    private Load load(String diseaseLoad, String vaccineLoad) {
        if (vaccineLoad.equals("-1")) {
            switch (diseaseLoad) {
                case "0":
                    return loads.WILD;
                case "1":
                    return loads.ALPHA;
                case "2":
                    return loads.DELTA;
                case "3":
                    return loads.OMICRON;
            }
        } else if (diseaseLoad.equals("-1")) {
            switch (vaccineLoad) {
                case "0":
                    return loads.PFIZER;
                case "1":
                    return loads.BOOSTER;
            }
        }

        throw new IllegalArgumentException("Could not find value for: disease=" + diseaseLoad +
                " vaccine=" + vaccineLoad);
    }

    private int endDayFromDiseaseHistory(Load load, int history, int age, int startDay) {
        var allStages = stages.values().stream()
                .sorted(Comparator.comparingInt(Stage::getEncoding).reversed())
                .collect(Collectors.toList());
        for (Stage stage : allStages) {
            if (history >= stage.getEncoding()) {
                history -= stage.getEncoding();
                if (stage != stages.DECEASED && stage != stages.HEALTHY) {
                    startDay += transitionsService.durationOf(load, stage, age);
                }
            }
        }
        if (history != 0) {
            throw new IllegalStateException("Could not process disease history");
        }
        return startDay;
    }
}
