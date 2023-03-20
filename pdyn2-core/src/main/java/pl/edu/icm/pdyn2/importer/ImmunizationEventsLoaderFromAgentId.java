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

package pl.edu.icm.pdyn2.importer;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ImmunizationEventsLoaderFromAgentId {
    private ImportedImmunizationEventMapper mapper;
    private final WorkDir workDir;
    private int capacity;

    @WithFactory
    public ImmunizationEventsLoaderFromAgentId(WorkDir workDir) {
        this.workDir = workDir;
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
        var stream = workDir.openForReading(new File(filename));
        stream.mark(Integer.MAX_VALUE);
        capacity = checkCapacity(stream);
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

    private int checkCapacity(InputStream stream) {
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
