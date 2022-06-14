package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.pdyn2.sowing.InfectedLoaderFromAgentId;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfectedLoaderFromAgentIdTest {

    @Mock
    FileToStreamService streamService;

    InfectedLoaderFromAgentId infectedLoaderFromAgentId;

    @Test
    void readInfected() throws FileNotFoundException {
        when(this.streamService.filename("1")).thenReturn(InfectedLoaderFromAgentIdTest.class.getResourceAsStream("/initialSowingCorrect.dat"));
        when(this.streamService.filename("2")).thenReturn(InfectedLoaderFromAgentIdTest.class.getResourceAsStream("/initialSowingWrongCount.dat"));
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
