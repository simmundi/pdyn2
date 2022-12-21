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

import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.sampleSpace.EnumSampleSpace;

class TransitionOracle {
    private Stage stage;
    private final EnumSampleSpace<Stage> outcomes = new EnumSampleSpace<>(Stage.class);
    private int duration;

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

    public void addProbableOutcome(float probability, Stage outcome) {
        outcomes.changeOutcome(outcome, probability);
    }

    public EnumSampleSpace<Stage> getOutcomes() {
        return outcomes;
    }
}
