package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.StaticSelectors;
import pl.edu.icm.trurl.util.Status;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SowingFromAgentId implements SowingStrategy {

    private final InfectedLoaderFromAgentId infectedLoaderFromAgentId;
    private final Board board;
    private final StaticSelectors staticSelectors;
    private final AgentStateService agentStateService;

    @WithFactory
    public SowingFromAgentId(InfectedLoaderFromAgentId infectedLoaderFromAgentId,
                                SowingStrategyProvider provider,
                                Board board,
                                AreaClusteredSelectors areaClusteredSelectors,
                                StaticSelectors staticSelectors, AgentStateService agentStateService) {
        this.infectedLoaderFromAgentId = infectedLoaderFromAgentId;
        this.board = board;
        this.staticSelectors = staticSelectors;
        this.agentStateService = agentStateService;
        provider.registerInitialSowingStrategy(this);
    }

    public void sow() {
        var status = Status.of("infecting people from file: " + infectedLoaderFromAgentId.getSowingFilename(), 10);

        List<InfectedAgentFromCsv> infectedList = null;
        try {
            infectedList = infectedLoaderFromAgentId.readInfected();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        var infectedMap = infectedList.stream().collect(Collectors.toMap(
                InfectedAgentFromCsv::getAgentId,
                Function.identity()
        ));

        AtomicInteger agentId = new AtomicInteger();

        Selector householdSelector = staticSelectors.select(staticSelectors.config().withMandatoryComponents(Household.class).build());

        board.getEngine().execute(EntityIterator.select(householdSelector).forEach(householdEntity -> {
            var household = householdEntity.get(Household.class);
            var members = household.getMembers();
            members.forEach(memberEntity -> {
                var id = agentId.getAndIncrement();
                if (infectedMap.containsKey(id)) {
                    status.tick();
                    agentStateService.infect(memberEntity, Load.WILD);
                    int elapsedDays = infectedMap.get(id).getElapsedDays();
                    Stage stage = Stage.LATENT;
                    if (elapsedDays > 5) {
                        stage = Stage.INFECTIOUS_SYMPTOMATIC;
                        elapsedDays -= 5;
                    }
                    agentStateService.progressToDiseaseStage(memberEntity, stage, elapsedDays);
                }
            });
        }));
        status.done();
    }
}
