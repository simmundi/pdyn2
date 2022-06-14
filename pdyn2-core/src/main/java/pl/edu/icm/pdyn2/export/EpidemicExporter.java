package pl.edu.icm.pdyn2.export;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.administration.Record;
import pl.edu.icm.pdyn2.model.administration.RecordType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EpidemicExporter {
    private final Board board;
    private final Filesystem filesystem;
    private final String defaultPopulationOutputFilename;
    private final DiseaseStageTransitionsService transitionsService;
    private final Selectors selectors;
    private ExportedImmunizationEventMapper mapper;

    @WithFactory
    public EpidemicExporter(String defaultPopulationOutputFilename,
                            Board board,
                            DiseaseStageTransitionsService transitionsService,
                            Selectors selectors) {
        this.defaultPopulationOutputFilename = defaultPopulationOutputFilename;
        this.board = board;
        this.selectors = selectors;
        this.filesystem = new DefaultFilesystem();
        this.transitionsService = transitionsService;
    }

    public EpidemicExporter(String defaultPopulationOutputFilename,
                            Board board,
                            Filesystem filesystem,
                            DiseaseStageTransitionsService transitionsService,
                            Selectors selectors) {
        this.defaultPopulationOutputFilename = defaultPopulationOutputFilename;
        this.board = board;
        this.filesystem = filesystem;
        this.transitionsService = transitionsService;
        this.selectors = selectors;
    }

    public void exportOrc() {
        Preconditions.checkState(mapper == null, "Mapper is not empty");
        mapper = new ExportedImmunizationEventMapper();
        Store store = new ArrayStore(checkCapacity());
        mapper.configureAndAttach(store);
        AtomicInteger counter = new AtomicInteger(0);

        board.getEngine().execute(EntityIterator
                .select(selectors.allWithComponents(Inhabitant.class))
                .detachEntities()
                .forEach(Inhabitant.class, (e, inhabitant) -> {
                    var immunization = e.get(Immunization.class);
                    if (immunization != null) {
                        var age = e.get(Person.class).getAge();
                        var medicalHistory = e.get(MedicalHistory.class);
                        var events = immunization.getEvents();
                        List<Record> records = null;
                        if (medicalHistory != null) {
                            records = medicalHistory.getRecords();
                        }
                        for (ImmunizationEvent event : events) {
                            var history = event.getDiseaseHistory();
                            var load = event.getLoad();
                            var startDay = startDayFromDiseaseHistory(event.getLoad(), history, age, event.getDay());
                            var possibleTestDay = startDay + transitionsService.durationOf(load, Stage.LATENT, age);

                            var exportedEvent = new ExportedImmunizationEvent();
                            exportedEvent.setId(e.getId());
                            exportedEvent.setStartDay(startDay);
                            exportedEvent.setDiseaseHistory(history);
                            exportedEvent.setBigUniversitySource(event.getBigUniversitySource());
                            exportedEvent.setHouseholdSource(event.getHouseholdSource());
                            exportedEvent.setKindergartenSource(event.getKindergartenSource());
                            exportedEvent.setSchoolSource(event.getSchoolSource());
                            exportedEvent.setSowingSource(event.getSowingSource());
                            exportedEvent.setStreetSource(event.getStreetSource());
                            exportedEvent.setUniversitySource(event.getUniversitySource());
                            exportedEvent.setWorkplaceSource(event.getWorkplaceSource());
                            exportedEvent.setTestedValue(testedValue(load, possibleTestDay, records));
                            exportedEvent.setVaccineLoad(vaccineLoad(load));
                            exportedEvent.setDiseaseLoad(diseaseLoad(load));
                            mapper.ensureCapacity(counter.get());
                            mapper.save(exportedEvent, counter.getAndIncrement());
                        }
                    }
                }));
        try {
            OrcStoreService orcStoreService = new OrcStoreService();
            orcStoreService.write(store, defaultPopulationOutputFilename + "_choroby.orc");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void exportCsv() {
        try {
            OutputStream outputStream = this.filesystem.openForWriting(new File(defaultPopulationOutputFilename + "_choroby.csv"));
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
            bufferedWriter.write("id,dzien_zakazenia,householdSource,workplaceSource,kindergartenSource," +
                    "schoolSource,universitySource,bigUniversitySource,streetSource,sowingSource,odmiana_wirusa," +
                    "odmiana_szczepionki,historia_stanow,test\n");
            board.getEngine().execute(EntityIterator
                    .select(selectors.allWithComponents(Inhabitant.class))
                    .detachEntities()
                    .forEach(Inhabitant.class, (e, inhabitant) -> {
                        var immunization = e.get(Immunization.class);
                        if (immunization != null) {
                            var age = e.get(Person.class).getAge();
                            var medicalHistory = e.get(MedicalHistory.class);
                            var events = immunization.getEvents();
                            List<Record> records = null;
                            if (medicalHistory != null) {
                                records = medicalHistory.getRecords();
                            }
                            for (ImmunizationEvent event : events) {
                                try {
                                    var history = event.getDiseaseHistory();
                                    var load = event.getLoad();
                                    var startDay = startDayFromDiseaseHistory(event.getLoad(), history, age, event.getDay());
                                    var possibleTestDay = startDay + transitionsService.durationOf(load, Stage.LATENT, age);
                                    bufferedWriter.write(e.getId() + ",");
                                    bufferedWriter.write(startDay + ",");
                                    bufferedWriter.write(event.getHouseholdSource() + ",");
                                    bufferedWriter.write(event.getWorkplaceSource() + ",");
                                    bufferedWriter.write(event.getKindergartenSource() + ",");
                                    bufferedWriter.write(event.getSchoolSource() + ",");
                                    bufferedWriter.write(event.getUniversitySource() + ",");
                                    bufferedWriter.write(event.getBigUniversitySource() + ",");
                                    bufferedWriter.write(event.getStreetSource() + ",");
                                    bufferedWriter.write(event.getSowingSource() + ",");
                                    bufferedWriter.write(diseaseLoad(load) + ",");
                                    bufferedWriter.write(vaccineLoad(load) + ",");
                                    bufferedWriter.write(history + ",");
                                    bufferedWriter.write(testedValue(load, possibleTestDay, records));
                                    bufferedWriter.write("\n");
                                } catch (IOException exception) {
                                    throw new IllegalStateException(exception);
                                }
                            }
                        }
                    }));
            bufferedWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String testedValue(Load load, int possibleTestDay, List<Record> records) {
        if (records == null) {
            return "0";
        }
        if (load.classification == LoadClassification.VACCINE) {
            return "-1";
        } else {
            for (Record record : records) {
                if (record.getType() == RecordType.POSITIVE_TEST && record.getDay() == possibleTestDay) {
                    return "1";
                }
            }
            return "0";
        }
    }

    private String diseaseLoad(Load load) {
        switch (load) {
            case WILD:
                return "0";
            case ALPHA:
                return "1";
            case DELTA:
                return "2";
            case OMICRON:
                return "3";
            case ASTRA:
            case PFIZER:
            case MODERNA:
            case BOOSTER:
                return "-1";
            default:
                throw new IllegalArgumentException("Could not find value for: " + load);
        }
    }

    private String vaccineLoad(Load load) {
        switch (load) {
            case WILD:
            case ALPHA:
            case DELTA:
            case OMICRON:
                return "-1";
            case ASTRA:
            case PFIZER:
            case MODERNA:
                return "0";
            case BOOSTER:
                return "1";
            default:
                throw new IllegalArgumentException("Could not find value for: " + load);
        }
    }

    private int startDayFromDiseaseHistory(Load load, int history, int age, int endDay) {
        var stages = Arrays.stream(Stage.values())
                .sorted(Comparator.comparingInt(Stage::getEncoding).reversed())
                .collect(Collectors.toList());
        for (Stage stage : stages) {
            if (history >= stage.getEncoding()) {
                history -= stage.getEncoding();
                if (stage != Stage.DECEASED && stage != Stage.HEALTHY) {
                    endDay -= transitionsService.durationOf(load, stage, age);
                }
            }
        }
        if (history != 0) {
            throw new IllegalStateException("Could not process disease history");
        }
        return endDay;
    }

    private int checkCapacity() {
        AtomicInteger counter = new AtomicInteger(0);
        board.getEngine().execute(EntityIterator
                .select(selectors.allWithComponents(Inhabitant.class))
                .detachEntities()
                .forEach(Inhabitant.class, (e, inhabitant) -> {
                    var immunization = e.get(Immunization.class);
                    if (immunization != null) {
                        counter.addAndGet(immunization.getEvents().size());
                    }
                }));
        return counter.get();
    }
}
