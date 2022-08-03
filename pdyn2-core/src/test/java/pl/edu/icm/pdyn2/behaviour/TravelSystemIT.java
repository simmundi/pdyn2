package pl.edu.icm.pdyn2.behaviour;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.index.AreaClusteredSelectors;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.quarantine.QuarantineService;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.util.RangeSelector;
import pl.edu.icm.trurl.ecs.util.Selectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelSystemIT {
    @Mock TravelService travelService;

    @Mock
    Engine engine;
    @Mock
    Session session;
    @Mock
    SessionFactory sessionFactory;
    @Mock
    MapperSet mapperSet;
    @Mock
    Mapper<Behaviour> behaviourMapper;
    @Mock
    Entity entity;
    @Mock
    Behaviour behaviour;
    @Mock
    AreaClusteredSelectors areaClusteredSelectors;
    @Mock
    QuarantineService quarantineService;

    @Mock
    RandomGenerator randomGenerator;

    BehaviourDrivenLogicBuilder behaviourDrivenLogicBuilder;



    @BeforeEach
    void before() {
        behaviourDrivenLogicBuilder = new BehaviourDrivenLogicBuilder(
                travelService,
                quarantineService,
                areaClusteredSelectors,
                new Selectors(engine),
                new RandomProvider(123));
    }



    @Test
    @DisplayName("Should create a system that will call service for each eligible entity")
    void createTravelSystem() {
        // given
        when(areaClusteredSelectors.personSelector()).thenReturn(new RangeSelector(0, 100, 10));
        when(sessionFactory.withModeAndCount(any(), anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.create(anyInt())).thenReturn(session);
        when(session.getEntity(anyInt())).thenReturn(entity);
        when(entity.get(Behaviour.class)).thenReturn(behaviour);
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Behaviour.class)).thenReturn(behaviourMapper);
        when(behaviourMapper.isPresent(anyInt())).thenAnswer(call -> call.getArgument(0, Integer.class) % 4 == 0);

        // execute
        var system = behaviourDrivenLogicBuilder.buildTravelSystem();
        system.execute(sessionFactory);

        // assert
        Mockito.verify(travelService, times(25))
                .processTravelLogic(any(), any(), any());
    }
}
