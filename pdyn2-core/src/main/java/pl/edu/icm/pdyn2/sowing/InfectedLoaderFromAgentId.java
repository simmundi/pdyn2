package pl.edu.icm.pdyn2.sowing;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InfectedLoaderFromAgentId {
    private final String sowingFilename;
    private InputStream workDir;

    @WithFactory
    public InfectedLoaderFromAgentId(WorkDir workDir, String sowingFilename) {
        this.sowingFilename = sowingFilename;
        this.workDir = workDir.openForReading(new File(sowingFilename));
    }

    public String getSowingFilename() {
        return sowingFilename;
    }

    public List<InfectedAgentFromCsv> readInfected() throws FileNotFoundException {
        List<InfectedAgentFromCsv> infectedList = new ArrayList<>();
        Scanner myReader = new Scanner(this.workDir);
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
