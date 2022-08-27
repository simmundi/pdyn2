package pl.edu.icm.pdyn2.impact;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.transmission.ContextImpactService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.impact.Impact;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.transmission.StageImpactConfig;
import pl.edu.icm.trurl.ecs.Entity;

/**
 * If agents impact has changed from the last time it was checked,
 * it is replaced (by withdrawing the old impact and applying the new)
 */
public class AgentImpactService {
    private final ContextsService contextsService;
    private final ContextImpactService contextImpactService;
    private final StatsService statsService;
    private final StageImpactConfig stageImpactConfig;
    private final int REMOVE = -1;
    private final int ADD = 1;

    @WithFactory
    public AgentImpactService(ContextsService contextsService, ContextImpactService contextImpactService, StatsService statsService, StageImpactConfig stageImpactConfig) {
        this.contextsService = contextsService;
        this.contextImpactService = contextImpactService;
        this.statsService = statsService;
        this.stageImpactConfig = stageImpactConfig;
    }

    public void updateImpact(Entity agentEntity) {
        Impact impact = agentEntity.getOrCreate(Impact.class);
        HealthStatus disease = agentEntity.get(HealthStatus.class);
        Behaviour behaviour = agentEntity.get(Behaviour.class);

        if (impact.isDifferentFrom(behaviour, disease)) {
            Person person = agentEntity.get(Person.class);
            applyAgentInfluence(agentEntity, impact, person, REMOVE);
            impact.affect(behaviour, disease);
            applyAgentInfluence(agentEntity, impact, person, ADD);
            statsService.tickChangedImpact();
        }
    }

    private void applyAgentInfluence(Entity agentEntity, Impact impact, Person person, int sign) {
        Stage currentStage = impact.getStage() == null ? Stage.HEALTHY : impact.getStage();
        Load load = currentStage.isInfectious() ? impact.getLoad() : null;

        float activityDelta = sign;
        float infectionDelta = stageImpactConfig.getInfluenceOf(currentStage) * sign;

        contextsService.findActiveContextsForAgent(agentEntity, impact).forEach(c -> {
            float influenceFraction = contextImpactService.calculateInfluenceFractionFor(person, c);
            c.updateAgentCount(activityDelta * influenceFraction);
            if (load != null) {
                float scaledInfectionDelta = infectionDelta * influenceFraction;
                c.changeContaminationLevel(load, scaledInfectionDelta);
            }
        });
    }
}
