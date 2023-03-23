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

package pl.edu.icm.pdyn2.clock;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Systems;

import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class OnDayTriggerTest {
    SimulationClock simulationClock = new SimulationClock("");
    @Mock
    SessionFactory sessionFactory;

    @Test
    void onDay() {
        //given
        var onday = new OnDayTrigger(simulationClock);
        final Map<Integer, Integer> day = new Int2IntArrayMap();
        //execute
        EntitySystem sequence = Systems.sequence(
                onday.onDay(0, (s) -> day.put(0, simulationClock.getDaysPassed())),
                onday.onDay(3, (s) -> day.put(3, simulationClock.getDaysPassed())),
                onday.onDay(5, (s) -> day.put(5, simulationClock.getDaysPassed())),
                simulationClock
        );
        IntStream.range(0, 6).forEach(x -> sequence.execute(sessionFactory));
        //assert
        day.forEach((x,y) -> assertThat(x).isEqualTo(y));
    }
}
