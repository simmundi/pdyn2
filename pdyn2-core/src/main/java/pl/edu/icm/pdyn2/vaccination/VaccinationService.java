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
import org.apache.commons.math3.random.RandomAdaptor;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.index.CommuneClusteredSelectors;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class VaccinationService {
    private final AgentStateService agentStateService;
    private final Random random;
    private final CommuneClusteredSelectors communeClusteredSelectors;
    private final Selectors selectors;
    private final StatsService statsService;

    @WithFactory
    public VaccinationService(AgentStateService agentStateService, RandomProvider randomProvider, CommuneClusteredSelectors communeClusteredSelectors, Selectors selectors, StatsService statsService) {
        this.agentStateService = agentStateService;
        this.random = RandomAdaptor.createAdaptor(randomProvider.getRandomGenerator(VaccinationService.class));
        this.communeClusteredSelectors = communeClusteredSelectors;
        this.selectors = selectors;
        this.statsService = statsService;
    }
    public <M, K, L, W> void vaccinate(Session session,
                                       ImmunizationEvent vaccinationEvent,
                                       int count,
                                       Collection<String> teryts,
                                       Status status,
                                       Class<M> mClass, Predicate<M> mPredicate, boolean mAcceptIfAbsent,
                                       Class<K> kClass, Predicate<K> kPredicate, boolean kAcceptIfAbsent,
                                       Class<L> lClass, Predicate<L> lPredicate, boolean lAcceptIfAbsent,
                                       Class<W> wClass, Predicate<W> wPredicate, boolean wAcceptIfAbsent) {

        List<Entity> eligibleAgents = selectors.filtered(communeClusteredSelectors.personInTerytSelector(teryts),
                        mClass, mPredicate, mAcceptIfAbsent,
                        kClass, kPredicate, kAcceptIfAbsent,
                        lClass, lPredicate, lAcceptIfAbsent,
                        wClass, wPredicate, wAcceptIfAbsent)
                .chunks()
                .flatMapToInt(Chunk::ids)
                .mapToObj(session::getEntity)
                .collect(Collectors.toCollection(ArrayList::new));
        actuallyVaccinate(vaccinationEvent, count, teryts, status, eligibleAgents);
    }


    private void actuallyVaccinate(ImmunizationEvent vaccinationEvent, int count, Collection<String> teryts, Status status, List<Entity> eligibleAgents) {
        if (eligibleAgents.size() < count)
            status.problem("only " + eligibleAgents.size() + " eligible for vaccination agents in teryts: " + teryts.stream().map(s -> s + "...").collect(Collectors.toList()) + "!");
        else
            Collections.shuffle(eligibleAgents, random);
        IntStream.range(0, min(eligibleAgents.size(), count))
                .peek(unused -> statsService.tickVaccinated())
                .forEach(i -> agentStateService.vaccinate(eligibleAgents.get(i), vaccinationEvent));
    }

}
