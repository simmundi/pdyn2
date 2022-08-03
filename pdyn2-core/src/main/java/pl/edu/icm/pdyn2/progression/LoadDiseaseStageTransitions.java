package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.util.EnumTable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class LoadDiseaseStageTransitions {
    private final Load load;
    private final EnumTable<AgeRange, Stage, TransitionOracle> stateTimeTransitions
            = new EnumTable<>(AgeRange.class, Stage.class);
    private final ImmunizationService immunizationService;
    private final SimulationTimer simulationTimer;

    public LoadDiseaseStageTransitions(String infectionTransitionsFilename,
                                       ImmunizationService immunizationService,
                                       SimulationTimer simulationTimer,
                                       WorkDir workDir,
                                       Load load) {
        this.immunizationService = immunizationService;
        this.simulationTimer = simulationTimer;
        this.load = load;
        try (InputStream stream = workDir.openForReading(new File(infectionTransitionsFilename))) {
            Scanner scanner = new Scanner(stream);
            scanner.useLocale(Locale.ENGLISH);
            while (scanner.hasNext()) { // table for age group
                scanner.nextLine(); // header
                ArrayList<TransitionOracle> transitions = new ArrayList<>();
                for (Stage stage : Stage.values()) { // rows (source states)
                    TransitionOracle transitionOracle = new TransitionOracle();
                    transitionOracle.setStage(stage);
                    transitions.add(transitionOracle);
                    scanner.next();
                    for (Stage target : Stage.values()) { // columns (target states)
                        if (target == stage) {
                            transitionOracle.setDuration(scanner.nextInt());
                        } else if (scanner.hasNextFloat()) {
                            float probability = scanner.nextFloat();
                            transitionOracle.addProbableOutcome(probability, target);
                        } else {
                            String x = scanner.next(); // should be x
                        }
                    }
                    var normalized = transitionOracle.getOutcomes().isNormalized();
                    if (!normalized && stage != Stage.DECEASED && stage != Stage.HEALTHY) {
                        throw new IllegalStateException("Probabilities for stage " + stage + " do not sum up to 1.0");
                    }
                }
                scanner.next(); // #
                int ageA = scanner.nextInt();
                scanner.next(); // -
                int ageB = scanner.nextInt();

                int ageFrom = Math.min(ageA, ageB);
                int ageTo = Math.max(ageA, ageB);

                AgeRange ageRange = AgeRange.ofRange(ageFrom, ageTo);
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
        return stateTimeTransitions.get(AgeRange.of(age), stage).getDuration();
    }

    public Stage outcomeOf(Stage stage,
                           Entity person,
                           double random) {
        return getPossibleTransitions(stage, person).sample(random);
    }

    public EnumSampleSpace<Stage> getPossibleTransitions(Stage stage,
                                                         Entity person) {
        var age = AgeRange.of(person.get(Person.class).getAge());
        var sampleSpace = stateTimeTransitions.get(age, stage).getOutcomes();
        var currentDay = simulationTimer.getDaysPassed();
        var immunization = person.get(Immunization.class);

        if (sampleSpace.hasNonZeroProbability(Stage.INFECTIOUS_SYMPTOMATIC)) {
            var sigmaObjawowy = immunizationService.getImmunizationCoefficient(immunization,
                    ImmunizationStage.OBJAWOWY,
                    load,
                    currentDay);
            sampleSpace.changeTwoOutcomes(Stage.INFECTIOUS_SYMPTOMATIC,
                    1 - sigmaObjawowy,
                    Stage.INFECTIOUS_ASYMPTOMATIC);
        } else {
            if (sampleSpace.hasNonZeroProbability(Stage.HOSPITALIZED_NO_ICU)) {
                var sigmaBezOiom = immunizationService.getImmunizationCoefficient(immunization,
                        ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM,
                        load,
                        currentDay);
                sampleSpace.changeTwoOutcomes(Stage.HOSPITALIZED_NO_ICU,
                        1 - sigmaBezOiom,
                        Stage.HEALTHY);
            }
            if (sampleSpace.hasNonZeroProbability(Stage.HOSPITALIZED_PRE_ICU)) {
                var sigmaPrzedOiom = immunizationService.getImmunizationCoefficient(immunization,
                        ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM,
                        load,
                        currentDay);
                sampleSpace.changeTwoOutcomes(Stage.HOSPITALIZED_PRE_ICU,
                        1 - sigmaPrzedOiom,
                        Stage.HEALTHY);
            }
        }
        return sampleSpace;
    }

}
