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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import common.Graph;
import common.MinWeightHungarianAlgorithm;
import common.Pair;


public class ModifiedFromBayesianMethod {
    static BufferedWriter debug;
    public static void modifiedBayesianMatching(HashMap<Integer, HashSet<Integer>> G,
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, double samplingRate) throws IOException, InterruptedException
    {
        debug = new BufferedWriter(new FileWriter("debug/modifiedBayesian.txt"));
        HashMap<Integer, Integer> g1NodeToDegreeMap = Graph.getGraphDegreeMap(g1);
        HashMap<Integer, Integer> g2NodeToDegreeMap = Graph.getGraphDegreeMap(g2);
        
        TreeMap<Integer, HashSet<Integer>> g1DegreeToNodeMap = Graph.getDegreeToNodeMap(g1);
        TreeMap<Integer, HashSet<Integer>> g2DegreeToNodeMap = Graph.getDegreeToNodeMap(g2);
        
        HashSet<Integer> g1anchorCandidates = new HashSet<Integer>();
        HashSet<Integer> g2anchorCandidates = new HashSet<Integer>();
        HashMap<Integer, HashMap<Integer, Integer>> g1TopNodesToAllNodesDistantMap = Graph.getTopNodeNodeDistanceMapSnap(g1,g1DegreeToNodeMap,100,g1anchorCandidates);
        HashMap<Integer, HashMap<Integer, Integer>> g2TopNodesToAllNodesDistantMap = Graph.getTopNodeNodeDistanceMapSnap(g2,g2DegreeToNodeMap,100,g2anchorCandidates);
        
        ArrayList<Integer> g1Anchor = new ArrayList<Integer>();
        ArrayList<Integer> g2Anchor = new ArrayList<Integer>();
        
        int candidateSetSize = 2;
        while (candidateSetSize <= G.size()) {
            System.out.println("candidateSetSize"+candidateSetSize);
            ArrayList<Integer> g1Candidates = getCandidates(g1DegreeToNodeMap,candidateSetSize);
            ArrayList<Integer> g2Candidates = getCandidates(g2DegreeToNodeMap,candidateSetSize);
            HashMap<Integer, ArrayList<Integer>> g1CandidatesToFingerprint = getCandidatesToFingerprintMap(g1Candidates, g1NodeToDegreeMap, g1TopNodesToAllNodesDistantMap,g1Anchor);
            HashMap<Integer, ArrayList<Integer>> g2CandidatesToFingerprint = getCandidatesToFingerprintMap(g2Candidates, g2NodeToDegreeMap, g2TopNodesToAllNodesDistantMap,g2Anchor);
            debug.write("g1Candidates"+g1Candidates+"\n");
            debug.write("g2Candidates"+g2Candidates+"\n");
            debug.write("g1Anchor"+g1Anchor+"\n");
            debug.write("g2Anchor"+g2Anchor+"\n");
            float[][] matrix = new float[g1Candidates.size()][g2Candidates.size()];
            float[][] Posterior = new float[g1Candidates.size()][g2Candidates.size()];
            System.out.println("start compute matrix");
            for (int i = 0; i < g1Candidates.size(); i++) {
                for (int j = 0; j < g2Candidates.size(); j++) {
                    float val = calculateCosSim(g1CandidatesToFingerprint.get(g1Candidates.get(i)),g2CandidatesToFingerprint.get(g2Candidates.get(j)));
                    if(Float.isNaN(val)||Float.isInfinite(val)){
                        System.out.println("here");
                    }
                    debug.write("c1:c2 "+ g1Candidates.get(i)+":"+ g2Candidates.get(j) +" F1:F2 "+g1CandidatesToFingerprint.get(g1Candidates.get(i))+":"+g2CandidatesToFingerprint.get(g2Candidates.get(j))+" val:"+val+"\n\n");
                    Posterior[i][j] = (float) val;
                    matrix[i][j]= (float) val*-1;
                }
            }
            System.out.println("start MinWeightHungarianAlgorithm");
            MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
            int[][] result = match.computeAssignments(matrix);
            System.out.println("Start add anchor");
            TreeMap<Float, HashSet<Pair<Integer, Integer>>> scoreToNodeMap = new TreeMap<Float, HashSet<Pair<Integer,Integer>>>();
            for(int i = 0;i < result.length;i++){
                int gr1 = result[i][0];
                int gr2 = result[i][1];
                if(!scoreToNodeMap.containsKey(Posterior[gr1][gr2])){
                    scoreToNodeMap.put(Posterior[gr1][gr2], new HashSet<Pair<Integer, Integer>>());
                }
                scoreToNodeMap.get(Posterior[gr1][gr2]).add(new Pair<Integer,Integer>(g1Candidates.get(gr1),g2Candidates.get(gr2)));
            }
            g1Anchor.clear();
            g2Anchor.clear();
            for (float score : scoreToNodeMap.descendingKeySet()) {
                for (Pair<Integer, Integer> pairs : scoreToNodeMap.get(score)) {
                    if (g1Anchor.size() < g1Candidates.size() / 2) {
                        if (g1anchorCandidates.contains(pairs.getKey())
                                && g2anchorCandidates
                                        .contains(pairs.getValue())) {
                            g1Anchor.add(pairs.getKey());
                            g2Anchor.add(pairs.getValue());
                        }

                    } else {
                        break;
                    }
                }
                if (g1Anchor.size() >= g1Candidates.size() / 2){
                    break;
                }
            }
            
            int count = 0;
            ArrayList<Integer> g1Nodes = new ArrayList<Integer>();
            ArrayList<Integer> g2Nodes = new ArrayList<Integer>();
            for (int i = 0; i < result.length; i++) {
                int gr1 = result[i][0];
                int gr2 = result[i][1];
               g1Nodes.add(g1Candidates.get(gr1));
               g2Nodes.add(g2Candidates.get(gr2));
               
                if (g1Candidates.get(gr1).equals(g2Candidates.get(gr2))) {
                    count++;
                }
            }
            System.out.println();
            System.out.println("matched g1 : "+g1Nodes);
            System.out.println("matched g2 : "+g2Nodes);
            System.out.println("matched correct "+count);
            
            System.out.println();System.out.println();
            if (candidateSetSize == G.size()) {
                break;
            }
            candidateSetSize *= 2;
            if(candidateSetSize>G.size()){
                candidateSetSize=G.size();
            }            
        }
    }
    
    private static float calculateCosSim(ArrayList<Integer> arrayList,
            ArrayList<Integer> arrayList2) {
        float magA=0,magB=0,ATimesB=0;
        for(int i = 0;i<arrayList.size();i++){
            magA+=arrayList.get(i)*arrayList.get(i);
            magB+=arrayList2.get(i)*arrayList2.get(i);
            ATimesB+=arrayList.get(i)*arrayList2.get(i);
        }
        return (float) (ATimesB/(Math.sqrt(magA)*Math.sqrt(magB)));
    }

    private static HashMap<Integer, ArrayList<Integer>> getCandidatesToFingerprintMap(
            ArrayList<Integer> candidates,
            HashMap<Integer, Integer> nodeToDegreeMap,
            HashMap<Integer, HashMap<Integer, Integer>> nodesNodesDistantMap,
            ArrayList<Integer> anchor) {
        HashMap<Integer, ArrayList<Integer>> candidateToFingerprintMap = new HashMap<Integer, ArrayList<Integer>>();
        for (int i : candidates) {
            candidateToFingerprintMap.put(i, new ArrayList<Integer>());
            candidateToFingerprintMap.get(i).add(nodeToDegreeMap.get(i));
            for (int j : anchor) {
                if(nodesNodesDistantMap.get(i).get(j)==null){
                    System.out.println("i = "+i+" j = "+j);
                    System.out.println("nodesNodesDistantMap.get(i).get(j) = "+nodesNodesDistantMap.get(i).get(j));
                    System.out.println("nodesNodesDistantMap.get(j).get(i) = "+nodesNodesDistantMap.get(j).get(i));
                    System.out.println("stop");
                    }
                candidateToFingerprintMap.get(i).add(nodesNodesDistantMap.get(i).get(j));
            }
        }
        return candidateToFingerprintMap;
    }
    
    private static ArrayList<Integer> getCandidates(
            TreeMap<Integer, HashSet<Integer>> degreeToNodeMap,
            int candidateSetSize) {
        ArrayList<Integer> candidateSet = new ArrayList<Integer>();
        for (int i : degreeToNodeMap.descendingKeySet()) {
            for(int j : degreeToNodeMap.get(i)){
                if (candidateSet.size() < candidateSetSize) {
                    candidateSet.add(j);
                } else {
                    break;
                }
            }
            if (candidateSet.size() >= candidateSetSize) {
                break;
            }
        }
        return candidateSet;
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        double prob = .999;
        HashMap<Integer, HashSet<Integer>> G = Graph.getLargestConnectedCompentent(Graph.readUndirectedGraph("Gowalla_edges.txt"));/*Graph.genRandomGraphSnap(n, n*edgePerNode);*/
        System.out.println("done gen graph");
        HashMap<Integer, HashSet<Integer>> G1 = Graph.sampleGraph(G, prob);
        System.out.println("done gen graph1");
        HashMap<Integer, HashSet<Integer>> G2 = Graph.sampleGraph(G, prob);
        System.out.println("done gen graph2");
        ModifiedFromBayesianMethod.modifiedBayesianMatching(G, G1, G2, prob);
    }
}
