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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.isolation.IsolationService;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingSystemTest {

    @Mock
    EligibleForTestsSelector eligibleForTestsSelector;

    @Mock
    TestingService testingService;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    IsolationService isolationService;

    @Mock
    Session session;

    @Mock
    Entity entity;

    @InjectMocks
    TestingSystemBuilder testingSystemBuilder;

    EntitySystem testingSystem;

    @BeforeEach
    void setUp() {
        testingSystem = testingSystemBuilder.buildTestingSystem();
    }

    @Test
    @DisplayName("Should run testing system service on each agent given by the selector")
    void execute() {
        // given
        when(eligibleForTestsSelector.chunks()).thenReturn(Stream.of(chunk(2,3,10,15)));
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.create(anyInt())).thenReturn(session);
        when(session.getEntity(anyInt())).thenReturn(entity);

        // execute
        testingSystem.execute(sessionFactory);

        // assert
        verify(session).getEntity(2);
        verify(session).getEntity(3);
        verify(session).getEntity(10);
        verify(session).getEntity(15);

        verify(testingService, times(4)).maybeTestAgent(entity);
        verify(isolationService, times(4)).maybeIsolateAgent(entity);
    }

    private Chunk chunk(int... ids) {
        return new Chunk(ChunkInfo.of(0, ids.length), IntStream.of(ids));
    }
}
