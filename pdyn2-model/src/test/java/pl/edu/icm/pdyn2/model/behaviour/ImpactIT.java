package pl.edu.icm.pdyn2.model.behaviour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImpactIT {

    private Mapper<Impact> mapper;
    Store store = new ArrayStore(10);

    @BeforeEach
    void before() {
        mapper = Mappers.create(Impact.class);
        mapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save and reload impact")
    public void save() {
        // given
        var health = new HealthStatus();
        health.setDiseaseLoad(Load.BA2);
        health.setStage(Stage.INFECTIOUS_ASYMPTOMATIC);
        var behavior = new Behaviour();
        behavior.transitionTo(BehaviourType.DORMANT, 2);
        var impact = new Impact();
        impact.affect(behavior, health);

        int idx = 3;
        mapper.save(impact, idx);

        // execute
        var copy = mapper.createAndLoad(idx);

        // assert
        assertThat(copy).isEqualTo(impact);
    }

    @Test
    @DisplayName("Should confirm that impact is different")
    public void isDifferentFrom() {
        // given
        var health = new HealthStatus();
        health.setDiseaseLoad(Load.BA2);
        health.setStage(Stage.INFECTIOUS_ASYMPTOMATIC);
        var behavior = new Behaviour();
        behavior.transitionTo(BehaviourType.ROUTINE, 2);
        var impact = new Impact();

        // execute
        impact.affect(behavior, health);
        behavior.setType(BehaviourType.HOSPITALIZED);
        var result = impact.isDifferentFrom(behavior, health);

        // assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should confirm that impact stayed the same even though day was changed")
    public void isDifferentFrom__same() {
        // given
        var health = new HealthStatus();
        health.setDiseaseLoad(Load.BA2);
        health.setStage(Stage.INFECTIOUS_ASYMPTOMATIC);
        var behavior = new Behaviour();
        var impact = new Impact();
        // execute
        impact.affect(behavior, health);
        behavior.setDayOfLastChange(3);
        var result = impact.isDifferentFrom(behavior, health);

        // assert
        assertThat(result).isFalse();
    }
}
