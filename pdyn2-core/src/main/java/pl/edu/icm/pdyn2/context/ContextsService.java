/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.pdyn2.model.context.Context;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.stream.Stream;

/**
 * Implementation should return a list of context that the agents currently interact with.
 */
@ImplementationSwitch(
        configKey = "pdyn2.infectivity.transmission.contexts.context-strategy",
        cases = {
                @ImplementationSwitch.When(name="behavior-based", implementation = BehaviorBasedContextsService.class),
        }
)
public interface ContextsService {
    Stream<Context> findActiveContextsForAgent(Entity agentEntity);

    Stream<Context> findActiveContextsForAgent(Entity agentEntity, Impact impact);
}
