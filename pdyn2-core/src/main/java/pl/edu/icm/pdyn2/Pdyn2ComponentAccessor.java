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

package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.AdministrationUnit;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSource;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationSources;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.trurl.ecs.ComponentAccessor;
import pl.edu.icm.trurl.ecs.EngineConfiguration;

public class Pdyn2ComponentAccessor implements ComponentAccessor {

    @WithFactory
    public Pdyn2ComponentAccessor(EngineConfiguration engineConfiguration) {
        engineConfiguration.setComponentIndexer(this);
        engineConfiguration.addComponentClasses(components());
    }

    @Override
    public int classToIndex(Class<?> componentClass) {
        if (componentClass == HealthStatus.class) return 0;
        if (componentClass == Context.class) return 1;
        if (componentClass == Inhabitant.class) return 2;
        if (componentClass == Immunization.class) return 3;
        if (componentClass == Behaviour.class) return 4;
        if (componentClass == Person.class) return 5;
        if (componentClass == Area.class) return 6;
        if (componentClass == Location.class) return 7;
        if (componentClass == Travel.class) return 8;
        if (componentClass == Household.class) return 9;
        if (componentClass == MedicalHistory.class) return 10;
        if (componentClass == AdministrationUnit.class) return 11;
        if (componentClass == Impact.class) return 12;
        if (componentClass == ImmunizationSources.class) return 13;
        throw new IllegalArgumentException("No support for " + componentClass);
    }

    @Override
    public Class<?> indexToClass(int index) {
        switch (index) {
            case 0:
                return HealthStatus.class;
            case 1:
                return Context.class;
            case 2:
                return Inhabitant.class;
            case 3:
                return Immunization.class;
            case 4:
                return Behaviour.class;
            case 5:
                return Person.class;
            case 6:
                return Area.class;
            case 7:
                return Location.class;
            case 8:
                return Travel.class;
            case 9:
                return Household.class;
            case 10:
                return MedicalHistory.class;
            case 11:
                return AdministrationUnit.class;
            case 12:
                return Impact.class;
            case 13:
                return ImmunizationSources.class;
        }
        throw new IllegalArgumentException("No support for index " + index);
    }

    public Class<?>[] components() {
        return new Class[]{
                HealthStatus.class,
                Context.class,
                Inhabitant.class,
                Immunization.class,
                Behaviour.class,
                Person.class,
                Area.class,
                Location.class,
                Travel.class,
                Household.class,
                MedicalHistory.class,
                AdministrationUnit.class,
                Impact.class,
                ImmunizationSources.class
        };
    }

    @Override
    public int componentCount() {
        return 14;
    }
}
