/*
 * 
 * The MIT License (MIT)
 * Copyright (c) <year> <copyright holders>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Sybil {
    public static void main(String[] args) throws IOException {
        String g1Name = args[0];
        String g2Name = args[1];
        String numSInstances = args[2];
        String rseed= args[3];
        String walkLengh= args[4];
        String balanceCondHva= args[5];
        System.out.println(runSybil(g1Name, g2Name, numSInstances, rseed, walkLengh, balanceCondHva)+"");;  
    }
    public static double runSybil(String g1Name, String g2Name, String numSInstances, String rseed, String walkLengh, String balanceCondHva) throws IOException {
        double g1ED = sybil(g1Name, numSInstances, rseed, walkLengh, balanceCondHva);
        double g2ED =  sybil(g2Name, numSInstances, rseed, walkLengh, balanceCondHva);
        System.out.println(g1ED);
        System.out.println(g2ED);
        return g2ED/g1ED;
    }
    private static double sybil(String gName, String numSInstances, String rseed, String walkLengh, String balanceCondHva) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./pmittal/sybil  -g "+gName+" -s "+numSInstances+" -h "+
        balanceCondHva+" -l "+walkLengh+" -r "+rseed;
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));

        String s = null;
        String lastStr = null;
        while ((s = stdInput.readLine()) != null) {
            lastStr=s;
        }
        String[] splitStr = lastStr.split(" ");
        double res = Double.parseDouble(splitStr[splitStr.length-1]);
        return res;
    } 
}
