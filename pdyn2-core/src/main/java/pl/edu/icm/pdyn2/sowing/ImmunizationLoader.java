package pl.edu.icm.pdyn2.sowing;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ImmunizationLoader {

    private final RandomGenerator randomGenerator;
    private final FileToStreamService fileToStreamService;
    private ImmunizationRecordFromCsvMapper mapper;

    @WithFactory
    ImmunizationLoader(RandomProvider randomProvider, FileToStreamService fileToStreamService) {
        this.randomGenerator = randomProvider.getRandomGenerator(ImmunizationLoader.class);
        this.fileToStreamService = fileToStreamService;
    }

    public void load(String immunizationFilename) throws IOException {
        mapper = new ImmunizationRecordFromCsvMapper();
        var status = Status.of("Loading immunization data from file: " + immunizationFilename);
        var infectedStore = new ArrayStore(1500);
        mapper.configureAndAttach(infectedStore);
        CsvParserSettings csvParserSettings = new CsvParserSettings();
        csvParserSettings.setLineSeparatorDetectionEnabled(true);
        csvParserSettings.setHeaderExtractionEnabled(true);

        CsvParser csvParser = new CsvParser(csvParserSettings);
        AtomicInteger counter = new AtomicInteger(0);
        csvParser.iterateRecords(fileToStreamService.filename(immunizationFilename),
                StandardCharsets.UTF_8).forEach(row -> {
            ImmunizationRecordFromCsv immunizationRecordFromCsv = new ImmunizationRecordFromCsv();
            immunizationRecordFromCsv.setAgeSex(row.getString("ageSex"));
            immunizationRecordFromCsv.setDays(row.getString("days"));
            immunizationRecordFromCsv.setLoad(row.getString("load"));
            immunizationRecordFromCsv.setAreaCode(row.getString("areaCode"));
            immunizationRecordFromCsv.setRecordCount(row.getInt("count"));
            mapper.ensureCapacity(counter.get());
            mapper.save(immunizationRecordFromCsv, counter.get());
            counter.getAndIncrement();
        });
        status.done();
    }

    public void forEach(Consumer<ImmunizationRecordFromCsv> consumer, String immunizationFilename) {

        try {
            load(immunizationFilename);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            consumer.accept(mapper.createAndLoad(row));
        }
    }
}
