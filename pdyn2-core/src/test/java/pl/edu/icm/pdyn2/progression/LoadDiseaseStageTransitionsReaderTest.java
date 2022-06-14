package pl.edu.icm.pdyn2.progression;

import org.assertj.core.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.ComponentCreator;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.util.Filesystem;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadDiseaseStageTransitionsReaderTest {

    @Mock
    Filesystem filesystem;

    @InjectMocks
    LoadDiseaseStageTransitionsReader reader;

    @Test
    @DisplayName("Should create a correct LoadDiseaseStageTransitions instance")
    void readFromFile() {
        // given
        when(filesystem.openForReading(any())).thenReturn(
                LoadDiseaseStageTransitionsTest.class.getResourceAsStream("/stanCzasTest.txt"));

        // execute
        LoadDiseaseStageTransitions transition = reader.readFromFile("stan_czas.txt", Load.WILD);

        // assert
        assertThat(transition.durationOf(Stage.LATENT, 7)).isPositive();
    }
}
