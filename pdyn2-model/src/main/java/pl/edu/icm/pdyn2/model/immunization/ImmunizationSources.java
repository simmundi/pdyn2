package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public class ImmunizationSources {
    @MappedCollection(margin = 5)
    private final List<ImmunizationSource> immunizationSources = new ArrayList<>(5);

    public ImmunizationSources() {
    }

    public List<ImmunizationSource> getImmunizationSources() {
        return immunizationSources;
    }
}
