package pl.edu.icm.pdyn2;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.progression.Stage;
import pl.edu.icm.trurl.util.DebugFile;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;

/**
 * Central place to store the essential statistical aggregates.
 */
public class StatsService {
    private final EnumMap<Stage, AtomicInteger> statsAll = new EnumMap<>(Stage.class);
    private final EnumMap<Stage, AtomicInteger> statsDailyNew = new EnumMap<>(Stage.class);
    private final Map<String, AtomicInteger> additionalStatistics = new HashMap<>();
    private DebugFile debugFile;


    @WithFactory
    public StatsService() {
        for (Stage stage : Stage.values()) {
            statsAll.put(stage, new AtomicInteger(0));
            statsDailyNew.put(stage, new AtomicInteger(0));
        }
        additionalStatistics.put("testedPositive", new AtomicInteger());
        additionalStatistics.put("isolated", new AtomicInteger());
        additionalStatistics.put("quarantined", new AtomicInteger());
        additionalStatistics.put("unquarantined", new AtomicInteger());
        additionalStatistics.put("changedImpact", new AtomicInteger());
    }

    /**
     * Allows for quick and dirty output of all the statistics
     *
     * @param out
     */
    public void debugOutputStats(PrintStream out) {
        for (Stage stage : statsAll.keySet()) {
            out.println(" - " + stage + ": " + statsAll.get(stage).get() + ", delta: " + statsDailyNew.get(stage).get());
        }
        out.println(" - tested positive: " + additionalStatistics.get("testedPositive").get());
        out.println(" - isolated: " + additionalStatistics.get("isolated").get());
        out.println(" - quarantined: " + additionalStatistics.get("quarantined").get());
        out.println(" - unquarantined: " + additionalStatistics.get("unquarantined").get());
        out.println(" - changed impact: " + additionalStatistics.get("changedImpact").get());
    }

    public void createStatsOutputFile(String simulationStatsOutputFilename) {
        checkState(debugFile == null, "Output file has already been created");
        try {
            debugFile = DebugFile.create(simulationStatsOutputFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        var d1_header = concat(
                Stream.of(Stage.values()).map(Enum::name),
                additionalStatistics.keySet().stream().map(String::toUpperCase)
        ).collect(joining(",d1_", "d1_", ","));
        var headerContinued = Stream.of(Stage.values()).map(Enum::name)
                .collect(joining(",", "", ""));
        debugFile.println(d1_header + headerContinued);
        debugFile.flush();
    }

    public void writeDayToStatsOutputFile() {
        checkNotNull(debugFile, "Output file has not been created");
        var line = concat(
                concat(
                        Stream.of(Stage.values()).map(stage -> String.valueOf(statsDailyNew.get(stage))),
                        additionalStatistics.values().stream().map(String::valueOf)
                ),
                Stream.of(Stage.values()).map(stage -> String.valueOf(statsAll.get(stage)))
        ).collect(joining(",", "", ""));
        debugFile.println(line);
        debugFile.flush();
    }

    /**
     * Resets all agregates
     */
    public void resetStats() {
        for (AtomicInteger value : statsAll.values()) {
            value.set(0);
        }
        for (AtomicInteger value : statsDailyNew.values()) {
            value.set(0);
        }
        additionalStatistics.get("testedPositive").set(0);
        additionalStatistics.get("isolated").set(0);
        additionalStatistics.get("unquarantined").set(0);
        additionalStatistics.get("quarantined").set(0);
        additionalStatistics.get("changedImpact").set(0);
    }

    /**
     * Adds a single agent to the "all in stage" aggregate
     */
    public void tickStage(Stage currentStage) {
        statsAll.get(currentStage).getAndIncrement();
    }

    /**
     * Adds a single agent to the "switched to stage" aggregate
     */
    public void tickStageChange(Stage stage) {
        statsDailyNew.get(stage).getAndIncrement();
    }

    /**
     * Adds a single agent to the "tested positive" aggregate
     */
    public void tickTestedPositive() {
        additionalStatistics.get("testedPositive").incrementAndGet();
    }

    /**
     * Adds a single agent to the "quarantined" total count
     */
    public void tickQuarantined() {
        additionalStatistics.get("quarantined").incrementAndGet();
    }

    /**
     * Adds a single agent to the "unquarantined" total count
     */
    public void tickUnquarantined() {
        additionalStatistics.get("unquarantined").incrementAndGet();
    }

    /**
     * Adds a single agent to the "isolated" aggregate
     */
    public void tickIsolated() {
        additionalStatistics.get("isolated").incrementAndGet();
    }

    /**
     * Adds a single agent to the "changed impact" aggregate
     */
    public void tickChangedImpact() {
        additionalStatistics.get("changedImpact").incrementAndGet();
    }

}
