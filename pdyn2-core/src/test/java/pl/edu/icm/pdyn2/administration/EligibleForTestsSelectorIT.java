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

package pl.edu.icm.pdyn2.administration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.HealthStatusMapper;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineCreationListener;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.util.RangeSelector;
import pl.edu.icm.trurl.ecs.util.Selectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EligibleForTestsSelectorIT {
    BasicConfig basicConfig = new BasicConfig();

    @Mock
    Engine engine;

    @Mock
    MapperSet mapperSet;

    @Mock
    EngineConfiguration engineConfiguration;

    @Mock
    HealthStatusMapper healthStatusMapper;

    @Mock
    AreaClusteredSelectors areaClusteredSelectors;

    Selectors selectors;

    @Mock
    SimulationTimer simulationTimer;

    final int DAYS = 10;
    final int STAGES = basicConfig.stages.values().size();
    final int COUNT = STAGES * DAYS;

    EligibleForTestsSelector selector;


    @BeforeEach
    void before() {
        doAnswer(args -> {
            args.getArgument(0, EngineCreationListener.class).onEngineCreated(engine);
            return null;
        }).when(engineConfiguration).addEngineCreationListener(any());
        selectors = new Selectors(engine);
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(HealthStatus.class)).thenReturn(healthStatusMapper);
        selector = new EligibleForTestsSelector(engineConfiguration, simulationTimer, selectors, areaClusteredSelectors, basicConfig.stages);
    }

    @Test
    @DisplayName("Should return correct agents")
    void ids() {
        // given
        when(areaClusteredSelectors.personSelector()).thenReturn(new RangeSelector(0, COUNT, 3));
        when(healthStatusMapper.getDayOfLastChange(anyInt())).thenAnswer(args -> {
            int id = args.getArgument(0, Integer.class);
            return dayFromId(id);
        });
        when(healthStatusMapper.getStage(anyInt())).thenAnswer(args -> {
            int id = args.getArgument(0, Integer.class);
            return stageFromId(id);
        });
        when(simulationTimer.getDaysPassed()).thenReturn(4);

        // execute
        var results = selector.chunks().flatMapToInt(Chunk::ids);

        // assert
        assertThat(results)
                .containsExactly(14, 24);    // the first digit is the stage ordinal, the second - the simulation day
    }

    private Stage stageFromId(int id) {
        int ordinal = id / DAYS;
        return basicConfig.stages.values().get(ordinal);
    }

    private int dayFromId(int id) {
        return id % DAYS;
    }
}
