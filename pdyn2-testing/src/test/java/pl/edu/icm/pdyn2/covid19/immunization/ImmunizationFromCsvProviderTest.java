/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.immunization;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmunizationFromCsvProviderTest {
    private final BasicConfig basicConfig = new BasicConfig();

    @Mock
    private WorkDir workDir;

    private ImmunizationFromCsvProvider immunizationFromCsvProvider;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        when(this.workDir.openForReading(new File("sFunctionTest.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"),
                ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"),
                ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"),
                ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"));
        when(this.workDir.openForReading(new File("crossImmunityTest.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest.csv"),
                ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest.csv"),
                ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest.csv"));
        when(this.workDir.openForReading(new File("crossImmunityTest2.csv"))).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest2.csv"));

        immunizationFromCsvProvider = new ImmunizationFromCsvProvider(workDir,
                basicConfig.loads,
                "sFunctionTest.csv",
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
        assertEquals(immunizationFromCsvProvider.getSFunction(basicConfig.ALPHA, ImmunizationStage.HOSPITALIZED_PRE_ICU, 10), 0.1);
        assertEquals(immunizationFromCsvProvider.getSFunction(basicConfig.PFIZER, ImmunizationStage.SYMPTOMATIC, 6), 0.39);
        assertEquals(immunizationFromCsvProvider.getSFunction(basicConfig.DELTA, ImmunizationStage.LATENT, 0), 0.88);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(basicConfig.PFIZER, basicConfig.ALPHA, ImmunizationStage.LATENT), 0.999);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(basicConfig.ALPHA, basicConfig.DELTA, ImmunizationStage.HOSPITALIZED_PRE_ICU), 0.977);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(basicConfig.WILD, basicConfig.WILD, ImmunizationStage.ASYMPTOMATIC), 0.99999);
        assertEquals(immunizationFromCsvProvider.getCrossImmunity(basicConfig.DELTA, basicConfig.ALPHA, ImmunizationStage.SYMPTOMATIC), 0.8);
    }
}
