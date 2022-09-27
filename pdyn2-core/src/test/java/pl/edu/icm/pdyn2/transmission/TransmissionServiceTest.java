package pl.edu.icm.pdyn2.transmission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransmissionServiceTest {
    @Mock
    TransmissionConfig transmissionConfig;
    @Mock
    ContextsService contextsService;
    @Mock
    SimulationTimer simulationTimer;
    @Mock
    ImmunizationService immunizationService;
    @Mock
    RelativeAlphaConfig relativeAlphaConfig;
    Session session;
    EntityMocker entityMocker;
    TransmissionService transmissionService;

    @BeforeEach
    void before() {
        entityMocker = new EntityMocker(session);
        when(relativeAlphaConfig.getRelativeAlpha(any())).thenReturn(10f);
        transmissionService = new TransmissionService(contextsService,
                relativeAlphaConfig,
                transmissionConfig,
                simulationTimer,
                immunizationService);
    }

    @Test
    void consideredForInfection() {
        // given
        Entity healthyAgent = entityMocker.entity(ComponentCreator.health(null, Stage.HEALTHY));
        Entity sickAgent = entityMocker.entity(ComponentCreator.health(Load.BA2, Stage.INFECTIOUS_SYMPTOMATIC));

        // execute
        var consideredHealthy = transmissionService.consideredForInfection(healthyAgent);
        var consideredSick = transmissionService.consideredForInfection(sickAgent);

        // assert
        assertThat(consideredHealthy).isTrue();
        assertThat(consideredSick).isFalse();
    }

    @Test
    void gatherExposurePerLoadAndContextForAgent() {
        // given
        Entity agent = entityMocker.entity();
        when(transmissionConfig.getTotalWeightForContextType(any())).thenReturn(1f);

        when(contextsService.findActiveContextsForAgent(agent))
                .thenReturn(Stream.of( // relativeAlpha is 10f, weightForType is always 1f:
                        // so in HOUSEHOLD (10 agents), 1f of exposure is equal to impact 1f
                        ComponentCreator.context(ContextType.HOUSEHOLD, 10, Map.of(Load.WILD, 1f, Load.BA2, 1f, Load.ALPHA, 3f)),
                        // so in SCHOOL (100 agents), 10f of exposure is equal to impact 1f
                        ComponentCreator.context(ContextType.SCHOOL, 100, Map.of(Load.DELTA, 10f, Load.BA2, 10f, Load.OMICRON, 80f)),
                        ComponentCreator.context(ContextType.STREET_10, 100, Map.of(Load.BA45, 100f)),
                        ComponentCreator.context(ContextType.STREET_20, 100, Map.of(Load.BA45, 50f))));

        // execute
        EnumSampleSpace<Load> exposure = new EnumSampleSpace<>(Load.class);
        EnumSampleSpace<ContextInfectivityClass> sources = new EnumSampleSpace<>(ContextInfectivityClass.class);
        transmissionService.gatherExposurePerLoadAndContextForAgent(
                exposure,
                sources,
                agent);

        // assert
        assertThat(exposure.getProbability(Load.WILD)).isEqualTo(1f);
        assertThat(exposure.getProbability(Load.BA2)).isEqualTo(2f);
        assertThat(exposure.getProbability(Load.ALPHA)).isEqualTo(3f);
        assertThat(exposure.getProbability(Load.DELTA)).isEqualTo(1f);
        assertThat(exposure.getProbability(Load.OMICRON)).isEqualTo(8f);
        assertThat(exposure.getProbability(Load.BA45)).isEqualTo(15f);
        assertThat(exposure.sumOfProbabilities()).isEqualTo(30f);

        assertThat(sources.getProbability(ContextInfectivityClass.HOUSEHOLD)).isEqualTo(5f);
        assertThat(sources.getProbability(ContextInfectivityClass.SCHOOL)).isEqualTo(10f);
        assertThat(sources.getProbability(ContextInfectivityClass.STREET)).isEqualTo(15f);
    }

    @Test
    void exposureToProbability() {
        // given
        when(transmissionConfig.getAlpha()).thenReturn(2.047250f);

        // execute
        double metNone = transmissionService.exposureToProbability(0);
        double metOneInThousand = transmissionService.exposureToProbability(1f / 1000f);
        double metMillion = transmissionService.exposureToProbability(1000000f);

        // assert
        assertThat(metNone).isZero();
        assertThat(metOneInThousand).isCloseTo(0, offset(0.01));
        assertThat(metMillion).isOne();
    }

    @Test
    void selectLoad() {
        // given
        EnumSampleSpace<Load> exposure = new EnumSampleSpace<>(Load.class);
        exposure.changeOutcome(Load.BA2, 0.1f);
        exposure.changeOutcome(Load.OMICRON, 0.1f);

        // execute
        var loadA = transmissionService.selectLoad(exposure, 0.49);
        var loadB = transmissionService.selectLoad(exposure, 0.51);
        var loadC = transmissionService.selectLoad(exposure, 0.999999999999);

        // assert
        assertThat(loadA).isEqualTo(Load.OMICRON);
        assertThat(loadB).isEqualTo(Load.BA2);
        assertThat(loadC).isEqualTo(Load.BA2);
    }

    @Test
    void adjustProbabilityWithImmunity() {
        // given
        int day = 5;
        Immunization immunization = ComponentCreator.immunization();
        Entity agent = entityMocker.entity(immunization);
        when(simulationTimer.getDaysPassed()).thenReturn(day);
        when(immunizationService.getImmunizationCoefficient(immunization, ImmunizationStage.LATENTNY, Load.WILD, day))
                .thenReturn(0.5f);

        // execute
        var result = transmissionService.adjustProbabilityWithImmunity(0.1, Load.WILD, agent);

        // assert
        assertThat(result).isEqualTo(0.05);
    }
}
