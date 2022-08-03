package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;

import static com.google.common.base.Preconditions.checkState;

public class ImmunizationStrategyProvider {
    private ImmunizationStrategy immunizationStrategy;

    @WithFactory
    ImmunizationStrategyProvider() {

    }

    public ImmunizationStrategy getImmunizationStrategy() {
        checkState(immunizationStrategy != null, "Strategy not registered");
        return immunizationStrategy;
    }

    public void registerImmunizationStrategy(ImmunizationStrategy immunizationStrategy) {
        checkState(this.immunizationStrategy == null, "Initializing two Strategies is not supported");
        this.immunizationStrategy = immunizationStrategy;
    }
}
