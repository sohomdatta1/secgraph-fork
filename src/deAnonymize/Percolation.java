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
package deAnonymize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import common.Graph;

public class Percolation {
    public static void main(String[] args) throws IOException {
        if(args.length!=5){
            System.out.println("Percolation usage :  g1 g2 numCredToKeep seedFile outputFileName");
            System.exit(0);
        }
        runPercolation(args[0],args[1],args[2],args[3],args[4]);
    }
    public static void runPercolation(String g1, String g2, String NumCredToKeep, String seedFile, String out) throws IOException {
        int numCredToKeep = Integer.parseInt(NumCredToKeep);
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> seed_mapping = Graph.getSeeds(seedFile);
        HashMap<Integer, Integer> res = Percolation.percolationMethod(G1, G2, seed_mapping, numCredToKeep);
        BufferedWriter w = new BufferedWriter(new FileWriter(out));
        int r = 0;
        for(int i : res.keySet()){
            if(i==res.get(i)){
                r++;
            }
            w.write(i+" "+res.get(i)+"\n");
        }
        w.write(out+" "+r+"/"+res.size()+" "+G1.size()+" "+G2.size()+"\n");
        w.flush();w.close();
    }
    public static HashMap<Integer, Integer> percolationMethod(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2_mapping,int r) throws IOException {
        HashMap<Integer, HashMap<Integer, Integer>> g1NodeToG2NodeToCreditMap
            = new HashMap<Integer, HashMap<Integer,Integer>>();
        /*for(int i : g1.keySet()){
            g1NodeToG2NodeToCreditMap.put(i, new HashMap<Integer,Integer>());
            for(int j : g2.keySet()){
                g1NodeToG2NodeToCreditMap.get(i).put(j, 0);
            }
        }*/
        //System.out.println("done init");
        Random rand = new Random();
        LinkedList<Integer> g1MappingCandidate = new LinkedList<Integer>();
        LinkedList<Integer> g2MappingCandidate = new LinkedList<Integer>();
        /*HashSet<Integer> usedG1Cand = new HashSet<Integer>();
        HashSet<Integer> usedG2Cand = new HashSet<Integer>();*/
        getMappingCandidates(g1ToG2_mapping,g1MappingCandidate,g2MappingCandidate);
        while(g1MappingCandidate.size()>0){
            int pos = rand.nextInt(g1MappingCandidate.size());
            int cand1 = g1MappingCandidate.remove(pos);
            int cand2 = g2MappingCandidate.remove(pos);
            /*if (usedG1Cand.contains(cand1) || usedG2Cand.contains(cand2)) {
                continue;
            }
            usedG1Cand.add(cand1);
            usedG2Cand.add(cand2);*/
            for (int g1Nei : g1.get(cand1)) {
                if (!g1NodeToG2NodeToCreditMap.containsKey(g1Nei)) {
                    g1NodeToG2NodeToCreditMap.put(g1Nei,
                            new HashMap<Integer, Integer>());
                }
                if (g1ToG2_mapping.containsKey(g1Nei)) {
                    continue;
                }
                for (int g2Nei : g2.get(cand2)) {
                    if (g1ToG2_mapping.containsValue(g2Nei)) {
                        continue;
                    }
                    
                    int newScore = 1;
                    if (g1NodeToG2NodeToCreditMap.get(g1Nei).containsKey(g2Nei)) {
                        newScore += g1NodeToG2NodeToCreditMap.get(g1Nei).get(g2Nei);
                    }
                    g1NodeToG2NodeToCreditMap.get(g1Nei).put(g2Nei, newScore);
                    if (newScore > r) {
                        g1MappingCandidate.add(g1Nei);
                        g2MappingCandidate.add(g2Nei);
                        g1ToG2_mapping.put(g1Nei, g2Nei);
                        System.out.println("added mapping " + g1Nei + " " + g2Nei);
                        g1NodeToG2NodeToCreditMap.remove(g1Nei);
                        break;
                    } 
                }
            }

        }
        /*for(int i : g1.keySet()){
            if(!g1ToG2_mapping.containsKey(i)){
                System.out.println(i);
            }
        }*/
        /*int c = 0,t=0;
        for(int i : g1ToG2_mapping.keySet()){
            if(i==g1ToG2_mapping.get(i)){
                c++;
            }
            t++;
            //System.out.println(i+" "+g1ToG2_mapping.get(i));
        }
        //System.out.println(c+"/"+t+" right");
*/        return g1ToG2_mapping;
    }

    private static void getMappingCandidates(
            HashMap<Integer, Integer> seed_mapping,
            LinkedList<Integer> g1MappingCandidate,
            LinkedList<Integer> g2MappingCandidate) {
        for(int i : seed_mapping.keySet()){
            g1MappingCandidate.add(i);
            g2MappingCandidate.add(seed_mapping.get(i));
        }
    }
    

}
