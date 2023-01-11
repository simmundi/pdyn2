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
import pl.edu.icm.pdyn2.model.immunization.*;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            bufferedWriter.write("id,dzien_zakazenia,odmiana_wirusa," +
                    "odmiana_szczepionki,historia_stanow,test,x,y,wiek,workplaceInfluence," +
                    "kindergartenInfluence,schoolInfluence,universityInfluence,streetInfluence," +
                    "bigUniversityInfluence,householdInfluence\n");
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
                            var sources = e.get(ImmunizationSources.class);
List<ImmunizationSource> sourceList = sources != null ? sources.getImmunizationSources() : new ArrayList<>();
List<Record> records = medicalHistory != null ? medicalHistory.getRecords() : null;
                            var sourceNumber = 0;
                            for (ImmunizationEvent event : events) {
                                try {
                                    var history = event.getDiseaseHistory();
                                    var load = event.getLoad();
                                    var startDay = startDayFromDiseaseHistory(event.getLoad(), history, age, event.getDay());
                                    var possibleTestDay = possibleTestedDay(startDay, load, age);
                                    var source = new ImmunizationSource();
                                    if (load.classification == LoadClassification.VIRUS) {
                                        source = sourceList.get(sourceNumber);
                                        sourceNumber++;
                                    }
                                    bufferedWriter.write(e.getId() + ",");
                                    bufferedWriter.write(startDay + ",");
                                    bufferedWriter.write(diseaseLoad(load) + ",");
                                    bufferedWriter.write(vaccineLoad(load) + ",");
                                    bufferedWriter.write(history + ",");
                                    bufferedWriter.write(testedValue(load, possibleTestDay, records) + ",");
                                    bufferedWriter.write(cell.getLegacyPdynCol() + ",");
                                    bufferedWriter.write(cell.getLegacyPdynRow() + ",");
                                    bufferedWriter.write(age + ",");
                                    bufferedWriter.write(source.getWorkplaceInfluence() + ",");
                                    bufferedWriter.write(source.getKindergartenInfluence() + ",");
                                    bufferedWriter.write(source.getSchoolInfluence() + ",");
                                    bufferedWriter.write(source.getUniversityInfluence() + ",");
                                    bufferedWriter.write(source.getStreetInfluence() + ",");
                                    bufferedWriter.write(source.getBigUniversityInfluence() + ",");
                                    bufferedWriter.write(Float.toString(source.getHouseholdInfluence()));
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

    private int possibleTestedDay(int startDay, Load load, int age) {
        if (load.classification == LoadClassification.VACCINE) {
            return 0;
        } else return startDay + transitionsService.durationOf(load, Stage.LATENT, age);

    }

    private int startDayFromDiseaseHistory(Load load, int history, int age, int endDay) {
        if (load.classification == LoadClassification.VACCINE) {
            return endDay;
        }
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
