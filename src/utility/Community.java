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
import java.util.HashMap;
import java.util.HashSet;
import common.MinWeightHungarianAlgorithm;

public class Community {
    public static HashMap<Integer, HashSet<Integer>> getCommunity(String g) throws IOException{
        HashMap<Integer, HashSet<Integer>> com = new HashMap<Integer, HashSet<Integer>>();
        
        Runtime rt = Runtime.getRuntime();
        String commands = "Snap-2.4/examples/secGraphTools/Community "+g;
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(proc.getInputStream()));
        String s = null;

        while ((s = stdInput.readLine()) != null) {
            String[] stringArray= s.split("\\s+");
            int nodeID = Integer.parseInt(stringArray[0]);
            int commID =Integer.parseInt(stringArray[1]);
            if(!com.containsKey(commID)){
                com.put(commID, new HashSet<Integer>());
            }
            com.get(commID).add(nodeID);
        }
        return com;
    }
    
    public static double compareComCos(HashMap<Integer, HashSet<Integer>> c1,
            HashMap<Integer, HashSet<Integer>> c2) {
        HashMap<Integer, HashMap<Integer, Integer>> nodeToNodeToGroupC1 = new HashMap<Integer, HashMap<Integer,Integer>>();
        HashMap<Integer, HashMap<Integer, Integer>> nodeToNodeToGroupC2 = new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int i : c1.keySet()){
            nodeToNodeToGroupC1.put(i, new HashMap<Integer,Integer>());
            for(int j : c1.keySet()){
                if(i>j){
                    continue;
                }
                int count = 0;
                for(int k : c1.get(i)){
                    for(int l : c1.get(j)){
                        if(k==l){
                            count++;
                        }
                    }
                }
                nodeToNodeToGroupC1.get(i).put(j, count);
                int count2 = 0;
                for(int k : c2.get(i)){
                    for(int l : c2.get(j)){
                        if(k==l){
                            count2++;
                        }
                    }
                }
                nodeToNodeToGroupC2.get(i).put(j, count2);
            }
        }
        
       
        double ab=0,aa=0,bb=0;
        for(int i :c1.keySet()){
            for(int j : c1.get(i)){
                int a = nodeToNodeToGroupC1.get(i).get(j);
                int b = nodeToNodeToGroupC2.get(i).get(j);
                aa+=a*a;
                bb+=a*b;
                ab+=a*b;                
            }
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }

    public static double compareComJar(HashMap<Integer, HashSet<Integer>> c1,
            HashMap<Integer, HashSet<Integer>> c2) {
        HashMap<Integer, HashMap<Integer, Integer>> nodeToNodeToGroupC1 = new HashMap<Integer, HashMap<Integer,Integer>>();
        HashMap<Integer, HashMap<Integer, Integer>> nodeToNodeToGroupC2 = new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int i : c1.keySet()){
            nodeToNodeToGroupC1.put(i, new HashMap<Integer,Integer>());
            nodeToNodeToGroupC2.put(i, new HashMap<Integer,Integer>());
            for(int j : c1.keySet()){
                if(i>=j){
                    continue;
                }
                int count = 0;
                for(int k : c1.get(i)){
                    for(int l : c1.get(j)){
                        if(k==l){
                            count++;
                        }
                    }
                }
                int count2 = 0;
                for(int k : c2.get(i)){
                    for(int l : c2.get(j)){
                        if(k==l){
                            count2++;
                        }
                    }
                }
                nodeToNodeToGroupC2.get(i).put(j, count2);
                nodeToNodeToGroupC1.get(i).put(j, count);
            }
        }
        
        int numInEither = 0;
        int numInBoth = 0;
        for(int i : nodeToNodeToGroupC2.keySet()){
            for(int j : nodeToNodeToGroupC2.get(i).keySet()){
                int a = nodeToNodeToGroupC1.get(i).get(j);
                int b = nodeToNodeToGroupC2.get(i).get(j);
                if(a>0||b>0){
                    numInEither++;   
                }
                if(a>0&&b>0){
                    numInBoth++;
                }
            }
        }
        return numInBoth/numInEither;
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("usage commFile1 commFile2");
            System.exit(0);
        }
        System.out.println(calcCommunitySim(args[0],args[1]));
    }

    public static double calcCommunitySim(String g1, String g2) throws IOException {
        HashMap<Integer, HashSet<Integer>>  comm1=getCommunity(g1);
        HashMap<Integer, HashSet<Integer>> comm2=getCommunity(g2);
        equalizeComm(comm1,comm2);
        return calcBipartSim(comm1,comm2);
    }

    private static double calcBipartSim(HashMap<Integer, HashSet<Integer>> a,
            HashMap<Integer, HashSet<Integer>> b) {
        float matrix[][] = new float[a.keySet().size()][b.keySet().size()];
        HashMap<Integer, Integer> commToIdMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> idToCommMap = new HashMap<Integer, Integer>();
        int ind = 0;
        for(int i : a.keySet()){
            commToIdMap.put(i, ind);
            idToCommMap.put(ind, i);
            ind++;
        }
        for(int i : a.keySet()){
            for(int j : b.keySet()){
                HashSet<Integer> setInA = a.get(i);
                HashSet<Integer> setInB = b.get(j);
                float jar = calcJaccard(setInA, setInB);
                matrix[commToIdMap.get(i)][commToIdMap.get(j)]=-1*jar;
            }
        }
        MinWeightHungarianAlgorithm hung = new MinWeightHungarianAlgorithm();
        int[][] res = hung.computeAssignments(matrix);
        float sum = 0;
        for (int i = 0; i < res.length; i++) {
            int setACommId = idToCommMap.get(res[i][0]);
            int setBCommId = idToCommMap.get(res[i][1]);
            HashSet<Integer> setInA = a.get(setACommId);
            HashSet<Integer> setInB = b.get(setBCommId);
            sum += calcJaccard(setInA, setInB);
        }
        return sum/res.length;
    }

    private static float calcJaccard(HashSet<Integer> setInA,
            HashSet<Integer> setInB) {
        HashSet<Integer> union = new HashSet<Integer>(setInA);
        union.addAll(setInB);
        HashSet<Integer> intersect = new HashSet<Integer>(setInA);
        intersect.retainAll(setInB);
        return ((float)intersect.size())/union.size();
    }

    private static void equalizeComm(HashMap<Integer, HashSet<Integer>> a,
            HashMap<Integer, HashSet<Integer>> b) {
        for(int i : a.keySet()){
            if(!b.containsKey(i)){
                b.put(i, new HashSet<Integer>());
            }
        }
        
        for(int i : b.keySet()){
            if(!a.containsKey(i)){
                a.put(i, new HashSet<Integer>());
            }
        }
        
    }
}
