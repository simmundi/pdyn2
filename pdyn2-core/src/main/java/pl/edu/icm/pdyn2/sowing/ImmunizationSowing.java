package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.agesex.AgeSex;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.bin.BinPool;

import java.util.HashMap;
import java.util.Map;

public class ImmunizationSowing {
    private final ImmunizationLoader immunizationLoader;
    private BinPool<String> areaCodes;
    private Map<AgeSex, Integer> ageSexMap;
    private BinPool<Load> loads;
    private BinPool<Integer> days;

    @WithFactory
    public ImmunizationSowing(ImmunizationLoader immunizationLoader) {
        this.immunizationLoader = immunizationLoader;
    }

    public void sow(String sowingFilename) {
        areaCodes = new BinPool<>();
        ageSexMap = new HashMap<>();
        loads = new BinPool<>();
        days = new BinPool<>();
        immunizationLoader.forEach(e -> {
            var recordCount = e.getRecordCount();
            var areaCode = e.getAreaCode();
            var ageSex = e.getAgeSex();
            var load = e.getLoad();
            var daysValue = e.getDays();
            if (!areaCode.equals("")) {
                areaCodes.add(areaCode, recordCount);
            }
            if (ageSex != null) {
                ageSexMap.compute(e.getAgeSex(), (t, v) -> (v == null) ? recordCount : v + recordCount);
            }
            if (load != null) {
                loads.add(load, recordCount);
            }
            if (daysValue >= 0) {
                days.add(daysValue, recordCount);
            }
        }, sowingFilename);
    }
}
