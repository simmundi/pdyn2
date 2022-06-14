package pl.edu.icm.pdyn2.sowing;

import org.junit.jupiter.api.Test;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

import static org.assertj.core.api.AssertionsForClassTypes.*;

public class VariantSowingIT {
    ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests();

    @Test
    void sowVariant() {
        // given
        var sowingSource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        sowingSource.changeOutcome(ContextInfectivityClass.SOWING, 1.0f);
        var variantSowingService = new VariantSowingService(data.agentStateService, new RandomProvider(123));
        data.allAgents.forEach(a -> data.agentStateService.infect(a, Load.WILD, sowingSource));
        data.session.close();

        // execute
        data.engine.execute(variantSowingService.sowVariant(Load.ALPHA, 4));

        // assert
        assertThat(data.engine.streamDetached()
                .filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getDiseaseLoad().equals(Load.ALPHA)).count()).isEqualTo(4);
    }
}
