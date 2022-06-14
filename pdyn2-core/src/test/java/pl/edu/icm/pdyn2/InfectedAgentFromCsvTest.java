package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.Test;
import pl.edu.icm.pdyn2.sowing.InfectedAgentFromCsv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class InfectedAgentFromCsvTest {

    @Test
    void constructorTest() {
        new InfectedAgentFromCsv(0, 0, 0);
        new InfectedAgentFromCsv(0, 0, 1);
        try {
            var v = new InfectedAgentFromCsv(0, 0, 2);
            fail("Exception was expected for symptomatic = 2");
        } catch (RuntimeException e) {
        }
        assertThatThrownBy(() -> new InfectedAgentFromCsv(0, 0, 2))
                .hasMessageContaining("symptomatic != 1 || symptomatic != 0, symptomatic = 2")
                .isInstanceOf(IllegalArgumentException.class);
    }

}
