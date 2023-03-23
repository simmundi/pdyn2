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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.util.SoftEnumTable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 * A helper class for use by the DiseaseStageTransitionService.
 * Contains a parser for the state-time-transition tables.
 */
public class LoadDiseaseStageTransitions {
    private final Load load;
    private final SoftEnumTable<AgeRange, Stage, TransitionOracle> stateTimeTransitions;

    private final AgeRanges ageRanges;
    private final OutcomeModifier outcomeModifier;

    public LoadDiseaseStageTransitions(String infectionTransitionsFilename,
                                       WorkDir workDir,
                                       Stages stages,
                                       AgeRanges ageRanges,
                                       Load load, OutcomeModifier outcomeModifier) {
        this.ageRanges = ageRanges;
        this.outcomeModifier = outcomeModifier;
        stateTimeTransitions = new SoftEnumTable<>(ageRanges, stages);
        this.load = load;
        try (InputStream stream = workDir.openForReading(new File(infectionTransitionsFilename))) {
            Scanner scanner = new Scanner(stream);
            scanner.useLocale(Locale.ENGLISH);
            while (scanner.hasNext()) { // table for age group
                scanner.nextLine(); // header
                ArrayList<TransitionOracle> transitions = new ArrayList<>();
                for (Stage stage : stages.values()) { // rows (source states)
                    TransitionOracle transitionOracle = new TransitionOracle(stages);
                    transitionOracle.setStage(stage);
                    transitions.add(transitionOracle);
                    scanner.next();
                    for (Stage target : stages.values()) { // columns (target states)
                        if (target == stage) {
                            transitionOracle.setDuration(scanner.nextInt());
                        } else if (scanner.hasNextFloat()) {
                            float probability = scanner.nextFloat();
                            transitionOracle.setProbableOutcome(probability, target);
                        } else {
                            String x = scanner.next(); // should be x
                        }
                    }
                    var normalized = transitionOracle.getOutcomes().isNormalized();
                    if (!normalized && stage != stages.DECEASED && stage != stages.HEALTHY) {
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

                for (TransitionOracle transition : transitions) {
                    stateTimeTransitions.put(ageRange, transition.getStage(), transition);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int durationOf(Stage stage, int age) {
        return stateTimeTransitions.get(ageRanges.of(age), stage).getDuration();
    }

    public Stage outcomeOf(Stage stage,
                           Entity person,
                           double random) {
        return getPossibleTransitions(stage, person).sample(random);
    }

    public SoftEnumDiscretePDF<Stage> getPossibleTransitions(Stage stage,
                                                             Entity person) {
        var age = ageRanges.of(person.get(Person.class).getAge());
        var sampleSpace = stateTimeTransitions.get(age, stage).getOutcomes();

        outcomeModifier.modifyOutcomes(sampleSpace, load, person);
        return sampleSpace;
    }
}
