package pl.edu.icm.pdyn2.sowing;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.geography.commune.AdministrationAreaType;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class InfectedLoaderFromDistribution {

    private final String sowingFromDistributionFilename;
    private final RandomGenerator randomGenerator;
    private final FileToStreamService fileToStreamService;
    private SowingDistributionRecordFromCsvMapper mapper;

    @WithFactory
    public InfectedLoaderFromDistribution(String sowingFromDistributionFilename,
                                          RandomProvider randomProvider,
                                          FileToStreamService fileToStreamService) {
        this.sowingFromDistributionFilename = sowingFromDistributionFilename;
        this.randomGenerator = randomProvider.getRandomGenerator(InfectedLoaderFromDistribution.class);
        this.fileToStreamService = fileToStreamService;
    }

    private void load() throws IOException {
        if (mapper == null) {
            mapper = new SowingDistributionRecordFromCsvMapper();
            var status = Status.of("Loading infected from distribution file");
            var infectedStore = new ArrayStore(1500);
            mapper.configureAndAttach(infectedStore);
            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(fileToStreamService.filename(sowingFromDistributionFilename),
                    StandardCharsets.UTF_8).forEach(row -> {
                SowingDistributionRecordFromCsv sowingDistributionRecordFromCsv = new SowingDistributionRecordFromCsv();
                sowingDistributionRecordFromCsv.setAdministrationAreaType(row.getString("type"));
                sowingDistributionRecordFromCsv.setTeryt(
                        fixTeryt(row.getString("teryt"), sowingDistributionRecordFromCsv.getAdministrationAreaType()));
                sowingDistributionRecordFromCsv.setSex(row.getString("sex"), randomGenerator);
                sowingDistributionRecordFromCsv.setAgeRange(row.getInt("age"));
                sowingDistributionRecordFromCsv.setSymptomatic(row.getInt("symptoms"));
                sowingDistributionRecordFromCsv.setState(row.getShort("state"));
                mapper.ensureCapacity(counter.get());
                mapper.save(sowingDistributionRecordFromCsv, counter.get());
                counter.getAndIncrement();
            });
            status.done();
        }
    }

    private String fixTeryt(String teryt, AdministrationAreaType type) {

        //fixing wrong teryt for Wrocław, Kraków, Wałbrzych, Gliwice, Śląskie and Poznań

        if (teryt.equals("0223") && type == AdministrationAreaType.CITY_XL) {
            teryt = "0264";
        } else if (teryt.equals("1206") && type == AdministrationAreaType.CITY_XL) {
            teryt = "1261";
        } else if (teryt.equals("3021") && type == AdministrationAreaType.CITY_XL) {
            teryt = "3064";
        } else if (teryt.equals("0265") && type != AdministrationAreaType.CITY_XL) {
            teryt = "0221";
        } else if (teryt.equals("2473") && type != AdministrationAreaType.CITY_XL) {
            teryt = "2412";
        } else if (teryt.equals("2462") && type != AdministrationAreaType.CITY_XL) {
            teryt = "24";
        } else if (teryt.equals("2466") && type != AdministrationAreaType.CITY_XL) {
            teryt = "24";
        } else if (teryt.equals("2469") && type != AdministrationAreaType.CITY_XL) {
            teryt = "24";
        }
        return teryt;
    }

    public void forEach(Consumer<SowingDistributionRecordFromCsv> consumer) {

        try {
            load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            consumer.accept(mapper.createAndLoad(row));
        }
    }
}
