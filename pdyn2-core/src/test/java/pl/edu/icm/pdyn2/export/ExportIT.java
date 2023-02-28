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
import pl.edu.icm.board.EngineIo;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.*;
import pl.edu.icm.pdyn2.administration.TestingConfig;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExportIT {
    private final BasicConfig basicConfig = new BasicConfig();
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(basicConfig, false);
    private final RandomProvider randomProvider = new MockRandomProvider();
    private final AgentStateService agentStateService = data.agentStateService;
    @Mock
    private DiseaseStageTransitionsService transitionsService;
    @Mock
    private StatsService statsService;
    WorkDir workDir = data.workDir;
    @Mock
    private EngineIo board;
    @Captor
    ArgumentCaptor<File> file;

    EpidemicExporter exporter;

    private ByteArrayOutputStream results;

    @BeforeEach
    public void before() throws FileNotFoundException {
        when(board.getEngine()).thenReturn(data.session.getEngine());
        results = new ByteArrayOutputStream();
        when(workDir.openForWriting(file.capture())).thenReturn(results);

        when(transitionsService.durationOf(basicConfig.loads.WILD, basicConfig.stages.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(basicConfig.loads.WILD, basicConfig.stages.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.HOSPITALIZED_ICU, 18)).thenReturn(2);
        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.HOSPITALIZED_PRE_ICU, 18)).thenReturn(4);
        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(basicConfig.loads.DELTA, basicConfig.stages.INFECTIOUS_ASYMPTOMATIC, 18)).thenReturn(5);
        when(transitionsService.durationOf(basicConfig.loads.DELTA, basicConfig.stages.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.HOSPITALIZED_NO_ICU, 18)).thenReturn(3);
        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(basicConfig.loads.OMICRON, basicConfig.stages.LATENT, 18)).thenReturn(7);
    }

    @Test
    public void export() {
        // given
        exporter = new EpidemicExporter("exportTest.csv",
                board,
                workDir,
                transitionsService,
                data.selectors, basicConfig.loads, basicConfig.stages);

        TestingService testingService = new TestingService(
                data.simulationTimer,
                randomProvider,
                statsService,
                agentStateService, new TestingConfig(1.0f));
        var sowingSource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        var streetSource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        streetSource.changeOutcome(ContextInfectivityClass.STREET, 1.0f);
        var universitySource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        universitySource.changeOutcome(ContextInfectivityClass.UNIVERSITY, 1.0f);
        var householdSource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        householdSource.changeOutcome(ContextInfectivityClass.HOUSEHOLD, 1.0f);
        var vaccination = new ImmunizationEvent();
        vaccination.setLoad(basicConfig.loads.BOOSTER);
        vaccination.setDay(0);

        agentStateService.infect(data.agent1, basicConfig.loads.WILD, 10);
        agentStateService.addSourcesDistribution(data.agent1, sowingSource);
        agentStateService.progressToDiseaseStage(data.agent1, basicConfig.stages.INFECTIOUS_SYMPTOMATIC, 7);
        agentStateService.infect(data.agentA, basicConfig.loads.DELTA, 3);
        agentStateService.addSourcesDistribution(data.agentA, sowingSource);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agent1, basicConfig.stages.HEALTHY);
        advance(1);
        agentStateService.progressToDiseaseStage(data.agentA, basicConfig.stages.INFECTIOUS_ASYMPTOMATIC);
        advance(5);
        agentStateService.progressToDiseaseStage(data.agentA, basicConfig.stages.HEALTHY);
        advance(1);
        agentStateService.vaccinate(data.agent1, vaccination);
        agentStateService.infect(data.agent2, basicConfig.loads.ALPHA);
        agentStateService.addSourcesDistribution(data.agent2, householdSource);
        agentStateService.infect(data.agent3, basicConfig.loads.DELTA);
        agentStateService.addSourcesDistribution(data.agent3, householdSource);
        agentStateService.infect(data.agent7, basicConfig.loads.OMICRON);
        agentStateService.addSourcesDistribution(data.agent7, universitySource);
        advance(7);
        testingService.maybeTestAgent(data.agent7);
        agentStateService.progressToDiseaseStage(data.agent7, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agent7, basicConfig.stages.HOSPITALIZED_PRE_ICU);
        advance(4);
        agentStateService.progressToDiseaseStage(data.agent7, basicConfig.stages.HOSPITALIZED_ICU);
        advance(2);
        agentStateService.progressToDiseaseStage(data.agent7, basicConfig.stages.DECEASED);
        agentStateService.infect(data.agentA, basicConfig.loads.OMICRON);
        agentStateService.addSourcesDistribution(data.agentA, householdSource);
        advance(7);
        agentStateService.progressToDiseaseStage(data.agentA, basicConfig.stages.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agentA, basicConfig.stages.HOSPITALIZED_NO_ICU);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agentA, basicConfig.stages.DECEASED);
        data.session.close();

        // execute
        exporter.export();

        // assert
        assertThat(results.toString()).isEqualTo("id,dzien_zakazenia,odmiana_wirusa,odmiana_szczepionki,historia_stanow,test,x,y,wiek,workplaceInfluence,kindergartenInfluence,schoolInfluence,universityInfluence,streetInfluence,bigUniversityInfluence,householdInfluence\n" +
                "100006,-10,0,-1,26,0,50,50,18,0.0,0.0,0.0,0.0,0.0,0.0,0.0\n" +
                "100006,0,-1,1,0,0,50,50,18,0.0,0.0,0.0,0.0,0.0,0.0,0.0\n" +
                "100012,10,3,-1,458,1,52,50,18,0.0,0.0,0.0,1.0,0.0,0.0,0.0\n" +
                "100015,-3,2,-1,22,0,52,50,18,0.0,0.0,0.0,0.0,0.0,0.0,0.0\n" +
                "100015,29,3,-1,298,0,52,50,18,0.0,0.0,0.0,0.0,0.0,0.0,1.0\n");
    }

    private void advance(int days) {
        for (int i = 0; i < days; i++) {
            data.simulationTimer.advanceOneDay();
        }
    }
}
