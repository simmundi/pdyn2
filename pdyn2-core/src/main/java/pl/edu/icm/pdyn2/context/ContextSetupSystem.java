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

package pl.edu.icm.pdyn2.context;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.*;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.util.EntityIterator;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.util.Status;

public class ContextSetupSystem implements EntitySystem {

    private final Selectors selectors;

    @WithFactory
    public ContextSetupSystem(Selectors selectors) {
        this.selectors = selectors;
    }

    @Override
    public void execute(SessionFactory sessionFactory) {
        var status = Status.of("configuring for pdyn2", 1_000_000);

        EntityIterator.select(selectors.allEntities()).forEach(entity -> {
            status.tick();
            if (entity.get(Household.class) != null) {
                Entity householdEntity = entity;
                Household household = householdEntity.get(Household.class);
                householdEntity.add(new Context(ContextType.HOUSEHOLD));
                for (Entity householdMember : household.getMembers()) {
                    householdMember.add(defaultBehaviour());
                    householdMember.add(defaultHealthStatus());
                    Inhabitant inhabitant = defaultInhabitant();

                    inhabitHousehold(householdEntity, inhabitant);
                    inhabitEducationalInstitutions(householdMember, inhabitant);
                    inhabitWorkplace(householdMember, inhabitant);

                    householdMember.add(inhabitant);
                }
            } else if (entity.get(Workplace.class) != null) {
                entity.add(new Context(ContextType.WORKPLACE));
            } else if (entity.get(EducationalInstitution.class) != null) {
                var eduInst = entity.get(EducationalInstitution.class);
                switch (eduInst.getLevel()) {
                    case BU:
                        entity.add(new Context(ContextType.BIG_UNIVERSITY));
                        break;
                    case U:
                        entity.add(new Context(ContextType.UNIVERSITY));
                        break;
                    case K:
                        entity.add(new Context(ContextType.KINDERGARTEN));
                        break;
                    case H: // thru
                    case P: // thru
                    case PH: // thru
                    default:
                        entity.add(new Context(ContextType.SCHOOL));
                }
            }
        }).execute(sessionFactory);

        status.done();
    }

    private Inhabitant defaultInhabitant() {
        return new Inhabitant();
    }

    private HealthStatus defaultHealthStatus() {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.transitionTo(Stage.HEALTHY, 0);
        return healthStatus;
    }

    private Behaviour defaultBehaviour() {
        Behaviour behaviour = new Behaviour();
        behaviour.transitionTo(BehaviourType.ROUTINE, 0);
        return behaviour;
    }

    private void inhabitHousehold(Entity entity, Inhabitant inhabitant) {
        inhabitant.setHomeContext(entity);
    }

    private void inhabitWorkplace(Entity householdMember, Inhabitant inhabitant) {
        Employee employee = householdMember.get(Employee.class);
        if (employee != null) {
            inhabitant.getContexts().add(employee.getWork());
        }
    }

    private void inhabitEducationalInstitutions(Entity inhabitantEntity, Inhabitant inhabitant) {
        Attendee attendee = inhabitantEntity.get(Attendee.class);

        if (attendee != null) {
            var institution = attendee.getInstitution();
            var secondaryInstitution = attendee.getSecondaryInstitution();
            if (institution != null) {
                inhabitant.getContexts().add(institution);
            }
            if (secondaryInstitution != null) {
                inhabitant.getContexts().add(secondaryInstitution);
            }
        }
    }
}
