/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumMap;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.immunization.ImmunizationStrategy;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ImmunizationStrategyFromPdyn1Rewritten implements ImmunizationStrategy {
    private final Map<ImmunizationStage, Map<Load, IntToDoubleFunction>> sFunction = new EnumMap<>(ImmunizationStage.class);
    private final Map<ImmunizationStage, Map<Load, Map<Load, Double>>> crossImmunity = new EnumMap<>(ImmunizationStage.class);
    private final Loads loads;

    private final Load WILD;
    private final Load ALPHA;
    private final Load DELTA;
    private final Load BA1;
    private final Load BA2;
    private final Load BA45;
    private final Load PFIZER;
    private final Load BOOSTER;

    @WithFactory
    public ImmunizationStrategyFromPdyn1Rewritten(Loads loads) {
        this.loads = loads;

        WILD = loads.getByName("WILD");
        ALPHA = loads.getByName("ALPHA");
        DELTA = loads.getByName("DELTA");
        BA1 = loads.getByName("BA1");
        BA2 = loads.getByName("BA2");
        BA45 = loads.getByName("BA45");
        PFIZER = loads.getByName("PFIZER");
        BOOSTER = loads.getByName("BOOSTER");

        sFunction.putAll(Map.of(
                ImmunizationStage.LATENT, new SoftEnumMap<>(loads, Map.of(
                        WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA1, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.SYMPTOMATIC, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        WILD, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        ALPHA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        DELTA, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA1, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA2, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BA45, day -> day < 90 ? 1.0 : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        PFIZER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90)),
                        BOOSTER, day -> day < 90 ? min(day / 14.0, 1.0) : max(0.800000, 1.0 - 0.00133333 * (day - 90))
                )),
                ImmunizationStage.ASYMPTOMATIC, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        WILD, day -> 1.0,
                        ALPHA, day -> 1.0,
                        DELTA, day -> 1.0,
                        BA1, day -> 1.0,
                        BA2, day -> 1.0,
                        BA45, day -> 1.0,
                        PFIZER, day -> min(day / 14.0, 1.0),
                        BOOSTER, day ->  min(day / 14.0, 1.0)
                )),
                ImmunizationStage.HOSPITALIZED_PRE_ICU, new SoftEnumMap<Load, IntToDoubleFunction>(loads, Map.of(
                        WILD, day -> 1.0,
                        ALPHA, day -> 1.0,
                        DELTA, day -> 1.0,
                        BA1, day -> 1.0,
                        BA2, day -> 1.0,
                        BA45, day -> 1.0,
                        PFIZER, day -> min(day / 14.0, 1.0),
                        BOOSTER, day ->  min(day / 14.0, 1.0)
                ))
        ));

        crossImmunity.putAll(Map.of(
                ImmunizationStage.LATENT, new SoftEnumMap<>(loads, Map.of(
                        WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.76, 0.76, 0.76),
                        BA1, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        BA2, generateCrossImmunity(0.76, 0.76, 0.76, 1.0, 1.0, 0.9),
                        BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.9, 0.9, 1.0),
                        PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.76, 0.76, 0.76),
                        BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.875, 0.875, 0.875)
                )),
                ImmunizationStage.SYMPTOMATIC, new SoftEnumMap<>(loads, Map.of(
                        WILD, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        ALPHA, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        DELTA, generateCrossImmunity(0.975, 0.975, 1.0, 0.0, 0.0, 0.0),
                        BA1, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        BA2, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        BA45, generateCrossImmunity(0.76, 0.76, 0.76, 0.0, 0.0, 0.0),
                        PFIZER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0),
                        BOOSTER, generateCrossImmunity(1.0, 1.0, 0.975, 0.0, 0.0, 0.0)
                )),
                ImmunizationStage.ASYMPTOMATIC, new SoftEnumMap<>(loads, Map.of(
                        WILD, generateCrossImmunity(0.8, 0.77, 0.77, 0.5, 0.5, 0.5),
                        ALPHA, generateCrossImmunity(0.77, 0.8, 0.77, 0.5, 0.5, 0.5),
                        DELTA, generateCrossImmunity(0.77, 0.77, 0.8, 0.5, 0.5, 0.5),
                        BA1, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        BA2, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        BA45, generateCrossImmunity(0.5, 0.5, 0.5, 0.8, 0.8, 0.8),
                        PFIZER, generateCrossImmunity(0.77, 0.77, 0.77, 0.5, 0.5, 0.5),
                        BOOSTER, generateCrossImmunity(0.82, 0.82, 0.82, 0.72, 0.72, 0.72)
                )),
                ImmunizationStage.HOSPITALIZED_PRE_ICU, new SoftEnumMap<>(loads, Map.of(
                        WILD, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        ALPHA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        DELTA, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        BA1, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        BA2, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        BA45, generateCrossImmunity(0.67, 0.67, 0.67, 0.87, 0.87, 0.87),
                        PFIZER, generateCrossImmunity(0.87, 0.87, 0.87, 0.67, 0.67, 0.67),
                        BOOSTER, generateCrossImmunity(0.90, 0.90, 0.90, 0.84, 0.84, 0.84)
                ))
        ));

    }

    private SoftEnumMap<Load, Double> generateCrossImmunity(double wildValue, double alphaValue, double deltaValue, double ba1Value, double ba2Value, double ba45Value) {
        var wildCrossImmunityInfection = new SoftEnumMap<Load, Double>(loads);
        wildCrossImmunityInfection.put(WILD, wildValue);
        wildCrossImmunityInfection.put(ALPHA, alphaValue);
        wildCrossImmunityInfection.put(DELTA, deltaValue);
        wildCrossImmunityInfection.put(BA1, ba1Value);
        wildCrossImmunityInfection.put(BA2, ba2Value);
        wildCrossImmunityInfection.put(BA45, ba45Value);
        return wildCrossImmunityInfection;
    }

    @Override
    public float getImmunizationCoefficient(Entity agent, ImmunizationStage immunizationStage, Load load, int day) {
        Immunization immunization = agent.get(Immunization.class);
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
