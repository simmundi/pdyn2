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

package pl.edu.icm.pdyn2.importer;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockLoadService;
import pl.edu.icm.pdyn2.immunization.LoadService;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;

import java.io.File;
import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImporterIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(false);
    private final AgentStateService agentStateService = data.agentStateService;
    @Mock
    private DiseaseStageTransitionsService transitionsService;
    @Mock
    private Board board;
    @Mock
    private WorkDir workDir;

    ImmunizationEventsImporterFromAgentId importer;

    private LoadService loadService = new MockLoadService();

    @BeforeEach
    public void before() throws FileNotFoundException {
        when(workDir.openForReading(new File("/importerTest.csv"))).thenReturn(ImporterIT.class
                .getResourceAsStream("/importerTest.csv"));
        when(board.getEngine()).thenReturn(data.session.getEngine());

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
    @Disabled
    public void test() {
        var loader = new ImmunizationEventsLoaderFromAgentId(workDir);
        var idMappingLoader = new AgentIdMappingLoader();
        importer = new ImmunizationEventsImporterFromAgentId(loader,
                idMappingLoader,
                board,
                data.selectors,
                agentStateService,
                data.simulationTimer,
                transitionsService,
                loadService);
        data.session.close();
        var orcFilename = String.valueOf(ImporterIT.class.getResource("/importerTest.orc"));
        importer.importEvents("/importerTest.csv", orcFilename, 1000);
        data.session.close();

        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(0).getDay()).isEqualTo(-997);
        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(0).getDiseaseHistory()).isEqualTo(26);
        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(data.wild);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(0).getDay()).isEqualTo(-971);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(0).getDiseaseHistory()).isEqualTo(458);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(data.omicron);
        assertThat(data.allAgents.get(2).get(Immunization.class).getEvents().get(0).getDay()).isEqualTo(-991);
        assertThat(data.allAgents.get(2).get(Immunization.class).getEvents().get(0).getDiseaseHistory()).isEqualTo(22);
        assertThat(data.allAgents.get(2).get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(data.delta);
        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(1).getDay()).isEqualTo(-955);
        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(1).getDiseaseHistory()).isEqualTo(298);
        assertThat(data.allAgents.get(1).get(Immunization.class).getEvents().get(1).getLoad()).isEqualTo(data.omicron);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(1).getDay()).isEqualTo(-900);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(1).getDiseaseHistory()).isEqualTo(0);
        assertThat(data.allAgents.get(0).get(Immunization.class).getEvents().get(1).getLoad()).isEqualTo(data.booster);
    }
}
