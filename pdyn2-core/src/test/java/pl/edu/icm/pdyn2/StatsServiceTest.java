package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.progression.Stage;
import tech.tablesaw.api.Table;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    TemporaryFolder folder = new TemporaryFolder();

    @BeforeEach
    void before() throws IOException {
        folder.create();
    }

    @Test
    void createStatsOutputFileShouldNotAcceptSecondFile() {
        //given
        var statsService = new StatsService();
        //execute
        statsService.createStatsOutputFile(folder.getRoot() + "/disease_scenario.csv");
        //assert
        assertThatThrownBy(
                () -> statsService.createStatsOutputFile(folder.getRoot() + "/disease_scenario2.csv")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void writeDayToStatsOutputFileShouldThrowNullPointerException() {
        //given
        var statsService = new StatsService();
        //execute and assert
        assertThatThrownBy(statsService::writeDayToStatsOutputFile)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void writeStatsCorrectly() throws IOException {
        //given
        var statsService = new StatsService();
        var filename = folder.getRoot() + "/d_s.csv";
        //execute
        statsService.createStatsOutputFile(filename);

        //2 hospitalised_icu
        for (int i = 0; i < 2; i++) {
            statsService.tickStage(Stage.HOSPITALIZED_ICU);
        }

        //3 new latent
        for (int i = 0; i < 3; i++) {
            statsService.tickStageChange(Stage.LATENT);
            statsService.tickStage(Stage.LATENT);
        }

        //1 new hospitalized_icu
        statsService.tickStageChange(Stage.HOSPITALIZED_ICU);
        statsService.tickStage(Stage.HOSPITALIZED_ICU);

        //4 new hospitalized_pre_icu
        for (int i = 0; i < 4; i++) {
            statsService.tickStageChange(Stage.HOSPITALIZED_PRE_ICU);
            statsService.tickStage(Stage.HOSPITALIZED_PRE_ICU);
        }

        //5 tested positive
        for (int i = 0; i < 5; i++) {
            statsService.tickTestedPositive();
        }

        statsService.writeDayToStatsOutputFile();

        Table table = Table.read().file(filename);

        //assertThat
        assertThat(table.column("d1_LATENT").get(0)).isEqualTo(3);
        assertThat(table.column("d1_INFECTIOUS_ASYMPTOMATIC").get(0)).isEqualTo(0);
        assertThat(table.column("d1_INFECTIOUS_SYMPTOMATIC").get(0)).isEqualTo(0);
        assertThat(table.column("d1_HOSPITALIZED_NO_ICU").get(0)).isEqualTo(0);
        assertThat(table.column("d1_HOSPITALIZED_PRE_ICU").get(0)).isEqualTo(4);
        assertThat(table.column("d1_HOSPITALIZED_ICU").get(0)).isEqualTo(1);
        assertThat(table.column("d1_DECEASED").get(0)).isEqualTo(0);
        assertThat(table.column("d1_HEALTHY").get(0)).isEqualTo(0);
        assertThat(table.column("d1_ISOLATED").get(0)).isEqualTo(0);
        assertThat(table.column("d1_UNQUARANTINED").get(0)).isEqualTo(0);
        assertThat(table.column("d1_TESTEDPOSITIVE").get(0)).isEqualTo(5);
        assertThat(table.column("d1_QUARANTINED").get(0)).isEqualTo(0);
        assertThat(table.column("LATENT").get(0)).isEqualTo(3);
        assertThat(table.column("INFECTIOUS_ASYMPTOMATIC").get(0)).isEqualTo(0);
        assertThat(table.column("INFECTIOUS_SYMPTOMATIC").get(0)).isEqualTo(0);
        assertThat(table.column("HOSPITALIZED_NO_ICU").get(0)).isEqualTo(0);
        assertThat(table.column("HOSPITALIZED_PRE_ICU").get(0)).isEqualTo(4);
        assertThat(table.column("HOSPITALIZED_ICU").get(0)).isEqualTo(3);
        assertThat(table.column("DECEASED").get(0)).isEqualTo(0);
        assertThat(table.column("HEALTHY").get(0)).isEqualTo(0);
    }
}
