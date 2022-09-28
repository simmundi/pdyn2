package pl.edu.icm.pdyn2.variantsowing;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VariantSowingFromCsvLoader {

    private final String variantSowingFilename;
    private final WorkDir workDir;
    private VariantSowingRecordFromCsvMapper mapper;

    @WithFactory
    public VariantSowingFromCsvLoader(String variantSowingFilename, WorkDir workDir) {
        this.variantSowingFilename = variantSowingFilename;
        this.workDir = workDir;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new VariantSowingRecordFromCsvMapper();
            var variantStore = new ArrayStore(1000);
            mapper.configureStore(variantStore);
            mapper.attachStore(variantStore);

            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(workDir.openForReading(new File(variantSowingFilename)), StandardCharsets.UTF_8)
                    .forEach(row -> {
                        VariantSowingRecordFromCsv variantFromCsv = new VariantSowingRecordFromCsv();
                        variantFromCsv.setSowingCount(row.getInt("licz_zm_odm"));
                        variantFromCsv.setDay(row.getInt("dzien_symulacji"));
                        variantFromCsv.setLoad(load(row.getString("odmiana")));
                        variantFromCsv.setMinAge(row.getInt("min_wiek"));
                        variantFromCsv.setMaxAge(row.getInt("max_wiek"));
                        variantFromCsv.setTeryts(row.getString("teryty"));
                        mapper.save(variantFromCsv, counter.get());
                        counter.getAndIncrement();
                    });
        }
    }

    public void forEach(Consumer<VariantSowingRecordFromCsv> consumer) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            VariantSowingRecordFromCsv variantFromCsv = new VariantSowingRecordFromCsv();
            mapper.load(null, variantFromCsv, row);
            consumer.accept(variantFromCsv);
        }
    }

    private Load load(String diseaseLoad) {
        switch (diseaseLoad) {
            case "0":
                return Load.WILD;
            case "1":
                return Load.ALPHA;
            case "2":
                return Load.DELTA;
            case "3":
                return Load.OMICRON;
            case "4":
                return Load.BA2;
            case "5":
                return Load.BA45;
        }
        throw new IllegalArgumentException("Could not find value for: disease=" + diseaseLoad);
    }
}
