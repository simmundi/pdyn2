package pl.edu.icm.pdyn2;

import org.assertj.core.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourMapper;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.ContextType;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.EntityIterator;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static pl.edu.icm.pdyn2.ComponentCreator.*;

public class ImpactStressIT {
    public static final int AGENT_COUNT = 1000;
    EmptyDataForIntegrationTests data = new EmptyDataForIntegrationTests();
    BehaviourType[] behaviours = { BehaviourType.ROUTINE, BehaviourType.PRIVATE_TRAVEL, BehaviourType.HOSPITALIZED };
    Load[] loads = { Load.WILD, Load.ALPHA, Load.OMICRON };
    Stage[] stages = { Stage.HEALTHY, Stage.INFECTIOUS_SYMPTOMATIC };

    @Test
    @DisplayName("Should keep contexts consistent in a congested scenario")
    public void stressTest() {
        data.engine.execute(s -> {
            Session session = s.create();

            Entity householdEntity = session.createEntity(household(), context(ContextType.HOUSEHOLD));
            Household household = householdEntity.get(Household.class);

            Entity resort = session.createEntity(household(), context(ContextType.HOUSEHOLD));

            for (int i = 0; i < AGENT_COUNT; i++) {
                household.getMembers().add(
                        session.createEntity(
                                behaviour(BehaviourType.ROUTINE),
                                health(null, Stage.HEALTHY),
                                person(18, Person.Sex.M),
                                travel(resort),
                                inhabitant(householdEntity)));
            }
            session.close();
        });

        AtomicInteger counter = new AtomicInteger();

        processAgentsSingleFile(entity -> {
            data.agentImpactService.updateImpact(entity);
            counter.incrementAndGet();
        });
        Context context = getContextFromEntityId(0);
        Assertions.assertThat(context.getAgentCount()).isEqualTo(AGENT_COUNT);

        AtomicInteger rounds = new AtomicInteger(0);
        long lastTime = 0;
        long firstTime = System.currentTimeMillis();
        do {
            processAgentsSingleFile(entity -> {
                randomizeAgent(entity);
            });
            int absentCount = getAbsentCount();
            processAgents(entity -> {
                data.agentImpactService.updateImpact(entity);
            });
            Context context1 = getContextFromEntityId(0);
            Context context2 = getContextFromEntityId(1);
            float sum = context1.getAgentCount() + context2.getAgentCount() + absentCount;
            Assertions.assertThat(sum).isEqualTo(AGENT_COUNT);
            rounds.incrementAndGet();
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - lastTime > 1000) {
                System.out.println("executed in total: " + rounds.get());
                lastTime = currentTimeMillis;
            }
            if (currentTimeMillis - 10000 > firstTime) {
                break;
            }
        } while (true);

        counter.set(0);
        processAgents(entity -> {
            cleanAgent(entity);
            data.agentImpactService.updateImpact(entity);
            counter.incrementAndGet();
        });
        Context context2 = getContextFromEntityId(0);
        Assertions.assertThat(context2.getAgentCount()).isEqualTo(AGENT_COUNT);
    }

    private int getAbsentCount() {
        BehaviourMapper behaviourMapper = (BehaviourMapper) data.engine.getMapperSet().classToMapper(Behaviour.class);
        int count = data.engine.getCount();
        int absentCount = 0;
        for (int i = 0; i < count; i++) {
            if (behaviourMapper.getType(i) == BehaviourType.HOSPITALIZED) {
                absentCount++;
            }
        }
        return absentCount;
    }

    private void processAgents(Consumer<Entity> agentProcessor) {
        data.engine.execute(
                EntityIterator.select(data.selectors.allEntities(100)).parallel().forEach(entity -> {
                    if (entity.get(Inhabitant.class) != null) {
                        agentProcessor.accept(entity);
                    }
                }));
    }

    private void processAgentsSingleFile(Consumer<Entity> agentProcessor) {
        data.engine.execute(
                EntityIterator.select(data.selectors.allEntities(500)).forEach(entity -> {
                    if (entity.get(Inhabitant.class) != null) {
                        agentProcessor.accept(entity);
                    }
                }));
    }

    private void cleanAgent(Entity agent) {
        modifyAgent(agent, BehaviourType.ROUTINE, Stage.HEALTHY, null);
    }

    private void randomizeAgent(Entity agent) {
        Random random = new Random();
        modifyAgent(agent, behaviours[random.nextInt(behaviours.length)],
                stages[random.nextInt(stages.length)],
                loads[random.nextInt(loads.length)]);
    }

    private void modifyAgent(Entity agent, BehaviourType behaviourType, Stage stage, Load load) {
        agent.get(Behaviour.class).transitionTo(behaviourType, 0);
        HealthStatus healthStatus = agent.get(HealthStatus.class);
        healthStatus.setDiseaseLoad(load);
        healthStatus.transitionTo(stage, 0);
    }

    private Context getContextFromEntityId(int id) {
        AtomicReference<Context> result = new AtomicReference<>();
        data.engine.execute(sessionFactory -> {
            Entity household = sessionFactory.create().getEntity(id);
            Context context = household.get(Context.class);
            result.set(context);
        });
        return result.get();
    }
}
