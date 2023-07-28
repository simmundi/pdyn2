package net.snowyhollows.epi.spatial.runner;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.epi.spatial.logic.SpatialStrategies;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;

import java.io.IOException;


public class Main {
    @WithFactory
    public Main(EngineConfiguration engineConfiguration, SpatialStrategies spatialStrategies) {
        Engine engine = engineConfiguration.getEngine();
        engine.execute(s -> {
            spatialStrategies.initializeContexts(s.create());
        });
    }

    public static void main(String[] args) throws IOException {
        EmConfig.configurer(new String[]{"--list-properties"})
                .loadHoconFile("pdyn2-spatial/pdyn2-spatial.conf")
                .getConfig()
                .get(MainFactory.IT);
    }
}
