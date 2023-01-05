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

package pl.edu.icm.pdyn2.export;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.administration.TestingConfig;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExportIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(false);
    private final RandomProvider randomProvider = new MockRandomProvider();
    private final AgentStateService agentStateService = data.agentStateService;
    @Mock
    private DiseaseStageTransitionsService transitionsService;
    @Mock
    private StatsService statsService;
    WorkDir workDir = data.workDir;
    @Mock
    private Board board;
    @Captor
    ArgumentCaptor<File> file;

    EpidemicExporter exporter;

    private ByteArrayOutputStream results;

    @BeforeEach
    public void before() throws FileNotFoundException {
        when(board.getEngine()).thenReturn(data.session.getEngine());
        results = new ByteArrayOutputStream();
        when(workDir.openForWriting(file.capture())).thenReturn(results);

        when(transitionsService.durationOf(data.wild, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(data.wild, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(data.omicron, Stage.HOSPITALIZED_ICU, 18)).thenReturn(2);
        when(transitionsService.durationOf(data.omicron, Stage.HOSPITALIZED_PRE_ICU, 18)).thenReturn(4);
        when(transitionsService.durationOf(data.omicron, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(data.omicron, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(data.delta, Stage.INFECTIOUS_ASYMPTOMATIC, 18)).thenReturn(5);
        when(transitionsService.durationOf(data.delta, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(data.omicron, Stage.HOSPITALIZED_NO_ICU, 18)).thenReturn(3);
        when(transitionsService.durationOf(data.omicron, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(data.omicron, Stage.LATENT, 18)).thenReturn(7);
    }

    @Test
    public void export() {
        // given
        exporter = new EpidemicExporter("exportTest.csv",
                board,
                workDir,
                transitionsService,
                data.selectors);

        TestingService testingService = new TestingService(
                data.simulationTimer,
                randomProvider,
                statsService,
                agentStateService, new TestingConfig(1.0f));

        agentStateService.infect(data.agent1, data.wild, 10);
        agentStateService.progressToDiseaseStage(data.agent1, Stage.INFECTIOUS_SYMPTOMATIC, 7);
        agentStateService.infect(data.agentA, data.delta, 3);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agent1, Stage.HEALTHY);
        advance(1);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.INFECTIOUS_ASYMPTOMATIC);
        advance(5);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.HEALTHY);
        advance(1);
        agentStateService.infect(data.agent2, data.alpha);
        agentStateService.infect(data.agent3, data.delta);
        agentStateService.infect(data.agent7, data.omicron);
        advance(7);
        testingService.maybeTestAgent(data.agent7);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.HOSPITALIZED_PRE_ICU);
        advance(4);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.HOSPITALIZED_ICU);
        advance(2);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.DECEASED);
        agentStateService.infect(data.agentA, data.omicron);
        advance(7);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.HOSPITALIZED_NO_ICU);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.DECEASED);
        data.session.close();

        // execute
        exporter.export();

        // assert
        assertThat(results.toString()).isEqualTo("id,dzien_zakazenia,miejsce_zakazenia,odmiana_wirusa,odmiana_szczepionki,historia_stanow,test,x,y,wiek\n" +
                "100006,-10,0,0,-1,26,0,50,50,18\n" +
                "100012,10,0,3,-1,458,1,52,50,18\n" +
                "100015,-3,0,2,-1,22,0,52,50,18\n" +
                "100015,29,0,3,-1,298,0,52,50,18\n");
    }

    private void advance(int days) {
        for (int i = 0; i < days; i++) {
            data.simulationTimer.advanceOneDay();
        }
    }
}
