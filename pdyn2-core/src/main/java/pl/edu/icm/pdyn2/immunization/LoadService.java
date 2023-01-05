package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;

import java.util.List;
import java.util.stream.Collectors;

public class LoadService {

    private List<Load> loads;
    private final String loadFilename;
    private final LoadServiceLoader loader;

    @WithFactory
    public LoadService(String loadFilename, LoadServiceLoader loadServiceLoader) {
        this.loadFilename = loadFilename;
        this.loader = loadServiceLoader;
        loads = loader.load();
    }

    public List<Load> getLoads() {
        return loads;
    }

    public List<Load> getVaccines() {
        return loads.stream().filter(load -> load.getClassification() == LoadClassification.VACCINE)
                .collect(Collectors.toList());
    }

    public List<Load> getViruses() {
        return loads.stream().filter(load -> load.getClassification() == LoadClassification.VIRUS)
                .collect(Collectors.toList());
    }

    public Load getLoad(int virusEncoding, int vaccineEncoding) {
        for (Load load:loads) {
            if (load.getVirusEncoding() == virusEncoding && load.getVaccineEncoding() == vaccineEncoding) {
                return load;
            }
        }
        throw new IllegalArgumentException("Could not find matching load for: disease=" + virusEncoding +
                " and vaccine=" + vaccineEncoding);
    }

    public Load getLoad(String loadName) {
        for (Load load:loads) {
            if (load.getLoadName() == loadName) {
                return load;
            }
        }
        throw new IllegalArgumentException("Could not find matching load for: loadName=" + loadName);
    }
}
