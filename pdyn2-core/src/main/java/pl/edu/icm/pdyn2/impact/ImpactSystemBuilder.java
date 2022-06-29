package pl.edu.icm.pdyn2.impact;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.EntityIterator;

public class ImpactSystemBuilder {
    private final AgentImpactService agentImpactService;
    private final AreaClusteredSelectors areaClusteredSelectors;

    @WithFactory
    public ImpactSystemBuilder(AgentImpactService agentImpactService,
                               AreaClusteredSelectors areaClusteredSelectors) {
        this.agentImpactService = agentImpactService;
        this.areaClusteredSelectors = areaClusteredSelectors;
    }

    public EntitySystem buildImpactSystem() {
        return EntityIterator.select(areaClusteredSelectors.personSelector())
                .parallel()
                .forEach(e -> {
                    agentImpactService.updateImpact(e);
                });
    }
}
