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

package pl.edu.icm.pdyn2.model.context;

public enum ContextType {
    HOUSEHOLD(ContextInfectivityClass.HOUSEHOLD),
    WORKPLACE(ContextInfectivityClass.WORKPLACE),
    KINDERGARTEN(ContextInfectivityClass.KINDERGARTEN),
    SCHOOL(ContextInfectivityClass.SCHOOL),
    UNIVERSITY(ContextInfectivityClass.UNIVERSITY),
    BIG_UNIVERSITY(ContextInfectivityClass.BIG_UNIVERSITY),
    STREET_00(ContextInfectivityClass.STREET),
    STREET_10(ContextInfectivityClass.STREET),
    STREET_20(ContextInfectivityClass.STREET),
    STREET_30(ContextInfectivityClass.STREET),
    STREET_40(ContextInfectivityClass.STREET),
    STREET_50(ContextInfectivityClass.STREET),
    STREET_60(ContextInfectivityClass.STREET),
    STREET_70(ContextInfectivityClass.STREET),
    STREET_80(ContextInfectivityClass.STREET),
    STREET_90(ContextInfectivityClass.STREET);

    private final ContextInfectivityClass infectivityClass;

    ContextType(ContextInfectivityClass infectivityClass) {
        this.infectivityClass = infectivityClass;
    }

    public ContextInfectivityClass getInfectivityClass() {
        return infectivityClass;
    }

    public static ContextType[] streetContexts() {
        return new ContextType[]{
                ContextType.STREET_00, ContextType.STREET_10,
                ContextType.STREET_20, ContextType.STREET_30,
                ContextType.STREET_40, ContextType.STREET_50,
                ContextType.STREET_60, ContextType.STREET_70,
                ContextType.STREET_80, ContextType.STREET_90,
        };
    }
}
