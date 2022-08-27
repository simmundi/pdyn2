package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.transmission.StageImpactConfig;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@ExtendWith(MockitoExtension.class)
class StageImpactConfigTest {

    @Test
    void getInfluenceOf() {
        //given
        var stageShareConfig = new StageImpactConfig(0.1f, 1.0f);
        //execute
        for (var stage : Stage.values()) {
            float value = 0f;
            switch (stage) {
                case INFECTIOUS_ASYMPTOMATIC:
                    value = 0.1f;
                    break;
                case INFECTIOUS_SYMPTOMATIC:
                    value = 1f;
                    break;
            }
            //assert
            assertThat(stageShareConfig.getInfluenceOf(stage)).isEqualTo(value);
        }
    }
}
