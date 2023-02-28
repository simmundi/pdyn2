/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumMap;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ImmunizationStrategyFromPdyn1Rewritten implements ImmunizationStrategy {
    private final Map<ImmunizationStage, Map<Load, IntToDoubleFunction>> sFunction = new EnumMap<>(ImmunizationStage.class);
    private final Map<ImmunizationStage, Map<Load, Map<Load, Double>>> crossImmunity = new EnumMap<>(ImmunizationStage.class);
    private final Loads loads;

    @WithFactory
    public ImmunizationStrategyFromPdyn1Rewritten(Loads loads) {
        this.loads = loads;

        sFunction.putAll(Map.of(
                ImmunizationStage.LATENTNY, new SoftEnumMap<>(loads, Map.of(
                        loads.WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.OMICRON, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.OBJAWOWY, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        loads.WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.OMICRON, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        loads.BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        loads.WILD, day -> 1.0,
                        loads.ALPHA, day -> 1.0,
                        loads.DELTA, day -> 1.0,
                        loads.OMICRON, day -> 1.0,
                        loads.BA2, day -> 1.0,
                        loads.BA45, day -> 1.0,
                        loads.PFIZER, day -> min(day / 14.0, 1.0),
                        loads.BOOSTER, day ->  min(day / 14.0, 1.0)
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        loads.WILD, day -> 1.0,
                        loads.ALPHA, day -> 1.0,
                        loads.DELTA, day -> 1.0,
                        loads.OMICRON, day -> 1.0,
                        loads.BA2, day -> 1.0,
                        loads.BA45, day -> 1.0,
                        loads.PFIZER, day -> min(day / 14.0, 1.0),
                        loads.BOOSTER, day ->  min(day / 14.0, 1.0)
                ))
        ));

        crossImmunity.putAll(Map.of(
                ImmunizationStage.LATENTNY, new SoftEnumMap<>(loads, Map.of(
                        loads.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        loads.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        loads.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.76, 0.76, 0.76),
                        loads.OMICRON, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        loads.BA2, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        loads.BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.9, 0.9, 1.0),
                        loads.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        loads.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.875, 0.875, 0.875)
                )),
                ImmunizationStage.OBJAWOWY, new SoftEnumMap<>(loads, Map.of(
                        loads.WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        loads.ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        loads.DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.0, 0.0, 0.0),
                        loads.OMICRON, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        loads.BA2, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        loads.BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        loads.PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        loads.BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0)
                )),
                ImmunizationStage.HOSPITALIZOWANY_BEZ_OIOM, new SoftEnumMap<>(loads, Map.of(
                        loads.WILD, generateCrossImmunity(0.8, 0.77, 0.77, 0.5, 0.5, 0.5),
                        loads.ALPHA, generateCrossImmunity(0.77, 0.8, 0.77, 0.5, 0.5, 0.5),
                        loads.DELTA, generateCrossImmunity(0.77, 0.77, 0.8, 0.5, 0.5, 0.5),
                        loads.OMICRON, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        loads.BA2, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        loads.BA45, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        loads.PFIZER, generateCrossImmunity(0.77, 0.77, 0.77, 0.5, 0.5, 0.5),
                        loads.BOOSTER, generateCrossImmunity(0.82, 0.82, 0.82, 0.72, 0.72, 0.72)
                )),
                ImmunizationStage.HOSPITALIZOWANY_PRZED_OIOM, new SoftEnumMap<>(loads, Map.of(
                        loads.WILD, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        loads.ALPHA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        loads.DELTA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        loads.OMICRON, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        loads.BA2, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        loads.BA45, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        loads.PFIZER, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        loads.BOOSTER, generateCrossImmunity(0.90, 0.90, 0.90, 0.84, 0.84, 0.84)
                ))
        ));

    }

    private SoftEnumMap<Load, Double> generateCrossImmunity(double wildValue, double alphaValue, double deltaValue, double omicronValue, double ba2Value, double ba45Value) {
        var wildCrossImmunityInfection = new SoftEnumMap<Load, Double>(loads);
        wildCrossImmunityInfection.put(loads.WILD, wildValue);
        wildCrossImmunityInfection.put(loads.ALPHA, alphaValue);
        wildCrossImmunityInfection.put(loads.DELTA, deltaValue);
        wildCrossImmunityInfection.put(loads.OMICRON, omicronValue);
        wildCrossImmunityInfection.put(loads.BA2, ba2Value);
        wildCrossImmunityInfection.put(loads.BA45, ba45Value);
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
