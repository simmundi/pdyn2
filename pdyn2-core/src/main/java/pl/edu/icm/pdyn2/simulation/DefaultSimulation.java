package pl.edu.icm.pdyn2.simulation;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.administration.TestingSystemBuilder;
import pl.edu.icm.pdyn2.behaviour.BehaviourDrivenLogicBuilder;
import pl.edu.icm.pdyn2.impact.ImpactSystemBuilder;
import pl.edu.icm.pdyn2.progression.DiseaseProgressionSystemBuilder;
import pl.edu.icm.pdyn2.transmission.TransmissionSystemBuilder;
import pl.edu.icm.pdyn2.vaccination.VaccinationFromCsvSystemBuilder;
import pl.edu.icm.pdyn2.variantsowing.VariantSowingFromCsvSystemBuilder;
import pl.edu.icm.trurl.ecs.EntitySystem;

import static pl.edu.icm.trurl.ecs.util.Systems.sequence;
import static pl.edu.icm.trurl.ecs.util.Systems.withStatusMessage;

/**
 * Builds a system representing the whole simulation logic for the disease spread.
 * Should be composable with other simulations, disease-oriented or not, running
 * in parallel.
 * <p>
 * Does not include ticking the simulation clock or gathering statistics.
 */
public class DefaultSimulation {
    private final TransmissionSystemBuilder transmissionSystemBuilder;
    private final DiseaseProgressionSystemBuilder diseaseProgressionSystemBuilder;
    private final BehaviourDrivenLogicBuilder behaviourDrivenLogicBuilder;
    private final TestingSystemBuilder testingSystemBuilder;
    private final ImpactSystemBuilder impactSystemBuilder;
    private final VariantSowingFromCsvSystemBuilder variantSowingSystemBuilder;
    private final VaccinationFromCsvSystemBuilder vaccinationSystemBuilder;

    @WithFactory
    public DefaultSimulation(TransmissionSystemBuilder transmissionSystemBuilder,
                             DiseaseProgressionSystemBuilder diseaseProgressionSystemBuilder,
                             BehaviourDrivenLogicBuilder behaviourDrivenLogicBuilder,
                             TestingSystemBuilder testingSystemBuilder,
                             ImpactSystemBuilder impactSystemBuilder, VariantSowingFromCsvSystemBuilder variantSowingSystemBuilder, VaccinationFromCsvSystemBuilder vaccinationSystemBuilder) {
        this.transmissionSystemBuilder = transmissionSystemBuilder;
        this.diseaseProgressionSystemBuilder = diseaseProgressionSystemBuilder;
        this.behaviourDrivenLogicBuilder = behaviourDrivenLogicBuilder;
        this.testingSystemBuilder = testingSystemBuilder;
        this.impactSystemBuilder = impactSystemBuilder;
        this.variantSowingSystemBuilder = variantSowingSystemBuilder;
        this.vaccinationSystemBuilder = vaccinationSystemBuilder;
    }

    public EntitySystem defaultSimulationDay() {
        var variantSowingSystem = variantSowingSystemBuilder.buildVariantSowingSystem();
        var vaccinationSystem = vaccinationSystemBuilder.buildVaccinationSystem();
        var progressionSystem = diseaseProgressionSystemBuilder.buildProgressionSystem();
        var transmissionSystem = transmissionSystemBuilder.buildTransmissionSystem();
        var behaviourSystem = behaviourDrivenLogicBuilder.buildTravelSystem();
        var testingSystem = testingSystemBuilder.buildTestingSystem();
        var impactSystem = impactSystemBuilder.buildImpactSystem();
        return sequence(
                (unused) -> {
                    System.out.println();
                    System.out.println("Calculating another day:");
                },
                variantSowingSystem,
                vaccinationSystem,
                withStatusMessage(impactSystem, "Impact"),
                withStatusMessage(transmissionSystem, "Transmission"),
                withStatusMessage(progressionSystem, "Progression"),
                withStatusMessage(behaviourSystem, "Travel and quarantine"),
                withStatusMessage(testingSystem, "Testing"));
    }
}
