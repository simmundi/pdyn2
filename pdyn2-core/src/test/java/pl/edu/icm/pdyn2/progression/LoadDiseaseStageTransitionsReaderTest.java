package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadDiseaseStageTransitionsReaderTest {

    @Mock
    WorkDir workDir;

    @InjectMocks
    LoadDiseaseStageTransitionsReader reader;

    @Test
    @DisplayName("Should create a correct LoadDiseaseStageTransitions instance")
    void readFromFile() {
        // given
        when(workDir.openForReading(any())).thenReturn(
                LoadDiseaseStageTransitionsTest.class.getResourceAsStream("/stanCzasTest.txt"));

        // execute
        LoadDiseaseStageTransitions transition = reader.readFromFile("stan_czas.txt", Load.WILD);

        // assert
        assertThat(transition.durationOf(Stage.LATENT, 7)).isPositive();
    }
}
