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

package pl.edu.icm.pdyn2.importer;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class ImportedImmunizationEvent {
    private int id;
    private int dzien_zakazenia;
    private int historia_stanow;
    private String odmiana_wirusa;
    private String odmiana_szczepionki;

    public ImportedImmunizationEvent() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDzien_zakazenia() {
        return dzien_zakazenia;
    }

    public void setDzien_zakazenia(int dzien_zakazenia) {
        this.dzien_zakazenia = dzien_zakazenia;
    }

    public String getOdmiana_wirusa() {
        return odmiana_wirusa;
    }

    public void setOdmiana_wirusa(String odmiana_wirusa) {
        this.odmiana_wirusa = odmiana_wirusa;
    }

    public String getOdmiana_szczepionki() {
        return odmiana_szczepionki;
    }

    public void setOdmiana_szczepionki(String odmiana_szczepionki) {
        this.odmiana_szczepionki = odmiana_szczepionki;
    }

    public int getHistoria_stanow() {
        return historia_stanow;
    }

    public void setHistoria_stanow(int historia_stanow) {
        this.historia_stanow = historia_stanow;
    }
}
