package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.util.EnumMap;

public class DiseaseStageTransitionsService {
    private final EnumMap<Load, LoadDiseaseStageTransitions> loadSpecificTransitions = new EnumMap<>(Load.class);

    @WithFactory
    public DiseaseStageTransitionsService(String wildStageTransitionsFilename,
                                          String alphaStageTransitionsFilename,
                                          String deltaStageTransitionsFilename,
                                          String omicronStageTransitionsFilename,
                                          String ba2StageTransitionsFilename,
                                          LoadDiseaseStageTransitionsReader reader) {
        loadSpecificTransitions.put(Load.WILD, reader.readFromFile(wildStageTransitionsFilename, Load.WILD));
        loadSpecificTransitions.put(Load.ALPHA, reader.readFromFile(alphaStageTransitionsFilename, Load.ALPHA));
        loadSpecificTransitions.put(Load.DELTA, reader.readFromFile(deltaStageTransitionsFilename, Load.DELTA));
        loadSpecificTransitions.put(Load.OMICRON, reader.readFromFile(omicronStageTransitionsFilename, Load.OMICRON));
        loadSpecificTransitions.put(Load.BA2, reader.readFromFile(ba2StageTransitionsFilename, Load.BA2));
    }

    public int durationOf(Load load, Stage stage, int age) {
        return loadSpecificTransitions.get(load).durationOf(stage, age);
    }

    public Stage outcomeOf(Stage stage,
                           Entity person,
                           Load load,
                           double random) {
        return loadSpecificTransitions.get(load).outcomeOf(stage, person, random);
    }

}
