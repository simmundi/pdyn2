package pl.edu.icm.pdyn2.transmission;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.geography.GeographicalServices;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Attendee;
import pl.edu.icm.board.model.EducationalInstitution;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Named;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.model.Workplace;
import pl.edu.icm.board.util.RandomForChunkProvider;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.index.AreaIndex;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.csv.CsvWriter;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.store.tablesaw.TablesawStore;
import pl.edu.icm.trurl.store.tablesaw.TablesawStoreFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransmissionSystemIT {

    private TransmissionSystemBuilder transmissionSystemBuilder;

    private Board board;

    @Mock
    private AreaIndex areaIndex;

    @Mock
    private AgentStateService agentStateService;

    @Mock
    private GeographicalServices geographicalServices;

    private ContextsService contextsService;

    @Mock
    private Selectors selectors;

    @Mock
    private TransmissionConfig transmissionConfig;

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private RandomGenerator randomGenerator;

    @Mock
    private RandomForChunkProvider randomForChunkProvider;

    @Mock
    private SimulationTimer simulationTimer;

    @Mock
    StatsService statsService;

    @Mock
    private ImmunizationService immunizationService;

    @Mock
    private RelativeAlphaConfig relativeAlphaConfig;

    private TransmissionService transmissionService;

    @Mock
    private AreaClusteredSelectors areaClusteredSelectors;

    private EntitySystem transmissionSystem;

    @BeforeEach
    void before() throws IOException {
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setStoreFactory(new TablesawStoreFactory());
        CsvWriter csvWriter = new CsvWriter();
        board = new Board(engineConfiguration, csvWriter, null, null);
        board.require(
                Area.class,
                Location.class,
                Household.class,
                Person.class,
                Named.class,
                AdministrationUnit.class,
                EducationalInstitution.class,
                Workplace.class,
                Inhabitant.class,
                Attendee.class,
                HealthStatus.class,
                Context.class,
                Immunization.class
        );
        areaClusteredSelectors = new AreaClusteredSelectors(engineConfiguration, 10, 10);
        board.load(TransmissionSystemBuilder.class.getResourceAsStream("/transmissionTest.csv"));
        when(randomProvider.getRandomGenerator(TransmissionSystemBuilder.class)).thenReturn(randomGenerator);
        when(randomProvider.getRandomForChunkProvider(TransmissionSystemBuilder.class)).thenReturn(randomForChunkProvider);
        contextsService = new ContextsService(areaIndex);
        transmissionService = new TransmissionService(contextsService, relativeAlphaConfig, transmissionConfig, simulationTimer, immunizationService);
        transmissionSystemBuilder = new TransmissionSystemBuilder(
                transmissionService,
                agentStateService,
                areaClusteredSelectors,
                selectors,
                statsService,
                randomProvider);

        transmissionSystem = transmissionSystemBuilder.buildTransmissionSystem();

        when(immunizationService.getImmunizationCoefficient(any(), any(), any(), anyInt())).thenReturn(0.5f);
        when(transmissionConfig.getAlpha()).thenReturn(1.0f);
        when(transmissionConfig.getTotalWeightForContextType(any())).thenReturn(1.0f);
        when(randomGenerator.nextDouble()).thenReturn(0.2);
        when(relativeAlphaConfig.getRelativeAlpha(any())).thenReturn(0.5f);
    }

    @Test
    @Disabled
    void test() throws IOException {
        board.getEngine().execute(transmissionSystem);
        var table = ((TablesawStore)board.getEngine().getComponentStore()).asTable("entities");

        assertThat(table.where(table.stringColumn("health.stage").isEqualTo("OBJAWOWY"))
                .rowCount()).isEqualTo(1);
        assertThat(table.where(table.stringColumn("health.stage").isEqualTo("LATENTNY"))
                .rowCount()).isEqualTo(1);

        board.save("transmissionTestOutput.csv");
    }

}
