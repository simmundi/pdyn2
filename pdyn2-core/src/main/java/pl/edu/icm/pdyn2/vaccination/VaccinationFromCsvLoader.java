/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2.vaccination;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
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
    private final Loads loads;

    @WithFactory
    public VaccinationFromCsvLoader(String vaccinationFilename, WorkDir workDir, Loads loads) {
        this.vaccinationFilename = vaccinationFilename;
        this.workDir = workDir;
        this.loads = loads;
    }

    public void load() throws IOException {
        if (mapper == null) {
            mapper = new VaccinationRecordFromCsvMapper(loads);
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
                return loads.PFIZER;
            case "1":
                return loads.BOOSTER;
        }
        throw new IllegalArgumentException("Could not find value for: vaccine=" + vaccineLoad);
    }
}
