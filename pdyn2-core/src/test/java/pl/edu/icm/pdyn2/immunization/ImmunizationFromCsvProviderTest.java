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

package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImmunizationFromCsvProviderTest {

    @Mock
    private WorkDir workDir;

    private ImmunizationFromCsvProvider immunizationFromCsvProvider;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        when(this.workDir.openForReading(new File("sFunctionTest.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"));
        when(this.workDir.openForReading(new File("crossImmunityTest.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest.csv"));
        when(this.workDir.openForReading(new File("crossImmunityTest2.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest2.csv"));

        immunizationFromCsvProvider = new ImmunizationFromCsvProvider(workDir,
                "sFunctionTest.csv",
                "sFunctionTest.csv",
                "sFunctionTest.csv",
                "crossImmunityTest.csv",
                "crossImmunityTest.csv",
                "crossImmunityTest2.csv",
                "crossImmunityTest.csv"
        );
    }

    @Test
    void test() throws IOException {
        immunizationFromCsvProvider.load();
        assertEquals(immunizationFromCsvProvider.getSFunction(Load.ALPHA, ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, 10), 0.1);
        assertEquals(immunizationFromCsvProvider.getSFunction(Load.PFIZER, ImmunizationStage.OBJAWOWY, 6), 0.39);
        assertEquals(immunizationFromCsvProvider.getSFunction(Load.DELTA, ImmunizationStage.LATENTNY, 0), 0.88);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(Load.PFIZER, Load.ALPHA, ImmunizationStage.LATENTNY), 0.999);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(Load.ALPHA, Load.DELTA, ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM), 0.977);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(Load.WILD, Load.WILD, ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM), 0.99999);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(Load.DELTA, Load.ALPHA, ImmunizationStage.OBJAWOWY), 0.8);
    }
}
