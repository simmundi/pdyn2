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

package pl.edu.icm.pdyn2.model.behaviour;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Behaviour {
    private int dayOfLastChange;
    private BehaviourType type = BehaviourType.DORMANT;

    public void transitionTo(BehaviourType type, int day) {
        this.type = type;
        this.dayOfLastChange = day;
    }

    public int getDayOfLastChange() {
        return dayOfLastChange;
    }

    void setDayOfLastChange(int dayOfLastChange) {
        this.dayOfLastChange = dayOfLastChange;
    }

    public BehaviourType getType() {
        return type;
    }

    void setType(BehaviourType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Behaviour{" +
                "dayOfLastChange=" + dayOfLastChange +
                ", type=" + type +
                '}';
    }
}
