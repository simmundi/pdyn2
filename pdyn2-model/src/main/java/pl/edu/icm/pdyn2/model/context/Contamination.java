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

package pl.edu.icm.pdyn2.model.context;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.annotation.NotMapped;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresSetup;

import static pl.edu.icm.pdyn2.model.context.Integerizer.toFloat;
import static pl.edu.icm.pdyn2.model.context.Integerizer.toInt;

@WithMapper
public final class Contamination implements RequiresSetup {
    private Load load;
    private int level;
    @NotMapped private int originalLevel;

    public Load getLoad() {
        return load;
    }

    public float getLevel() {
        return toFloat(level);
    }

    float changeLevel(float integerizedDelta) {
        return toFloat(changeIntegerizedLevel(toInt(integerizedDelta)));
    }

    @Override
    public void setup() {
        this.originalLevel = level;
    }

    @Override
    public String toString() {
        return "Contamination{" +
                "load=" + load +
                ", level=" + toFloat(level) +
                '}';
    }

    void setLevel(float level) {
        this.level = toInt(level);
    }

    void setLoad(Load load) {
        this.load = load;
    }

    int changeIntegerizedLevel(int integerizedDelta) {
        this.level = Math.max(0, this.level + integerizedDelta);
        return level;
    }

    int getTotalIntegerizedLevelChange() { return this.level - this.originalLevel; }

}
