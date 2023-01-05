package pl.edu.icm.pdyn2;

import pl.edu.icm.pdyn2.immunization.LoadService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.LoadClassification;

public class MockLoadService extends LoadService {
    public MockLoadService() {
        super("", null);
        this.getLoads().add(new Load("WILD", LoadClassification.VIRUS,-1,0,"wild"));
        this.getLoads().add(new Load("ALPHA", LoadClassification.VIRUS,-1,1,"alpha"));
        this.getLoads().add(new Load("DELTA", LoadClassification.VIRUS,-1,2,"delta"));
        this.getLoads().add(new Load("OMICRON", LoadClassification.VIRUS,-1,3,"omicron"));
        this.getLoads().add(new Load("BA2", LoadClassification.VIRUS,-1,4,"ba2"));
        this.getLoads().add(new Load("BA45", LoadClassification.VIRUS,-1,5,"ba45"));
        this.getLoads().add(new Load("PFIZER", LoadClassification.VACCINE,0,-1,""));
        this.getLoads().add(new Load("ASTRA", LoadClassification.VACCINE,0,-1,""));
        this.getLoads().add(new Load("MODERNA", LoadClassification.VACCINE,0,-1,""));
        this.getLoads().add(new Load("BOOSTER", LoadClassification.VACCINE,1,-1,""));
    }
}
