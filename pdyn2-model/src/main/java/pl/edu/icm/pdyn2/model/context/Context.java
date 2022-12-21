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
import pl.edu.icm.trurl.ecs.mapper.feature.CanBeNormalized;
import pl.edu.icm.trurl.ecs.mapper.feature.CanResolveConflicts;
import pl.edu.icm.trurl.ecs.mapper.feature.IsDirtyMarked;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresSetup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static pl.edu.icm.pdyn2.model.context.Integerizer.toFloat;
import static pl.edu.icm.pdyn2.model.context.Integerizer.toInt;

@WithMapper
public final class Context
        implements IsDirtyMarked, CanResolveConflicts<Context>, CanBeNormalized, RequiresSetup {
    private ContextType contextType;
    private int agentCount;
    private int originalAgentCount;
    @NotMapped
    private boolean dirty = true;
    @NotMapped
    private int ownerId;
    private List<Contamination> contaminations = new ArrayList<>(3);

    public Context() {
    }

    public Context(ContextType contextType) {
        this.contextType = contextType;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    public List<Contamination> getContaminations() {
        return contaminations;
    }

    public Contamination getContaminationByLoad(Load load) {
        int size = contaminations.size();
        for (int i = 0; i < size; i++) {
            Contamination candidate = contaminations.get(i);
            if (candidate.getLoad() == load) {
                return candidate;
            }
        }
        Contamination fresh = new Contamination();
        contaminations.add(fresh);
        fresh.setLoad(load);
        return fresh;
    }

    public void changeContaminationLevel(Load load, float delta) {
        if (delta != 0) {
            Contamination contamination = getContaminationByLoad(load);
            contamination.changeLevel(delta);
            dirty = true;
        }
    }

    public float getAgentCount() {
        return toFloat(agentCount);
    }

    void setAgentCount(float agentCount) {
        this.agentCount = toInt(agentCount);
    }

    public void updateAgentCount(float delta) {
        int integerDelta = toInt(delta);

        if (integerDelta != 0) {
            this.agentCount += integerDelta;
            dirty = true;
        }
    }


    public String toString() {
        return "Context{" +
                "contextType=" + contextType +
                ", agentCount=" + toFloat(agentCount) +
                ", contaminations=" + contaminations +
                '}';
    }


    public boolean isDirty() {
        return dirty;
    }


    public Context resolve(Context other) {
        for (Contamination contamination : contaminations) {
            other.getContaminationByLoad(contamination.getLoad())
                    .changeIntegerizedLevel(contamination.getTotalIntegerizedLevelChange());
        }
        other.agentCount += getTotalIntegerizedAgentCountChange();
        return other;
    }

    public int getOwnerId() {
        return ownerId;
    }


    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }


    public void markAsClean() {
        dirty = false;
    }


    public void normalize() {
        Iterator<Contamination> i = contaminations.iterator();
        while (i.hasNext()) {
            if (i.next().getLevel() == 0) {
                i.remove();
            }
        }
        contaminations.sort(Comparator.comparingInt(c -> c.getLoad().ordinal()));
    }

    private float getTotalIntegerizedAgentCountChange() {
        return this.agentCount - this.originalAgentCount;
    }

    @Override
    public void setup() {
        this.originalAgentCount = this.agentCount;
    }
}
