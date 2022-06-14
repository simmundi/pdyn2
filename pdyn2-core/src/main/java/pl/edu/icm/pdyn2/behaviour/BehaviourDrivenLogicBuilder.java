package pl.edu.icm.pdyn2.behaviour;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.quarantine.QuarantineService;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Selectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class BehaviourDrivenLogicBuilder {
    private final TravelService travelService;
    private final QuarantineService quarantineService;
    private final AreaClusteredSelectors areaClusteredSelectors;
    private final Selectors selectors;
    private final RandomForChunkProvider randomForChunkProvider;

    @WithFactory
    public BehaviourDrivenLogicBuilder(TravelService travelService,
                                       QuarantineService quarantineService,
                                       AreaClusteredSelectors areaClusteredSelectors,
                                       Selectors selectors,
                                       RandomProvider randomProvider) {
        this.travelService = travelService;
        this.quarantineService = quarantineService;
        this.areaClusteredSelectors = areaClusteredSelectors;
        this.selectors = selectors;
        this.randomForChunkProvider = randomProvider.getRandomForChunkProvider(BehaviourDrivenLogicBuilder.class);
    }

    public EntitySystem buildTravelSystem() {
        return select(selectors.filtered(areaClusteredSelectors.personSelector(), selectors.hasComponents(Behaviour.class)))
                .parallel()
                .forEach(randomForChunkProvider, (random, e) -> {
                    travelService.processTravelLogic(e, e.get(Behaviour.class), random);
                    quarantineService.maybeEndQuarantine(e);
                });
    }
}
