package pl.edu.icm.pdyn2.isolation;

import net.snowyhollows.bento2.annotation.WithFactory;

public class IsolationConfig {
    final float baseProbabilityOfSelfIsolation;
    float selfIsolationWeight;

    @WithFactory
    public IsolationConfig(float baseProbabilityOfSelfIsolation) {
        this.baseProbabilityOfSelfIsolation = baseProbabilityOfSelfIsolation;
    }

    public float getBaseProbabilityOfSelfIsolation() {
        return baseProbabilityOfSelfIsolation;
    }

    public float getSelfIsolationWeight() {
        return selfIsolationWeight;
    }

    public IsolationConfig setSelfIsolationWeight(float selfIsolationWeight) {
        this.selfIsolationWeight = selfIsolationWeight;
        return this;
    }
}
