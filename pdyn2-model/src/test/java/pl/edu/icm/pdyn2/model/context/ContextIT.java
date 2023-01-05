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
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static pl.edu.icm.pdyn2.model.context.ContextType.HOUSEHOLD;
import static pl.edu.icm.pdyn2.model.context.ContextType.SCHOOL;
import static pl.edu.icm.pdyn2.model.context.ContextType.STREET_10;
import static pl.edu.icm.pdyn2.model.context.ContextType.WORKPLACE;

public class ContextIT {
    private Mapper<Context> mapper;
    private final Load wild = new Load("WILD", LoadClassification.VIRUS,-1,0,"", 1.0f);
    private final Load alpha = new Load("ALPHA", LoadClassification.VIRUS,-1,1,"", 10f);
    private final Load delta = new Load("DELTA", LoadClassification.VIRUS,-1,2,"", 10f);
    private final Load omicron = new Load("OMICRON", LoadClassification.VIRUS,-1,3,"", 10f);
    private final Load ba2 = new Load("BA2", LoadClassification.VIRUS,-1,4,"", 10f);


    Store store = new ArrayStore(10000);

    @BeforeEach
    void before() {
        mapper = Mappers.create(Context.class);
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
                .containsExactly(SCHOOL, HOUSEHOLD, STREET_10, WORKPLACE, HOUSEHOLD);
        assertThat(items.size()).isEqualTo(5);
        assertThat(items).extracting(Context::getAgentCount)
                .containsExactly(100f, 5f, 50000f, 20f, 3f);
        assertThat(items.stream().flatMap(i -> i.getContaminations().stream()))
                .extracting(Contamination::getLevel, Contamination::getLoad)
                .containsExactly(
                        tuple(136f, wild),
                        tuple(45f, alpha),
                        tuple(89f, delta),
                        tuple(5f, omicron),
                        tuple(20000.23f, omicron),
                        tuple(10f, delta),
                        tuple(10f, omicron));
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
        context.changeContaminationLevel(delta, 3.4f); // was 10f
        context.changeContaminationLevel(wild, 15f);
        mapper.save(context, 3);

        // assert
        var result = mapper.createAndLoad(3);
        assertThat(result.getContaminationByLoad(wild).getLevel()).isEqualTo(15f);
        assertThat(result.getContaminationByLoad(delta).getLevel()).isEqualTo(13.4f);
        assertThat(result.getContaminationByLoad(omicron).getLevel()).isEqualTo(10f);
        assertThat(result.getContaminations()).hasSize(3);
    }

    @Test
    @DisplayName("Should resolve conflicts by summing two contexts")
    void resolve() {
        // given
        Context contextA = exampleData().get(0);
        Context contextB = exampleData().get(0);

        contextA.changeContaminationLevel(wild, 10f);
        contextA.changeContaminationLevel(alpha, 20f);
        contextA.changeContaminationLevel(delta, 30f);
        contextA.changeContaminationLevel(omicron, 40f);
        contextA.updateAgentCount(50);

        contextB.changeContaminationLevel(wild, 100f);
        contextB.changeContaminationLevel(alpha, 200f);
        contextB.changeContaminationLevel(ba2, 300f);
        contextB.updateAgentCount(-100);

        // execute
        Context resolved = contextA.resolve(contextB);
        assertThat(resolved.getAgentCount()).isEqualTo(100f + 50f - 100f);
        assertThat(resolved.getContaminationByLoad(wild).getLevel()).isEqualTo(136f + 10f + 100f);
        assertThat(resolved.getContaminationByLoad(alpha).getLevel()).isEqualTo(45f + 20f + 200f);
        assertThat(resolved.getContaminationByLoad(delta).getLevel()).isEqualTo(89f + 30f + 0f);
        assertThat(resolved.getContaminationByLoad(omicron).getLevel()).isEqualTo(5f + 40f + 0f);
        assertThat(resolved.getContaminationByLoad(ba2).getLevel()).isEqualTo(0f + 0f + 300f);
    }

    @Test
    @DisplayName("should accumulate level and normalize everything")
    void multipleChanges() {
        // given
        Context context = context(HOUSEHOLD, 10, contamination(10, wild));

        // execute
        context.changeContaminationLevel(wild, 23);
        context.changeContaminationLevel(wild, 230);
        context.changeContaminationLevel(wild, 2300);
        context.changeContaminationLevel(omicron, 100);
        context.changeContaminationLevel(ba2, 200);
        context.changeContaminationLevel(omicron, -100);

        // assert
        assertThat(context.getContaminations()).hasSize(3);
        assertThat(context.getContaminationByLoad(wild).getLevel()).isEqualTo(10 + 23 + 230 + 2300);
        assertThat(context.getContaminationByLoad(omicron).getLevel()).isZero();
        assertThat(context.getContaminationByLoad(ba2).getLevel()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should normalize: sort contaminations by load and remove empty")
    void normalize() {
        // given
        Context context = context(HOUSEHOLD, 10, contamination(10, wild));

        context.changeContaminationLevel(ba2, 10f);
        context.changeContaminationLevel(omicron, 20f);
        context.changeContaminationLevel(delta, 30f);
        context.updateAgentCount(50);
        context.getContaminationByLoad(omicron).setLevel(0f);
        context.getContaminationByLoad(alpha).setLevel(0f);

        // execute
        context.normalize();

        // execute
        assertThat(context.getContaminations()).extracting(Contamination::getLoad).containsExactly(wild, delta, ba2);
        assertThat(context.getContaminations()).extracting(Contamination::getLevel).doesNotContain(0f);
    }


    private List<Context> exampleData() {
        return List.of(
                context(SCHOOL, 100,
                        contamination(136, wild),
                        contamination(45, alpha),
                        contamination(89, delta),
                        contamination(5, omicron)),
                context(HOUSEHOLD, 5),
                context(STREET_10, 50000,
                        contamination(20000.23f, omicron)),
                context(WORKPLACE, 20,
                        contamination(10, delta),
                        contamination(10, omicron)),
                context(HOUSEHOLD, 3)
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
