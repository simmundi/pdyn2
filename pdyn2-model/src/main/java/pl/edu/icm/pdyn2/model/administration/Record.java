package pl.edu.icm.pdyn2.model.administration;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class Record {

    private int day;
    private RecordType type;

    public Record() {
    }

    public Record(RecordType type, int day) {
        this.type = type;
        this.day = day;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record that = (Record) o;
        return day == that.day && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, type);
    }
}
