package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmunizationStrategyFromPdyn1RewrittenTest {

    @Mock
    Immunization immunization1;
    @Mock
    Immunization immunization2;
    ImmunizationEvent immunizationEvent1 = new ImmunizationEvent();
    ImmunizationEvent immunizationEvent2 = new ImmunizationEvent();
    private final ImmunizationStrategyProvider provider = new ImmunizationStrategyProvider();

    @BeforeEach
    void setup() {
        immunizationEvent1.setLoad(Load.ALPHA);
        immunizationEvent1.setDay(1);
        immunizationEvent2.setLoad(Load.BA45);
        immunizationEvent2.setDay(59);
        when(immunization1.getEvents()).thenReturn(List.of(immunizationEvent1, immunizationEvent2));
        when(immunization2.getEvents()).thenReturn(List.of(immunizationEvent1));
    }

    @Test
    void getImmunizationCoefficient() {
        //given
        var immunizationStrategy = new ImmunizationStrategyFromPdyn1Rewritten(provider);
        //execute
        var coef1 = immunizationStrategy.getImmunizationCoefficient(immunization1, ImmunizationStage.LATENTNY, Load.OMICRON, 73);
        var coef2 = immunizationStrategy.getImmunizationCoefficient(immunization2, ImmunizationStage.LATENTNY, Load.OMICRON, 73);
        //assert
        assertThat(coef1).isEqualTo(0.9f);
        assertThat(coef2).isEqualTo(0.76f);
        assertThat(provider.getImmunizationStrategy()).isEqualTo(immunizationStrategy);
    }
}