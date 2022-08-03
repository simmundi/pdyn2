package pl.edu.icm.pdyn2.progression;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.StatsService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.ecs.Entity;

import java.io.File;
import java.util.EnumMap;

public class DiseaseStageTransitionsService {
    private final EnumMap<Load, LoadDiseaseStageTransitions> loadSpecificTransitions = new EnumMap<>(Load.class);

    @WithFactory
    public DiseaseStageTransitionsService(String infectionTransitionsDirectory,
                                          WorkDir filesystem, LoadDiseaseStageTransitionsReader loadDiseaseStageTransitionsReader) {
        File[] files = filesystem.listFiles(new File(infectionTransitionsDirectory), file -> file.getName().endsWith(".txt"));

        for (File file : files) {
            String loadName = file.getName().replaceFirst("^.*?([^_]*)\\.txt$", "$1");
            Load load = Load.valueOf(loadName);
            loadSpecificTransitions.put(load, loadDiseaseStageTransitionsReader.readFromFile(file.getAbsolutePath(), load));
        }
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
