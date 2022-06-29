package pl.edu.icm.pdyn2.sowing;

import org.junit.jupiter.api.Test;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.trurl.ecs.util.EntityIterator;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

public class VariantSowingIT {
    ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests();

    @Test
    void sowVariant() {
        // given
        var variantSowingService = new VariantSowingService(
                data.agentStateService,
                new RandomProvider(123),
                data.selectors);
        data.allAgents.forEach(a -> data.agentStateService.infect(a, Load.WILD));
        data.session.close();

        // execute
        int TO_SAW = 4;
        data.engine.execute(variantSowingService.sowVariant(Load.ALPHA, TO_SAW));

        // assert
        AtomicInteger allAgentsCounter = new AtomicInteger();
        AtomicInteger agentsWithAlphaCounter = new AtomicInteger();

        data.engine.execute(select(data.selectors.allWithComponents(HealthStatus.class)).forEach(e -> {
            allAgentsCounter.incrementAndGet();
            HealthStatus healthStatus = e.get(HealthStatus.class);
            if (healthStatus.getDiseaseLoad() == Load.ALPHA) {
                agentsWithAlphaCounter.incrementAndGet();
            }
        }));
        assertThat(allAgentsCounter.get()).isGreaterThan(agentsWithAlphaCounter.get());
        assertThat(agentsWithAlphaCounter.get()).isEqualTo(TO_SAW);
    }
}
