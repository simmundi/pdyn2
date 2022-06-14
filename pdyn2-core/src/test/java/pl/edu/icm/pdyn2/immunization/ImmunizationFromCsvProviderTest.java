package pl.edu.icm.pdyn2.immunization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImmunizationFromCsvProviderTest {

    @Mock
    private FileToStreamService streamService = new FileToStreamService();

    private ImmunizationFromCsvProvider immunizationFromCsvProvider;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        when(this.streamService.filename("sFunctionTest.csv")).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/sFunctionTest.csv"));
        when(this.streamService.filename("crossImmunityTest.csv")).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest.csv"));
        when(this.streamService.filename("crossImmunityTest2.csv")).thenReturn(ImmunizationFromCsvProviderTest.class.getResourceAsStream("/crossImmunityTest2.csv"));

        immunizationFromCsvProvider = new ImmunizationFromCsvProvider(streamService,
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