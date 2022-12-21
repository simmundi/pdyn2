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

import org.apache.commons.math3.random.RandomGenerator;
import pl.edu.icm.board.geography.commune.AdministrationAreaType;
import pl.edu.icm.board.model.Person;
import pl.edu.icm.board.urizen.household.model.AgeRange;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class SowingDistributionRecordFromCsv {
    private String teryt;
    private AdministrationAreaType administrationAreaType;
    private short state;
    private Person.Sex sex;
    private AgeRange ageRange;
    boolean symptomatic;

    public String getTeryt() {
        return teryt;
    }

    public void setTeryt(String teryt) {
        if (teryt.length() == 3) {
            teryt = "0" + teryt;
        }
        this.teryt = teryt;
    }

    public AdministrationAreaType getAdministrationAreaType() {
        return administrationAreaType;
    }

    public void setAdministrationAreaType(AdministrationAreaType type) {
        this.administrationAreaType = type;
    }

    public void setAdministrationAreaType(String administrationUnitType) {
        if (administrationUnitType == null) {
            setAdministrationAreaType(AdministrationAreaType.VILLAGE);
            return;
        }
        switch (administrationUnitType) {
            case "Wieś":
                setAdministrationAreaType(AdministrationAreaType.VILLAGE);
                break;
            case "Miasto poniżej 20 tys. mieszkańców":
                setAdministrationAreaType(AdministrationAreaType.CITY_S);
                break;
            case "Miasto 20-49 tys.":
                setAdministrationAreaType(AdministrationAreaType.CITY_M);
                break;
            case "Miasto 50-99 tys.":
                setAdministrationAreaType(AdministrationAreaType.CITY_L);
                break;
            case "Miasto ≥ 100 tys.":
                setAdministrationAreaType(AdministrationAreaType.CITY_XL);
                break;
            default:
                throw new IllegalArgumentException("Could not find AdministrationAreaType for: "
                        + administrationUnitType);
        }
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Person.Sex getSex() {
        return sex;
    }

    public void setSex(Person.Sex sex) {
        this.sex = sex;
    }

    public void setSex(String sex, RandomGenerator randomGenerator) {
        if (sex == null) {
            setSex(Person.Sex.values()[randomGenerator.nextInt(Person.Sex.values().length)]);
            return;
        }
        switch (sex) {
            case "K":
            case "Kobieta":
                setSex(Person.Sex.K);
                break;
            case "M":
            case "Mężczyzna":
                setSex(Person.Sex.M);
                break;
            default:
                throw new IllegalArgumentException("Could not determine sex for: " + sex);
        }
    }

    public AgeRange getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(AgeRange ageRange) {
        this.ageRange = ageRange;
    }

    public void setAgeRange(int age) {
        setAgeRange(AgeRange.fromAge(age));
    }

    public boolean isSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(boolean symptomatic) {
        this.symptomatic = symptomatic;
    }

    public void setSymptomatic(int symptomatic) {
        if (symptomatic == 0) {
            setSymptomatic(Boolean.FALSE);
        } else if (symptomatic == 1) {
            setSymptomatic(Boolean.TRUE);
        } else {
            throw new IllegalArgumentException("Symptomatic should be 0 or 1");
        }
    }
}
