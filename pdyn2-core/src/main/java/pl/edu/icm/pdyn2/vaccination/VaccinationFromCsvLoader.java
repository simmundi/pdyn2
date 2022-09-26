package pl.edu.icm.pdyn2.vaccination;

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

public class VaccinationFromCsvLoader {
    private final String vaccinationFilename;
    private final WorkDir workDir;
    private VaccinationRecordFromCsvMapper mapper;

    @WithFactory
    public VaccinationFromCsvLoader(String vaccinationFilename, WorkDir workDir) {
        this.vaccinationFilename = vaccinationFilename;
        this.workDir = workDir;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new VaccinationRecordFromCsvMapper();
            var vaccinationStore = new ArrayStore(1000);
            mapper.configureStore(vaccinationStore);
            mapper.attachStore(vaccinationStore);

            CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.setLineSeparatorDetectionEnabled(true);
            csvParserSettings.setHeaderExtractionEnabled(true);

            CsvParser csvParser = new CsvParser(csvParserSettings);
            AtomicInteger counter = new AtomicInteger(0);
            csvParser.iterateRecords(workDir.openForReading(new File(vaccinationFilename)), StandardCharsets.UTF_8)
                    .forEach(row -> {
                        VaccinationRecordFromCsv vaccinationFromCsv = new VaccinationRecordFromCsv();
                        vaccinationFromCsv.setVaccineCount(row.getInt("licz_szcz"));
                        vaccinationFromCsv.setDay(row.getInt("dzien_symulacji"));
                        vaccinationFromCsv.setLoad(load(row.getString("id_szczep")));
                        vaccinationFromCsv.setMinAge(row.getInt("min_wiek"));
                        vaccinationFromCsv.setMaxAge(row.getInt("max_wiek"));
                        vaccinationFromCsv.setTeryts(row.getString("teryty"));
                        mapper.save(vaccinationFromCsv, counter.get());
                        counter.getAndIncrement();
                    });
        }
    }

    public void forEach(Consumer<VaccinationRecordFromCsv> consumer) {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int row = 0; row < mapper.getCount(); row++) {
            VaccinationRecordFromCsv vaccinationFromCsv = new VaccinationRecordFromCsv();
            mapper.load(null, vaccinationFromCsv, row);
            consumer.accept(vaccinationFromCsv);
        }
    }

    private Load load(String vaccineLoad) {
        switch (vaccineLoad) {
            case "0":
                return Load.PFIZER;
            case "1":
                return Load.BOOSTER;
        }
        throw new IllegalArgumentException("Could not find value for: vaccine=" + vaccineLoad);
    }
}
