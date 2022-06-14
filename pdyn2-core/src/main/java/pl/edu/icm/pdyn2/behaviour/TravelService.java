package pl.edu.icm.pdyn2.behaviour;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;

public class TravelService {
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final AgentStateService agentStateService;
    private final TravelConfig travelConfig;
    private RandomAccessSelector households;

    @WithFactory
    public TravelService(AreaClusteredSelectors areaClusteredSelectors, AgentStateService agentStateService,
                         TravelConfig travelConfig) {
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.agentStateService = agentStateService;
        this.travelConfig = travelConfig;
    }

    public void processTravelLogic(Entity e, Behaviour behaviour, RandomGenerator randomGenerator) {

        switch (behaviour.getType()) {
            case ROUTINE:
                if (randomGenerator.nextFloat() < travelConfig.getProbabilityOfTravel()) {
                    Person person = e.get(Person.class);
                    if (person.getAge() >= 17) {
                        int targetHouseholdId = getHouseholdSelector().getInt(randomGenerator.nextFloat());
                        agentStateService.beginTravel(e, e.getSession().getEntity(targetHouseholdId));
                    }
                }
                break;
            case PRIVATE_TRAVEL:
                if (randomGenerator.nextFloat() < travelConfig.getProbabilityOfEndingTravel()) {
                    agentStateService.endTravel(e);
                }
                break;
            default:
                // travel does not apply
        }
    }

    private RandomAccessSelector getHouseholdSelector() {
        if (this.households == null) {
            this.households = areaClusteredSelectors.householdSelector();
        }
        return this.households;
    }
}
