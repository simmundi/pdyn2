package pl.edu.icm.pdyn2.variantsowing;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.ExampleDataForIntegrationTests;
import pl.edu.icm.pdyn2.MockRandomProvider;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.index.CommuneClusteredSelectors;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static pl.edu.icm.trurl.ecs.util.Systems.sequence;

@ExtendWith(MockitoExtension.class)
public class VariantSowingIT {
    private final ExampleDataForIntegrationTests data = new ExampleDataForIntegrationTests(true);
    private final AgentStateService agentStateService = data.agentStateService;
    private final SimulationTimer simulationTimer = data.simulationTimer;
    @Mock
    private WorkDir workDir;

    private RandomProvider randomProvider = new MockRandomProvider();
    @Mock
    private StatsService statsService;
    @Mock
    private CommuneClusteredSelectors communeClusteredSelectors;

    @BeforeEach
    public void before() {
        when(workDir.openForReading(new File("/variantSowingTest.csv")))
                .thenReturn(VariantSowingIT.class.getResourceAsStream("/variantSowingTest.csv"));
        when(communeClusteredSelectors.personInTerytSelector(List.of("24")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "240102"),
                data.householdContext1.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
        )));
        when(communeClusteredSelectors.personInTerytSelector(List.of("012345")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "012345"),
                data.householdContext2.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
        )));
        when(communeClusteredSelectors.personInTerytSelector(List.of("10")))
                .thenReturn(() -> Stream.of(new Chunk(ChunkInfo.of(0, 10, "100102"),
                data.householdContext3.get(Household.class).getMembers().stream().mapToInt(Entity::getId)
        )));
    }

    @Test
    @DisplayName("Should change load according to the given data")
    public void test(){
        var loader = new VariantSowingFromCsvLoader("/variantSowingTest.csv", workDir);
        var variantSowingService = new VariantSowingService(agentStateService,
                randomProvider,
                communeClusteredSelectors,
                data.selectors,
                statsService);
        var variantSowingSystemBuilder = new VariantSowingFromCsvSystemBuilder(loader,
                variantSowingService,
                simulationTimer);
        variantSowingSystemBuilder.load();

        agentStateService.infect(data.agent1, Load.ALPHA);
        agentStateService.infect(data.agent2, Load.WILD);
        agentStateService.infect(data.agent3, Load.WILD);
        agentStateService.infect(data.agent4, Load.ALPHA);
        agentStateService.infect(data.agent5, Load.ALPHA);
        agentStateService.infect(data.agent6, Load.ALPHA);
        agentStateService.infect(data.agentA, Load.WILD);
        data.session.close();

        var variantSowing = variantSowingSystemBuilder.buildVariantSowingSystem();
        while (simulationTimer.getDaysPassed() < 4) {
            data.engine.execute(sequence(variantSowing, simulationTimer));
        }

        assertThat(data.agent1.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.ALPHA);
        assertThat(data.agent2.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.ALPHA);
        assertThat(data.agent3.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.ALPHA);
        assertThat(data.agent4.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.DELTA);
        assertThat(data.agent5.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.DELTA);
        assertThat(data.agent6.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.DELTA);
        assertThat(data.agentA.get(HealthStatus.class).getDiseaseLoad()).isEqualTo(Load.ALPHA);
    }
}
