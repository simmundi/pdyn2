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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.clock.SimulationClock;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadDiseaseStageTransitionsTest {
    private BasicConfig basicConfig = new BasicConfig();

    private LoadDiseaseStageTransitions loadDiseaseStageTransitions;

    @Mock
    private SimulationClock simulationClock;

    @Mock
    private WorkDir workDir;

    @Mock
    Entity entity1;

    @Mock
    Entity entity2;

    @BeforeEach
    void before() {
        when(workDir.openForReading(any())).thenReturn(
                LoadDiseaseStageTransitionsTest.class.getResourceAsStream("/stanCzasTest.txt"));
        loadDiseaseStageTransitions = new LoadDiseaseStageTransitions("stanCzasTest.txt",
                workDir,
                basicConfig.stages,
                basicConfig.ageRanges,
                basicConfig.OMICRON,
                basicConfig.outcomeModifier);
        when(basicConfig.immunizationStrategy.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.SYMPTOMATIC), any(), anyInt())).thenReturn(1.0f);
        when(basicConfig.immunizationStrategy.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.ASYMPTOMATIC), any(), anyInt())).thenReturn(0.4f);
        when(basicConfig.immunizationStrategy.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.HOSPITALIZED_PRE_ICU), any(), anyInt())).thenReturn(0.5f);

        Person person1 = new Person();
        person1.setAge(5);
        person1.setSex(Person.Sex.M);

        Person person2 = new Person();
        person2.setAge(121);
        person2.setSex(Person.Sex.K);

        when(entity1.get(Person.class)).thenReturn(person1);
        when(entity2.get(Person.class)).thenReturn(person2);
    }

    @Test
    @DisplayName("Should load and parse the transition table")
    void getPossibleTransitions() {
        var p1 = loadDiseaseStageTransitions
                .getPossibleTransitions(basicConfig.stages.INFECTIOUS_SYMPTOMATIC, entity1);

        assertThat(p1.get(basicConfig.HOSPITALIZED_PRE_ICU)).isEqualTo(0.001f);
        assertThat(p1.get(basicConfig.HOSPITALIZED_NO_ICU)).isEqualTo(0.006f);
        assertThat(p1.get(basicConfig.stages.HEALTHY)).isEqualTo(0.992f);
        assertThat(p1.get(basicConfig.stages.DECEASED)).isEqualTo(0.001f);

        var p2 = loadDiseaseStageTransitions
                .getPossibleTransitions(basicConfig.LATENT, entity2);

        assertThat(p2.get(basicConfig.INFECTIOUS_ASYMPTOMATIC)).isEqualTo(1.0f);
        assertThat(p2.get(basicConfig.stages.INFECTIOUS_SYMPTOMATIC)).isEqualTo(0.0f);
    }
}
