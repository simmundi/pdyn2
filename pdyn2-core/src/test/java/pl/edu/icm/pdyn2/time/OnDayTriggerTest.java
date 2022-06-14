package pl.edu.icm.pdyn2.time;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.util.Systems;

import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class OnDayTriggerTest {
    SimulationTimer simulationTimer = new SimulationTimer("");
    @Mock
    SessionFactory sessionFactory;

    @Test
    void onDay() {
        //given
        var onday = new OnDayTrigger(simulationTimer);
        final Map<Integer, Integer> day = new Int2IntArrayMap();
        //execute
        EntitySystem sequence = Systems.sequence(
                onday.onDay(0, (s) -> day.put(0,simulationTimer.getDaysPassed())),
                onday.onDay(3, (s) -> day.put(3,simulationTimer.getDaysPassed())),
                onday.onDay(5, (s) -> day.put(5,simulationTimer.getDaysPassed())),
                simulationTimer
        );
        IntStream.range(0, 6).forEach(x -> sequence.execute(sessionFactory));
        //assert
        day.forEach((x,y) -> assertThat(x).isEqualTo(y));
    }
}
