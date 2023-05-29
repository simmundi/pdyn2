/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.pdyn2.covid19.immunization;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.pdyn2.immunization.ImmunizationStage;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.pdyn2.model.immunization.Loads;
import pl.edu.icm.trurl.util.Status;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ImmunizationFromCsvProvider {
    private final Loads loads;

    private final List<List<double[]>> sFunction = new ArrayList<>(4);
    private final List<List<double[]>> crossImmunity = new ArrayList<>(4);
    private final String sFunctionInfectionFilename;
    private final String sFunctionSymptomsFilename;
    private final String sFunctionHospitalizationFilename;
    private final String sFunctionIcuFilename;
    private final String crossImmunityInfectionFilename;
    private final String crossImmunitySymptomsFilename;
    private final String crossImmunityHospitalizationFilename;
    private final String crossImmunityIcuFilename;
    private final List<BufferedInputStream> sFunctionStream = new ArrayList<>(4);
    private final List<BufferedInputStream> crossImmunityStream = new ArrayList<>(4);
    private final List<Load> immunityLabelsList = new ArrayList<>();
    private final List<Load> diseaseLabelsList = new ArrayList<>();

    @WithFactory
    public ImmunizationFromCsvProvider(WorkDir fileToStreamService,
                                       Loads loads,
                                       String sFunctionInfectionFilename,
                                       String sFunctionSymptomsFilename,
                                       String sFunctionHospitalizationFilename,
                                       String sFunctionIcuFilename,
                                       String crossImmunityInfectionFilename,
                                       String crossImmunitySymptomsFilename,
                                       String crossImmunityHospitalizationFilename,
                                       String crossImmunityIcuFilename) {
        this.loads = loads;
        this.sFunctionInfectionFilename = sFunctionInfectionFilename;
        this.sFunctionSymptomsFilename = sFunctionSymptomsFilename;
        this.sFunctionHospitalizationFilename = sFunctionHospitalizationFilename;
        this.sFunctionIcuFilename = sFunctionIcuFilename;
        this.crossImmunityInfectionFilename = crossImmunityInfectionFilename;
        this.crossImmunitySymptomsFilename = crossImmunitySymptomsFilename;
        this.crossImmunityHospitalizationFilename = crossImmunityHospitalizationFilename;
        this.crossImmunityIcuFilename = crossImmunityIcuFilename;

        sFunctionStream.add(0, new BufferedInputStream(fileToStreamService.openForReading(new File(sFunctionInfectionFilename))));
        sFunctionStream.add(1, new BufferedInputStream(fileToStreamService.openForReading(new File(sFunctionSymptomsFilename))));
        sFunctionStream.add(2, new BufferedInputStream(fileToStreamService.openForReading(new File(sFunctionHospitalizationFilename))));
        sFunctionStream.add(3, new BufferedInputStream(fileToStreamService.openForReading(new File(sFunctionIcuFilename))));
        crossImmunityStream.add(0, new BufferedInputStream(fileToStreamService.openForReading(new File(crossImmunityInfectionFilename))));
        crossImmunityStream.add(1, new BufferedInputStream(fileToStreamService.openForReading(new File(crossImmunitySymptomsFilename))));
        crossImmunityStream.add(2, new BufferedInputStream(fileToStreamService.openForReading(new File(crossImmunityHospitalizationFilename))));
        crossImmunityStream.add(3, new BufferedInputStream(fileToStreamService.openForReading(new File(crossImmunityIcuFilename))));

        for (int i = 0; i < 4; i++) {
            sFunction.add(new ArrayList<>());
        }

        for (int i = 0; i < 4; i++) {
            crossImmunity.add(new ArrayList<>());
        }
    }

    public void load() throws IOException {
        var status = Status.of("Loading s and cross immunity functions");
        loadFunction(crossImmunityStream.get(0), crossImmunity.get(0), true, true);
        loadFunction(crossImmunityStream.get(1), crossImmunity.get(1), false, true);
        loadFunction(crossImmunityStream.get(2), crossImmunity.get(2), false, true);
        loadFunction(crossImmunityStream.get(3), crossImmunity.get(3), false, true);
        loadFunction(sFunctionStream.get(0), sFunction.get(0), false, false);
        loadFunction(sFunctionStream.get(1), sFunction.get(1), false, false);
        loadFunction(sFunctionStream.get(2), sFunction.get(2), false, false);
        loadFunction(sFunctionStream.get(3), sFunction.get(3), false, false);
        status.done("Functions loaded from files: \n" +
                sFunctionInfectionFilename + " (S INFECTION) \n" +
                sFunctionSymptomsFilename + " (S SYMPTOMS) \n" +
                sFunctionHospitalizationFilename + " (S HOSPITALIZATION) \n" +
                sFunctionIcuFilename + " (S ICU) \n" +
                crossImmunityInfectionFilename + " (CI INFECTION) \n" +
                crossImmunitySymptomsFilename + " (CI SYMPTOMS) \n" +
                crossImmunityHospitalizationFilename + " (CI HOSPITALIZATION) \n" +
                crossImmunityIcuFilename + " (CI ICU) \n");
    }

    private void loadFunction(InputStream stream, List<double[]> list, boolean getLabels, boolean checkDiseaseLabels) throws IOException {
        stream.mark(Integer.MAX_VALUE);
        Scanner cFunctionScanner = new Scanner(stream);
        int cFunctionCols = 0;
        int cFunctionRow = 0;
        if (cFunctionScanner.hasNextLine()) {
            cFunctionRow++;
            String data = cFunctionScanner.nextLine();
            List<String> elements = new ArrayList<>(Arrays.asList(data.split(",")));
            cFunctionCols = elements.size();
            if (getLabels) {
                for (int i = 1; i < cFunctionCols; i++) {
                    diseaseLabelsList.add(loads.getByName(elements.get(i)));
                }
            }
            if (checkDiseaseLabels) {
                if (diseaseLabelsList.size() != elements.size() - 1) {
                    elements.remove(0);
                    throw new IllegalArgumentException("Column labels: " + elements + " differ in length from previously loaded: " + diseaseLabelsList);
                }
                for (int i = 1; i < cFunctionCols; i++) {
                    if (!diseaseLabelsList.get(i - 1).equals(loads.getByName(elements.get(i)))) {
                        throw new IllegalArgumentException("Column label: " + loads.getByName(elements.get(i)) + " is different from previously loaded: " + diseaseLabelsList.get(cFunctionRow - 1));
                    }
                }
            }
        }
        while (cFunctionScanner.hasNextLine()) {
            list.add(new double[cFunctionCols - 1]);
            String data = cFunctionScanner.nextLine();
            String[] elements = data.split(",");

            if (getLabels) {
                immunityLabelsList.add(cFunctionRow - 1, loads.getByName(elements[0]));
            } else if (!immunityLabelsList.get(cFunctionRow - 1).equals(loads.getByName(elements[0]))) {
                throw new IllegalArgumentException("Row label: " + loads.getByName(elements[0]) + " is different from previously loaded: " + immunityLabelsList.get(cFunctionRow - 1));
            }

            for (int i = 1; i < cFunctionCols; i++) {
                list.get(cFunctionRow - 1)[i - 1] = Double.parseDouble(elements[i]);
            }
            cFunctionRow++;
        }
        stream.reset();
    }

    public double getCrossImmunity(Load immunizationType,
                                   Load diseaseType,
                                   ImmunizationStage immunizationStage) {
        return crossImmunity.get(immunizationStage.id)
                .get(immunityLabelsList.indexOf(immunizationType))[diseaseLabelsList.indexOf(diseaseType)];
    }

    public double getSFunction(Load immunizationType,
                               ImmunizationStage immunizationStage,
                               int day) {
        return sFunction.get(immunizationStage.id)
                .get(immunityLabelsList.indexOf(immunizationType))[day];
    }

}
