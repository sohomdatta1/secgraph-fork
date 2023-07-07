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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import common.Graph;

public class DeferredMatchingVariantPercolation {
    public static void percolationMethod(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2_mapping,int r) throws IOException {
        HashMap<Integer, HashMap<Integer, Integer>> g1NodeToG2NodeToCreditMap
            = new HashMap<Integer, HashMap<Integer,Integer>>();
        LinkedList<Integer> g1MappingCandidate = new LinkedList<Integer>();
        LinkedList<Integer> g2MappingCandidate = new LinkedList<Integer>();
        HashSet<Integer> usedG1Cand = new HashSet<Integer>();
        HashSet<Integer> usedG2Cand = new HashSet<Integer>();
        getMappingCandidates(g1ToG2_mapping,g1MappingCandidate,g2MappingCandidate);
        
        while(g1MappingCandidate.size()!=usedG1Cand.size()){
            for(int i = 0;i< g1MappingCandidate.size();i++){
                int cand1 = g1MappingCandidate.get(i);
                int cand2 = g2MappingCandidate.get(i);
                if(usedG1Cand.contains(cand1)||usedG2Cand.contains(cand2)){
                    continue;
                }
                usedG1Cand.add(cand1);
                usedG2Cand.add(cand2);
                for(int g1Nei : g1.get(cand1)){
                    if(!g1NodeToG2NodeToCreditMap.containsKey(g1Nei)){
                        g1NodeToG2NodeToCreditMap.put(g1Nei, new HashMap<Integer, Integer>());
                    }
                    if(g1ToG2_mapping.containsKey(g1Nei)){
                        continue;
                    }
                    for(int g2Nei : g2.get(cand2)){
                        if(g1ToG2_mapping.containsValue(g2Nei)){
                            continue;
                        }
                        int newScore = 1;
                        if(g1NodeToG2NodeToCreditMap.get(g1Nei).containsKey(g2Nei)){
                            newScore+=g1NodeToG2NodeToCreditMap.get(g1Nei).get(g2Nei);
                        }
                        g1NodeToG2NodeToCreditMap.get(g1Nei).put(g2Nei, newScore);
                    }
                }
                int maxI = -1, maxJ = -1, maxCredit = 0;
                for(int I : g1NodeToG2NodeToCreditMap.keySet()){
                    if(g1ToG2_mapping.containsKey(I)){
                        continue;
                    }
                    for(int J : g1NodeToG2NodeToCreditMap.get(I).keySet()){
                        if(g1ToG2_mapping.containsValue(J)){
                            continue;
                        }
                        if(g1NodeToG2NodeToCreditMap.get(I).get(J)>maxCredit){
                            maxI=I;
                            maxJ=J;
                            maxCredit=g1NodeToG2NodeToCreditMap.get(I).get(J);
                        }
                    }
                }
                if(maxCredit>r){
                    g1MappingCandidate.add(maxI);
                    g2MappingCandidate.add(maxJ);
                    g1NodeToG2NodeToCreditMap.remove(maxI);
                    g1ToG2_mapping.put(maxI, maxJ);
                    System.out.println("added "+maxI+" "+maxJ);
                }
            }
        }
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
    
    public static void main(String[] args) throws IOException {
        //double prob = .95;
        //HashMap<Integer, HashSet<Integer>> G = Graph.getLargestConnectedCompentent(Graph.readUndirectedGraph("Gowalla_edges.txt"));/*Graph.genRandomGraphSnap(n, n*edgePerNode);*/
        //HashMap<Integer, HashSet<Integer>> G1 = Graph.sampleGraph(G, prob);
        //HashMap<Integer, HashSet<Integer>> G2 = Graph.sampleGraph(G, prob);
        String g1 = "graphs/sample/Email-EnronLCCSampleB900";
        String g2 = "graphs/sample/Email-EnronLCCSampleA900";
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);//Graph.sampleGraph(G, prob);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> cTosMapping = getSeeds(G1,50);
        DeferredMatchingVariantPercolation.percolationMethod(G1, G2, cTosMapping, 0);
    }

    private static HashMap<Integer, Integer> getSeeds(
            HashMap<Integer, HashSet<Integer>> g,
            int n) {
        HashMap<Integer, Integer> seeds = new HashMap<Integer,Integer>();
        TreeMap<Integer, HashSet<Integer>> degreeToNodeMap = Graph.getDegreeToNodeMap(g);
        for(int degree : degreeToNodeMap.descendingKeySet()){
            for(int node : degreeToNodeMap.get(degree)){
                if(seeds.size()<n){
                    seeds.put(node, node);
                }else{
                    break;
                }
            }
            if(seeds.size()>=n){
                break;
            }
        }
        return seeds;
    }
}
