package pl.edu.icm.pdyn2.administration;

import net.snowyhollows.bento2.annotation.WithFactory;

public class TestingConfig {
    private final float baseProbabilityOfTest;

    @WithFactory
    public TestingConfig(float baseProbabilityOfTest) {
        this.baseProbabilityOfTest = baseProbabilityOfTest;
    }

    public float getBaseProbabilityOfTest() {
        return baseProbabilityOfTest;
    }
}
