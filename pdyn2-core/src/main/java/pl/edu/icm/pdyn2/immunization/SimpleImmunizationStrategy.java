package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

public class SimpleImmunizationStrategy implements ImmunizationStrategy {
    private final float simpleImmunizationStrategyValue;

    @WithFactory
    public SimpleImmunizationStrategy(ImmunizationStrategyProvider provider, float simpleImmunizationStrategyValue){
        this.simpleImmunizationStrategyValue = simpleImmunizationStrategyValue;
        provider.registerImmunizationStrategy(this);
    }

    @Override
    public float getImmunizationCoefficient(Immunization immunization, ImmunizationStage immunizationStage, Load load, int day) {
        return simpleImmunizationStrategyValue;
    }
}
