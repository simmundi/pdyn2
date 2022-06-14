package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public final class Context {
    private ContextType contextType;
    private float agentCount;
    private List<Contamination> contaminations = new ArrayList<>(3);

    public Context() {
    }

    public Context(ContextType contextType) {
        this.contextType = contextType;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    public List<Contamination> getContaminations() {
        return contaminations;
    }

    public Contamination getContaminationByLoad(Load load) {
        int size = contaminations.size();
        for (int i = 0; i < size; i++) {
            Contamination candidate = contaminations.get(i);
            if (candidate.getLoad() == load) {
                return candidate;
            }
        }
        Contamination fresh = new Contamination();
        contaminations.add(fresh);
        fresh.setLoad(load);
        return fresh;
    }

    public Contamination changeContaminationLevel(Load load, float delta) {
        return getContaminationByLoad(load).changeLevel(delta);
    }

    public float getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(float agentCount) {
        this.agentCount = agentCount;
    }

    public void updateAgentCount(float delta) {
        this.agentCount += delta;
    }

    @Override
    public String toString() {
        return "Context{" +
                "contextType=" + contextType +
                ", agentCount=" + agentCount +
                ", contaminations=" + contaminations +
                '}';
    }
}
