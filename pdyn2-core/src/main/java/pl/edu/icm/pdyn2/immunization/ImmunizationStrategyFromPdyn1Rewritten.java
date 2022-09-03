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
                        Load.WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.OMICRON, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.OBJAWOWY, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.OMICRON, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        Load.BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> 1.0,
                        Load.ALPHA, day -> 1.0,
                        Load.DELTA, day -> 1.0,
                        Load.OMICRON, day -> 1.0,
                        Load.BA2, day -> 1.0,
                        Load.BA45, day -> 1.0,
                        Load.PFIZER, day -> min(day / 14.0, 1.0),
                        Load.BOOSTER, day ->  min(day / 14.0, 1.0)
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new EnumMap<Load, IntToDoubleFunction>(Map.of(
                        Load.WILD, day -> 1.0,
                        Load.ALPHA, day -> 1.0,
                        Load.DELTA, day -> 1.0,
                        Load.OMICRON, day -> 1.0,
                        Load.BA2, day -> 1.0,
                        Load.BA45, day -> 1.0,
                        Load.PFIZER, day -> min(day / 14.0, 1.0),
                        Load.BOOSTER, day ->  min(day / 14.0, 1.0)
                ))
        ));

        crossImmunity.putAll(Map.of(
                ImmunizationStage.LATENTNY, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.76, 0.76, 0.76),
                        Load.OMICRON, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        Load.BA2, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        Load.BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.9, 0.9, 1.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.875, 0.875, 0.875)
                )),
                ImmunizationStage.OBJAWOWY, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        Load.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        Load.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.0, 0.0, 0.0),
                        Load.OMICRON, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        Load.BA2, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        Load.BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        Load.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        Load.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0)
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(0.8, 0.77, 0.77, 0.5, 0.5, 0.5),
                        Load.ALPHA, generateCrossImmunity(0.77, 0.8, 0.77, 0.5, 0.5, 0.5),
                        Load.DELTA, generateCrossImmunity(0.77, 0.77, 0.8, 0.5, 0.5, 0.5),
                        Load.OMICRON, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        Load.BA2, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        Load.BA45, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        Load.PFIZER, generateCrossImmunity(0.77, 0.77, 0.77, 0.5, 0.5, 0.5),
                        Load.BOOSTER, generateCrossImmunity(0.82, 0.82, 0.82, 0.72, 0.72, 0.72)
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new EnumMap<>(Map.of(
                        Load.WILD, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        Load.ALPHA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        Load.DELTA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        Load.OMICRON, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        Load.BA2, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        Load.BA45, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        Load.PFIZER, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        Load.BOOSTER, generateCrossImmunity(0.90, 0.90, 0.90, 0.84, 0.84, 0.84)
                ))
        ));

    }

    private EnumMap<Load, Double> generateCrossImmunity(double wildValue, double alphaValue, double deltaValue, double omicronValue, double ba2Value, double ba45Value) {
        var wildCrossImmunityInfection = new EnumMap<Load, Double>(Load.class);
        wildCrossImmunityInfection.put(Load.WILD, wildValue);
        wildCrossImmunityInfection.put(Load.ALPHA, alphaValue);
        wildCrossImmunityInfection.put(Load.DELTA, deltaValue);
        wildCrossImmunityInfection.put(Load.OMICRON, omicronValue);
        wildCrossImmunityInfection.put(Load.BA2, ba2Value);
        wildCrossImmunityInfection.put(Load.BA45, ba45Value);
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
            coefficient =
                    Float.max(coefficient, (float) (crossImmunity.get(immunizationStage).get(immunizationLoad).get(load) *
                            sFunction.get(immunizationStage).get(immunizationLoad).applyAsDouble(days)));
        }
        return coefficient;
    }
}
