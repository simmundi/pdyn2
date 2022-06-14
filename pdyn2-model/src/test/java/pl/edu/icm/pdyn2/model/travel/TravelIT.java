package pl.edu.icm.pdyn2.model.travel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelIT {
    private Mapper<Travel> mapper;
    Store store = new ArrayStore(10);

    @Mock
    Session session;
    @Mock
    MapperSet mapperSet;

    @BeforeEach
    void before() {
        mapper = Mappers.create(Travel.class);
        mapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should persist a travel and read it back")
    void persistAndReadBack() {
        // given
        when(session.getEntity(67)).then(args -> entity(args.getArgument(0, Integer.class)));

        // execute
        mapper.save(travel(67, 45), 4);
        Travel result = mapper.createAndLoad(session, 4);

        // assert
        assertThat(result.getDayOfTravel()).isEqualTo((short)45);
        assertThat(result.getStayingAt().getId()).isEqualTo(67);
    }

    private Travel travel(int entityId, int day) {
        Entity entity = entity(entityId);
        Travel travel = new Travel();
        travel.setDayOfTravel((short)day);
        travel.setStayingAt(entity);
        return travel;
    }

    private Entity entity(int entityId) {
        Entity entity = new Entity(mapperSet, session, entityId);
        return entity;
    }

}
