package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ImmunizationStrategyFromPdyn1Rewritten implements ImmunizationStrategy {
    private final Map<ImmunizationStage, Map<Load, IntToDoubleFunction>> sFunction = new EnumMap<>(ImmunizationStage.class);
    private final Map<ImmunizationStage, Map<Load, Map<Load, Double>>> crossImmunity = new EnumMap<>(ImmunizationStage.class);

    @WithFactory
    public ImmunizationStrategyFromPdyn1Rewritten(ImmunizationStrategyProvider provider) {
        provider.registerImmunizationStrategy(this);
        sFunction.putAll(Map.of(
                ImmunizationStage.LATENTNY, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.ALPHA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.DELTA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.OMICRON, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.PFIZER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.BOOSTER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30))
                )),
                ImmunizationStage.OBJAWOWY, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.ALPHA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.DELTA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.OMICRON, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.PFIZER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.BOOSTER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30))
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.ALPHA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.DELTA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.OMICRON, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.PFIZER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.BOOSTER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30))
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.ALPHA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.DELTA, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.OMICRON, day -> day < 30 ? 1.0 : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.PFIZER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30)),
                        Load.BOOSTER, day -> day < 30 ? min(day / 14.0, 1.0) : max(0.900000, 1.0 - 0.0017 * (day - 30))
                ))
        ));

        crossImmunity.putAll(Map.of(
                ImmunizationStage.LATENTNY, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.9),
                        Load.OMICRON, generateCrossImmunity(0.9, 0.9, 0.9, 1.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9)
                )),
                ImmunizationStage.OBJAWOWY, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.9),
                        Load.OMICRON, generateCrossImmunity(0.9, 0.9, 0.9, 1.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9)
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.9),
                        Load.OMICRON, generateCrossImmunity(0.9, 0.9, 0.9, 1.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9)
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.9),
                        Load.OMICRON, generateCrossImmunity(0.9, 0.9, 0.9, 1.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.9)
                ))
        ));

    }

    private EnumMap<Load, Double> generateCrossImmunity(double wildValue, double alphaValue, double deltaValue, double omicronValue) {
        var wildCrossImmunityInfection = new EnumMap<Load, Double>(Load.class);
        wildCrossImmunityInfection.put(Load.WILD, wildValue);
        wildCrossImmunityInfection.put(Load.ALPHA, alphaValue);
        wildCrossImmunityInfection.put(Load.DELTA, deltaValue);
        wildCrossImmunityInfection.put(Load.OMICRON, omicronValue);
        return wildCrossImmunityInfection;
    }

    @Override
    public float getImmunizationCoefficient(Immunization immunization, ImmunizationStage immunizationStage, Load load, int day) {
        float coefficient = 0;
        if (immunization == null) {
            return coefficient;
        }

        for (ImmunizationEvent event : immunization.getEvents()) {
            var immunizationLoad = event.getLoad();
            var days = day - event.getDay();
            coefficient = Float.max(coefficient, (float) (crossImmunity.get(immunizationStage).get(immunizationLoad).get(load) *
                    sFunction.get(immunizationStage).get(immunizationLoad).applyAsDouble(days)));
        }
        return coefficient;
    }
}
