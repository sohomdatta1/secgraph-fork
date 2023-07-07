package anonymize;

import java.io.IOException;

public class xiaoDP {
    public static void runXiao(String gName, String epsilonHRG, String epsilonE, String eq, String stop) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./privHRG-master/privHRG -f "+gName+" -epsilonHRG "+epsilonHRG+" -epsilonE "+epsilonE+
                "-eq "+eq+" -stop "+stop;
        Process proc = rt.exec(commands);
        proc.waitFor();
    }
    public static void runXiao(String gName, String epsilonHRG, String epsilonE) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./privHRG-master/privHRG -f "+gName+" -epsilonHRG "+epsilonHRG+" -epsilonE "+epsilonE;
        Process proc = rt.exec(commands);
        proc.waitFor();
    }  
    public static void runXiao(String gName) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./privHRG-master/privHRG -f "+gName;
        Process proc = rt.exec(commands);
        proc.waitFor();
    }
}
