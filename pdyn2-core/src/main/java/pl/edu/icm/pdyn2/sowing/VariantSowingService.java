package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomAdaptor;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.EntitySystem;

import java.util.Random;
import java.util.stream.IntStream;

import static it.unimi.dsi.fastutil.ints.IntArrays.shuffle;
import static java.lang.Math.min;

public class VariantSowingService {
    private final AgentStateService agentStateService;
    private final Random random;

    @WithFactory
    public VariantSowingService(AgentStateService agentStateService, RandomProvider randomProvider) {
        this.agentStateService = agentStateService;
        this.random = RandomAdaptor.createAdaptor(randomProvider.getRandomGenerator(VariantSowingService.class));
    }

    public EntitySystem sowVariant(Load load, int count) {
        return sessionFactory -> {
            Session session = sessionFactory.create();
            var latentAgents
                    = session.getEngine().streamDetached()
                    .filter(e -> e.get(HealthStatus.class) != null)
                    .filter(e -> e.get(HealthStatus.class).getStage().equals(Stage.LATENT))
                    .mapToInt(Entity::getId).toArray();
            shuffle(latentAgents, random);
            IntStream.range(0, min(latentAgents.length, count))
                    .forEach(i -> agentStateService.changeLoad(session.getEntity(latentAgents[i]), load));
            session.close();
        };
    }
}
