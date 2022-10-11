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
