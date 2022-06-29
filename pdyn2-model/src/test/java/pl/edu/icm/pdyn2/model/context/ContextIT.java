package pl.edu.icm.pdyn2.model.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static pl.edu.icm.pdyn2.model.context.ContextType.HOUSEHOLD;
import static pl.edu.icm.pdyn2.model.context.ContextType.SCHOOL;
import static pl.edu.icm.pdyn2.model.context.ContextType.STREET_10;
import static pl.edu.icm.pdyn2.model.context.ContextType.WORKPLACE;
import static pl.edu.icm.pdyn2.model.immunization.Load.ALPHA;
import static pl.edu.icm.pdyn2.model.immunization.Load.BA2;
import static pl.edu.icm.pdyn2.model.immunization.Load.DELTA;
import static pl.edu.icm.pdyn2.model.immunization.Load.OMICRON;
import static pl.edu.icm.pdyn2.model.immunization.Load.WILD;

public class ContextIT {
    private Mapper<Context> mapper;

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
                        tuple(136f, WILD),
                        tuple(45f, ALPHA),
                        tuple(89f, DELTA),
                        tuple(5f, OMICRON),
                        tuple(20000.23f, OMICRON),
                        tuple(10f, DELTA),
                        tuple(10f, OMICRON));
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
        context.changeContaminationLevel(DELTA, 3.4f); // was 10f
        context.changeContaminationLevel(WILD, 15f);
        mapper.save(context, 3);

        // assert
        var result = mapper.createAndLoad(3);
        assertThat(result.getContaminationByLoad(WILD).getLevel()).isEqualTo(15f);
        assertThat(result.getContaminationByLoad(DELTA).getLevel()).isEqualTo(13.4f);
        assertThat(result.getContaminationByLoad(OMICRON).getLevel()).isEqualTo(10f);
        assertThat(result.getContaminations()).hasSize(3);
    }

    @Test
    @DisplayName("Should resolve conflicts by summing two contexts")
    void resolve() {
        // given
        Context contextA = exampleData().get(0);
        Context contextB = exampleData().get(0);

        contextA.changeContaminationLevel(WILD, 10f);
        contextA.changeContaminationLevel(ALPHA, 20f);
        contextA.changeContaminationLevel(DELTA, 30f);
        contextA.changeContaminationLevel(OMICRON, 40f);
        contextA.updateAgentCount(50);

        contextB.changeContaminationLevel(WILD, 100f);
        contextB.changeContaminationLevel(ALPHA, 200f);
        contextB.changeContaminationLevel(BA2, 300f);
        contextB.updateAgentCount(-100);

        // execute
        Context resolved = contextA.resolve(contextB);
        assertThat(resolved.getAgentCount()).isEqualTo(100f + 50f - 100f);
        assertThat(resolved.getContaminationByLoad(WILD).getLevel()).isEqualTo(136f + 10f + 100f);
        assertThat(resolved.getContaminationByLoad(ALPHA).getLevel()).isEqualTo(45f + 20f + 200f);
        assertThat(resolved.getContaminationByLoad(DELTA).getLevel()).isEqualTo(89f + 30f + 0f);
        assertThat(resolved.getContaminationByLoad(OMICRON).getLevel()).isEqualTo(5f + 40f + 0f);
        assertThat(resolved.getContaminationByLoad(BA2).getLevel()).isEqualTo(0f + 0f + 300f);
    }

    @Test
    @DisplayName("Should normalize: sort contaminations by load and remove empty")
    void normalize() {
        // given
        Context context = context(HOUSEHOLD, 10, contamination(10, WILD));

        context.changeContaminationLevel(BA2, 10f);
        context.changeContaminationLevel(OMICRON, 20f);
        context.changeContaminationLevel(DELTA, 30f);
        context.updateAgentCount(50);
        context.getContaminationByLoad(OMICRON).setLevel(0f);
        context.getContaminationByLoad(ALPHA).setLevel(0f);

        // execute
        context.normalize();

        // execute
        assertThat(context.getContaminations()).extracting(Contamination::getLoad).containsExactly(WILD, DELTA, BA2);
        assertThat(context.getContaminations()).extracting(Contamination::getLevel).doesNotContain(0f);
    }


    private List<Context> exampleData() {
        return List.of(
                context(SCHOOL, 100,
                        contamination(136, WILD),
                        contamination(45, ALPHA),
                        contamination(89, DELTA),
                        contamination(5, OMICRON)),
                context(HOUSEHOLD, 5),
                context(STREET_10, 50000,
                        contamination(20000.23f, OMICRON)),
                context(WORKPLACE, 20,
                        contamination(10, DELTA),
                        contamination(10, OMICRON)),
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
