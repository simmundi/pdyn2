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

package pl.edu.icm.pdyn2.model.progression;

import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.trurl.ecs.annotation.EnumManagedBy;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper(namespace = "health_status")
public class HealthStatus {
    @EnumManagedBy(Stages.class)
    private Stage stage;
    @EnumManagedBy(Loads.class)
    private Load diseaseLoad;
    private int dayOfLastChange;
    private int diseaseHistory;

    public HealthStatus() {
    }

    public HealthStatus(Load diseaseLoad) {
        this.diseaseLoad = diseaseLoad;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        addStageToHistory(stage);
    }

    public int getElapsedDays(int currentDay) {
        return currentDay - dayOfLastChange;
    }

    void setDayOfLastChange(int dayOfLastChange) {
        this.dayOfLastChange = dayOfLastChange;
    }

    public int getDayOfLastChange() {
        return dayOfLastChange;
    }

    public void transitionTo(Stage stage, int dayOfLastChange) {
        this.stage = stage;
        this.dayOfLastChange = dayOfLastChange;
        addStageToHistory(stage);
    }

    public Load getDiseaseLoad() {
        return diseaseLoad;
    }

    public void setDiseaseLoad(Load diseaseLoad) {
        this.diseaseLoad = diseaseLoad;
    }

    public int getDiseaseHistory() {
        return diseaseHistory;
    }

    void setDiseaseHistory(int diseaseHistory) {
        this.diseaseHistory = diseaseHistory;
    }

    public void resetDiseaseHistory() {
        this.diseaseHistory = 0;
    }

    public static HealthStatus of(Load load) {
        return new HealthStatus(load);
    }

    private void addStageToHistory(Stage stage) {
        diseaseHistory += stage.encoding;
    }

    @Override
    public String toString() {
        return "HealthStatus{" +
                "stage=" + stage +
                ", dayOfLastChange=" + dayOfLastChange +
                ", diseaseHistory=" + diseaseHistory +
                '}';
    }
}
