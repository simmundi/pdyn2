package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class SimpleImmunizationStrategyTest {
    @Mock
    ImmunizationStrategyProvider provider;
    @Mock
    Immunization immunization;

    @Test
    void getImmunizationCoefficient() {
        //given
        var simpleImmunizationStrategy = new SimpleImmunizationStrategy(provider, 0.3f);
        //execute
        var coef = simpleImmunizationStrategy.getImmunizationCoefficient(immunization,
                ImmunizationStage.LATENTNY,
                Load.WILD,
                0);
        //assert
        assertThat(coef).isEqualTo(0.3f);
    }
}