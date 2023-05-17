/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.progression.lookup;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.trurl.util.Quadruple;

import java.util.function.Consumer;

public class ConfigLookupTransitionProvider implements LookupTransitionsProvider{

    private final Stages stages;
    private final AgeRanges ageRanges;
    private final Loads loads;
    private final Bento bento;

    @WithFactory
    public ConfigLookupTransitionProvider(Stages stages, AgeRanges ageRanges, Loads loads, Bento bento) {
        this.stages = stages;
        this.ageRanges = ageRanges;
        this.loads = loads;
        this.bento = bento;
    }

    @Override
    public void readTransitions(Consumer<Quadruple<Load, AgeRange, Stage, TransitionDescriptor>> consumer) {
        for (Load load : loads.values()) {
            for (AgeRange ageRange : ageRanges.values()) {
                for (Stage stage : stages.values()) {
                    String durationProperty = String.format("pdyn2.progression.transition.%s.%s.%s.duration", load.name(), ageRange.name(), stage.name());

                    if (bento.get(durationProperty, this) != this) {
                        TransitionDescriptor transitionDescriptor = new TransitionDescriptor(stages);
                        transitionDescriptor.setDuration(bento.getInt(durationProperty));

                        for (Stage targetStage : stages.values()) {
                            String outcomeProbability = String.format("pdyn2.progression.transition.%s.%s.%s.%s", load.name(), ageRange.name(), stage.name(), targetStage.name());
                            if (bento.get(outcomeProbability, this) != this) {
                                transitionDescriptor.setProbableOutcome(bento.getFloat(outcomeProbability), targetStage);
                            }
                        }

                        consumer.accept(Quadruple.of(load, ageRange, stage, transitionDescriptor));
                    }
                }
            }
        }
    }
}
