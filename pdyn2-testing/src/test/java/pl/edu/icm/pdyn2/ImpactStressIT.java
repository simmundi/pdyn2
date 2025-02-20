/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourMapper;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.context.Context;
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

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.icm.pdyn2.ComponentCreator.*;

public class ImpactStressIT {
    public static final int AGENT_COUNT = 100000;
    public static final int TOTAL_TEST_TIME_IN_MILLIS = 10000;

    BasicConfig basicConfig = new BasicConfig();

    EmptyDataForIntegrationTests data = new EmptyDataForIntegrationTests(basicConfig);
    BehaviourType[] behaviours = { BehaviourType.ROUTINE, BehaviourType.PRIVATE_TRAVEL, BehaviourType.HOSPITALIZED };
    Load[] loads = { basicConfig.WILD, basicConfig.ALPHA, basicConfig.BA1};
    Stage[] stages = { basicConfig.HEALTHY, basicConfig.INFECTIOUS_SYMPTOMATIC, basicConfig.INFECTIOUS_ASYMPTOMATIC };

    Random random = new Random(0);

    @Test
    @DisplayName("Should keep contexts consistent in a congested scenario")
    public void stressTest() {

        // given

        // First, build two households: "home" and "resort";
        // put all the AGENT_COUNT agents in the "home" household,
        // make them eligible to travel to the "resort".
        data.engine.execute(s -> {
            Session session = s.create();

            Entity householdEntity = session.createEntity(household(), context(basicConfig.contextTypes.HOUSEHOLD));
            Household household = householdEntity.get(Household.class);

            Entity resort = session.createEntity(household(), context(basicConfig.contextTypes.HOUSEHOLD));

            for (int i = 0; i < AGENT_COUNT; i++) {
                household.getMembers().add(
                        session.createEntity(
                                behaviour(BehaviourType.ROUTINE),
                                health(null, basicConfig.HEALTHY),
                                person(18, Person.Sex.M),
                                travel(resort),
                                inhabitant(householdEntity)));
            }
            session.close();
        });

        // execute
        // calculate initial count of agents
        processAgentsSingleFile(entity -> {
            data.agentImpactVisitor.updateImpact(entity);
        });
        Context context = getContextFromEntityId(0);
        float initialAgentCount = context.getAgentCount();

        AtomicInteger rounds = new AtomicInteger(0);
        long lastTime = 0;
        long firstTime = System.currentTimeMillis();

        // repeat for TOTAL_TIME milliseconds (see `break` below)
        do {
            long currentTimeMillis = System.currentTimeMillis();

            // randomize state of agents (sequentially)
            processAgentsSingleFile(entity -> {
                randomizeAgent(entity);
            });

            // Process the impact logic
            processAgents(entity -> {
                // multiple threads continually update just two households.
                // This is the piece we are actually testing.
                data.agentImpactVisitor.updateImpact(entity);
            });

            // count agents: hospitalized, at home and in resort.
            int hospitalizedCount = getHospitalizedCount();
            float countAtHome = getContextFromEntityId(0).getAgentCount();
            float countAtResort = getContextFromEntityId(1).getAgentCount();
            assertThat(countAtHome + countAtResort + hospitalizedCount).isEqualTo(AGENT_COUNT);

            // if this was really random, the tests below could fail (but we control the seed)
            assertThat(countAtHome).isGreaterThan(0);
            assertThat(countAtResort).isGreaterThan(0);
            assertThat(hospitalizedCount).isGreaterThan(0);
            rounds.incrementAndGet();

            // display some handy debug data (useful when tweaking the test manually)
            if (currentTimeMillis - lastTime > 1000) {
                System.out.println("executed in total: " + rounds.get());

                lastTime = currentTimeMillis;
                System.out.println("home: " + getContextFromEntityId(0));
                System.out.println("resort: " + getContextFromEntityId(1));
                System.out.println("hospitalized: " + hospitalizedCount);
            }

            if (currentTimeMillis - TOTAL_TEST_TIME_IN_MILLIS > firstTime) {
                break;
            }
        } while (true);

        // all agents recover and return to routing
        processAgents(entity -> {
            cleanAgent(entity);
            data.agentImpactVisitor.updateImpact(entity);
        });

        // assert
        Context homeContext = getContextFromEntityId(0);
        float finalAgentCount = homeContext.getAgentCount();

        assertThat(initialAgentCount).isEqualTo(AGENT_COUNT);
        assertThat(finalAgentCount).isEqualTo(AGENT_COUNT);
        assertThat(homeContext.getContaminations()).isEmpty();
    }

    private int getHospitalizedCount() {
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
        modifyAgent(agent, BehaviourType.ROUTINE, basicConfig.HEALTHY, null);
    }

    private void randomizeAgent(Entity agent) {
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
