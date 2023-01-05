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

package pl.edu.icm.pdyn2.model.immunization;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class Load {

    private String loadName;
    private LoadClassification classification;
    private int vaccineEncoding;
    private int virusEncoding;
    private String stageTransitionsFilename;

    public Load() {

    }

    public Load(String loadName,
                LoadClassification classification,
                int vaccineEncoding,
                int virusEncoding,
                String stageTransitionsFilename) {
        this.loadName = loadName;
        this.classification = classification;
        this.vaccineEncoding = vaccineEncoding;
        this.virusEncoding = virusEncoding;
        this.stageTransitionsFilename = stageTransitionsFilename;
    }

    public String getLoadName() {
        return loadName;
    }

    public void setLoadName(String loadName) {
        this.loadName = loadName;
    }

    public LoadClassification getClassification() {
        return classification;
    }

    public void setClassification(LoadClassification classification) {
        this.classification = classification;
    }

    public int getVaccineEncoding() {
        return vaccineEncoding;
    }

    public void setVaccineEncoding(int vaccineEncoding) {
        this.vaccineEncoding = vaccineEncoding;
    }

    public int getVirusEncoding() {
        return virusEncoding;
    }

    public void setVirusEncoding(int virusEncoding) {
        this.virusEncoding = virusEncoding;
    }

    public String getStageTransitionsFilename() {
        return stageTransitionsFilename;
    }

    public void setStageTransitionsFilename(String stageTransitionsFilename) {
        this.stageTransitionsFilename = stageTransitionsFilename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Load that = (Load) o;
        return Objects.equals(loadName, that.loadName);
    }
}
