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

public class SJCCS2014 {
    public static void main(String[] args) throws IOException {
        if(args.length!=11){
            System.out.println("CCSRolling useage : g1 g2 seedFile BipartSize numToKeepPerBipart degSimW neiSimW disSimW numNeiToCons outputFileName mode");
                                                  //0   1   2       3           4                   5       6       7       8           9             10
            System.exit(0);
        }
        runSJCCS2014(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10]);
    }

    public static void runSJCCS2014(String g1, String g2, String SeedFile, String BipartSize, String NumToKeep,
            String DegSimW, String NeiSimW, String DisSimW,String NumNeiToCons, String out, String mode) throws IOException {
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> cTosMapping = Graph.getSeeds(SeedFile);
        int bipartSize = Integer.parseInt(BipartSize);
        int numToKeep = Integer.parseInt(NumToKeep);
        double degSimW=Double.parseDouble(DegSimW),neiSimW=Double.parseDouble(NeiSimW),disSimW=Double.parseDouble(DisSimW);
        int numNeiToCons = Integer.parseInt(NumNeiToCons);
        HashMap<Integer, Integer> res = runSJCCS2014(G1, G2, cTosMapping, bipartSize,numToKeep, degSimW, neiSimW, disSimW, numNeiToCons,mode);
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

    private static HashMap<Integer, Integer> runSJCCS2014(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Seeds, int bipartSize,
            int numToKeep, double degSimW, double neiSimW, double disSimW,
            int numNeiToCons,String mode) {
        HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance = new HashMap<Integer, HashMap<Integer, Integer>>();
        HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance = new HashMap<Integer, HashMap<Integer, Integer>>();
        HashSet<Integer> g2Landmark = new HashSet<Integer>();
        HashSet<Integer> g1Landmark = new HashSet<Integer>();
        LinkedList<Integer> matchedG1Nodes = new LinkedList<Integer>();
        LinkedList<Integer> matchedG2Nodes = new LinkedList<Integer>();
        for(int g1Cand : g1ToG2Seeds.keySet()){
            int g2Cand = g1ToG2Seeds.get(g1Cand);
            g2Landmark.add(g2Cand);
            g1Landmark.add(g1Cand);
            matchedG1Nodes.add(g1Cand);
            matchedG2Nodes.add(g2Cand);
            g2seedsToAllNodeToDistance.put(g2Cand, new HashMap<Integer, Integer>());
            g1seedsToAllNodeToDistance.put(g1Cand, new HashMap<Integer, Integer>());
            span(g2, g2Cand, g2seedsToAllNodeToDistance.get(g2Cand));
            span(g1, g1Cand, g1seedsToAllNodeToDistance.get(g1Cand));
        }

        LinkedList<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1,g1Landmark);
        LinkedList<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2,g2Landmark);
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
                    score[g2Cand][g1Cand]=calcSim(g1,g2,candidateG1.get(g1Cand),candidateG2.get(g2Cand),
                            g1seedsToAllNodeToDistance,
                            g2seedsToAllNodeToDistance,
                            g1ToG2Seeds, degSimW, neiSimW, disSimW, numNeiToCons);
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

    private static float calcSim(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            int g1Cand,
            int g2Cand,
            HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance,
            HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance,
            HashMap<Integer, Integer> g1ToG2Seeds, double degSimW,
            double neiSimW, double disSimW, int numNeiToCons) {
        double degSim = calcDegreeSim(g1, g2, g1Cand, g2Cand);
        double neiSim = calcNeighSim(g1, g2, g1Cand, g2Cand, numNeiToCons);
        double disSim = calcRefDisSim(g1seedsToAllNodeToDistance,
                g2seedsToAllNodeToDistance, g1ToG2Seeds, g1Cand, g2Cand);
        return (float) ((disSimW * disSim + degSimW * degSim + neiSimW * neiSim) * -1);
    }
    private static double calcRefDisSim(
            HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance,
            HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance,
            HashMap<Integer, Integer> g1ToG2Seeds, int g1Cand, int g2Cand) {
        ArrayList<Integer> g1Vec = new ArrayList<Integer>();
        ArrayList<Integer> g2Vec = new ArrayList<Integer>();
        for (int i : g1ToG2Seeds.keySet()) {
            g1Vec.add(g1seedsToAllNodeToDistance.get(i).get(g1Cand));
            g2Vec.add(g2seedsToAllNodeToDistance.get(g1ToG2Seeds.get(i)).get(
                    g2Cand));
        }
        return Utilities.cosSimInt(g1Vec, g2Vec);
    }

    private static double calcNeighSim(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, int g1Cand, int g2Cand,
            int numNeiToCons) {
        ArrayList<Integer> g1Vec = getBNeiArray(g1, g1Cand, numNeiToCons);
        ArrayList<Integer> g2Vec = getBNeiArray(g2, g2Cand, numNeiToCons);
        return Utilities.cosSimInt(g1Vec, g2Vec);
    }

    private static ArrayList<Integer> getBNeiArray(
            HashMap<Integer, HashSet<Integer>> g, int cand, int numNei) {
        PriorityQueue<Pair<Integer, Integer>> neiQueueByDegree = new PriorityQueue<Pair<Integer, Integer>>(
                g.get(cand).size(), new Comparator<Pair<Integer, Integer>>() {

                    @Override
                    public int compare(Pair<Integer, Integer> o1,
                            Pair<Integer, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }

                });
        for (int i : g.get(cand)) {
            neiQueueByDegree
                    .add(new Pair<Integer, Integer>(i, g.get(i).size()));
        }
        ArrayList<Integer> res = new ArrayList<Integer>();
        while (res.size() < numNei) {
            if (!neiQueueByDegree.isEmpty()) {
                Pair<Integer, Integer> here = neiQueueByDegree.poll();
                res.add(here.getValue());
            } else {
                res.add(0);
            }
        }
        return res;
    }

    private static double calcDegreeSim(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, int g1Cand, int g2Cand) {
        return Math.sqrt(1.0 / (Math.abs(g1.get(g1Cand).size()
                - g2.get(g2Cand).size()) + 1));
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
            LinkedList<Pair<Integer,Integer>> queue = new LinkedList<Pair<Integer,Integer>>();
            HashSet<Integer> visited = new HashSet<Integer>();
            Pair<Integer, Integer> start = new Pair<Integer, Integer>(id, 0);
            distance.put(id, 0);
            visited.add(id);
            queue.addLast(start);
            while(!queue.isEmpty()){
                Pair<Integer, Integer> here = queue.pollFirst();
                int dist = here.getValue()+1;
                for(int i : graph.get(here.getKey())){
                    if (!visited.contains(i)) {
                        visited.add(i);
                        distance.put(i, dist);  
                        queue.add(new Pair<Integer, Integer>(i, dist));
                    }
                }
            }
        }
}
