package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.Test;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ImmunizationStrategyProviderTest {
    private final ImmunizationStrategyProvider immunizationStrategyProvider = new ImmunizationStrategyProvider();

    @Test
    void getAndRegisterImmunizationStragegy() {
//        given
        var immunizationStrategy = new ImmunizationStrategy() {
            @Override
            public float getImmunizationCoefficient(Immunization immunization,
                                                    ImmunizationStage immunizationStage,
                                                    Load load, int day) {
                return 0;
            }
        };
//        execute
        immunizationStrategyProvider.registerImmunizationStrategy(immunizationStrategy);
        var registeredImmunizationStrategy = immunizationStrategyProvider.getImmunizationStrategy();
//        assert
        assertThat(registeredImmunizationStrategy).isSameAs(immunizationStrategy);
    }

}