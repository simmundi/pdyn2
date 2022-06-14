package pl.edu.icm.pdyn2.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.EntityMocker;
import pl.edu.icm.pdyn2.model.context.ContextType;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;

@ExtendWith(MockitoExtension.class)
class ContextFractionServiceTest {

    EntityMocker entityMocker = new EntityMocker(null);

    @InjectMocks
    ContextFractionService fractionService;

    @Test
    @DisplayName("Should generally return one")
    void calculateLoadFractionFor() {
        // given
        var person = ComponentCreator.person(25, Person.Sex.K);

        var household = ComponentCreator.context(ContextType.HOUSEHOLD);
        var school = ComponentCreator.context(ContextType.SCHOOL);
        var workspace = ComponentCreator.context(ContextType.WORKPLACE);
        var university = ComponentCreator.context(ContextType.UNIVERSITY);

        // execute
        var householdFraction = fractionService.calculateLoadFractionFor(person, household);
        var schoolFraction = fractionService.calculateLoadFractionFor(person, school);
        var workspaceFraction = fractionService.calculateLoadFractionFor(person, workspace);
        var universityFraction = fractionService.calculateLoadFractionFor(person, university);

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
        var streets = Arrays.stream(ContextType.streetContexts()).map(type ->
            ComponentCreator.context(type)
        ).collect(Collectors.toList());
        var street60 = ComponentCreator.context(ContextType.STREET_60);

        // execute
        var mainFraction = fractionService.calculateLoadFractionFor(agent, street60);
        var fractions = streets.stream().mapToDouble(context -> fractionService.calculateLoadFractionFor(agent, context))
                .toArray();

        // assert
        var sum = Arrays.stream(fractions).sum();
        var max = Arrays.stream(fractions).max().getAsDouble();

        assertThat(sum).isCloseTo(1, offset(0.0001)); // close enough
        assertThat(max).isEqualTo(mainFraction);
    }

}
