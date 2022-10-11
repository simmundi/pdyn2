package pl.edu.icm.pdyn2.dynamic;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.EpidemicConfig;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.time.LocalDate;

import static pl.edu.icm.pdyn2.dynamic.DynamicVariablesService.DynamicVariable.*;

public class DynamicVariableSystemBuilder {

    private final DynamicVariablesService service;
    private final SimulationTimer timer;
    private final EpidemicConfig config;

    @WithFactory
    public DynamicVariableSystemBuilder(DynamicVariablesService dynamicVariablesService,
                                        SimulationTimer timer,
                                        EpidemicConfig config) {
        this.service = dynamicVariablesService;
        this.timer = timer;
        this.config = config;
    }

    public EntitySystem buildDynamicVariableSystem() {
        return (session) -> {
            LocalDate day = timer.getCurrentDate();
            float household = service.getFloatForDate(HOUSEHOLD_W, day);
            float workplace = service.getFloatForDate(WORKPLACE_W, day);
            float kindergarten = service.getFloatForDate(KINDERGARTEN_W, day);
            float school = service.getFloatForDate(SCHOOL_W, day);
            float university = service.getFloatForDate(UNIVERSITY_W, day);
            float bigUniversity = service.getFloatForDate(BIG_UNIVERSITY_W, day);
            float street = service.getFloatForDate(STREET_W, day);
            float isolation = service.getFloatForDate(ISOLATION_P, day);

            config.household(household)
                    .workplace(workplace)
                    .kindergarten(kindergarten)
                    .school(school)
                    .university(university)
                    .bigUniversity(bigUniversity)
                    .street(street)
                    .isolation(isolation);
        };
    }
}
