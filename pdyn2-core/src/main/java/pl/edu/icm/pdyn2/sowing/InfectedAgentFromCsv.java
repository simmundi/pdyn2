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

package pl.edu.icm.pdyn2.sowing;

public class InfectedAgentFromCsv {
    private int agentId;
    private int elapsedDays;
    private boolean symptomatic;

    public InfectedAgentFromCsv(int agentId, int elapsedDays, int symptomatic) {
        this.agentId = agentId;
        this.elapsedDays = elapsedDays;
        if (symptomatic != 1 && symptomatic != 0) {
            throw new IllegalArgumentException("symptomatic != 1 || symptomatic != 0" +
                    ", symptomatic = " + String.valueOf(symptomatic));
        }
        if (symptomatic == 1) {
            this.symptomatic = true;
        } else {
            this.symptomatic = false;
        }
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public void setElapsedDays(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    public boolean isSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(boolean symptomatic) {
        this.symptomatic = symptomatic;
    }
}
