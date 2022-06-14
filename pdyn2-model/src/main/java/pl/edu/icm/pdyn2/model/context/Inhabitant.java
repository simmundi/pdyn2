package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithMapper
public final class Inhabitant {
    private Entity homeContext;
    private final List<Entity> contexts = new ArrayList<>();

    public List<Entity> getContexts() {
        return contexts;
    }

    public Entity getHomeContext() {
        return homeContext;
    }

    public void setHomeContext(Entity homeContext) {
        this.homeContext = homeContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inhabitant that = (Inhabitant) o;
        return Objects.equals(contexts, that.contexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contexts);
    }
}
