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
import java.util.HashSet;
import java.util.TreeMap;

import common.Graph;

public class degreeDist {
    public static double compDegDist(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2){
        TreeMap<Integer, HashSet<Integer>> tm1 = Graph.getDegreeToNodeMap(g1);
        TreeMap<Integer, HashSet<Integer>> tm2 = Graph.getDegreeToNodeMap(g2);
        int largestDeg = tm1.lastEntry().getKey();
        if(largestDeg<tm2.lastEntry().getKey()){
            largestDeg=tm2.lastEntry().getKey();
        }
        
        double aa=0,ab=0,bb=0;
        for(int i = 1;i<=largestDeg;i++){
            if(tm1.containsKey(i)){
                aa+=tm1.get(i).size()*tm1.get(i).size();
            }
            if (tm2.containsKey(i)) {
                bb+=tm2.get(i).size()*tm2.get(i).size();
            }
            if (tm1.containsKey(i)&&tm2.containsKey(i)) {
                ab+=tm1.get(i).size()*tm2.get(i).size();
            }
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("usage g1 g2");
            System.exit(0);
        }
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph("graphs/n2/Email-EnronLCC.txt");
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph("graphs/n2/randAddAndDelete-10Email-Enron.txt");
        System.out.println(compDegDist(g1,g2));
        
    }
}
