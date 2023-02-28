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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.pdyn2.model.BasicConfig;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ContextIT {
    BasicConfig basicConfig = new BasicConfig();
    private Mapper<Context> mapper;

    Store store = new ArrayStore(10000);

    @BeforeEach
    void before() {
        mapper = new Mappers(basicConfig.bento).create(Context.class);
        mapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should persist a sequence of Contexts and read them back")
    void persistAndReadBack() {
        // given
        AtomicInteger id = new AtomicInteger();
        exampleData()
                .forEach(i -> mapper.save(i, id.getAndIncrement()));

        // execute
        var items = IntStream.range(0, mapper.getCount())
                .mapToObj(mapper::createAndLoad).collect(Collectors.toList());

        // assert
        assertThat(items.size()).isEqualTo(5);
        assertThat(items)
                .extracting(i -> i.getContaminations().size())
                .containsExactly(4, 0, 1, 2, 0);
        assertThat(items)
                .extracting(Context::getContextType)
                .containsExactly(basicConfig.contextTypes.SCHOOL,
                        basicConfig.contextTypes.HOUSEHOLD,
                        basicConfig.contextTypes.STREET_10,
                        basicConfig.contextTypes.WORKPLACE,
                        basicConfig.contextTypes.HOUSEHOLD);
        assertThat(items.size()).isEqualTo(5);
        assertThat(items).extracting(Context::getAgentCount)
                .containsExactly(100f, 5f, 50000f, 20f, 3f);
        assertThat(items.stream().flatMap(i -> i.getContaminations().stream()))
                .extracting(Contamination::getLevel, Contamination::getLoad)
                .containsExactly(
                        tuple(136f, basicConfig.loads.WILD),
                        tuple(45f, basicConfig.loads.ALPHA),
                        tuple(89f, basicConfig.loads.DELTA),
                        tuple(5f, basicConfig.loads.OMICRON),
                        tuple(20000.23f, basicConfig.loads.OMICRON),
                        tuple(10f, basicConfig.loads.DELTA),
                        tuple(10f, basicConfig.loads.OMICRON));
    }

    @Test
    @DisplayName("Should modify a context entity")
    public void changeAndReadBack() {
        // given
        AtomicInteger id = new AtomicInteger();
        exampleData()
                .forEach(i -> mapper.save(i, id.getAndIncrement()));

        // execute
        Context context = mapper.createAndLoad(3);
        context.changeContaminationLevel(basicConfig.loads.DELTA, 3.4f); // was 10f
        context.changeContaminationLevel(basicConfig.loads.WILD, 15f);
        mapper.save(context, 3);

        // assert
        var result = mapper.createAndLoad(3);
        assertThat(result.getContaminationByLoad(basicConfig.loads.WILD).getLevel()).isEqualTo(15f);
        assertThat(result.getContaminationByLoad(basicConfig.loads.DELTA).getLevel()).isEqualTo(13.4f);
        assertThat(result.getContaminationByLoad(basicConfig.loads.OMICRON).getLevel()).isEqualTo(10f);
        assertThat(result.getContaminations()).hasSize(3);
    }

    @Test
    @DisplayName("Should resolve conflicts by summing two contexts")
    void resolve() {
        // given
        Context contextA = exampleData().get(0);
        Context contextB = exampleData().get(0);

        contextA.changeContaminationLevel(basicConfig.loads.WILD, 10f);
        contextA.changeContaminationLevel(basicConfig.loads.ALPHA, 20f);
        contextA.changeContaminationLevel(basicConfig.loads.DELTA, 30f);
        contextA.changeContaminationLevel(basicConfig.loads.OMICRON, 40f);
        contextA.updateAgentCount(50);

        contextB.changeContaminationLevel(basicConfig.loads.WILD, 100f);
        contextB.changeContaminationLevel(basicConfig.loads.ALPHA, 200f);
        contextB.changeContaminationLevel(basicConfig.loads.BA2, 300f);
        contextB.updateAgentCount(-100);

        // execute
        Context resolved = contextA.resolve(contextB);
        assertThat(resolved.getAgentCount()).isEqualTo(100f + 50f - 100f);
        assertThat(resolved.getContaminationByLoad(basicConfig.loads.WILD).getLevel()).isEqualTo(136f + 10f + 100f);
        assertThat(resolved.getContaminationByLoad(basicConfig.loads.ALPHA).getLevel()).isEqualTo(45f + 20f + 200f);
        assertThat(resolved.getContaminationByLoad(basicConfig.loads.DELTA).getLevel()).isEqualTo(89f + 30f + 0f);
        assertThat(resolved.getContaminationByLoad(basicConfig.loads.OMICRON).getLevel()).isEqualTo(5f + 40f + 0f);
        assertThat(resolved.getContaminationByLoad(basicConfig.loads.BA2).getLevel()).isEqualTo(0f + 0f + 300f);
    }

    @Test
    @DisplayName("should accumulate level and normalize everything")
    void multipleChanges() {
        // given
        Context context = context(basicConfig.contextTypes.HOUSEHOLD, 10, contamination(10, basicConfig.loads.WILD));

        // execute
        context.changeContaminationLevel(basicConfig.loads.WILD, 23);
        context.changeContaminationLevel(basicConfig.loads.WILD, 230);
        context.changeContaminationLevel(basicConfig.loads.WILD, 2300);
        context.changeContaminationLevel(basicConfig.loads.OMICRON, 100);
        context.changeContaminationLevel(basicConfig.loads.BA2, 200);
        context.changeContaminationLevel(basicConfig.loads.OMICRON, -100);

        // assert
        assertThat(context.getContaminations()).hasSize(3);
        assertThat(context.getContaminationByLoad(basicConfig.loads.WILD).getLevel()).isEqualTo(10 + 23 + 230 + 2300);
        assertThat(context.getContaminationByLoad(basicConfig.loads.OMICRON).getLevel()).isZero();
        assertThat(context.getContaminationByLoad(basicConfig.loads.BA2).getLevel()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should normalize: sort contaminations by load and remove empty")
    void normalize() {
        // given
        Context context = context(basicConfig.contextTypes.HOUSEHOLD, 10, contamination(10, basicConfig.loads.WILD));

        context.changeContaminationLevel(basicConfig.loads.BA2, 10f);
        context.changeContaminationLevel(basicConfig.loads.OMICRON, 20f);
        context.changeContaminationLevel(basicConfig.loads.DELTA, 30f);
        context.updateAgentCount(50);
        context.getContaminationByLoad(basicConfig.loads.OMICRON).setLevel(0f);
        context.getContaminationByLoad(basicConfig.loads.ALPHA).setLevel(0f);

        // execute
        context.normalize();

        // execute
        assertThat(context.getContaminations()).extracting(Contamination::getLoad).containsExactly(basicConfig.loads.WILD,
                basicConfig.loads.DELTA,
                basicConfig.loads.BA2);
        assertThat(context.getContaminations()).extracting(Contamination::getLevel).doesNotContain(0f);
    }


    private List<Context> exampleData() {
        return List.of(
                context(basicConfig.contextTypes.SCHOOL, 100,
                        contamination(136, basicConfig.loads.WILD),
                        contamination(45, basicConfig.loads.ALPHA),
                        contamination(89, basicConfig.loads.DELTA),
                        contamination(5, basicConfig.loads.OMICRON)),
                context(basicConfig.contextTypes.HOUSEHOLD, 5),
                context(basicConfig.contextTypes.STREET_10, 50000,
                        contamination(20000.23f, basicConfig.loads.OMICRON)),
                context(basicConfig.contextTypes.WORKPLACE, 20,
                        contamination(10, basicConfig.loads.DELTA),
                        contamination(10, basicConfig.loads.OMICRON)),
                context(basicConfig.contextTypes.HOUSEHOLD, 3)
        );
    }

    private Context context(ContextType type, float agentCount, Contamination... contaminations) {
        Context context = new Context();
        context.setAgentCount(agentCount);
        context.setContextType(type);
        context.getContaminations().addAll(Arrays.asList(contaminations));
        context.setup();
        return context;
    }

    private Contamination contamination(float level, Load load) {
        Contamination contamination = new Contamination();
        contamination.setLoad(load);
        contamination.setLevel(level);
        contamination.setup();
        return contamination;
    }
}
