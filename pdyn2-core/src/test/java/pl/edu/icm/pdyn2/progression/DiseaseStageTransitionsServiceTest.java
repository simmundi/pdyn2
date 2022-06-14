package pl.edu.icm.pdyn2.progression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.File;
import java.io.FileFilter;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DiseaseStageTransitionsServiceTest {
    @Mock
    Filesystem filesystem;
    @Mock
    LoadDiseaseStageTransitionsReader reader;
    @Mock
    LoadDiseaseStageTransitions transitions;

    @BeforeEach
    void before() {
        when(filesystem.listFiles(any(), any()))
                .thenAnswer(call -> {
                    FileFilter filter = call.getArgument(1, FileFilter.class);
                    return Stream.of("a_WILD.txt", "test.xxx", ".gitignore", "b_OMICRON.txt")
                            .map(File::new)
                            .filter(filter::accept)
                            .toArray(count -> new File[count]);
                });
    }

    @Test
    @DisplayName("Should parse file names and load tables for correct loads")
    void construct() {
        // execute
        new DiseaseStageTransitionsService("xxx", filesystem, reader);

        // assert
        verify(reader).readFromFile(endsWith("/a_WILD.txt"), eq(Load.WILD));
        verify(reader).readFromFile(endsWith("/b_OMICRON.txt"), eq(Load.OMICRON));
    }


    @Test
    @DisplayName("Should delegate durationOf to correct LoadDiseaseStageTransition instance")
    void durationOf() {
        // given
        when(reader.readFromFile(any(), eq(Load.WILD))).thenReturn(null);
        when(reader.readFromFile(any(), eq(Load.OMICRON))).thenReturn(transitions);
        DiseaseStageTransitionsService service = new DiseaseStageTransitionsService("xxx", filesystem, reader);

        // execute
        service.durationOf(Load.OMICRON, Stage.HEALTHY, 23);

        // assert
        verify(transitions).durationOf(Stage.HEALTHY, 23);
    }

    @Test
    @DisplayName("Should delegate outcomeOf to correct LoadDiseaseStageTransition instance")
    void outcomeOf() {
        // given
        when(reader.readFromFile(any(), eq(Load.WILD))).thenReturn(transitions);
        DiseaseStageTransitionsService service = new DiseaseStageTransitionsService("xxx", filesystem, reader);

        // execute
        service.durationOf(Load.WILD, Stage.INFECTIOUS_ASYMPTOMATIC, 11);

        // assert
        verify(transitions).durationOf(Stage.INFECTIOUS_ASYMPTOMATIC, 11);
    }
}
