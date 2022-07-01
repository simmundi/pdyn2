package pl.edu.icm.pdyn2;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.pdyn2.context.ContextFractionService;
import pl.edu.icm.pdyn2.context.ContextsService;
import pl.edu.icm.pdyn2.model.behaviour.Behaviour;
import pl.edu.icm.pdyn2.model.behaviour.BehaviourType;
import pl.edu.icm.pdyn2.model.immunization.Immunization;
import pl.edu.icm.pdyn2.model.immunization.ImmunizationEvent;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;
import pl.edu.icm.pdyn2.model.progression.HealthStatus;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.pdyn2.model.travel.Travel;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.Entity;

import static pl.edu.icm.pdyn2.model.progression.Stage.LATENT;
import static pl.edu.icm.pdyn2.model.progression.Stage.HEALTHY;

/**
 * <p>
 * Agent's state consists of orthogonal, interacting substates:
 * </p>
 *
 * <ul>
 *     <li>behavior</li>
 *     <li>immunization history</li>
 *     <li>medical records</li>
 *     <li>travel status</li>
 *     <li>progression of diseases</li>
 * </ul>
 * <p>
 *     Transitions between states are interdependent: an agent who is hospitalized - cannot travel;
 *     an agent who becomes healthy - has their immunization history updated.
 * <p>
 *     Transitions can be triggered from multiple places in the codebase,
 *     e.g. both the sowing module and the transmission module might
 *     make an agent sick, hence the need to centralize the transition logic.
 * <p>
 *     Any changes to the state of agent SHOULD happen through this service.
 */
public class AgentStateService {

    private final SimulationTimer simulationTimer;

    @WithFactory
    public AgentStateService(SimulationTimer simulationTimer) {
        this.simulationTimer = simulationTimer;
    }

    public void activate(Entity agentEntity) {
        Behaviour behavior = agentEntity.getOrCreate(Behaviour.class);
        if (behavior.getType() != BehaviourType.DORMANT) {
            throw new IllegalArgumentException("Agent must be DORMANT to be activated; tried to activate " + behavior.getType() + " (" + agentEntity.getId() + ")");
        }
        behavior.transitionTo(BehaviourType.ROUTINE, simulationTimer.getDaysPassed());
    }


    /**
     * Starts a disease.
     * <p>
     * Agent becomes latent as of the current simulation day.
     * <p>
     * Subsequent changes of the state can be performed with
     * progressToDiseaseStage methods
     *
     * @param agentEntity
     * @param load
     */
    public void infect(Entity agentEntity, Load load) {
        infect(agentEntity, load, 0);
    }

    /**
     * Starts a disease.
     * <p>
     * Agent becomes latent at the specified day in the past.
     * <p>
     * Subsequent changes of the state can be performed with
     * progressToDiseaseStage methods
     *
     * @param agentEntity
     * @param load
     * @param dayInState
     */
    public void infect(Entity agentEntity, Load load, int dayInState) {
        Preconditions.checkArgument(dayInState >= 0, "Cannot infect agent at a future date. dayInState should be >= 0: %s", dayInState);
        HealthStatus healthStatus = getHealthStatusWhenNotSick(agentEntity);
        healthStatus.setDiseaseLoad(load);
        healthStatus.resetDiseaseHistory();
        healthStatus.transitionTo(Stage.LATENT, simulationTimer.getDaysPassed() - dayInState);
    }

    /**
     * <p>
     * Assuming an agent is already infected, this method allows to change the stage
     * of their disease and updates related states:
     * <ul>
     *     <li>immunization history (when the disease ends)
     *     <li>behavior (when agent is hospitalized or dead)
     * </ul>
     *
     * @param agentEntity
     * @param targetStage
     * @param dayInState
     */
    public void progressToDiseaseStage(Entity agentEntity, Stage targetStage, int dayInState) {
        Preconditions.checkArgument(dayInState >= 0, "Cannot progress agent at a future date. dayInState should be >= 0: %s", dayInState);
        HealthStatus healthStatus = getHealthStatus(agentEntity);
        healthStatus.transitionTo(targetStage, simulationTimer.getDaysPassed() - dayInState);

        switch (targetStage) {
            case DECEASED:
                agentEntity.getOrCreate(Behaviour.class).transitionTo(BehaviourType.DEAD, simulationTimer.getDaysPassed());
                // thru
            case HEALTHY:
                Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
                if (behaviour.getType() == BehaviourType.SELF_ISOLATION) {
                    behaviour.transitionTo(BehaviourType.ROUTINE, simulationTimer.getDaysPassed());
                }
                Immunization immunization = agentEntity.getOrCreate(Immunization.class);
                immunization
                        .getEvents()
                        .add(createEventFromHealth(healthStatus));
                break;
            case HOSPITALIZED_NO_ICU:
                // thru
            case HOSPITALIZED_PRE_ICU:
                // thru
            case HOSPITALIZED_ICU:
                agentEntity.getOrCreate(Behaviour.class)
                        .transitionTo(BehaviourType.HOSPITALIZED, simulationTimer.getDaysPassed());
                break;
            default:
                break;
        }

    }

    public void progressToDiseaseStage(Entity agentEntity, Stage targetState) {
        progressToDiseaseStage(agentEntity, targetState, 0);
    }

    public void beginTravel(Entity agentEntity, Entity targetHousehold) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        Preconditions.checkArgument(behaviour.getType() == BehaviourType.ROUTINE, "Illegal agent behaviour for private travel: %s", behaviour.getType());

        behaviour.transitionTo(BehaviourType.PRIVATE_TRAVEL, simulationTimer.getDaysPassed());
        Travel travel = getOrCreateTravel(agentEntity);
        travel.setDayOfTravel((short) simulationTimer.getDaysPassed());
        travel.setStayingAt(targetHousehold);

    }

    public void endTravel(Entity agentEntity) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        Preconditions.checkArgument(behaviour.getType() == BehaviourType.PRIVATE_TRAVEL, "Cannot end travel since the agent state is: %s", behaviour.getType());

        resumeRoutine(agentEntity, behaviour);
    }

    public void beginQuarantine(Entity agentEntity) {
        beginQuarantineOnDay(agentEntity, simulationTimer.getDaysPassed());
    }

    public void beginQuarantineOnDay(Entity agentEntity, int day) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        if (behaviour.getType() == BehaviourType.QUARANTINE) {
            return;
        }

        behaviour.transitionTo(BehaviourType.QUARANTINE, day);
    }

    public void endQuarantine(Entity agentEntity) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        Preconditions.checkArgument(behaviour.getType() == BehaviourType.QUARANTINE, "Cannot end quarantine behaviour since the current behaviour is: %s", behaviour.getType());

        resumeRoutine(agentEntity, behaviour);
    }

    public boolean beginIsolation(Entity agentEntity) {
        Behaviour behaviour = agentEntity.getOrCreate(Behaviour.class);
        if (behaviour.getType() == BehaviourType.SELF_ISOLATION
                || behaviour.getType() == BehaviourType.QUARANTINE) {
            return false;
        }

        behaviour.transitionTo(BehaviourType.SELF_ISOLATION, simulationTimer.getDaysPassed());
        return true;
    }

    public void changeLoad(Entity agentEntity, Load targetLoad) {
        var health = getHealthStatus(agentEntity);
        Preconditions.checkArgument(targetLoad.classification.equals(LoadClassification.VIRUS), "Variant can be changed only to another virus load");
        Preconditions.checkArgument(health.getStage().equals(LATENT), "Only latent agents can have their load changed");

        health.setDiseaseLoad(targetLoad);
    }

    private ImmunizationEvent createEventFromHealth(HealthStatus healthStatus) {
        ImmunizationEvent immunizationEvent = new ImmunizationEvent();
        immunizationEvent.setLoad(healthStatus.getDiseaseLoad());
        immunizationEvent.setDay(healthStatus.getDayOfLastChange());
        immunizationEvent.setDiseaseHistory(healthStatus.getDiseaseHistory());
        return immunizationEvent;
    }

    private HealthStatus getHealthStatusWhenNotSick(Entity agentEntity) {
        HealthStatus healthStatus = agentEntity.get(HealthStatus.class);
        Preconditions.checkArgument(healthStatus.getStage() == HEALTHY,
                "Agent to infect must be alive and healthy - tried to infect %s agent.",
                healthStatus.getStage());
        return healthStatus;
    }

    private Travel getOrCreateTravel(Entity agentEntity) {
        Travel travel = agentEntity.get(Travel.class);
        if (travel == null) {
            travel = agentEntity.add(new Travel());
        }
        return travel;
    }

    private HealthStatus getHealthStatus(Entity agentEntity) {
        HealthStatus healthStatus = agentEntity.get(HealthStatus.class);
        Preconditions.checkNotNull(healthStatus, "Agent must have a healthStatus in order to progress it; none found.");
        return healthStatus;
    }

    private void resumeRoutine(Entity agentEntity, Behaviour behaviour) {
        behaviour.transitionTo(BehaviourType.ROUTINE, simulationTimer.getDaysPassed());
    }

}
