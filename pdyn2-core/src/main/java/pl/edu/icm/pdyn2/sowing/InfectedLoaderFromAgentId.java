package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.board.util.FileToStreamService;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InfectedLoaderFromAgentId {
    private final String sowingFilename;
    private InputStream inputStream;

    @WithFactory
    public InfectedLoaderFromAgentId(FileToStreamService fileToStreamService, String sowingFilename) {
        this.sowingFilename = sowingFilename;
        try {
            this.inputStream = fileToStreamService.filename(sowingFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getSowingFilename() {
        return sowingFilename;
    }

    public List<InfectedAgentFromCsv> readInfected() throws FileNotFoundException {
        List<InfectedAgentFromCsv> infectedList = new ArrayList<>();
        Scanner myReader = new Scanner(this.inputStream);
        int expectedInfectedCount = 0;
        int realInfectedCount = 0;
        if (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            expectedInfectedCount = Integer.parseInt(data);
        }
        while (true) {
            if (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] elements = data.split(" ");
                int agentId = Integer.parseInt(elements[0]);
                int elapsedDays = Integer.parseInt(elements[2]);
                int symptomatic = Integer.parseInt(elements[3]);
                InfectedAgentFromCsv ia = new InfectedAgentFromCsv(agentId, elapsedDays, symptomatic);
                infectedList.add(ia);
                ++realInfectedCount;
            } else {
                break;
            }
        }

        if (expectedInfectedCount != realInfectedCount) {
            throw new IllegalStateException("infectedCount != infectedCountInFile" +
                    " ,infectedCount = " + expectedInfectedCount +
                    " ,infectedCountInFile = " + realInfectedCount);
        }
        return infectedList;
    }
}
