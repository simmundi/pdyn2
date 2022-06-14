package pl.edu.icm.pdyn2.immunization;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.FileToStreamService;
import pl.edu.icm.pdyn2.model.immunization.Load;
import pl.edu.icm.trurl.util.Status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ImmunizationFromCsvProvider {

    private final List<List<double[]>> sFunction = new ArrayList<>(3);
    private final List<List<double[]>> crossImmunity = new ArrayList<>(4);
    private final String sFunctionLatentnyObjawowyFilename;
    private final String sFunctionHospitalizowanyBezOiomFilename;
    private final String sFunctionHospitalizowanyPrzedOiomFilename;
    private final String crossImmunityLatentnyFilename;
    private final String crossImmunityObjawowyFilename;
    private final String crossImmunityHospitalizowanyBezOiomFilename;
    private final String crossImmunityHospitalizowanyPrzedOiomFilename;
    private final List<InputStream> sFunctionStream = new ArrayList<>(3);
    private final List<InputStream> crossImmunityStream = new ArrayList<>(4);
    private final List<Load> immunityLabelsList = new ArrayList<>();
    private final List<Load> diseaseLabelsList = new ArrayList<>();

    @WithFactory
    public ImmunizationFromCsvProvider(FileToStreamService fileToStreamService,
                                       String sFunctionLatentnyObjawowyFilename,
                                       String sFunctionHospitalizowanyBezOiomFilename,
                                       String sFunctionHospitalizowanyPrzedOiomFilename,
                                       String crossImmunityLatentnyFilename,
                                       String crossImmunityObjawowyFilename,
                                       String crossImmunityHospitalizowanyBezOiomFilename,
                                       String crossImmunityHospitalizowanyPrzedOiomFilename) {
        this.sFunctionLatentnyObjawowyFilename = sFunctionLatentnyObjawowyFilename;
        this.sFunctionHospitalizowanyBezOiomFilename = sFunctionHospitalizowanyBezOiomFilename;
        this.sFunctionHospitalizowanyPrzedOiomFilename = sFunctionHospitalizowanyPrzedOiomFilename;
        this.crossImmunityLatentnyFilename = crossImmunityLatentnyFilename;
        this.crossImmunityObjawowyFilename = crossImmunityObjawowyFilename;
        this.crossImmunityHospitalizowanyBezOiomFilename = crossImmunityHospitalizowanyBezOiomFilename;
        this.crossImmunityHospitalizowanyPrzedOiomFilename = crossImmunityHospitalizowanyPrzedOiomFilename;
        try {
            sFunctionStream.add(0, fileToStreamService.filename(sFunctionLatentnyObjawowyFilename));
            sFunctionStream.add(1, fileToStreamService.filename(sFunctionHospitalizowanyBezOiomFilename));
            sFunctionStream.add(2, fileToStreamService.filename(sFunctionHospitalizowanyPrzedOiomFilename));
            crossImmunityStream.add(0, fileToStreamService.filename(crossImmunityLatentnyFilename));
            crossImmunityStream.add(1, fileToStreamService.filename(crossImmunityObjawowyFilename));
            crossImmunityStream.add(2, fileToStreamService.filename(crossImmunityHospitalizowanyBezOiomFilename));
            crossImmunityStream.add(3, fileToStreamService.filename(crossImmunityHospitalizowanyPrzedOiomFilename));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {
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
        status.done("Functions loaded from files: \n" +
                sFunctionLatentnyObjawowyFilename + " (S LATENTNY I OBJAWOWY) \n" +
                sFunctionHospitalizowanyBezOiomFilename + " (S HOSPITALIZOWANY_BEZ_OIOM) \n" +
                sFunctionHospitalizowanyPrzedOiomFilename + " (S HOSPITALIZOWANY_PRZED_OIOM) \n" +
                crossImmunityLatentnyFilename + " (CI LATENTNY) \n" +
                crossImmunityObjawowyFilename + " (CI OBJAWOWY) \n" +
                crossImmunityHospitalizowanyBezOiomFilename + " (CI HOSPITALIZOWANY_BEZ_OIOM) \n" +
                crossImmunityHospitalizowanyPrzedOiomFilename + " (CI HOSPITALIZOWANY_PRZED_OIOM) \n");
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
                    diseaseLabelsList.add(Load.valueOf(elements.get(i)));
                }
            }
            if (checkDiseaseLabels) {
                if (diseaseLabelsList.size() != elements.size() - 1) {
                    elements.remove(0);
                    throw new IllegalArgumentException("Column labels: " + elements + " differ in length from previously loaded: " + diseaseLabelsList);
                }
                for (int i = 1; i < cFunctionCols; i++) {
                    if (!diseaseLabelsList.get(i - 1).equals(Load.valueOf(elements.get(i)))) {
                        throw new IllegalArgumentException("Column label: " + Load.valueOf(elements.get(i)) + " is different from previously loaded: " + diseaseLabelsList.get(cFunctionRow - 1));
                    }
                }
            }
        }
        while (cFunctionScanner.hasNextLine()) {
            list.add(new double[cFunctionCols - 1]);
            String data = cFunctionScanner.nextLine();
            String[] elements = data.split(",");

            if (getLabels) {
                immunityLabelsList.add(cFunctionRow - 1, Load.valueOf(elements[0]));
            } else if (!immunityLabelsList.get(cFunctionRow - 1).equals(Load.valueOf(elements[0]))) {
                throw new IllegalArgumentException("Row label: " + Load.valueOf(elements[0]) + " is different from previously loaded: " + immunityLabelsList.get(cFunctionRow - 1));
            }

            {
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
        if (immunizationStage == ImmunizationStage.LATENTNY) {
            return sFunction.get(immunizationStage.id)
                    .get(immunityLabelsList.indexOf(immunizationType))[day];
        }
        return sFunction.get(immunizationStage.id - 1)
                .get(immunityLabelsList.indexOf(immunizationType))[day];
    }

}
