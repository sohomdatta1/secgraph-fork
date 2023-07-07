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

public class Sprout {
    public static void main(String[] args) throws IOException {
        String g1Name = args[0];
        String g2Name = args[1];
        
        System.out.println(runSprout(g1Name,g2Name)+"");;  
    }
    public static double runSprout(String g1Name, String g2Name) throws IOException {
        double g1ED = sprout(g1Name);
        double g2ED =  sprout(g2Name);
        return g2ED/g1ED;
    }
    private static double sprout(String g1Name) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./pmittal/sprout -g "+g1Name;
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getErrorStream()));

        String s = null;
        String lastStr = null;
        while ((s = stdInput.readLine()) != null) {
            lastStr=s;
        }
        System.out.println(lastStr);
        return Double.parseDouble(lastStr);
    }  
}
