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

package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.sowing.InfectedLoaderFromAgentId;

import java.io.File;
import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfectedLoaderFromAgentIdTest {

    @Mock
    WorkDir streamService;

    InfectedLoaderFromAgentId infectedLoaderFromAgentId;

    @Test
    void readInfected() throws FileNotFoundException {
        when(this.streamService.openForReading(new File("1"))).thenReturn(InfectedLoaderFromAgentIdTest.class.getResourceAsStream("/initialSowingCorrect.dat"));
        when(this.streamService.openForReading(new File("2"))).thenReturn(InfectedLoaderFromAgentIdTest.class.getResourceAsStream("/initialSowingWrongCount.dat"));
        infectedLoaderFromAgentId = new InfectedLoaderFromAgentId(this.streamService, "1");
        var list = infectedLoaderFromAgentId.readInfected();
        assertEquals(list.size(), 11);

        assertEquals(list.get(0).getAgentId(), 35588785);
        assertEquals(list.get(0).getElapsedDays(), 1);
        assertEquals(list.get(0).isSymptomatic(), true);

        assertEquals(list.get(list.size() - 1).getAgentId(), 9646933);
        assertEquals(list.get(list.size() - 1).getElapsedDays(), 1);
        assertEquals(list.get(list.size() - 1).isSymptomatic(), false);

        infectedLoaderFromAgentId = new InfectedLoaderFromAgentId(this.streamService, "2");
        assertThatThrownBy(() -> infectedLoaderFromAgentId.readInfected())
                .hasMessageContaining("infectedCount != infectedCountInFile")
                .isInstanceOf(IllegalStateException.class);


//        readInfected = new ReadInfected("initialSowingWrongCount.dat");
    }
}
