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
package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RunSNAP {
    public static HashMap<Integer, Double> getSnap(String g1Name,String cmd) throws IOException {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        Runtime rt = Runtime.getRuntime();
        String commands = cmd+" "+g1Name;
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));

        String s = null;
        while ((s = stdInput.readLine()) != null) {
            String[] stringArray= s.split("\\s+");
            map.put(Integer.parseInt(stringArray[0]), Double.parseDouble(stringArray[1]));
        }
        return map;
    }
    
    public static double getDoubleSnap(String g1Name,String cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String commands = cmd+" "+g1Name;
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));

        String s = null;
        String lastStr = null;
        while ((s = stdInput.readLine()) != null) {
            lastStr=s;
        }
        return Double.parseDouble(lastStr);
    }
}
