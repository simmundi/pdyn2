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

package pl.edu.icm.pdyn2.model.immunization;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnumManager;

import java.util.stream.Collectors;

public class Loads extends SoftEnumManager<Load> {
    public final Load WILD;
    private final Load[] strains;


    @WithFactory
    public Loads(Bento bento) {
        super(bento, "loads", LoadFactory.IT);
        WILD = getByName("WILD");
        strains = values().stream().filter(load -> load.classification == LoadClassification.VIRUS).collect(Collectors.toList()).toArray(emptyArray());
    }

    @Override
    public Load[] emptyArray() {
        return new Load[0];
    }

    public Load[] viruses() {
        return strains;
    }
}
