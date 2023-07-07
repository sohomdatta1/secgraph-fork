package anonymize;

import java.io.IOException;

public class kIso {
    public static void runKISO(String k, String curNNum, String g,String result,String resultEdge) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./kISO/run "+k+" "+curNNum+" "+g+" "+result+" "+resultEdge;
        Process proc = rt.exec(commands);
        proc.waitFor();
    }
}
