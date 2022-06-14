package pl.edu.icm.pdyn2.immunization;

import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.Load;

public interface ImmunizationStrategy {
    float getImmunizationCoefficient(Immunization immunization,
                                     ImmunizationStage immunizationStage,
                                     Load load,
                                     int day);
}
