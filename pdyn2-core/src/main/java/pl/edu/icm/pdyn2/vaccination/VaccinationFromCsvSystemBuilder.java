package pl.edu.icm.pdyn2.vaccination;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.time.SimulationTimer;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.util.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VaccinationFromCsvSystemBuilder {
    private final VaccinationFromCsvLoader loader;
    private final SimulationTimer simulationTimer;
    private final Map<Integer, Set<VaccinationRecord>> vaccinationRecords = new HashMap<>();


    @WithFactory
    public VaccinationFromCsvSystemBuilder(VaccinationFromCsvLoader vaccinationFromCsvLoader,
                                           SimulationTimer simulationTimer) {
        this.loader = vaccinationFromCsvLoader;
        this.simulationTimer = simulationTimer;
    }

    public void load() {
        var loadingStatus = Status.of("Loading vaccination records from file");
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loader.forEach(recordFromCsv -> vaccinationRecords.compute(recordFromCsv.getDay(), (day, records) -> {
            var record = new VaccinationRecord();
            record.setLoad(recordFromCsv.getLoad());
            record.setMinAge(recordFromCsv.getMinAge());
            record.setMaxAge(recordFromCsv.getMaxAge());
            record.setVaccineCount(recordFromCsv.getVaccineCount());
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

    public EntitySystem buildVaccinationSystem() {
        return sessionFactory -> {
            var currentDay = simulationTimer.getDaysPassed();
            if (vaccinationRecords.isEmpty()) {
                throw new IllegalStateException("No vaccination records");
            }
            if (!vaccinationRecords.containsKey(currentDay)) {
                return;
            }
            for (VaccinationRecord record : vaccinationRecords.get(currentDay)) {
                //todo: implement vaccine system
            }
        };
    }
}
