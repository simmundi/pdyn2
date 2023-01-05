package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;

import java.util.ArrayList;
import java.util.List;

public class LoadServiceLoader {

    private final String loadFilename;

    @WithFactory
    public LoadServiceLoader(String loadFilename) {
        this.loadFilename = loadFilename;
    }

    public List<Load> load() {
        var loads = new ArrayList<Load>();


        return loads;
    }
}
