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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.WorkDir;
import net.snowyhollows.bento.config.WorkDirFactory;
import org.mockito.Mockito;
import pl.edu.icm.board.model.Area;
import pl.edu.icm.board.model.Household;
import pl.edu.icm.board.model.Location;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.clock.SimulationClockFactory;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.context.ContextsServiceFactory;
import pl.edu.icm.pdyn2.impact.AgentImpactVisitor;
import pl.edu.icm.pdyn2.impact.AgentImpactVisitorFactory;
import pl.edu.icm.pdyn2.model.administration.MedicalHistory;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.context.Inhabitant;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.clock.SimulationClock;;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineConfigurationFactory;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.ecs.util.SelectorsFactory;

public class EmptyDataForIntegrationTests {
    public Selectors selectors;
    public Bento config;
    public AgentStateService agentStateService;
    public AgentImpactVisitor agentImpactVisitor;
    public ContextsService contextsService;
    public SimulationClock simulationClock;
    public Engine engine;
    public final int cols = 100;
    public final int rows = 100;
    public final WorkDir workDir = Mockito.mock(WorkDir.class);

    public EmptyDataForIntegrationTests(BasicConfig basicConfig) {
        config = basicConfig.bento;
        config.register("contextService", "behaviourBased");
        config.register("contextImpactService", "basic");
        config.register("gridRows", rows);
        config.register("gridColumns", cols);
        config.register("alpha", 0.5f);
        config.register("simulationStartDate", "1977-04-01");
        config.register("asymptomaticInfluenceShare", 0.1);
        config.register("symptomaticInfluenceShare", 1.0);
        config.register(WorkDirFactory.IT, workDir);

        agentStateService = config.get(AgentStateServiceFactory.IT);
        contextsService = config.get(ContextsServiceFactory.IT);
        agentImpactVisitor = config.get(AgentImpactVisitorFactory.IT);
        simulationClock = config.get(SimulationClockFactory.IT);
        selectors = config.get(SelectorsFactory.IT);

        EngineConfiguration engineConfiguration = config.get(EngineConfigurationFactory.IT);
        engineConfiguration.addComponentClasses(
                Person.class,
                Location.class,
                Area.class,
                HealthStatus.class,
                Context.class,
                Inhabitant.class,
                Immunization.class,
                Behaviour.class,
                Travel.class,
                Household.class,
                MedicalHistory.class,
                Impact.class);
        engine = engineConfiguration.getEngine();
    }
}
