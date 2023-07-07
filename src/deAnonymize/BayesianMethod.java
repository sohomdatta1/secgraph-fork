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
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.commons.math3.util.CombinatoricsUtils;

import common.Graph;
import common.MinWeightHungarianAlgorithm;
import common.Pair;


public class BayesianMethod {
    //static BufferedWriter debug;
    public static void BayesianMatching(HashMap<Integer, HashSet<Integer>> G,
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, double samplingRate,
            String outFile) throws IOException, InterruptedException
    {
        BufferedWriter w = new BufferedWriter(new FileWriter(outFile));
        HashMap<Integer, Integer> g1NodeToDegreeMap = Graph.getGraphDegreeMap(g1);
        HashMap<Integer, Integer> g2NodeToDegreeMap = Graph.getGraphDegreeMap(g2);
         
        TreeMap<Integer, HashSet<Integer>> gDegreeToNodeMap = Graph.getDegreeToNodeMap(G);
        TreeMap<Integer, HashSet<Integer>> g1DegreeToNodeMap = Graph.getDegreeToNodeMap(g1);
        TreeMap<Integer, HashSet<Integer>> g2DegreeToNodeMap = Graph.getDegreeToNodeMap(g2);
         
        
        HashMap<Integer, HashMap<Integer, Integer>> gNodesNodesDistantMap = Graph.getNodeNodeDistanceMapSnap(G);
        HashMap<Integer, HashMap<Integer, Integer>> g1NodesNodesDistantMap = Graph.getNodeNodeDistanceMapSnap(g1);
        HashMap<Integer, HashMap<Integer, Integer>> g2NodesNodesDistantMap = Graph.getNodeNodeDistanceMapSnap(g2);
        
        HashMap<Integer,Integer> gDistanceFreqMap = getDistanceFreq(gNodesNodesDistantMap);
       
        
        HashMap<Integer, HashMap<Integer, Float>> qDegreeLookup = createQDegreeLookup(G.size(),samplingRate);
        HashMap<Integer, HashMap<Integer, Float>> qDistLookup = createQDistLookup(G.size(),samplingRate);
        
       /* LinkedList<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1);
        LinkedList<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2);*/
    
        ArrayList<Integer> g1Anchor = new ArrayList<Integer>();
        ArrayList<Integer> g2Anchor = new ArrayList<Integer>();
        
        int candidateSetSize = 2;
        while (candidateSetSize <= G.size()) {
            w.write("candidateSetSize"+candidateSetSize+"\n");
            ArrayList<Integer> g1Candidates = getCandidates(g1DegreeToNodeMap,candidateSetSize);
            ArrayList<Integer> g2Candidates = getCandidates(g2DegreeToNodeMap,candidateSetSize);
            HashMap<Integer, ArrayList<Integer>> g1CandidatesToFingerprint = getCandidatesToFingerprintMap(
                    g1Candidates, g1NodeToDegreeMap, g1NodesNodesDistantMap,g1Anchor);
            HashMap<Integer, ArrayList<Integer>> g2CandidatesToFingerprint = getCandidatesToFingerprintMap(
                    g2Candidates, g2NodeToDegreeMap, g2NodesNodesDistantMap,g2Anchor);
            
            float[][] matrix = new float[g1Candidates.size()][g2Candidates.size()];
            float[][] Posterior = new float[g1Candidates.size()][g2Candidates.size()];
             
            for (int i = 0; i < g1Candidates.size(); i++) {
                for (int j = 0; j < g2Candidates.size(); j++) {
                    float val;
                    if(g2Candidates.size()>=257){
                        val = calculatePosterior(qDegreeLookup,qDistLookup,g1CandidatesToFingerprint.get(g1Candidates.get(i)),g2CandidatesToFingerprint.get(g2Candidates.get(j)),gDistanceFreqMap,gDegreeToNodeMap,G.size(),false);
                    }else{
                        val = calculatePosterior(qDegreeLookup,qDistLookup,g1CandidatesToFingerprint.get(g1Candidates.get(i)),g2CandidatesToFingerprint.get(g2Candidates.get(j)),gDistanceFreqMap,gDegreeToNodeMap,G.size(),false);
                        
                    }
                    Posterior[i][j] = (float) val;
                    matrix[i][j]= (float) val;
                }
            }
            for (int i = 0; i < g1Candidates.size(); i++) {
                for (int j = 0; j < g2Candidates.size(); j++) {
                     float val = normalize(Posterior[i][j],matrix,i,j);
                     
                    Posterior[i][j]=val;
                }
            }
            for (int i = 0; i < g1Candidates.size(); i++) {
                for (int j = 0; j < g2Candidates.size(); j++) {
                    matrix[i][j]=-1*Posterior[i][j];//Negative because the Hungarian Algorithm is doing a min weight matching
                }
            }
            MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
            int[][] result = match.computeAssignments(matrix);
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
                    if (g1Anchor.size() < g1Candidates.size() / 2 || g1Anchor.size() < 300) {
                        g1Anchor.add(pairs.getKey());
                        g2Anchor.add(pairs.getValue());
                    } else {
                        break;
                    }
                }
                if (g1Anchor.size() >= g1Candidates.size() / 2 || g1Anchor.size() >= 300){
                    break;
                }
            }
            ArrayList<Integer> g1Nodes = new ArrayList<Integer>();
            ArrayList<Integer> g2Nodes = new ArrayList<Integer>();
            for (int i = 0; i < result.length; i++) {
                int gr1 = result[i][0];
                int gr2 = result[i][1];
                g1Nodes.add(g1Candidates.get(gr1));
                g2Nodes.add(g2Candidates.get(gr2));
                if (g1Candidates.get(gr1).equals(g2Candidates.get(gr2))) {
                }
            }
            
            if (candidateSetSize == G.size()) {
                break;
            }
            candidateSetSize *= 2;
            if(candidateSetSize>G.size()){
                candidateSetSize=G.size();
            }            
        }
        w.flush();
        
        w.close();
    }
    
    public static LinkedList<Integer> getNodesInDegreeSortedOrder(
            HashMap<Integer, HashSet<Integer>> s) {
        LinkedList<Integer> sortedNode = new LinkedList<Integer>();
        TreeMap<Integer, HashSet<Integer>> sortedSDegreeToNodes = Graph.getDegreeToNodeMap(s);
        for(int i : sortedSDegreeToNodes.descendingKeySet()){
            for(int j : sortedSDegreeToNodes.get(i)){
                sortedNode.add(j);
            }
        }
        return sortedNode;
    }
    
    
    private static float normalize(float f, float[][] matrix, int i, int j) throws IOException {
        if(f==0.0){
            return 0.0F;
        }
        float sumOne=0;
        for(int I = 0 ;I < matrix.length;I++){
            sumOne+=matrix[I][j];
            
        }
        float sumTwo=0;
        for (int J = 0; J < matrix[0].length; J++) {
            sumTwo+=matrix[i][J];
        }
        return (float) (f/Math.sqrt(sumOne*sumTwo));
        //return f;
    }

    private static float calculatePosterior(HashMap<Integer, HashMap<Integer, Float>> qDegreeLookup, HashMap<Integer, HashMap<Integer, Float>> qDistLookup, ArrayList<Integer> g1Fingerprint, ArrayList<Integer> g2Fingerprint, HashMap<Integer, Integer> gDistanceFreqMap, TreeMap<Integer, HashSet<Integer>> gDegreeToNodeMap, int totalNode,boolean detailed) throws IOException {
        float A ;
        if(detailed){
            A = calculateA(qDegreeLookup,qDistLookup,g1Fingerprint,g2Fingerprint,gDegreeToNodeMap,gDistanceFreqMap,totalNode,false);
        }else{
         A = calculateA(qDegreeLookup,qDistLookup,g1Fingerprint,g2Fingerprint,gDegreeToNodeMap,gDistanceFreqMap,totalNode);
        }
        if(A==0){return 0;}
        float B = calculateB(qDegreeLookup,qDistLookup,g1Fingerprint,g2Fingerprint,gDegreeToNodeMap,gDistanceFreqMap,totalNode);
        if(B==0){return 1;}
        return A/(A+B);
    }

    private static float calculateA(
            HashMap<Integer, HashMap<Integer, Float>> qDegreeLookup,
            HashMap<Integer, HashMap<Integer, Float>> qDistLookup,
            ArrayList<Integer> g1Fingerprint, ArrayList<Integer> g2Fingerprint,
            TreeMap<Integer, HashSet<Integer>> gDegreeToNodeMap,
            HashMap<Integer, Integer> gDistanceFreqMap, int totalNode) throws IOException {
        
        return calculateA(qDegreeLookup,qDistLookup,g1Fingerprint,g2Fingerprint,gDegreeToNodeMap,gDistanceFreqMap,totalNode,false);
    }

    private static float calculateA(HashMap<Integer, HashMap<Integer, Float>> qDegreeLookup, HashMap<Integer, HashMap<Integer, Float>> qDistLookup, ArrayList<Integer> g1Fingerprint, ArrayList<Integer> g2Fingerprint, TreeMap<Integer, HashSet<Integer>> gDegreeToNodeMap, HashMap<Integer, Integer> gDistanceFreqMap, int totalNodes,boolean detail) throws IOException {
        float degreeVal = 0;
        int g1Degree = g1Fingerprint.get(0);
        int g2Degree = g2Fingerprint.get(0);
        for(int degree : gDegreeToNodeMap.keySet()){
            float numNodeAtDegree = ((float)gDegreeToNodeMap.get(degree).size());
            degreeVal+=qDegreeLookup.get(g1Degree).get(degree)*qDegreeLookup.get(g2Degree).get(degree)*numNodeAtDegree/totalNodes;
        }
        float distVal = 1;
        float totalPath = totalNodes*(totalNodes-1);
        for(int i = 1; i<g1Fingerprint.size();i++){
            float distValAtI = 0;
            int g1Dist = g1Fingerprint.get(i);
            int g2Dist = g2Fingerprint.get(i);
            if(g1Dist==0||g2Dist==0){
                continue;
            }
            for(int dist : gDistanceFreqMap.keySet()){
                if(dist==0){continue;}
                float numAtDist = gDistanceFreqMap.get(dist);
                if(detail){
                 }
                distValAtI+=qDistLookup.get(g1Dist).get(dist)*qDistLookup.get(g2Dist).get(dist)*numAtDist/totalPath;
                if(detail){
            }
            distVal*=distValAtI;
        }
            }
        return degreeVal*distVal*1/totalNodes;
    }
    
    private static float calculateB(HashMap<Integer, HashMap<Integer, Float>> qDegreeLookup, HashMap<Integer, HashMap<Integer, Float>> qDistLookup, ArrayList<Integer> g1Fingerprint, ArrayList<Integer> g2Fingerprint, TreeMap<Integer, HashSet<Integer>> gDegreeToNodeMap, HashMap<Integer, Integer> gDistanceFreqMap, int totalNodes) {
        float g1degreeVal = 0;
        float g2degreeVal = 0;
        int g1Degree = g1Fingerprint.get(0);
        int g2Degree = g2Fingerprint.get(0);
        for(int degree : gDegreeToNodeMap.keySet()){
            float numNodeAtDegree = ((float)gDegreeToNodeMap.get(degree).size());
            g1degreeVal+=qDegreeLookup.get(g1Degree).get(degree)*numNodeAtDegree/totalNodes;
            g2degreeVal+=qDegreeLookup.get(g2Degree).get(degree)*numNodeAtDegree/totalNodes;
        }
        float degreeVal = g1degreeVal*g2degreeVal;
        
        float distVal = 1;
        float totalPath = (totalNodes*(totalNodes-1));
        for(int i = 1; i<g1Fingerprint.size();i++){
            float g1distValAtI = 0;
            float g2distValAtI = 0;
            int g1Dist = g1Fingerprint.get(i);
            int g2Dist = g2Fingerprint.get(i);
            if(g1Dist==0||g2Dist==0){
                continue;
            }
            for(int dist : gDistanceFreqMap.keySet()){
                if(dist==0){continue;}
                float numNodeAtDistance = ((float)gDistanceFreqMap.get(dist));
                g1distValAtI+=qDistLookup.get(g1Dist).get(dist)*numNodeAtDistance/totalPath;
                g2distValAtI+=qDistLookup.get(g2Dist).get(dist)*numNodeAtDistance/totalPath;
            }
            distVal*=g1distValAtI*g2distValAtI;
        }
        return degreeVal*distVal*(1-1/totalNodes);
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
    private static HashMap<Integer, HashMap<Integer, Float>> createQDistLookup(
            int size, double s) {
        HashMap<Integer, HashMap<Integer, Float>> qDist = new HashMap<Integer, HashMap<Integer, Float>>();
        for (int x = 1; x < size; x++) {
            qDist.put(x, new HashMap<Integer, Float>());
            for (int y = 1; y < size; y++) {
                if(y<=x && x<=2*y){
                    float val = (float) (CombinatoricsUtils
                            .binomialCoefficientDouble(y, x - y)
                            * Math.pow(1 - s, x - y) * Math.pow(s, x));
                    qDist.get(x).put(y, val);
                }else{
                    qDist.get(x).put(y, (float) 0);
                }
            }
        }
        return qDist;
    }

    private static HashMap<Integer, HashMap<Integer, Float>> createQDegreeLookup(
            int size, double s) {
        HashMap<Integer, HashMap<Integer, Float>> qDegree = new HashMap<Integer, HashMap<Integer, Float>>();
        for (int x = 1; x < size; x++) {
            qDegree.put(x, new HashMap<Integer, Float>());
            for (int y = 1; y < size; y++) {
                if (y < x ) {
                    qDegree.get(x).put(y, (float) 0);
                } else {
                    float val =  (float) (CombinatoricsUtils.binomialCoefficientDouble(y, x)
                            * Math.pow(s, x)
                            * (Math.pow(1.0 - s, y - x)));
                    qDegree.get(x).put(y, val);
                }
            }
        }
        return qDegree;
    }

    private static HashMap<Integer, Integer> getDistanceFreq(
            HashMap<Integer, HashMap<Integer, Integer>> gNodesNodesDistantMap) {
        HashMap<Integer, Integer> distanceToFreqMap = new HashMap<Integer, Integer>();
        for(int i : gNodesNodesDistantMap.keySet()){
            for(int j : gNodesNodesDistantMap.get(i).keySet()){
                int distance = gNodesNodesDistantMap.get(i).get(j);
                if(!distanceToFreqMap.containsKey(distance)){
                    distanceToFreqMap.put(distance, 1);
                }else{
                    int newDist =distanceToFreqMap.get(distance)+1;
                    distanceToFreqMap.put(distance,newDist);
                }
            }
        }
        return distanceToFreqMap;
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        HashMap<Integer, HashSet<Integer>> G =Graph.readUndirectedGraph(args[0]);
        HashMap<Integer, HashSet<Integer>> G1 =Graph.readUndirectedGraph(args[1]);
        HashMap<Integer, HashSet<Integer>> G2 =Graph.readUndirectedGraph(args[2]);
        BayesianMethod.BayesianMatching(G, G1, G2, Double.parseDouble(args[3]),args[4]);
    }
}
