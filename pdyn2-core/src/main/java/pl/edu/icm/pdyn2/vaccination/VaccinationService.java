package pl.edu.icm.pdyn2.vaccination;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomAdaptor;
import pl.edu.icm.board.geography.commune.HouseholdsInCommuneAccessor;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.util.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class VaccinationService {
    private final AgentStateService agentStateService;
    private final Random random;
    private final HouseholdsInCommuneAccessor householdsInCommuneAccessor;
    private final StatsService statsService;

    @WithFactory
    public VaccinationService(AgentStateService agentStateService, RandomProvider randomProvider, HouseholdsInCommuneAccessor householdsInCommuneAccessor, StatsService statsService) {
        this.agentStateService = agentStateService;
        this.random = RandomAdaptor.createAdaptor(randomProvider.getRandomGenerator(VaccinationService.class));
        this.householdsInCommuneAccessor = householdsInCommuneAccessor;
        this.statsService = statsService;
    }

    public void vaccinate(Session session, ImmunizationEvent vaccinationEvent, int count, Collection<String> teryts, Predicate<Entity> entityPredicate, Status status) {
        var eligibleAgents = householdsInCommuneAccessor.getHouseholdIdsForTeryts(teryts)
                .mapToObj(session::getEntity)
                .flatMap(e -> e.get(Household.class).getMembers().stream())
                .filter(entityPredicate)
                .collect(Collectors.toCollection(ArrayList::new));
        if (eligibleAgents.size() < count) {
            status.problem("only " + eligibleAgents.size() + " eligible for variant sowing agents in teryts: "
                    + teryts.stream().map(s -> s + "...").collect(Collectors.toList())
                    + "!");
        } else {
            Collections.shuffle(eligibleAgents, random);
        }
        IntStream.range(0, min(eligibleAgents.size(), count))
                .peek(unused -> statsService.tickVaccinated())
                .forEach(i -> agentStateService.vaccinate(eligibleAgents.get(i), vaccinationEvent));
    }

}
