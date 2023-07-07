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

import java.io.IOException;
import java.util.HashMap;

import common.RunSNAP;
import common.Utilities;

public class Betweenness {
    public static void main(String[] args) throws IOException {
        String g1Name = args[0];
        String g2Name = args[1];
        System.out.println(runBC(g1Name,g2Name)+"");;  
    }
    public static double runBC(String g1Name, String g2Name) throws IOException {
        String cmd = "Snap-2.4/examples/secGraphTools/BC";
        HashMap<Integer, Double> g1BC = RunSNAP.getSnap(g1Name, cmd);
        HashMap<Integer, Double> g2BC =  RunSNAP.getSnap(g2Name, cmd);
        return Utilities.mapCosSim(g1BC, g2BC);
    }  
}
