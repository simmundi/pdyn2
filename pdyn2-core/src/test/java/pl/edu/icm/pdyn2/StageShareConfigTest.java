package pl.edu.icm.pdyn2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.pdyn2.model.progression.Stage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StageShareConfigTest {

    @Test
    void getInfluenceOf() {
        //given
        var stageShareConfig = new StageShareConfig(0.1f, 1.0f);
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