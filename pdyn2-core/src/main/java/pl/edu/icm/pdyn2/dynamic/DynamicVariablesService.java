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

package pl.edu.icm.pdyn2.dynamic;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class DynamicVariablesService {
    public enum DynamicVariable {
        TERYT("TERYT"),
        HOUSEHOLD_W("household_wm"),
        KINDERGARTEN_W("kindergarten_wm"),
        SCHOOL_W("school_wm"),
        WORKPLACE_W("workplace_wm"),
        UNIVERSITY_W("university_wm"),
        BIG_UNIVERSITY_W("big_university_wm"),
        STREET_W("streets_wm"),
        LOWER_AGE_SCHOOL("lower_age_schools_open"),
        UPPER_AGE_SCHOOL("upper_age_schools_open"),
        ISOLATION_P("fraction_of_symp_not_isolated"),
        TRAVEL_P("probability_of_travel"),
        TRAVEL_ENDING_P("probability_of_ending_travel");

        private final String csvColumn;

        DynamicVariable(String csvColumn) {
            this.csvColumn = csvColumn;
        }
    }

    private EnumMap<DynamicVariable, NavigableMap<LocalDate, String>> values = new EnumMap<>(DynamicVariable.class);

    @WithFactory
    public DynamicVariablesService(WorkDir workDir, String dynamicVariablesFilename) {
        File dynamicVariables = new File(dynamicVariablesFilename);
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically(new char[]{',', '\t'});
        settings.setHeaderExtractionEnabled(true);
        CsvParser csvParser = new CsvParser(settings);

        try (InputStream inputStream = workDir.openForReading(dynamicVariables)) {
            List<Record> records = csvParser.parseAllRecords(inputStream);
            for (Record record : records) {
                LocalDate date = LocalDate.parse(record.getString("date"));
                for (DynamicVariable variable : DynamicVariable.values()) {
                    String value = record.getString(variable.csvColumn);
                    if (value != null) {
                        values
                                .computeIfAbsent(variable, (unused) -> new TreeMap<>())
                                .put(date, value);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValueForDate(DynamicVariable type, LocalDate date) {
        return values.get(type).floorEntry(date).getValue();
    }

    public float getFloatForDate(DynamicVariable type, LocalDate date) {
        return Float.parseFloat(getValueForDate(type, date));
    }
}
