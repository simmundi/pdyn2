/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.pdyn2;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.em.common.DebugTextFile;
import pl.edu.icm.em.common.DebugTextFileService;
import pl.edu.icm.pdyn2.model.progression.Stage;

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
    private final DebugTextFileService debugFileService;
    private DebugTextFile debugTextFile;


    @WithFactory
    public StatsService(DebugTextFileService debugFileService) {
        this.debugFileService = debugFileService;
        for (Stage stage : Stage.values()) {
            statsAll.put(stage, new AtomicInteger(0));
            statsDailyNew.put(stage, new AtomicInteger(0));
        }
        additionalStatistics.put("testedPositive", new AtomicInteger());
        additionalStatistics.put("isolated", new AtomicInteger());
        additionalStatistics.put("quarantined", new AtomicInteger());
        additionalStatistics.put("unquarantined", new AtomicInteger());
        additionalStatistics.put("changedImpact", new AtomicInteger());
        additionalStatistics.put("changedVariant", new AtomicInteger());
        additionalStatistics.put("vaccinated", new AtomicInteger());
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
        out.println(" - changed variant: " + additionalStatistics.get("changedVariant").get());
        out.println(" - vaccinated: " + additionalStatistics.get("vaccinated").get());
    }

    public void createStatsOutputFile(String simulationStatsOutputFilename) {
        checkState(debugTextFile == null, "Output file has already been created");
            debugTextFile = debugFileService.createTextFile(simulationStatsOutputFilename);
        var d1_header = concat(
                Stream.of(Stage.values()).map(Enum::name),
                additionalStatistics.keySet().stream().map(String::toUpperCase)
        ).collect(joining(",d1_", "d1_", ","));
        var headerContinued = Stream.of(Stage.values()).map(Enum::name)
                .collect(joining(",", "", ""));
        debugTextFile.println(d1_header + headerContinued);
        debugTextFile.flush();
    }

    public void writeDayToStatsOutputFile() {
        checkNotNull(debugFileService, "Output file has not been created");
        var line = concat(
                concat(
                        Stream.of(Stage.values()).map(stage -> String.valueOf(statsDailyNew.get(stage))),
                        additionalStatistics.values().stream().map(String::valueOf)
                ),
                Stream.of(Stage.values()).map(stage -> String.valueOf(statsAll.get(stage)))
        ).collect(joining(",", "", ""));
        debugTextFile.println(line);
        debugTextFile.flush();
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
        additionalStatistics.get("changedVariant").set(0);
        additionalStatistics.get("vaccinated").set(0);
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
    public void tickChangedVariant() {
        additionalStatistics.get("changedVariant").incrementAndGet();
    }

    public void tickVaccinated() {
        additionalStatistics.get("vaccinated").incrementAndGet();
    }
}
