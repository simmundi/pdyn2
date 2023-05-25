/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.edu.icm.pdyn2.ComponentCreator.context;
import static pl.edu.icm.pdyn2.ComponentCreator.health;

@ExtendWith(MockitoExtension.class)
class TransmissionVisitorTest {
    private final BasicConfig basicConfig = new BasicConfig();
    @Mock
    private Session session;
    private final MockRandomProvider randomProvider = new MockRandomProvider();
    private EntityMocker entityMocker;
    @Mock
    private ContextsService contextsService;
    private TransmissionVisitor transmissionVisitor;
    @Mock
    private AgentStateService agentStateService;
    @Mock
    private StatsService statsService;
    @Mock
    private RelativeAlphaConfig relativeAlphaConfig;
    @Mock
    private TransmissionConfig transmissionConfig;
    @Mock
    private SimulationClock simulationClock;

    @BeforeEach
    void setUp() {
        entityMocker = new EntityMocker(basicConfig, session);
        when(transmissionConfig.getTotalWeightForContextType(basicConfig.contextTypes.HOUSEHOLD)).thenReturn(1f);
        when(transmissionConfig.getTotalWeightForContextType(basicConfig.contextTypes.SCHOOL)).thenReturn(.5f);
        when(transmissionConfig.getTotalWeightForContextType(basicConfig.contextTypes.WORKPLACE)).thenReturn(1f);
        when(transmissionConfig.getAlpha()).thenReturn(1f);

        when(relativeAlphaConfig.getRelativeAlpha(any())).thenReturn(1000f);
        when(relativeAlphaConfig.getRelativeAlpha(basicConfig.BA2)).thenReturn(1f);
        when(relativeAlphaConfig.getRelativeAlpha(basicConfig.BA1)).thenReturn(2f);

        TransmissionService transmissionService = new TransmissionService(contextsService,
                relativeAlphaConfig,
                transmissionConfig,
                simulationClock,
                basicConfig.loads,
                basicConfig.stages,
                basicConfig.HEALTHY.name(),
                basicConfig.immunizationStrategy);

        transmissionVisitor = new TransmissionVisitor(transmissionService,
                agentStateService,
                statsService,
                basicConfig.loads,
                basicConfig.stages,
                basicConfig.contextInfectivityClasses,
                basicConfig.HEALTHY.name());
    }

    @Test
    void visit() {
        // given

        Entity agent = entityMocker.entity(health(basicConfig.WILD, basicConfig.HEALTHY));
        when(contextsService.findActiveContextsForAgent(agent)).then(unused -> Stream.of(
                context(
                        basicConfig.contextTypes.HOUSEHOLD, 10,
                        Map.of(basicConfig.BA1, 0f, basicConfig.BA2, 3f)
                ),
                context(
                        basicConfig.contextTypes.SCHOOL, 100,
                        Map.of(basicConfig.BA1, 0f, basicConfig.BA2, 1f)
                ),
                context(
                        basicConfig.contextTypes.WORKPLACE, 7,
                        Map.of(basicConfig.BA1, 1f, basicConfig.BA2, 3f)
                )));

        when(randomProvider.getRandomGenerator().nextDouble())
                .thenReturn(0.0).thenReturn(0.17915835) //BA1
                .thenReturn(0.17915837).thenReturn(0.6391473) //BA2
                .thenReturn(0.6391475).thenReturn(0.9999999); //NOT INFECTED

        // execute
        for (int i = 0; i < 6; i++) {
            transmissionVisitor.visit(randomProvider.getRandomGenerator(), agent);
        }

        // assert
        verify(agentStateService, times(2)).infect(agent, basicConfig.BA1);
        verify(agentStateService, times(2)).infect(agent, basicConfig.BA2);
    }
}