package pl.edu.icm.pdyn2.importer;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.util.*;
import java.util.stream.Collectors;

public class ImmunizationEventsImporterFromAgentId {
    private final ImmunizationEventsLoaderFromAgentId loader;
    private final AgentIdMappingLoader mappingLoader;
    private final Board board;
    private final Selectors selectors;
    private final AgentStateService agentStateService;
    private final SimulationTimer simulationTimer;
    private final DiseaseStageTransitionsService transitionsService;

    @WithFactory
    public ImmunizationEventsImporterFromAgentId(ImmunizationEventsLoaderFromAgentId immunizationEventsLoaderFromAgentId,
                                                 AgentIdMappingLoader mappingLoader,
                                                 Board board,
                                                 Selectors selectors,
                                                 AgentStateService agentStateService,
                                                 SimulationTimer simulationTimer,
                                                 DiseaseStageTransitionsService transitionsService) {
        this.loader = immunizationEventsLoaderFromAgentId;
        this.mappingLoader = mappingLoader;
        this.board = board;
        this.selectors = selectors;
        this.agentStateService = agentStateService;
        this.simulationTimer = simulationTimer;
        this.transitionsService = transitionsService;
    }

    public void importEvents(String filename, String idsFilename, int durationOfPreviousSimulation) {

        Map<Integer, List<ImmunizationEvent>> immunizationMap = new HashMap<>();
        Map<Integer, Integer> idsMap = new HashMap<>();
        var currentDay = simulationTimer.getDaysPassed();

        mappingLoader.forEach(idsFilename, idsMapping -> idsMap.put(idsMapping.getPdyn2Id(), idsMapping.getPdyn1Id()));
        loader.forEach(filename, importedEvent -> {
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

        board.getEngine().execute(EntityIterator
                .select(selectors.allWithComponents(Household.class))
                .forEach(Household.class, (householdEntity, household) -> {
                    var members = household.getMembers();
                    members.forEach(memberEntity -> {
                        var pdyn2Id = memberEntity.getId();
                        var pdyn1Id = idsMap.getOrDefault(pdyn2Id, -1);

                        if (pdyn1Id == -1) {
                            throw new IllegalStateException("Could not find id mapping for pdyn2Id=" + pdyn2Id);
                        }
                        if (immunizationMap.containsKey(pdyn1Id)) {
                            var age = memberEntity.get(Person.class).getAge();
                            for (ImmunizationEvent event : immunizationMap.get(pdyn1Id)) {
                                event.setDay(
                                        endDayFromDiseaseHistory(event.getLoad(), event.getDiseaseHistory(), age, event.getDay()));
                                agentStateService.addImmunizationEvent(memberEntity, event);
                                status.tick();
                            }
                        }
                    });
                }));
        status.done();
    }

    private Load load(String diseaseLoad, String vaccineLoad) {
        if (vaccineLoad.equals("-1")) {
            switch (diseaseLoad) {
                case "0":
                    return Load.WILD;
                case "1":
                    return Load.ALPHA;
                case "2":
                    return Load.DELTA;
                case "3":
                    return Load.OMICRON;
            }
        } else if (diseaseLoad.equals("-1")) {
            switch (vaccineLoad) {
                case "0":
                    return Load.PFIZER;
                case "1":
                    return Load.BOOSTER;
            }
        }

        throw new IllegalArgumentException("Could not find value for: disease=" + diseaseLoad +
                " vaccine=" + vaccineLoad);
    }

    private int endDayFromDiseaseHistory(Load load, int history, int age, int startDay) {
        var stages = Arrays.stream(Stage.values())
                .sorted(Comparator.comparingInt(Stage::getEncoding).reversed())
                .collect(Collectors.toList());
        for (Stage stage : stages) {
            if (history >= stage.getEncoding()) {
                history -= stage.getEncoding();
                if (stage != Stage.DECEASED && stage != Stage.HEALTHY) {
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
