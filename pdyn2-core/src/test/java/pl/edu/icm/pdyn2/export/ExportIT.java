package pl.edu.icm.pdyn2.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.administration.TestingConfig;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.context.ContextInfectivityClass;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExportIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests();
    private final RandomProvider randomProvider = new MockRandomProvider();
    private final AgentStateService agentStateService = data.agentStateService;
    @Mock
    private DiseaseStageTransitionsService transitionsService;
    @Mock
    private StatsService statsService;
    @Mock
    Filesystem filesystem;
    @Mock
    private Board board;
    @Mock
    private CommuneManager communeManager;
    @Captor
    ArgumentCaptor<File> file;

    EpidemicExporter epidemicExporter;
    AgentExporter agentExporter;

    private ByteArrayOutputStream results;

    @BeforeEach
    public void before() throws FileNotFoundException {
        results = new ByteArrayOutputStream();
        when(board.getEngine()).thenReturn(data.session.getEngine());
        when(filesystem.openForWriting(file.capture())).thenReturn(results);
    }

    @Test
    @DisplayName("Should export epidemic data correctly")
    public void exportEpidemic() {
        when(transitionsService.durationOf(Load.WILD, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(Load.WILD, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(Load.OMICRON, Stage.HOSPITALIZED_ICU, 18)).thenReturn(2);
        when(transitionsService.durationOf(Load.OMICRON, Stage.HOSPITALIZED_PRE_ICU, 18)).thenReturn(4);
        when(transitionsService.durationOf(Load.OMICRON, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(Load.OMICRON, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(Load.DELTA, Stage.INFECTIOUS_ASYMPTOMATIC, 18)).thenReturn(5);
        when(transitionsService.durationOf(Load.DELTA, Stage.LATENT, 18)).thenReturn(7);

        when(transitionsService.durationOf(Load.OMICRON, Stage.HOSPITALIZED_NO_ICU, 18)).thenReturn(3);
        when(transitionsService.durationOf(Load.OMICRON, Stage.INFECTIOUS_SYMPTOMATIC, 18)).thenReturn(6);
        when(transitionsService.durationOf(Load.OMICRON, Stage.LATENT, 18)).thenReturn(7);

        // given
        epidemicExporter = new EpidemicExporter("exportTest",
                board,
                filesystem,
                transitionsService,
                data.selectors);

        TestingService testingService = new TestingService(
                data.simulationTimer,
                randomProvider,
                statsService,
                agentStateService, new TestingConfig(1.0f));

        var sowingSource = new EnumSampleSpace<>(ContextInfectivityClass.class);
        sowingSource.changeOutcome(ContextInfectivityClass.SOWING, 1.0f);
        var source1 = new EnumSampleSpace<>(ContextInfectivityClass.class);
        source1.changeOutcome(ContextInfectivityClass.HOUSEHOLD, 0.2f);
        source1.changeOutcome(ContextInfectivityClass.STREET, 0.1f);
        source1.changeOutcome(ContextInfectivityClass.KINDERGARTEN, 0.4f);
        source1.changeOutcome(ContextInfectivityClass.SCHOOL, 0.3f);
        var source2 = new EnumSampleSpace<>(ContextInfectivityClass.class);
        source2.changeOutcome(ContextInfectivityClass.WORKPLACE, 0.3f);
        source2.changeOutcome(ContextInfectivityClass.HOUSEHOLD, 0.4f);
        source2.changeOutcome(ContextInfectivityClass.UNIVERSITY, 0.2f);
        source2.changeOutcome(ContextInfectivityClass.BIG_UNIVERSITY, 0.1f);

        agentStateService.infect(data.agent1, Load.WILD, sowingSource, 10);
        agentStateService.progressToDiseaseStage(data.agent1, Stage.INFECTIOUS_SYMPTOMATIC, 7);
        agentStateService.infect(data.agentA, Load.DELTA, sowingSource, 3);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agent1, Stage.HEALTHY);
        advance(1);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.INFECTIOUS_ASYMPTOMATIC);
        advance(5);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.HEALTHY);
        advance(1);
        agentStateService.infect(data.agent2, Load.ALPHA, source1);
        agentStateService.infect(data.agent3, Load.DELTA, source2);
        agentStateService.infect(data.agent7, Load.OMICRON, source1);
        advance(7);
        testingService.maybeTestAgent(data.agent7);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.HOSPITALIZED_PRE_ICU);
        advance(4);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.HOSPITALIZED_ICU);
        advance(2);
        agentStateService.progressToDiseaseStage(data.agent7, Stage.DECEASED);
        agentStateService.infect(data.agentA, Load.OMICRON, source2);
        advance(7);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.INFECTIOUS_SYMPTOMATIC);
        advance(6);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.HOSPITALIZED_NO_ICU);
        advance(3);
        agentStateService.progressToDiseaseStage(data.agentA, Stage.DECEASED);
        data.session.close();

        // execute
        epidemicExporter.exportCsv();

        // assert
        assertThat(results.toString()).isEqualTo("id,dzien_zakazenia,householdSource,workplaceSource,kindergartenSource," +
                "schoolSource,universitySource,bigUniversitySource,streetSource,sowingSource,odmiana_wirusa," +
                "odmiana_szczepionki,historia_stanow,test\n" +
                "100006,-10,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0,-1,26,0\n" +
                "100012,10,0.2,0.0,0.4,0.3,0.0,0.0,0.1,0.0,3,-1,458,1\n" +
                "100015,-3,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,2,-1,22,0\n" +
                "100015,29,0.4,0.3,0.0,0.0,0.2,0.1,0.0,0.0,3,-1,298,0\n");
    }

    @Test
    @DisplayName("Should export agents correctly")
    public void exportAgents() {
        var commune = new Commune("1412132", "a",
                Set.of(KilometerGridCell.fromPl1992ENKilometers(111,222)));
        when(communeManager.communeAt(any())).thenReturn(commune);
        agentExporter = new AgentExporter("exportTest",
                board,
                filesystem,
                data.selectors,
                communeManager);
        data.session.close();
        agentExporter.exportCsv();
        assertThat(results.toString()).isEqualTo("id,age,sex,areaCode,n,e\n" +
                "100006,18,M,1412132,825,121\n" +
                "100007,18,M,1412132,825,121\n" +
                "100008,18,M,1412132,825,121\n" +
                "100009,18,M,1412132,825,121\n" +
                "100010,18,M,1412132,825,121\n" +
                "100011,18,M,1412132,825,121\n" +
                "100012,18,M,1412132,825,123\n" +
                "100013,18,M,1412132,825,123\n" +
                "100014,18,M,1412132,825,123\n" +
                "100015,18,M,1412132,825,123\n");
    }

    private void advance(int days) {
        for (int i = 0; i < days; i++) {
            data.simulationTimer.advanceOneDay();
        }
    }
}
