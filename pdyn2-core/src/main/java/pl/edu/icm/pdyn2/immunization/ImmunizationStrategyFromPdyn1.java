package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.io.IOException;

public class ImmunizationStrategyFromPdyn1 implements ImmunizationStrategy {
    private final ImmunizationFromCsvProvider immunizationFromCsvProvider;

    @WithFactory
    public ImmunizationStrategyFromPdyn1(ImmunizationStrategyProvider provider,
                                         ImmunizationFromCsvProvider immunizationFromCsvProvider) {
        this.immunizationFromCsvProvider = immunizationFromCsvProvider;
        provider.registerImmunizationStrategy(this);
        try {
            immunizationFromCsvProvider.load();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public float getImmunizationCoefficient(Immunization immunization,
                                            ImmunizationStage immunizationStage,
                                            Load load,
                                            int day) {
        float coefficient = 0;
        if (immunization == null) return coefficient;

        for (ImmunizationEvent event : immunization.getEvents()) {
            var immunizationLoad = event.getLoad();
            var days = day - event.getDay();
            coefficient = Float.max(coefficient, (float) (immunizationFromCsvProvider.getCrossImmunity(immunizationLoad, load, immunizationStage) *
                    immunizationFromCsvProvider.getSFunction(immunizationLoad, immunizationStage, days)));
        }
        return coefficient;
    }
}
