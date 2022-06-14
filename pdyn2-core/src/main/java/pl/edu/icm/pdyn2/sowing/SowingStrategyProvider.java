package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento2.annotation.WithFactory;

import static com.google.common.base.Preconditions.checkState;

public class SowingStrategyProvider {
    private SowingStrategy sowingStrategy;

    @WithFactory
    SowingStrategyProvider() {

    }

    public SowingStrategy getInitialSowingStrategy() {
        checkState(sowingStrategy != null, "Strategy not registered");
        return sowingStrategy;
    }

    public void registerInitialSowingStrategy(SowingStrategy sowingStrategy) {
        checkState(this.sowingStrategy == null, "Initializing two Strategies is not supported");
        this.sowingStrategy = sowingStrategy;
    }
}
