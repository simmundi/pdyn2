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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;

public class ImmunizationIT {
    private Mapper<Immunization> mapper;
    private final Load alpha = new Load("ALPHA", LoadClassification.VIRUS,-1,1,"",10f);
    private final Load delta = new Load("DELTA", LoadClassification.VIRUS,-1,2,"",10f);
    private final Load pfizer = new Load("ASTRA", LoadClassification.VACCINE,0,-1,"");
    private final Load astra = new Load("ASTRA", LoadClassification.VACCINE,1,-1,"");
    private final Load moderna = new Load("MODERNA", LoadClassification.VACCINE,2,-1,"");
    private final Load booster = new Load("BOOSTER", LoadClassification.VACCINE,3,-1,"");


    Store store = new ArrayStore(10);

    @BeforeEach
    void before() {
        mapper = Mappers.create(Immunization.class);
        mapper.configureStore(store);
        mapper.attachStore(store);
    }

    @Test
    @DisplayName("Should persist a sequence of Immunizations and read them back")
    void persistAndReadBack() {
        // given
        AtomicInteger id = new AtomicInteger();
        exampleData()
                .forEach(i -> mapper.save(i, id.getAndIncrement()));

        // execute
        var items = IntStream.range(0, mapper.getCount())
                .mapToObj(i -> {
                    var item = mapper.create();
                    mapper.load(null, item, i);
                    return item;
                }).collect(Collectors.toList());

        // assert
        assertThat(items.size()).isEqualTo(4);
        assertThat(items)
                .extracting(i -> i.getEvents().size())
                .containsExactly(6, 2, 2, 2);
        assertThat(items.size()).isEqualTo(4);
        assertThat(items.stream().flatMap(i -> i.getEvents().stream()))
                .extracting(ImmunizationEvent::getDay, ImmunizationEvent::getLoad)
                .containsExactly(
                        tuple(0, alpha),
                        tuple(4, astra),
                        tuple(0, alpha),
                        tuple(0, null),
                        tuple(100, booster),
                        tuple(Integer.MAX_VALUE, alpha),
                        tuple(34, alpha),
                        tuple(21, pfizer),
                        tuple(67, alpha),
                        tuple(34, moderna),
                        tuple(-15, null),
                        tuple(478, delta)
                );
    }

    @Test
    void readBackAndChange() {
        // given
        AtomicInteger id = new AtomicInteger();
        exampleData()
                .forEach(i -> mapper.save(i, id.getAndIncrement()));
        int index = 2;

        // execute
        var item = mapper.create();
        mapper.load(null, item, index);
        item.getEvents().remove(0);
        item.getEvents().add(event(99, booster));
        var modified = mapper.isModified(item, index);
        mapper.save(item, index);

        // assert
        var result = mapper.create();
        mapper.load(null, result, index);

        assertThat(result.getEvents().stream())
                .extracting(ImmunizationEvent::getDay, ImmunizationEvent::getLoad)
                .containsExactly(
                        tuple(34, moderna),
                        tuple(99, booster));
        assertThat(modified).isTrue();
    }

    private List<Immunization> exampleData() {
        return List.of(
                immunization(
                        event(0, alpha),
                        event(4, astra),
                        event(0, alpha),
                        event(0, null),
                        event(100, booster),
                        event(Integer.MAX_VALUE, alpha)),
                immunization(
                        event(34, alpha),
                        event(21, pfizer)),
                immunization(
                        event(67, alpha),
                        event(34, moderna)),
                immunization(
                        event(-15, null),
                        event(478, delta)));
    }

    private Immunization immunization(ImmunizationEvent... events) {
        Immunization immunization = new Immunization();
        immunization.getEvents().addAll(Arrays.asList(events));
        return immunization;
    }

    private ImmunizationEvent event(int day, Load load) {
        ImmunizationEvent immunizationEvent = new ImmunizationEvent();
        immunizationEvent.setDay(day);
        immunizationEvent.setLoad(load);
        return immunizationEvent;
    }

}
