/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;

public class Covid19Loads {
    public final Load WILD;
    public final Load ALPHA;
    public final Load DELTA;
    public final Load OMICRON;
    public final Load BA2;
    public final Load BA45;
    public final Load PFIZER;
    public final Load BOOSTER;
    public final Load ASTRA;
    public final Load MODERNA;

    @WithFactory
    public Covid19Loads(Loads loads) {
        WILD = loads.getByName("WILD");
        ALPHA = loads.getByName("ALPHA");
        DELTA = loads.getByName("DELTA");
        BOOSTER = loads.getByName("BOOSTER");
        PFIZER = loads.getByName("PFIZER");
        OMICRON = loads.getByName("OMICRON");
        BA2 = loads.getByName("BA2");
        BA45 = loads.getByName("BA45");
        ASTRA = loads.getByName("ASTRA");
        MODERNA = loads.getByName("MODERNA");
    }
}
