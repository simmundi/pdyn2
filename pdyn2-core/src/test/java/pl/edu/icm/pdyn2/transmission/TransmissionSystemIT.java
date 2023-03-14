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

import net.snowyhollows.bento.config.Configurer;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.geography.GeographicalServices;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.*;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.context.BehaviourBasedContextsService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.query.SelectorFromQueryService;
import pl.edu.icm.trurl.ecs.util.IteratingSystemBuilder;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.Visit;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransmissionSystemIT {
    private BasicConfig basicConfig = new BasicConfig();

    private TransmissionVisitor transmissionVisitor;

    private EngineIo board;

    @Mock
    private AreaIndex areaIndex;

    @Mock
    private AgentStateService agentStateService;

    @Mock
    private GeographicalServices geographicalServices;

    private ContextsService contextsService;

    @Mock
    private Selectors selectors;

    @Mock
    private TransmissionConfig transmissionConfig;

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private RandomGenerator randomGenerator;

    @Mock
    private RandomForChunkProvider randomForChunkProvider;

    @Mock
    private SimulationTimer simulationTimer;

    @Mock
    StatsService statsService;

    @Mock
    private ImmunizationService immunizationService;

    @Mock
    private RelativeAlphaConfig relativeAlphaConfig;

    private TransmissionService transmissionService;

    @Mock
    private AreaClusteredSelectors areaClusteredSelectors;

    private EntitySystem transmissionSystem;
    @Mock
    private SelectorFromQueryService selectorFromQueryService;
    @Mock
    private CommuneManager communeManager;

    @BeforeEach
    void before() throws IOException {
        EngineConfiguration engineConfig = new Configurer().setParam("trurl.engine.storeFactory", TablesawStoreFactory.class.getName()).getConfig().get(EngineConfigurationFactory.IT);
        CsvWriter csvWriter = new CsvWriter();
        board = new EngineIo(engineConfig, csvWriter, null, null);
        board.require(
                Area.class,
                Location.class,
                Household.class,
                Person.class,
                Named.class,
                AdministrationUnit.class,
                EducationalInstitution.class,
                Workplace.class,
                Inhabitant.class,
                Attendee.class,
                HealthStatus.class,
                Context.class,
                Immunization.class
        );
        areaClusteredSelectors = new AreaClusteredSelectors(engineConfig, selectorFromQueryService, communeManager);
        board.load(TransmissionVisitor.class.getResourceAsStream("/transmissionTest.csv"));
        when(randomProvider.getRandomGenerator(TransmissionVisitor.class)).thenReturn(randomGenerator);
        when(randomProvider.getRandomForChunkProvider(TransmissionVisitor.class)).thenReturn(randomForChunkProvider);
        contextsService = new BehaviourBasedContextsService(areaIndex);
        transmissionService = new TransmissionService(contextsService, relativeAlphaConfig, transmissionConfig, simulationTimer, basicConfig.loads, basicConfig.stages, immunizationService);
        transmissionVisitor = new TransmissionVisitor(
                transmissionService,
                agentStateService,
                statsService,
                basicConfig.loads,
                basicConfig.stages);

        transmissionSystem = IteratingSystemBuilder.iteratingOver(areaClusteredSelectors.personSelector())
                .persistingAll()
                .withContext(randomForChunkProvider)
                .perform(Visit.of(transmissionVisitor::visit))
                .build();

        when(immunizationService.getImmunizationCoefficient(any(), any(), any(), anyInt())).thenReturn(0.5f);
        when(transmissionConfig.getAlpha()).thenReturn(1.0f);
        when(transmissionConfig.getTotalWeightForContextType(any())).thenReturn(1.0f);
        when(randomGenerator.nextDouble()).thenReturn(0.2);
        when(relativeAlphaConfig.getRelativeAlpha(any())).thenReturn(0.5f);
    }

    @Test
    @Disabled
    void test() throws IOException {
        board.getEngine().execute(transmissionSystem);
        var table = ((TablesawStore) board.getEngine().getStore()).asTable("entities");

        assertThat(table.where(table.stringColumn("health.stage").isEqualTo("OBJAWOWY"))
                .rowCount()).isEqualTo(1);
        assertThat(table.where(table.stringColumn("health.stage").isEqualTo("LATENTNY"))
                .rowCount()).isEqualTo(1);

        board.save("transmissionTestOutput.csv");
    }
}
