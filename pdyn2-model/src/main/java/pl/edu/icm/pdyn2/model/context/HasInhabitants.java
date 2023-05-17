package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public class HasInhabitants {
    private List<Entity> inhabitedBy = new ArrayList<>();
    public List<Entity> getInhabitedBy() {
        return inhabitedBy;
    }
}
