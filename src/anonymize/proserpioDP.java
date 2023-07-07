package anonymize;

import java.io.IOException;

public class proserpioDP {

    public static void runproserpioDP(String gName, String numberOfThreads,String outputFile, String  mcmcIterations, String epsilon) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"./Proserpio/VLDB2014 Weighted PINQ/VLDB2014 Experiments/bin/Debug/VLDB2014 Weighted PINQ.exe",gName,numberOfThreads,outputFile,mcmcIterations,epsilon};
        Process proc = rt.exec(commands);
        proc.waitFor();
    }
}
