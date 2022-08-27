package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithMapper
public class Immunization {
    @MappedCollection(margin = 5)
    private List<ImmunizationEvent> events = new ArrayList<>(5);

    public List<ImmunizationEvent> getEvents() {
        return events;
    }

    public void add(ImmunizationEvent immunizationEvent) {
        events.add(immunizationEvent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Immunization that = (Immunization) o;
        return Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }
}
