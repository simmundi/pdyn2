package pl.edu.icm.pdyn2.importer;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ImmunizationEventsLoaderFromAgentId {
    private ImportedImmunizationEventMapper mapper;
    private final FileToStreamService fileToStreamService;
    private int capacity;

    @WithFactory
    public ImmunizationEventsLoaderFromAgentId(FileToStreamService fileToStreamService) {
        this.fileToStreamService = fileToStreamService;
    }

    private void load(String filename) throws IOException {
        var status = Status.of("Loading immunization events from file: " + filename);
        if (mapper != null) {
            status.problem("Mapper is not empty. Deleting existing data.");
        }
        mapper = new ImportedImmunizationEventMapper();

        CsvParserSettings csvParserSettings = new CsvParserSettings();
        csvParserSettings.setLineSeparatorDetectionEnabled(true);
        csvParserSettings.setHeaderExtractionEnabled(true);
        CsvParser csvParser = new CsvParser(csvParserSettings);

        var stream = fileToStreamService.filename(filename);
        stream.mark(Integer.MAX_VALUE);
        capacity = checkCapacity(csvParser, stream);
        stream.reset();
        AtomicInteger counter = new AtomicInteger(0);
        var store = new ArrayStore(capacity);
        mapper.configureAndAttach(store);

        csvParser.iterateRecords(stream,
                StandardCharsets.UTF_8).forEach(row -> {
            ImportedImmunizationEvent immunizationEvent = new ImportedImmunizationEvent();
            immunizationEvent.setId(row.getInt("id"));
            immunizationEvent.setDzien_zakazenia(row.getInt("dzien_zakazenia"));
            immunizationEvent.setOdmiana_szczepionki(row.getString("odmiana_szczepionki"));
            immunizationEvent.setOdmiana_wirusa(row.getString("odmiana_wirusa"));
            immunizationEvent.setHistoria_stanow(row.getInt("historia_stanow"));
            mapper.save(immunizationEvent, counter.get());
            counter.getAndIncrement();
        });
        status.done();
    }

    public void forEach(String filename, Consumer<ImportedImmunizationEvent> consumer) {

        try {
            load(filename);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            consumer.accept(mapper.createAndLoad(row));
        }
    }

    private int checkCapacity(CsvParser csvParser, InputStream stream) throws IOException {
        int lines = 0;
        var scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            scanner.nextLine();
            lines++;
        }
        return lines + 1;
    }

    public int getCapacity() {
        return capacity;
    }
}
