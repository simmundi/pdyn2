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

package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.agesex.AgeSex;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.AdministrationAreaType;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.PopulationService;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSources;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.bin.BinPool;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.ArraySelector;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.util.Status;

import java.util.*;

import static it.unimi.dsi.fastutil.ints.IntArrays.shuffle;

public class SowingFromDistribution implements SowingService {

    private final InfectedLoaderFromDistribution loader;
    private final StatsService statsService;
    private final EngineIo board;
    private final CommuneManager communeManager;
    private final Loads loads;
    private final Stages stages;
    private final RandomGenerator randomGenerator;
    private final Random random;
    private final PopulationService populationService;
    private final AgentStateService agentStateService;
    private final DiseaseStageTransitionsService diseaseStageTransitionsService;
    private final TestingService testingService;

    private final Map<String, Integer> teryt = new HashMap<>();
    private final Map<AgeSex, Integer> ageSex = new HashMap<>();
    private final Map<String, Map<AdministrationAreaType, Integer>> types = new HashMap<>();
    private final Map<String, Map<AdministrationAreaType, Boolean>> changedTypes = new HashMap<>();
    private final BinPool<Boolean> symptoms = new BinPool<>();
    private final BinPool<Short> states = new BinPool<>();
    private final Set<String> terytWithWrongType = new HashSet<>();

    @WithFactory
    public SowingFromDistribution(InfectedLoaderFromDistribution loader,
                                  StatsService statsService,
                                  EngineIo board,
                                  Loads loads,
                                  Stages stages,
                                  RandomProvider randomProvider,
                                  CommuneManager communeManager,
                                  PopulationService populationService,
                                  AgentStateService agentStateService,
                                  DiseaseStageTransitionsService diseaseStageTransitionsService,
                                  TestingService testingService) {
        this.statsService = statsService;
        this.loader = loader;
        this.board = board;
        this.loads = loads;
        this.stages = stages;
        this.randomGenerator = randomProvider.getRandomGenerator(InfectedLoaderFromDistribution.class);
        this.communeManager = communeManager;
        this.populationService = populationService;
        this.agentStateService = agentStateService;
        this.random = RandomAdaptor.createAdaptor(randomProvider.getRandomGenerator(SowingFromDistribution.class));
        this.diseaseStageTransitionsService = diseaseStageTransitionsService;
        this.testingService = testingService;
    }

    public void sow() {
        populationService.load();
        loader.forEach(e -> {
            var type = e.getAdministrationAreaType();
            var currentTeryt = e.getTeryt();
            teryt.compute(currentTeryt, (t, v) -> (v == null) ? 1 : v + 1);
            ageSex.compute(AgeSex.fromAgeRangeSex(e.getAgeRange(), e.getSex()), (t, v) -> (v == null) ? 1 : v + 1);
            if (types.containsKey(currentTeryt)) {
                types.get(currentTeryt).compute(type, (t, v) -> (v == null) ? 1 : v + 1);
                changedTypes.get(currentTeryt).put(type, false);
            } else {
                var map = new HashMap<AdministrationAreaType, Integer>();
                map.put(type, 1);
                types.put(currentTeryt, map);
                var changedMap = new HashMap<AdministrationAreaType, Boolean>();
                changedMap.put(type, false);
                changedTypes.put(currentTeryt, changedMap);
            }
            symptoms.add(e.isSymptomatic(), 1);
            states.add(e.getState(), 1);
        });
        var sowingCount = symptoms.getTotalCount();
        var households = board.getEngine().streamDetached()
                .filter(e -> e.get(Household.class) != null)
                .mapToInt(Entity::getId).toArray();
        shuffle(households, random);
        ArraySelector householdsSelector = new ArraySelector(households);
        var status = Status.of("Infecting agents", 100);
        board.getEngine().execute(EntityIterator.select(householdsSelector).forEach(entity -> {
            var members = entity.get(Household.class).getMembers();
            var toBeInfected = (int) (members.size() * 0.5);
            if (toBeInfected == 0) {
                return;
            }
            var location = entity.get(Location.class);
            if (location == null) {
                return;
            }
            var currentTeryt = communeManager
                    .communeAt(KilometerGridCell.fromLocation(location))
                    .getTeryt();
            var fourDigitTeryt = currentTeryt.substring(0, 4);
            var twoDigitTeryt = currentTeryt.substring(0, 2);
            var type = populationService.typeFromLocation(location);

            String chosenTeryt = null;
            if (teryt.containsKey(fourDigitTeryt) && teryt.get(fourDigitTeryt) >= toBeInfected) {
                chosenTeryt = fourDigitTeryt;
            } else if (teryt.containsKey(twoDigitTeryt) && teryt.get(twoDigitTeryt) >= toBeInfected) {
                chosenTeryt = twoDigitTeryt;
            }
            var wrongTypeForTeryt = terytWithWrongType.contains(chosenTeryt);
            if (chosenTeryt != null &&
                    (wrongTypeForTeryt || (types.get(chosenTeryt).containsKey(type) &&
                            types.get(chosenTeryt).get(type) >= toBeInfected))) {

                int canBeInfected = 0;
                Map<AgeSex, Integer> householdAgeSex = new HashMap<>();
                for (Entity member : members) {
                    var person = member.get(Person.class);
                    householdAgeSex.compute(AgeSex.fromAgeSex(person.getAge(), person.getSex()), (a, v) ->
                            (v == null) ? 1 : v + 1);
                }
                for (AgeSex currentAgeSex : householdAgeSex.keySet()) {
                    if (ageSex.containsKey(currentAgeSex)) {
                        canBeInfected += Math.min(ageSex.get(currentAgeSex), householdAgeSex.get(currentAgeSex));
                    }
                }

                if (canBeInfected >= toBeInfected) {
                    teryt.compute(chosenTeryt, (t, v) -> v - toBeInfected);

                    //for teryt with wrong type: declared type is ignored
                    //in most cases these are cities with slightly bigger/smaller population than declared
                    if (!wrongTypeForTeryt) {
                        types.get(chosenTeryt).compute(type, (t, v) -> v - toBeInfected);
                        changedTypes.get(chosenTeryt).put(type, true);
                    }
                    var infected = 0;
                    for (Entity member : members) {
                        var person = member.get(Person.class);
                        var age = person.getAge();
                        var memberAgeSex = AgeSex.fromAgeSex(age, person.getSex());
                        if (ageSex.containsKey(memberAgeSex) && ageSex.get(memberAgeSex) > 0) {
                            var stage = states.sample(randomGenerator.nextDouble()).pick();
                            var durationLatentny = diseaseStageTransitionsService.durationOf(loads.WILD, stages.LATENT, age);
                            var symptomatic = symptoms.sample(randomGenerator.nextDouble()).pick();

                            agentStateService.infect(member, loads.WILD, stage);
                            agentStateService.addSourcesDistribution(member,
                                    new EnumSampleSpace<>(ContextInfectivityClass.class));
                            status.tick();
                            if (stage >= durationLatentny) {
                                testingService.maybeTestAgentOnDay(member, stage - durationLatentny);
                                if (symptomatic) {
                                    agentStateService.progressToDiseaseStage(member,
                                            stages.INFECTIOUS_SYMPTOMATIC,
                                            stage - durationLatentny);
                                    var durationSymptomatic = diseaseStageTransitionsService.durationOf(loads.WILD,
                                            stages.INFECTIOUS_SYMPTOMATIC,
                                            age);
                                    var daysInNextStage = stage - durationLatentny - durationSymptomatic;
                                    if (daysInNextStage >= 0) {
                                        var nextStage = diseaseStageTransitionsService.outcomeOf(stages.INFECTIOUS_SYMPTOMATIC,
                                                member,
                                                loads.WILD,
                                                random.nextDouble());
                                        agentStateService.progressToDiseaseStage(member, nextStage, daysInNextStage);
                                        statsService.tickStageChange(nextStage);
                                    } else {
                                        statsService.tickStageChange(stages.INFECTIOUS_SYMPTOMATIC);
                                    }
                                } else {
                                    agentStateService.progressToDiseaseStage(member,
                                            stages.INFECTIOUS_ASYMPTOMATIC,
                                            stage - durationLatentny);
                                    var durationAsymptomatic = diseaseStageTransitionsService.durationOf(loads.WILD,
                                            stages.INFECTIOUS_ASYMPTOMATIC,
                                            age);
                                    var daysInNextStage = stage - durationLatentny - durationAsymptomatic;
                                    if (daysInNextStage >= 0) {
                                        var nextStage = diseaseStageTransitionsService.outcomeOf(stages.INFECTIOUS_SYMPTOMATIC,
                                                member,
                                                loads.WILD,
                                                random.nextDouble());
                                        agentStateService.progressToDiseaseStage(member, nextStage, daysInNextStage);
                                        statsService.tickStageChange(nextStage);
                                    } else {
                                        statsService.tickStageChange(stages.INFECTIOUS_ASYMPTOMATIC);
                                    }
                                }
                            } else {
                                statsService.tickStageChange(stages.LATENT);
                            }
                            ageSex.compute(memberAgeSex, (a, v) -> v - 1);
                            infected++;
                        }

                        //looking for teryts where no agents were found after 50% progress (wrong type assigned)
                        if (symptoms.getTotalCount() == sowingCount / 2) {
                            for (String teryt : changedTypes.keySet()) {
                                for (AdministrationAreaType areaType : changedTypes.get(teryt).keySet())
                                    if (!changedTypes.get(teryt).get(areaType)) {
                                        terytWithWrongType.add(teryt);
                                    }
                            }
                        }
                        if (infected >= toBeInfected) break;
                    }
                }
            }
        }));
        if (symptoms.getTotalCount() != 0) {
            var infected = sowingCount - symptoms.getTotalCount();
            throw new IllegalStateException("Infected " + infected + " from " + sowingCount + " expected");
        }
        status.done();
    }
}
