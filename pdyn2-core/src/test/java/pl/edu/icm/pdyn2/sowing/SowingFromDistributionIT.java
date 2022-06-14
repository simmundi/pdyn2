package pl.edu.icm.pdyn2.sowing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.commune.AdministrationAreaType;
import pl.edu.icm.board.geography.commune.Commune;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.geography.commune.PopulationService;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.administration.TestingService;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.pdyn2.time.SimulationTimer;

import java.io.FileNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SowingFromDistributionIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests();
    private final RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private final FileToStreamService fileToStreamService = new FileToStreamService();
    @Mock
    private SowingStrategyProvider sowingStrategyProvider;
    @Mock
    private CommuneManager communeManager;
    @Mock
    private StatsService statsService;
    @Mock
    private PopulationService populationService;
    @Mock
    private DiseaseStageTransitionsService diseaseStageTransitionsService;
    @Mock
    private Board board;
    @Mock
    private TestingService testingService;

    private final AgentStateService agentStateService = data.agentStateService;

    @BeforeEach
    public void before() throws FileNotFoundException {
        when(board.getEngine()).thenReturn(data.session.getEngine());
        when(fileToStreamService.filename("/sowingDistributionTest.csv")).thenReturn(SowingFromDistributionIT.class
                .getResourceAsStream("/sowingDistributionTest.csv"));
        when(diseaseStageTransitionsService.durationOf(any(), any(), anyInt())).thenReturn(5);
        when(populationService.typeFromLocation(any())).thenReturn(AdministrationAreaType.VILLAGE);
        var communeA = new Commune();
        communeA.setTeryt("0223013");
        var communeC = new Commune();
        communeC.setTeryt("0605014");
        when(communeManager.communeAt(data.cellA)).thenReturn(communeA);
        when(communeManager.communeAt(data.cellC)).thenReturn(communeC);
    }

    @Test
    public void sow() {
        // given
        var loader = new InfectedLoaderFromDistribution("/sowingDistributionTest.csv",
                randomProvider,
                fileToStreamService);

        var sowingFromDistribution = new SowingFromDistribution(sowingStrategyProvider,
                loader,
                statsService,
                board,
                randomProvider,
                communeManager,
                populationService,
                agentStateService,
                data.selectors,
                diseaseStageTransitionsService,
                testingService);
        data.session.close();

        // execute
        sowingFromDistribution.sow();

        // assert
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.INFECTIOUS_SYMPTOMATIC)).count()).isEqualTo(1);
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.LATENT)).count()).isEqualTo(1);
        assertThat(data.engine.streamDetached().filter(e -> e.get(HealthStatus.class) != null)
                .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.INFECTIOUS_ASYMPTOMATIC)).count()).isEqualTo(1);
    }
}
