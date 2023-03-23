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

package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.em.common.math.pdf.SoftEnumDiscretePDF;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.progression.Stages;

/**
 * Helper for LoadDiseaseStageTransitions, holds information about a single stage/age/load combination
 * (i.e. possible transitions to other states and the duration)
 */
class TransitionOracle {
    private Stage stage;
    private final SoftEnumDiscretePDF<Stage> outcomes;
    private int duration;

    @WithFactory
    public TransitionOracle(Stages stages) {
        outcomes = new SoftEnumDiscretePDF<>(stages);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setProbableOutcome(float probability, Stage outcome) {
        outcomes.set(outcome, probability);
    }

    public SoftEnumDiscretePDF<Stage> getOutcomes() {
        return outcomes;
    }
}
