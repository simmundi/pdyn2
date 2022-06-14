package pl.edu.icm.pdyn2.model.administration;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithMapper
public class MedicalHistory {
    private final List<Record> records = new ArrayList<>();

    public List<Record> getRecords() {
        return records;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalHistory that = (MedicalHistory) o;
        return records.equals(that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }
}
