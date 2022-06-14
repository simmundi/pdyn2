package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

public class ImmunizationService {
    private final ImmunizationStrategy immunizationStrategy;

    @WithFactory
    public ImmunizationService(ImmunizationStrategyProvider provider) {
        this.immunizationStrategy = provider.getImmunizationStrategy();
    }

    public float getImmunizationCoefficient(Immunization immunization,
                                            ImmunizationStage immunizationStage,
                                            Load load,
                                            int day) {
        return immunizationStrategy.getImmunizationCoefficient(immunization, immunizationStage, load, day);
    }
}
