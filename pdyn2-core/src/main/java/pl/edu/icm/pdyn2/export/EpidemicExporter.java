package pl.edu.icm.pdyn2.export;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.administration.Record;
import pl.edu.icm.pdyn2.model.administration.RecordType;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EpidemicExporter {
    private final Board board;
    private final WorkDir workDir;
    private final String diseaseExportFilename;
    private final DiseaseStageTransitionsService transitionsService;
    private final Selectors selectors;


    @WithFactory
    public EpidemicExporter(String diseaseExportFilename,
                            Board board,
                            WorkDir workDir,
                            DiseaseStageTransitionsService transitionsService,
                            Selectors selectors) {
        this.diseaseExportFilename = diseaseExportFilename;
        this.board = board;
        this.workDir = workDir;
        this.transitionsService = transitionsService;
        this.selectors = selectors;
    }

    public void export() {
        try {
            OutputStream outputStream = this.workDir.openForWriting(new File(diseaseExportFilename));
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
            bufferedWriter.write("id,dzien_zakazenia,miejsce_zakazenia,odmiana_wirusa," +
                    "odmiana_szczepionki,historia_stanow,test,x,y,wiek\n");
            board.getEngine().execute(EntityIterator
                    .select(selectors.allWithComponents(Inhabitant.class))
                    .detachEntities()
                    .forEach(Inhabitant.class, (e, inhabitant) -> {
                        var immunization = e.get(Immunization.class);
                        KilometerGridCell cell = null;
                        var location = inhabitant.getHomeContext().get(Location.class);
                        if (location != null) {
                            cell = KilometerGridCell.fromLocation(location);
                        } else {
                            cell = KilometerGridCell.fromLegacyPdynCoordinates(0, 0);
                        }

                        var age = e.get(Person.class).getAge();
                        if (immunization != null) {
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
                                    bufferedWriter.write(infectionContext(ContextInfectivityClass.HOUSEHOLD) + ",");
                                    bufferedWriter.write(diseaseLoad(load) + ",");
                                    bufferedWriter.write(vaccineLoad(load) + ",");
                                    bufferedWriter.write(history + ",");
                                    bufferedWriter.write(testedValue(load, possibleTestDay, records) + ",");
                                    bufferedWriter.write(cell.getLegacyPdynCol() + ",");
                                    bufferedWriter.write(cell.getLegacyPdynRow() + ",");
                                    bufferedWriter.write(Integer.toString(age));
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

    private String infectionContext(ContextInfectivityClass contextType) {
        switch (contextType) {
            case HOUSEHOLD:
                return "0";
            case SCHOOL:
                return "2";
            case UNIVERSITY:
                return "3";
            case BIG_UNIVERSITY:
                return "4";
            case WORKPLACE:
                return "5";
            case STREET:
                return "7";
            default:
                throw new IllegalArgumentException("Could not find context infectivity type for: " + contextType);
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
}
