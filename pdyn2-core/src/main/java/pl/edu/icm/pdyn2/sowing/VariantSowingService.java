package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.commons.math3.random.RandomAdaptor;
import pl.edu.icm.board.util.RandomProvider;
import pl.edu.icm.pdyn2.AgentStateService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.Selectors;

import java.util.Random;
import java.util.stream.IntStream;

import static it.unimi.dsi.fastutil.ints.IntArrays.shuffle;
import static java.lang.Math.min;

public class VariantSowingService {
    private final AgentStateService agentStateService;
    private final Random random;
    private final Selectors selectors;

    @WithFactory
    public VariantSowingService(AgentStateService agentStateService,
                                RandomProvider randomProvider,
                                Selectors selectors) {
        this.agentStateService = agentStateService;
        this.random = RandomAdaptor.createAdaptor(randomProvider.getRandomGenerator(VariantSowingService.class));
        this.selectors = selectors;
    }

    public EntitySystem sowVariant(Load load, int count) {
        return sessionFactory -> {
            Session session = sessionFactory.create();
            var latentAgents
                    = selectors.filtered(
                            selectors.allEntities(),
                            HealthStatus.class,
                            healthStatus -> healthStatus.getStage() == Stage.LATENT).chunks()
                    .flatMapToInt(chunk -> chunk.ids()).toArray();
            shuffle(latentAgents, random);
            IntStream.range(0, min(latentAgents.length, count))
                    .forEach(i -> agentStateService.changeLoad(session.getEntity(latentAgents[i]), load));
            session.close();
        };
    }
}
