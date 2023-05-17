 /*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.progression.lookup.LookupTransitionsProvider;
import pl.edu.icm.pdyn2.progression.lookup.TransitionDescriptor;
import pl.edu.icm.trurl.util.Quadruple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class Covid19StageTransitionsProvider implements LookupTransitionsProvider {
    private final String wildStageTransitionsFilename;
    private final String alphaStageTransitionsFilename;
    private final String deltaStageTransitionsFilename;
    private final String omicronStageTransitionsFilename;
    private final String ba2StageTransitionsFilename;
    private final Loads loads;
    private final WorkDir workDir;
    private final Stages stages;
    private final AgeRanges ageRanges;
    private final Stage DECEASED;
    private final Stage HEALTHY;


    @WithFactory
    public Covid19StageTransitionsProvider(String wildStageTransitionsFilename,
                                           String alphaStageTransitionsFilename,
                                           String deltaStageTransitionsFilename,
                                           String omicronStageTransitionsFilename,
                                           String ba2StageTransitionsFilename,
                                           Loads loads,
                                           WorkDir workDir,
                                           Stages stages,
                                           AgeRanges ageRanges) {
        this.wildStageTransitionsFilename = wildStageTransitionsFilename;
        this.alphaStageTransitionsFilename = alphaStageTransitionsFilename;
        this.deltaStageTransitionsFilename = deltaStageTransitionsFilename;
        this.omicronStageTransitionsFilename = omicronStageTransitionsFilename;
        this.ba2StageTransitionsFilename = ba2StageTransitionsFilename;
        this.loads = loads;
        this.workDir = workDir;
        this.stages = stages;
        this.ageRanges = ageRanges;

        this.DECEASED = stages.getByName("DECEASED");
        this.HEALTHY = stages.getByName("HEALTHY");
    }

    @Override
    public void readTransitions(Consumer<Quadruple<Load, AgeRange, Stage, TransitionDescriptor>> consumer) {
        readFromFile(wildStageTransitionsFilename, loads.getByName("WILD"), consumer);
        readFromFile(alphaStageTransitionsFilename, loads.getByName("ALPHA"), consumer);
        readFromFile(deltaStageTransitionsFilename, loads.getByName("DELTA"), consumer);
        readFromFile(omicronStageTransitionsFilename, loads.getByName("OMICRON"), consumer);
        readFromFile(ba2StageTransitionsFilename, loads.getByName("BA2"), consumer);    }

    public void readFromFile(String infectionTransitionsFilename, Load load, Consumer<Quadruple<Load, AgeRange, Stage, TransitionDescriptor>> consumer) {
        try (InputStream stream = workDir.openForReading(new File(infectionTransitionsFilename))) {
            Scanner scanner = new Scanner(stream);
            scanner.useLocale(Locale.ENGLISH);
            while (scanner.hasNext()) { // table for age group
                scanner.nextLine(); // header
                Map<Stage, TransitionDescriptor> transitions = new HashMap<>();
                for (Stage stage : stages.values()) { // rows (source states)
                    TransitionDescriptor transitionDescriptor = new TransitionDescriptor(stages);
                    transitions.put(stage, transitionDescriptor);
                    scanner.next();
                    for (Stage target : stages.values()) { // columns (target states)
                        if (target == stage) {
                            transitionDescriptor.setDuration(scanner.nextInt());
                        } else if (scanner.hasNextFloat()) {
                            float probability = scanner.nextFloat();
                            transitionDescriptor.setProbableOutcome(probability, target);
                        } else {
                            String x = scanner.next(); // should be x
                        }
                    }
                    var normalized = transitionDescriptor.getOutcomes().isNormalized();
                    if (!normalized && stage != DECEASED && stage != HEALTHY) {
                        throw new IllegalStateException("Probabilities for stage " + stage + " do not sum up to 1.0");
                    }

                }
                scanner.next(); // #
                int ageA = scanner.nextInt();
                scanner.next(); // -
                int ageB = scanner.nextInt();

                int ageFrom = Math.min(ageA, ageB);
                int ageTo = Math.max(ageA, ageB);

                AgeRange ageRange = ageRanges.ofRange(ageFrom, ageTo);
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                for (Map.Entry<Stage, TransitionDescriptor> transition : transitions.entrySet()) {
                    consumer.accept(Quadruple.of(load, ageRange, transition.getKey(), transition.getValue()));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
