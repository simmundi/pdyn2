package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmunizationServiceTest {
    @Mock
    ImmunizationStrategyProvider mockprovider;
    @Mock
    ImmunizationStrategy immunizationStrategy;

    @BeforeEach
    void before() {
        when(mockprovider.getImmunizationStrategy()).thenReturn(immunizationStrategy);
        when(immunizationStrategy.getImmunizationCoefficient(any(), any(), any(), anyInt())).thenReturn(0.5f);
    }

    @Test
    void getImmunizationCoefficient() {
        //given
        var immunizationService = new ImmunizationService(mockprovider);
        //execute
        var coef = immunizationService.getImmunizationCoefficient(new Immunization(),
                ImmunizationStage.LATENTNY,
                Load.WILD,
                0);
        //assert
        assertThat(coef).isEqualTo(0.5f);
    }
}