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

package pl.edu.icm.pdyn2.progression.lookup;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.model.AgeRange;
import pl.edu.icm.pdyn2.model.AgeRanges;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;
import pl.edu.icm.pdyn2.progression.DiseaseStageTransitionsService;
import pl.edu.icm.pdyn2.progression.OutcomeModifier;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.util.SoftEnumCube;

public class LookupStageTransitionsService implements DiseaseStageTransitionsService {
    private final SoftEnumCube<Load, AgeRange, Stage, TransitionDescriptor> transitions;
    private final AgeRanges ageRanges;
    private final OutcomeModifier outcomeModifier;
    private final ThreadLocal<SoftEnumDiscretePDF<Stage>> tmp;

    @WithFactory
    public LookupStageTransitionsService(Loads loads,
                                         LookupTransitionsProvider lookupTransitionsProvider,
                                         AgeRanges ageRanges,
                                         Stages stages,
                                         OutcomeModifier outcomeModifier) {
        this.outcomeModifier = outcomeModifier;
        this.transitions = new SoftEnumCube<>(loads, ageRanges, stages);
        this.ageRanges = ageRanges;
        this.tmp = ThreadLocal.withInitial(() -> new SoftEnumDiscretePDF<>(stages));

        lookupTransitionsProvider.readTransitions(data -> {
            transitions.put(data.first, data.second, data.third, data.fourth);
        });
    }

    @Override
    public int selectDurationOf(Stage stage,
                                Entity person,
                                Load load,
                                double random) {
        return retrieveTransition(stage, person, load).getDuration();
    }

    @Override
    public Stage selectOutcomeOf(Stage stage,
                                 Entity person,
                                 Load load,
                                 double random) {
        SoftEnumDiscretePDF<Stage> outcomes = tmp.get();
        outcomes.copy(retrieveTransition(stage, person, load).getOutcomes());
        outcomeModifier.modifyOutcomes(outcomes, load, person);

        return outcomes.sample(random);
    }

    private TransitionDescriptor retrieveTransition(Stage stage, Entity person, Load load) {
        var age = ageRanges.of(person.get(Person.class).getAge());
        var transition = transitions.get(load, age, stage);
        return transition;
    }
}
