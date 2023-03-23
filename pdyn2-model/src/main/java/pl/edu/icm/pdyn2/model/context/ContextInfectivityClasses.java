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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

public class ContextInfectivityClasses extends SoftEnumManager<ContextInfectivityClass> {
    public final ContextInfectivityClass HOUSEHOLD;
    public final ContextInfectivityClass WORKPLACE;
    public final ContextInfectivityClass KINDERGARTEN;
    public final ContextInfectivityClass SCHOOL;
    public final ContextInfectivityClass UNIVERSITY;
    public final ContextInfectivityClass BIG_UNIVERSITY;
    public final ContextInfectivityClass STREET;

    @WithFactory
    public ContextInfectivityClasses(Bento bento) {
        super(bento, "contextInfectivityClasses", ContextInfectivityClassFactory.IT);

        HOUSEHOLD = getByName("HOUSEHOLD");
        WORKPLACE = getByName("WORKPLACE");
        KINDERGARTEN = getByName("KINDERGARTEN");
        SCHOOL = getByName("SCHOOL");
        UNIVERSITY = getByName("UNIVERSITY");
        BIG_UNIVERSITY = getByName("BIG_UNIVERSITY");
        STREET = getByName("STREET");
    }




    @Override
    public ContextInfectivityClass[] emptyArray() {
        return new ContextInfectivityClass[0];
    }
}
