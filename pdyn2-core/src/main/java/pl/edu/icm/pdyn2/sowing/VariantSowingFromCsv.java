package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VariantSowingFromCsv {
    private final VariantSowingFromCsvLoader loader;
    private final VariantSowingService sowingService;
    private final SimulationTimer simulationTimer;
    private Map<Integer, Set<VariantSowingRecord>> sowingRecords = new HashMap<>();


    @WithFactory
    public VariantSowingFromCsv(VariantSowingFromCsvLoader variantSowingFromCsvLoader,
                                VariantSowingService variantSowingService,
                                SimulationTimer simulationTimer) {
        this.loader = variantSowingFromCsvLoader;
        this.sowingService = variantSowingService;
        this.simulationTimer = simulationTimer;
    }

    public void load() {
        var loadingStatus = Status.of("Loading variant sowing records from file");
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loader.forEach(recordFromCsv -> sowingRecords.compute(recordFromCsv.getDay(), (day, records) -> {
            var record = new VariantSowingRecord();
            record.setLoad(recordFromCsv.getLoad());
            record.setMinAge(recordFromCsv.getMinAge());
            record.setMaxAge(recordFromCsv.getMaxAge());
            record.setSowingCount(recordFromCsv.getSowingCount());
            record.setTeryts(List.of(recordFromCsv.getTeryts().split(", ")));
            if (records == null) {
                return Set.of(record);
            } else {
                records.add(record);
                return records;
            }
        }));
        loadingStatus.done();
    }

    public EntitySystem sowFromFile() {
        return sessionFactory -> {
            var currentDay = simulationTimer.getDaysPassed();
            if (sowingRecords.isEmpty()) {
                throw new IllegalStateException("No variant sowing records");
            }
            if (!sowingRecords.containsKey(currentDay)) {
                return;
            }
            for (VariantSowingRecord record : sowingRecords.get(currentDay)) {
                //todo: sow
            }
        };
    }
}
