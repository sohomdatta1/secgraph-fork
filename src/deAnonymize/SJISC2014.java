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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.TreeMap;

import common.Graph;
import common.MinWeightHungarianAlgorithm;
import common.Pair;
import common.Utilities;

public class SJISC2014 {
    public static void main(String[] args) throws IOException {
        if(args.length!=12){
            System.out.println("ISC usage : g1 g2 g1CenFile g2CenFile mode SeedFile BipartSize NumToKeep DisSimW StrSimW InhSimW out");
            System.exit(0);
        }
        runISC(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
    }

    public static void runISC(String g1,String g2, String g1CenFile, String g2CenFile,String mode,String SeedFile,
            String BipartSize, String NumToKeep,
            String DisSimW,String StrSimW,String InhSimW,String out) throws IOException {
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);//Graph.sampleGraph(G, prob);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> cTosMapping = Graph.getSeeds(SeedFile);//Graph.getSeeds(G1,Integer.parseInt(args[4]),Integer.parseInt(args[11]));
        int bipartSize = Integer.parseInt(BipartSize);
        int numToKeep = Integer.parseInt(NumToKeep);
        double disSimW=Double.parseDouble(DisSimW), 
                strSimW=Double.parseDouble(StrSimW), 
                inhSimW=Double.parseDouble(InhSimW);
        HashMap<Integer, Integer>  res = runISC(G1, G2, cTosMapping, bipartSize, numToKeep, g1CenFile, g2CenFile,disSimW,strSimW,inhSimW,mode);
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

    private static HashMap<Integer, Integer> runISC(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Seeds, int bipartSize,
            int numToKeep, String g1CenFile, String g2CenFile, double disSimW,
            double strSimW, double inhSimW, String mode) throws IOException {
        LinkedList<Integer> matchedG1Nodes = new LinkedList<Integer>();
        LinkedList<Integer> matchedG2Nodes = new LinkedList<Integer>();
        HashSet<Integer> g1Seeds = new HashSet<Integer>();
        HashSet<Integer> g2Seeds = new HashSet<Integer>();
        HashMap<Integer, Integer> g1ToG2Nodes = new HashMap<Integer, Integer>();
        HashMap<Integer, Double> g1NodeToNormalizedClosness = readCloseness(g1CenFile);
        HashMap<Integer, Double> g2NodeToNormalizedClosness = readCloseness(g2CenFile);
        HashMap<Integer, Double> g1nodeToNormalizedBetweeness = readBetweeness(g1CenFile);
        HashMap<Integer, Double> g2nodeToNormalizedBetweeness = readBetweeness(g2CenFile);
        HashMap<Integer, Double> g1nodeToNormalizedDegree = calcDegree(g1);
        HashMap<Integer, Double> g2nodeToNormalizedDegree = calcDegree(g2);
        HashMap<Integer, HashMap<Integer, Double>> g1ToG2InhScore = new HashMap<Integer, HashMap<Integer,Double>>();
        for(int i : g1.keySet()){
            g1ToG2InhScore.put(i, new HashMap<Integer, Double>());
        }
        for (int g1Node : g1ToG2Seeds.keySet()) {
            int g2Node = g1ToG2Seeds.get(g1Node);
            g1ToG2InhScore.get(g1Node).put(g2Node, 1.0);
            
            matchedG1Nodes.add(g1Node);
            matchedG2Nodes.add(g2Node);
            g1Seeds.add(g1Node);
            g2Seeds.add(g2Node);
            g1ToG2Nodes.put(g1Node, g2Node);
        }
        HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance = calcDistanceToSeeds(g1, g1Seeds);
        HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance = calcDistanceToSeeds(g2, g2Seeds);
        LinkedList<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1,g1Seeds);
        LinkedList<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2,g2Seeds);
        if(sortedG1Nodes.size()!=sortedG2Nodes.size()){
            throw new RuntimeException("total unmatched size are not the same");
        }
        while(sortedG2Nodes.size()>0 && sortedG1Nodes.size()>0){
            ArrayList<Integer> candidateG2 = fillCandidate(sortedG2Nodes,bipartSize);
            ArrayList<Integer> candidateG1 = fillCandidate(sortedG1Nodes,bipartSize);
            
            float[][] score = new float[candidateG2.size()][candidateG2.size()];
            float[][] matrix = new float[candidateG2.size()][candidateG2.size()];
            for(int g2Cand = 0;g2Cand<candidateG2.size();g2Cand++){
                for(int g1Cand = 0;g1Cand<candidateG1.size();g1Cand++){
                    score[g2Cand][g1Cand]=calcSim(g1, g2, 
                            g1ToG2InhScore, g1ToG2Nodes, 
                            g1ToG2Seeds, 
                            g1NodeToNormalizedClosness, g2NodeToNormalizedClosness, 
                            g1nodeToNormalizedBetweeness, g2nodeToNormalizedBetweeness, 
                            g1nodeToNormalizedDegree, g2nodeToNormalizedDegree, 
                            g1seedsToAllNodeToDistance, g2seedsToAllNodeToDistance, 
                            candidateG1.get(g1Cand), candidateG2.get(g2Cand),
                            disSimW, strSimW, inhSimW);
                    matrix[g2Cand][g1Cand]=score[g2Cand][g1Cand];
                }
            }
           
            MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
            int[][] result = match.computeAssignments(matrix);
            PriorityQueue<Pair<Pair<Integer,Integer>,Float>> queue = createNodePairToScoreQueue();
            for(int i = 0;i<result.length;i++){
                int nodeG1 = candidateG1.get(result[i][1]);
                int nodeG2 = candidateG2.get(result[i][0]);
                queue.add(new Pair<Pair<Integer,Integer>, Float>(new Pair<Integer,Integer>(nodeG1, nodeG2),score[result[i][0]][result[i][1]]));
            }
            int count = 0;
            while(!queue.isEmpty()){
                Pair<Pair<Integer, Integer>, Float> here = queue.poll();
                if(count<numToKeep){
                    
                    matchedG1Nodes.add(here.getKey().getKey());
                    matchedG2Nodes.add(here.getKey().getValue());
                    
                }else{
                    returnUnmatchedNode(sortedG1Nodes,here.getKey().getKey(),mode);
                    returnUnmatchedNode(sortedG2Nodes,here.getKey().getValue(),mode);
                }
                count++;
            }
        }
        HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
        for(int i = 0;i<matchedG1Nodes.size();i++){
            res.put(matchedG2Nodes.get(i),   matchedG1Nodes.get(i));
        }
        int right = 0;
        for(int i = 0;i<matchedG1Nodes.size();i++){
            if(matchedG1Nodes.get(i).equals(matchedG2Nodes.get(i))){
                right++;
            }
        }
        System.out.println("numRight = "+right+"/"+matchedG1Nodes.size()+" "+matchedG2Nodes.size()+" "+g1.size()+" "+g2.size()+"\n");
        return res;
    }

    private static void returnUnmatchedNode(LinkedList<Integer> sortedG1Nodes,
            Integer node, String mode) {
       if(mode.equals("queue")){
           sortedG1Nodes.addLast(node);
       }else{
           sortedG1Nodes.addFirst(node);
       }
    }

    private static ArrayList<Integer> fillCandidate(
            LinkedList<Integer> nodes, int bipartSize) {
        ArrayList<Integer> candidates = new ArrayList<>();
        for(int i = 0;i<bipartSize;i++){
            candidates.add(nodes.poll());
            if(nodes.isEmpty()){
                break;
            }
        }
        return candidates;
    }
    
    private static PriorityQueue<Pair<Pair<Integer, Integer>, Float>> createNodePairToScoreQueue() {
        return new PriorityQueue<Pair<Pair<Integer,Integer>,Float>>(100*100, new Comparator<Pair<Pair<Integer,Integer>,Float>>(){
            @Override
            public int compare(Pair<Pair<Integer,Integer>, Float> o1,
                    Pair<Pair<Integer,Integer>, Float> o2) {
                return Float.compare(o1.getValue(), o2.getValue());
            }
        });
    }   
    private static HashMap<Integer, Double> readCloseness(String g) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(g));
        String line = br.readLine();
        HashMap<Integer, Double> closenessFile = new HashMap<Integer, Double>();
        double maxBet = -1;
        while(line!=null){
            if(line.contains("#")||line.equals("")){
                line=br.readLine();
                continue;
            }
            String[] segments = line.split("\t");
            int node = Integer.parseInt(segments[0]);
            double closeness = Double.parseDouble(segments[2]);
            closenessFile.put(node, closeness);
            if(maxBet<closeness){
                maxBet=closeness;
            }
            line = br.readLine();
        }
        br.close();
        HashSet<Integer> keyset = new HashSet<Integer>(closenessFile.keySet());
        for(int i : keyset){
            double normedBetweeness = closenessFile.get(i)/maxBet;
            closenessFile.put(i, normedBetweeness);
        }
        return closenessFile;
    }
    private static HashMap<Integer, Double> readBetweeness(String g) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(g));
        String line = br.readLine();
        HashMap<Integer, Double> betweenessFile = new HashMap<Integer, Double>();
        double maxBet = -1;
        while(line!=null){
            if(line.contains("#")||line.equals("")){
                line=br.readLine();
                continue;
            }
            String[] segments = line.split("\t");
            int node = Integer.parseInt(segments[0]);
            double betweenness = Double.parseDouble(segments[3]);
            betweenessFile.put(node, betweenness);
            if(maxBet<betweenness){
                maxBet=betweenness;
            }
            line = br.readLine();
        }
        br.close();
        HashSet<Integer> keyset = new HashSet<Integer>(betweenessFile.keySet());
        for(int i : keyset){
            double normedBetweeness = betweenessFile.get(i)/maxBet;
            betweenessFile.put(i, normedBetweeness);
        }
        return betweenessFile;
    }
    
    private static HashMap<Integer, Double> calcDegree(HashMap<Integer, HashSet<Integer>> g){
        HashMap<Integer, Double> nodeToDegree = new HashMap<Integer, Double>();
        int maxDegree = 0;
        for(int i : g.keySet()){
            int degreeHere = g.get(i).size();
            nodeToDegree.put(i, (double) degreeHere );
            if(degreeHere >maxDegree){
                maxDegree=degreeHere;
            }
        }
        HashSet<Integer> keyset = new HashSet<Integer>(nodeToDegree.keySet());
        for(int i : keyset){
            double normaledDegree =  nodeToDegree.get(i)/maxDegree;
            nodeToDegree.put(i, normaledDegree);
        }
        return nodeToDegree;
    }
    private static HashMap<Integer, HashMap<Integer, Integer>> calcDistanceToSeeds(HashMap<Integer, HashSet<Integer>> g, HashSet<Integer> seeds){
        HashMap<Integer, HashMap<Integer, Integer>> seedToAllNodeToDistance = new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int i : seeds){
            seedToAllNodeToDistance.put(i, new HashMap<Integer, Integer>());
            span(g, i, seedToAllNodeToDistance.get(i));
        }
        return seedToAllNodeToDistance;
    }
    public static float calcSim(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, HashMap<Integer, Double>>g1ToG2InhScore,
            HashMap<Integer, Integer> g1ToG2Nodes,
            HashMap<Integer, Integer> g1ToG2Seeds,
            HashMap<Integer, Double> g1NodeToNormalizedClosness,     HashMap<Integer, Double> g2NodeToNormalizedClosness,
            HashMap<Integer, Double> g1nodeToNormalizedBetweeness,   HashMap<Integer, Double> g2nodeToNormalizedBetweeness,
            HashMap<Integer, Double> g1nodeToNormalizedDegree,       HashMap<Integer, Double> g2nodeToNormalizedDegree,
            HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance,
            HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance,
            int g1Cand, int g2Cand,double disSimW,double strSimW,double inhSimW) 
    {
        double strSim  = calcStructualSim(g1NodeToNormalizedClosness,   g2NodeToNormalizedClosness, 
                                          g1nodeToNormalizedBetweeness, g2nodeToNormalizedBetweeness, 
                                          g1nodeToNormalizedDegree,     g2nodeToNormalizedDegree, 
                                          g1Cand, g2Cand);
        double distSim = calcRelativeDistanceSim(g1seedsToAllNodeToDistance, g2seedsToAllNodeToDistance, 
                                                 g1Cand, g2Cand, g1ToG2Seeds);
        double inhSim  = calcInheritedSim(g1,g2,g1ToG2InhScore,g1ToG2Nodes,g1Cand,g2Cand);
        //System.out.println(g1Cand+" "+g2Cand+" "+strSim+" "+distSim+" "+inhSim+" "+(0.8*distSim+0.1*strSim+0.1*inhSim));
        return (float) (-1*(disSimW*distSim+strSimW*strSim+inhSimW*inhSim));
    }
    private static LinkedList<Integer> getNodesInDegreeSortedOrder(
            HashMap<Integer, HashSet<Integer>> s, HashSet<Integer> Landmark) {
        LinkedList<Integer> sortedNode = new LinkedList<Integer>();
        TreeMap<Integer, HashSet<Integer>> sortedSDegreeToNodes = Graph.getDegreeToNodeMap(s);
        for(int i : sortedSDegreeToNodes.descendingKeySet()){
            for(int j : sortedSDegreeToNodes.get(i)){
                if(Landmark.contains(j)){
                    continue;
                }
                sortedNode.add(j);
            }
        }
        return sortedNode;
    }
    
    private static void span(HashMap<Integer, HashSet<Integer>> graph, int id,
            HashMap<Integer, Integer> distance) {
        LinkedList<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>>();
        HashSet<Integer> visited = new HashSet<Integer>();
        Pair<Integer, Integer> start = new Pair<Integer, Integer>(id, 0);
        distance.put(id, 0);
        visited.add(id);
        queue.addLast(start);
        while (!queue.isEmpty()) {
            Pair<Integer, Integer> here = queue.pollFirst();
            int dist = here.getValue() + 1;
            for (int i : graph.get(here.getKey())) {
                if (!visited.contains(i)) {
                    visited.add(i);
                    distance.put(i, dist);
                    queue.add(new Pair<Integer, Integer>(i, dist));
                }
            }
        }
    }

    private static double calcInheritedSim(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, HashMap<Integer, Double>> g1ToG2InhScore,
            HashMap<Integer, Integer> g1ToG2Nodes,
            int g1Cand,int g2Cand) {
        int numOfCommonNei = 0;
        double sumOfCommonNeiScore = 0;
        for(int i : g1.get(g1Cand)){
            if(g1ToG2Nodes.containsKey(i)){
                int node = g1ToG2Nodes.get(i);
                if(g1ToG2InhScore.get(i).containsKey(g2Cand)){
                    numOfCommonNei++;
                    sumOfCommonNeiScore+=g1ToG2InhScore.get(i).get(node);
                }
            }
        }
        if(numOfCommonNei==0){
            return 0;
        }
        return sumOfCommonNeiScore
                / numOfCommonNei
                * (1 - (Math.abs(g1.get(g1Cand).size() - g2.get(g2Cand).size())
                        / Math.max(g1.get(g1Cand).size(), g2.get(g2Cand).size())));
    }
    private static double calcRelativeDistanceSim(HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance,HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance,int g1Cand,int g2Cand,HashMap<Integer, Integer> g1ToG2SeedsMap) {
        ArrayList<Double> g1Vec = new ArrayList<Double>();
        ArrayList<Double> g2Vec = new ArrayList<Double>();
        for(int i : g1ToG2SeedsMap.keySet()){
            g1Vec.add(((double)g1seedsToAllNodeToDistance.get(i).get(g1Cand)));
            g2Vec.add(((double)g2seedsToAllNodeToDistance.get(g1ToG2SeedsMap.get(i)).get(g2Cand)));
        }
        return Utilities.cosSimDou(g1Vec, g2Vec);
    }
    private static double calcStructualSim(HashMap<Integer, Double> g1NodeToNormalizedClosness,    HashMap<Integer, Double> g2NodeToNormalizedClosness, 
                                         HashMap<Integer,   Double> g1nodeToNormalizedBetweeness,  HashMap<Integer, Double> g2nodeToNormalizedBetweeness,
                                         HashMap<Integer,   Double> g1nodeToNormalizedDegree,      HashMap<Integer, Double> g2nodeToNormalizedDegree,
                                         int g1Cand,int g2Cand) {
        ArrayList<Double> g1Vec = new ArrayList<Double>();
        ArrayList<Double> g2Vec = new ArrayList<Double>();
        g1Vec.add(g1NodeToNormalizedClosness.get(g1Cand));
        g1Vec.add(g1nodeToNormalizedBetweeness.get(g1Cand));
        g1Vec.add(g1nodeToNormalizedDegree.get(g1Cand));
        
        g2Vec.add(g2NodeToNormalizedClosness.get(g2Cand));
        g2Vec.add(g2nodeToNormalizedBetweeness.get(g2Cand));
        g2Vec.add(g2nodeToNormalizedDegree.get(g2Cand));
        return Utilities.cosSimDou(g1Vec, g2Vec);
    }
}
