package pl.edu.icm.pdyn2.importer;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.pdyn1.ExportedId;
import pl.edu.icm.board.pdyn1.ExportedIdMapper;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AgentIdMappingLoader {

    private ExportedIdMapper mapper;
    private final WorkDir workDir;

    @WithFactory
    public AgentIdMappingLoader(WorkDir workDir) {
        this.workDir = workDir;
    }

    private void load(String filename) throws IOException {
        var status = Status.of("Loading ids mapping from file: " + filename);
        if (mapper != null) {
            status.problem("Mapper is not empty. Deleting existing data.");
        }
        mapper = new ExportedIdMapper();

        AtomicInteger counter = new AtomicInteger(0);
        var store = new ArrayStore();
        mapper.configureAndAttach(store);

        OrcStoreService orcStoreService = new OrcStoreService();
        orcStoreService.read(store, filename);

        status.done();
    }

    public void forEach(String filename, Consumer<ExportedId> consumer) {
        try {
            load(filename);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            consumer.accept(mapper.createAndLoad(row));
        }
    }
}
