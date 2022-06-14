package pl.edu.icm.pdyn2.time;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

class SimulationTimerTest {

    @Test
    @DisplayName("Should initialize with the current date")
    void construct() {
        // given & execute
        SimulationTimer simulationTimer = new SimulationTimer("");

        // assert
        Assertions.assertThat(simulationTimer.getCurrentDate())
                .isToday();
    }

    @Test
    @DisplayName("Should initialize with the given date")
    void construct__given_date() {
        // given & execute
        SimulationTimer simulationTimer = new SimulationTimer("1977-04-01");

        // assert
        Assertions.assertThat(simulationTimer.getCurrentDate())
                .isEqualTo(LocalDate.of(1977, Month.APRIL, 1));
    }

    @Test
    @DisplayName("Should advance the time by the given number of days")
    void getCurrentDate() {
        // given
        SimulationTimer simulationTimer = new SimulationTimer("1977-04-01");

        // execute
        for (int i = 0; i < 30; i++) {
            simulationTimer.advanceOneDay();
        }

        // assert
        Assertions.assertThat(simulationTimer.getCurrentDate())
                .isEqualTo(LocalDate.of(1977, Month.MAY, 1));

    }
}
