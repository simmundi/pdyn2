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

package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithMapper
public class Immunization {
    @MappedCollection(margin = 8)
    private List<ImmunizationEvent> events = new ArrayList<>(8);

    public List<ImmunizationEvent> getEvents() {
        return events;
    }

    public void add(ImmunizationEvent immunizationEvent) {
        events.add(immunizationEvent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Immunization that = (Immunization) o;
        return Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events);
    }
}
