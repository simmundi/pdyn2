package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;

public class SowingService {
    private final SowingStrategy sowingStrategy;

    @WithFactory
    public SowingService(SowingStrategyProvider provider) {
        this.sowingStrategy = provider.getInitialSowingStrategy();
    }

    public void sow() {
        sowingStrategy.sow();
    }
}
