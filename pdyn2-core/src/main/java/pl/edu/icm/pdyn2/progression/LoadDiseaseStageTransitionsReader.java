package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.time.SimulationTimer;

public class LoadDiseaseStageTransitionsReader {
    private final ImmunizationService immunizationService;
    private final SimulationTimer simulationTimer;
    private final WorkDir workDir;


    @WithFactory
    public LoadDiseaseStageTransitionsReader(ImmunizationService immunizationService, SimulationTimer simulationTimer, WorkDir workDir) {
        this.immunizationService = immunizationService;
        this.simulationTimer = simulationTimer;
        this.workDir = workDir;
    }

    public LoadDiseaseStageTransitions readFromFile(String absolutePath, Load load) {
        return new LoadDiseaseStageTransitions(
                    absolutePath,
                    immunizationService,
                    simulationTimer,
                workDir,
                    load);
    }
}
