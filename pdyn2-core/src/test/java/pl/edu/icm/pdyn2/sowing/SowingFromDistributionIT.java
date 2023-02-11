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

package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.commune.AdministrationAreaType;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.PopulationService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;

import java.io.File;
import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SowingFromDistributionIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(false);
    private final RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private WorkDir workDir;
    @Mock
    private CommuneManager communeManager;
    @Mock
    private StatsService statsService;
    @Mock
    private PopulationService populationService;
    @Mock
    private DiseaseStageTransitionsService diseaseStageTransitionsService;
    @Mock
    private Board board;
    @Mock
    private TestingService testingService;

    private final AgentStateService agentStateService = data.agentStateService;

    @BeforeEach
    public void before() throws FileNotFoundException {
        when(board.getEngine()).thenReturn(data.session.getEngine());
        when(workDir.openForReading(new File("/sowingDistributionTest.csv"))).thenReturn(SowingFromDistributionIT.class
                .getResourceAsStream("/sowingDistributionTest.csv"));
        when(diseaseStageTransitionsService.durationOf(any(), any(), anyInt())).thenReturn(5);
        when(populationService.typeFromLocation(any())).thenReturn(AdministrationAreaType.VILLAGE);
        var communeA = new Commune();
        communeA.setTeryt("0223013");
        var communeC = new Commune();
        communeC.setTeryt("0605014");
        when(communeManager.communeAt(data.cellA)).thenReturn(communeA);
        when(communeManager.communeAt(data.cellC)).thenReturn(communeC);
    }

    @Test
    public void sow() {
        // given
        var loader = new InfectedLoaderFromDistribution("/sowingDistributionTest.csv",
                randomProvider,
                workDir);

        var sowingFromDistribution = new SowingFromDistribution(
                loader,
                statsService,
                board,
                randomProvider,
                communeManager,
                populationService,
                agentStateService,
                diseaseStageTransitionsService,
                testingService);
        data.session.close();

        // execute
        sowingFromDistribution.sow();

        // assert
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.INFECTIOUS_SYMPTOMATIC)).count()).isEqualTo(1);
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.LATENT)).count()).isEqualTo(1);
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.INFECTIOUS_ASYMPTOMATIC)).count()).isEqualTo(1);
    }
}
