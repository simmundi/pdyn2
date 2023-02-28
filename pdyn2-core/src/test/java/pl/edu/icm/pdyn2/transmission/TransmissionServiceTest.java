/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2.transmission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.sampleSpace.SoftEnumSampleSpace;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransmissionServiceTest {
    BasicConfig basicConfig = new BasicConfig();
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
        entityMocker = new EntityMocker(basicConfig, session);
        when(relativeAlphaConfig.getRelativeAlpha(any())).thenReturn(10f);
        transmissionService = new TransmissionService(contextsService,
                relativeAlphaConfig,
                transmissionConfig,
                simulationTimer,
                basicConfig.loads,
                basicConfig.stages,
                immunizationService);
    }

    @Test
    void consideredForInfection() {
        // given
        Entity healthyAgent = entityMocker.entity(ComponentCreator.health(null, basicConfig.stages.HEALTHY));
        Entity sickAgent = entityMocker.entity(ComponentCreator.health(basicConfig.loads.BA2, basicConfig.stages.INFECTIOUS_SYMPTOMATIC));

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
                        ComponentCreator.context(basicConfig.contextTypes.HOUSEHOLD, 10, Map.of(basicConfig.loads.WILD, 1f, basicConfig.loads.BA2, 1f, basicConfig.loads.ALPHA, 3f)),
                        // so in SCHOOL (100 agents), 10f of exposure is equal to impact 1f
                        ComponentCreator.context(basicConfig.contextTypes.SCHOOL, 100, Map.of(basicConfig.loads.DELTA, 10f, basicConfig.loads.BA2, 10f, basicConfig.loads.OMICRON, 80f)),
                        ComponentCreator.context(basicConfig.contextTypes.STREET_10, 100, Map.of(basicConfig.loads.BA45, 100f)),
                        ComponentCreator.context(basicConfig.contextTypes.STREET_20, 100, Map.of(basicConfig.loads.BA45, 50f))));

        // execute
        SoftEnumSampleSpace<Load> exposure = new SoftEnumSampleSpace<>(basicConfig.loads);
        EnumSampleSpace<ContextInfectivityClass> sources = new EnumSampleSpace<>(ContextInfectivityClass.class);
        transmissionService.gatherExposurePerLoadAndContextForAgent(
                exposure,
                sources,
                agent);

        // assert
        assertThat(exposure.getProbability(basicConfig.loads.WILD)).isEqualTo(1f);
        assertThat(exposure.getProbability(basicConfig.loads.BA2)).isEqualTo(2f);
        assertThat(exposure.getProbability(basicConfig.loads.ALPHA)).isEqualTo(3f);
        assertThat(exposure.getProbability(basicConfig.loads.DELTA)).isEqualTo(1f);
        assertThat(exposure.getProbability(basicConfig.loads.OMICRON)).isEqualTo(8f);
        assertThat(exposure.getProbability(basicConfig.loads.BA45)).isEqualTo(15f);
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
        SoftEnumSampleSpace<Load> exposure = new SoftEnumSampleSpace<>(basicConfig.loads);
        exposure.changeOutcome(basicConfig.loads.BA2, 0.1f);
        exposure.changeOutcome(basicConfig.loads.OMICRON, 0.1f);

        // execute
        var loadA = transmissionService.selectLoad(exposure, 0.49);
        var loadB = transmissionService.selectLoad(exposure, 0.51);
        var loadC = transmissionService.selectLoad(exposure, 0.999999999999);

        // assert
        assertThat(loadA).isEqualTo(basicConfig.loads.OMICRON);
        assertThat(loadB).isEqualTo(basicConfig.loads.BA2);
        assertThat(loadC).isEqualTo(basicConfig.loads.BA2);
    }

    @Test
    void adjustProbabilityWithImmunity() {
        // given
        int day = 5;
        Immunization immunization = ComponentCreator.immunization();
        Entity agent = entityMocker.entity(immunization);
        when(simulationTimer.getDaysPassed()).thenReturn(day);
        when(immunizationService.getImmunizationCoefficient(immunization, ImmunizationStage.LATENTNY, basicConfig.loads.WILD, day))
                .thenReturn(0.5f);

        // execute
        var result = transmissionService.adjustProbabilityWithImmunity(0.1, basicConfig.loads.WILD, agent);

        // assert
        assertThat(result).isEqualTo(0.05);
    }
}
