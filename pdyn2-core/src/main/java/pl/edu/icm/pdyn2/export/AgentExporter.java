package pl.edu.icm.pdyn2.export;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.Board;
import pl.edu.icm.board.export.ExportedAgentMapper;
import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.board.geography.commune.CommuneManager;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentExporter {
    private final Board board;
    private final Filesystem filesystem;
    private final String defaultPopulationOutputFilename;
    private final Selectors selectors;
    private final CommuneManager communeManager;
    private ExportedAgentMapper mapper;

    @WithFactory
    public AgentExporter(String defaultPopulationOutputFilename,
                         Board board,
                         Selectors selectors,
                         CommuneManager communeManager) {
        this.defaultPopulationOutputFilename = defaultPopulationOutputFilename;
        this.board = board;
        this.selectors = selectors;
        this.filesystem = new DefaultFilesystem();
        this.communeManager = communeManager;
    }

    public AgentExporter(String defaultPopulationOutputFilename,
                         Board board,
                         Filesystem filesystem,
                         Selectors selectors,
                         CommuneManager communeManager) {
        this.defaultPopulationOutputFilename = defaultPopulationOutputFilename;
        this.board = board;
        this.filesystem = filesystem;
        this.selectors = selectors;
        this.communeManager = communeManager;
    }

    public void exportOrc() {
        Preconditions.checkState(mapper == null, "Mapper is not empty");
        mapper = new ExportedAgentMapper();
        var capacity = board.getEngine().getMapperSet().classToMapper(Inhabitant.class).getCount();
        Store store = new ArrayStore(capacity);
        mapper.configureAndAttach(store);
        AtomicInteger counter = new AtomicInteger(0);

        board.getEngine().execute(EntityIterator
                .select(selectors.allWithComponents(Inhabitant.class))
                .detachEntities()
                .forEach(Inhabitant.class, (e, inhabitant) -> {
                    var location = inhabitant.getHomeContext().get(Location.class);
                    var cell = KilometerGridCell.fromLocation(location);
                    var person = e.get(Person.class);
                    var sex = person.getSex();
                    var age = person.getAge();
                    var areaCode = communeManager.communeAt(cell).getTeryt();

                    var exportedAgent = new ExportedAgent();
                    exportedAgent.setSex(sex.toString());
                    exportedAgent.setAge(age);
                    exportedAgent.setAreaCode(areaCode);
                    exportedAgent.setE(location.getE());
                    exportedAgent.setN(location.getN());

                    mapper.ensureCapacity(counter.get());
                    mapper.save(exportedAgent, counter.getAndIncrement());
                }));
        try {
            OrcStoreService orcStoreService = new OrcStoreService();
            orcStoreService.write(store, defaultPopulationOutputFilename + "_agents.orc");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void exportCsv() {
        try {
            OutputStream outputStream = this.filesystem.openForWriting(new File(defaultPopulationOutputFilename + "_agents.csv"));
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
            bufferedWriter.write("id,age,sex,areaCode,n,e\n");
            board.getEngine().execute(EntityIterator
                    .select(selectors.allWithComponents(Inhabitant.class))
                    .detachEntities()
                    .forEach(Inhabitant.class, (e, inhabitant) -> {
                        var location = inhabitant.getHomeContext().get(Location.class);
                        var cell = KilometerGridCell.fromLocation(location);
                        var person = e.get(Person.class);
                        var sex = person.getSex();
                        var age = person.getAge();
                        var areaCode = communeManager.communeAt(cell).getTeryt();
                        try {
                            bufferedWriter.write(e.getId() + ",");
                            bufferedWriter.write(age + ",");
                            bufferedWriter.write(sex + ",");
                            bufferedWriter.write(areaCode + ",");
                            bufferedWriter.write(cell.getN() + ",");
                            bufferedWriter.write(cell.getE() + "\n");
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }

                    }));
            bufferedWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
