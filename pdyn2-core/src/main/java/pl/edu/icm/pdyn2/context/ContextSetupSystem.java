package pl.edu.icm.pdyn2.context;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.*;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
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
                Household household = entity.get(Household.class);
                entity.add(new Context(ContextType.HOUSEHOLD));
                for (Entity householdMember : household.getMembers()) {
                    Inhabitant inhabitant = householdMember.add(new Inhabitant());
                    inhabitant.setHomeContext(entity);

                    Attendee attendee = householdMember.get(Attendee.class);
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
                    Employee employee = householdMember.get(Employee.class);
                    if (employee != null) {
                        inhabitant.getContexts().add(employee.getWork());
                    }
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
}
