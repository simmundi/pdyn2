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

package pl.edu.icm.pdyn2.vaccination;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.*;
import pl.edu.icm.pdyn2.index.CommuneClusteredSelectors;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.simulation.StatsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static pl.edu.icm.trurl.ecs.util.Systems.sequence;

@ExtendWith(MockitoExtension.class)
public class VaccinationIT {
    private final BasicConfig basicConfig = new BasicConfig();
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(basicConfig, true);
    private final AgentStateService agentStateService = data.agentStateService;
    private final SimulationTimer simulationTimer = data.simulationTimer;
    @Mock
    private WorkDir workDir;

    private final RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private StatsService statsService;
    @Mock
    private CommuneClusteredSelectors communeClusteredSelectors;

    @BeforeEach
    public void before() {
        when(workDir.openForReading(new File("/vaccinationTest.csv")))
                .thenReturn(VaccinationIT.class.getResourceAsStream("/vaccinationTest.csv"));
        when(communeClusteredSelectors.personInTerytSelector(List.of("2401")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "240102"),
                        data.householdContext1.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
                )));
        when(communeClusteredSelectors.personInTerytSelector(List.of("0123")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "012345"),
                        data.householdContext2.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
                )));
        when(communeClusteredSelectors.personInTerytSelector(List.of("1001")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "100102"),
                        data.householdContext3.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
                )));
    }

    @Test
    @DisplayName("Should vaccinate agents according to the given data")
    public void test() {
        var loader = new VaccinationFromCsvLoader("/vaccinationTest.csv", workDir, basicConfig.loads);
        var vaccinationService = new VaccinationService(agentStateService,
                randomProvider,
                communeClusteredSelectors,
                data.selectors,
                statsService);
        var vaccinationSystemBuilder = new VaccinationFromCsvSystemBuilder(loader,
                simulationTimer,
                vaccinationService, basicConfig.stages);
        vaccinationSystemBuilder.load();

        var event1 = new ImmunizationEvent();
        event1.setDay(0);
        event1.setLoad(basicConfig.loads.PFIZER);

        agentStateService.vaccinate(data.agent1, event1);
        agentStateService.vaccinate(data.agent7, event1);
        agentStateService.vaccinate(data.agent8, event1);
        agentStateService.infect(data.agent9, basicConfig.loads.OMICRON);

        data.session.close();
        var vaccinationSystem = vaccinationSystemBuilder.buildVaccinationSystem();
        while (simulationTimer.getDaysPassed() < 4) {
            data.engine.execute(sequence(vaccinationSystem,
                    simulationTimer
            ));
        }

        assertThat(data.agent1.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.PFIZER);
        assertThat(data.agent2.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.PFIZER);
        assertThat(data.agent3.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.PFIZER);
        assertThat(data.agent4.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.BOOSTER);
        assertThat(data.agent5.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.BOOSTER);
        assertThat(data.agent6.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.BOOSTER);
        assertThat(data.agentA.get(Immunization.class).getEvents().get(0).getLoad()).isEqualTo(basicConfig.loads.PFIZER);
    }
}
