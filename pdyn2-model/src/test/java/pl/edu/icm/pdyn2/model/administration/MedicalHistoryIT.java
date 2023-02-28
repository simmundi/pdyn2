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

package pl.edu.icm.pdyn2.model.administration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryIT {

    private Mapper<MedicalHistory> mapper;
    Store store = new ArrayStore(10);

    @BeforeEach
    void before() {
        mapper = new Mappers().create(MedicalHistory.class);
        mapper.configureStore(store);
        mapper.attachStore(store);
    }

    @Test
    public void save__list() {
        // given
        var original = new MedicalHistory();
        int idx = 12;
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 3));
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 8));
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 40));
        mapper.save(original, idx);

        // execute
        var copy = mapper.create();
        mapper.load(null, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_shorter() {
        // given
        var original = new MedicalHistory();
        int idx = 10;
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 3));
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 8));
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 40));
        mapper.save(original, idx);
        original.getRecords().remove(2);
        original.getRecords().remove(1);
        mapper.save(original, idx);

        // execute
        var copy = mapper.create();
        mapper.load(null, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_longer() {
        // given
        var original = new MedicalHistory();
        int idx = 10;
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 3));
        mapper.save(original, idx);
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 8));
        original.getRecords().add(new Record(RecordType.POSITIVE_TEST, 12));
        mapper.save(original, idx);

        // execute
        var copy = mapper.create();
        mapper.load(null, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

}
