package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadDiseaseStageTransitionsTest {

    private LoadDiseaseStageTransitions loadDiseaseStageTransitions;

    @Mock
    private ImmunizationService immunizationService;

    @Mock
    private SimulationTimer simulationTimer;

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
                immunizationService,
                simulationTimer,
                workDir,
                Load.OMICRON);
        when(immunizationService.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.OBJAWOWY), any(), anyInt())).thenReturn(1.0f);
        when(immunizationService.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM), any(), anyInt())).thenReturn(0.4f);
        when(immunizationService.getImmunizationCoefficient(any(),
                eq(ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM), any(), anyInt())).thenReturn(0.5f);
        when(simulationTimer.getDaysPassed()).thenReturn(5);

        Person person1 = new Person();
        person1.setAge(5);
        person1.setSex(Person.Sex.M);
        entity1.add(person1);

        Person person2 = new Person();
        person2.setAge(121);
        person2.setSex(Person.Sex.K);
        entity2.add(person2);

        when(entity1.get(Person.class)).thenReturn(person1);
        when(entity2.get(Person.class)).thenReturn(person2);
    }

    @Test
    @DisplayName("Should load and parse the transition table")
    void getPossibleTransitions() {
        var p1 = loadDiseaseStageTransitions
                .getPossibleTransitions(Stage.INFECTIOUS_SYMPTOMATIC, entity1);

        assertThat(p1.getProbability(Stage.HOSPITALIZED_PRE_ICU)).isEqualTo(0.001f);
        assertThat(p1.getProbability(Stage.HOSPITALIZED_NO_ICU)).isEqualTo(0.006f);
        assertThat(p1.getProbability(Stage.HEALTHY)).isEqualTo(0.992f);
        assertThat(p1.getProbability(Stage.DECEASED)).isEqualTo(0.001f);

        var p2 = loadDiseaseStageTransitions
                .getPossibleTransitions(Stage.LATENT, entity2);

        assertThat(p2.getProbability(Stage.INFECTIOUS_ASYMPTOMATIC)).isEqualTo(1.0f);
        assertThat(p2.getProbability(Stage.INFECTIOUS_SYMPTOMATIC)).isEqualTo(0.0f);
    }
}
