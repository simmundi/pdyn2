package pl.edu.icm.pdyn2.importer;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.board.pdyn1.ExportedId;
import pl.edu.icm.board.pdyn1.ExportedIdMapper;
import pl.edu.icm.trurl.io.orc.OrcStoreService;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
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

        var store = new ArrayStore();
        mapper.configureAndAttach(store);

        var file = workDir.absolutizeFile(new File(filename)).getPath();
        OrcStoreService orcStoreService = new OrcStoreService();
        orcStoreService.read(store, file);
        mapper.attachStore(store);
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
