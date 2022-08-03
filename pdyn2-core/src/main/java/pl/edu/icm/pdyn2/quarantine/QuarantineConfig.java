package pl.edu.icm.pdyn2.quarantine;

import net.snowyhollows.bento.annotation.WithFactory;

public class QuarantineConfig {
    private final int quarantineLengthDays;

    @WithFactory
    public QuarantineConfig(int quarantineLengthDays) {
        this.quarantineLengthDays = quarantineLengthDays;
    }

    public int getQuarantineLengthDays() {
        return quarantineLengthDays;
    }
}
