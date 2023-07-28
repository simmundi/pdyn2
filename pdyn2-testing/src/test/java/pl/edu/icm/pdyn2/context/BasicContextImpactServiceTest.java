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

package pl.edu.icm.pdyn2.context;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.BasicConfig;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.context.ContextTypes;
import pl.edu.icm.pdyn2.transmission.BasicContextImpactService;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@ExtendWith(MockitoExtension.class)
class BasicContextImpactServiceTest {

    BasicConfig basicConfig = new BasicConfig();

    @Spy
    AgeRanges ageRanges = basicConfig.ageRanges;
    @Spy
    ContextTypes contextTypes = basicConfig.contextTypes;
    @InjectMocks
    BasicContextImpactService fractionService;

    @Test
    @DisplayName("Should generally return one")
    void calculateLoadFractionFor() {
        // given
        var person = ComponentCreator.person(25, Person.Sex.K);

        var household = ComponentCreator.context(basicConfig.contextTypes.HOUSEHOLD);
        var school = ComponentCreator.context(basicConfig.contextTypes.SCHOOL);
        var workspace = ComponentCreator.context(basicConfig.contextTypes.WORKPLACE);
        var university = ComponentCreator.context(basicConfig.contextTypes.UNIVERSITY);

        // execute
        var householdFraction = fractionService.calculateInfluenceFractionFor(person, household);
        var schoolFraction = fractionService.calculateInfluenceFractionFor(person, school);
        var workspaceFraction = fractionService.calculateInfluenceFractionFor(person, workspace);
        var universityFraction = fractionService.calculateInfluenceFractionFor(person, university);

        // assert
        assertThat(householdFraction).isEqualTo(1);
        assertThat(schoolFraction).isEqualTo(1);
        assertThat(workspaceFraction).isEqualTo(1);
        assertThat(universityFraction).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return fractions for street contexts")
    void calculateLoadFractionFor__streets() {
        // given
        var agent = ComponentCreator.person(62, Person.Sex.K);
        var streets = basicConfig.contextTypes.streetContexts().stream().map(type ->
            ComponentCreator.context(type)
        ).collect(Collectors.toList());
        var street60 = ComponentCreator.context(basicConfig.contextTypes.STREET_60);

        // execute
        var mainFraction = fractionService.calculateInfluenceFractionFor(agent, street60);
        var fractions = streets.stream().mapToDouble(context -> fractionService.calculateInfluenceFractionFor(agent, context))
                .toArray();

        // assert
        var sum = Arrays.stream(fractions).sum();
        var max = Arrays.stream(fractions).max().getAsDouble();

        assertThat(sum).isCloseTo(1, offset(0.0001)); // close enough
        assertThat(max).isEqualTo(mainFraction);
    }

}
