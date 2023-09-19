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

package pl.edu.icm.pdyn2.model.context;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

import java.util.List;

public class ContextTypes extends SoftEnumManager<ContextType> {
    public final ContextType HOUSEHOLD;
    public final ContextType WORKPLACE;
    public final ContextType BIG_UNIVERSITY;
    public final ContextType UNIVERSITY;
    public final ContextType KINDERGARTEN;

    public final ContextType SCHOOL;
    final public ContextType STREET_00;
    final public ContextType STREET_10;
    final public ContextType STREET_20;
    final public ContextType STREET_30;
    final public ContextType STREET_40;
    final public ContextType STREET_50;
    final public ContextType STREET_60;
    final public ContextType STREET_70;
    final public ContextType STREET_80;
    final public ContextType STREET_90;
    @WithFactory
    public ContextTypes(Bento bento) {
        super(bento, "pdyn2.infectivity.transmission.contexts.context-types", ContextTypeFactory.IT);
        STREET_00 = getByName("STREET_00");
        STREET_10 = getByName("STREET_10");
        STREET_20 = getByName("STREET_20");
        STREET_30 = getByName("STREET_30");
        STREET_40 = getByName("STREET_40");
        STREET_50 = getByName("STREET_50");
        STREET_60 = getByName("STREET_60");
        STREET_70 = getByName("STREET_70");
        STREET_80 = getByName("STREET_80");
        STREET_90 = getByName("STREET_90");

        HOUSEHOLD = getByName("HOUSEHOLD");
        WORKPLACE = getByName("WORKPLACE");
        BIG_UNIVERSITY = getByName("BIG_UNIVERSITY");
        UNIVERSITY = getByName("UNIVERSITY");
        KINDERGARTEN = getByName("KINDERGARTEN");
        SCHOOL = getByName("SCHOOL");

    }

    @Override
    public ContextType[] emptyArray() {
        return new ContextType[0];
    }

    public List<ContextType> streetContexts() {
        return List.of(
                STREET_00, STREET_10,
                STREET_20, STREET_30,
                STREET_40, STREET_50,
                STREET_60, STREET_70,
                STREET_80, STREET_90);
    }
}
