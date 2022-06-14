package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.immunization.ImmunizationService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

public class LoadDiseaseStageTransitionsReader {
    private final ImmunizationService immunizationService;
    private final SimulationTimer simulationTimer;
    private final Filesystem filesystem;


    @WithFactory
    public LoadDiseaseStageTransitionsReader(ImmunizationService immunizationService, SimulationTimer simulationTimer) {
        this(immunizationService, simulationTimer, new DefaultFilesystem());
    }

    public LoadDiseaseStageTransitionsReader(ImmunizationService immunizationService, SimulationTimer simulationTimer, Filesystem filesystem) {
        this.immunizationService = immunizationService;
        this.simulationTimer = simulationTimer;
        this.filesystem = filesystem;
    }

    public LoadDiseaseStageTransitions readFromFile(String absolutePath, Load load) {
        return new LoadDiseaseStageTransitions(
                    absolutePath,
                    immunizationService,
                    simulationTimer,
                    filesystem,
                    load);
    }
}
