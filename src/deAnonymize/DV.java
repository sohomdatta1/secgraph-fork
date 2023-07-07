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

public class DV {
    public static void main(String[] args) throws IOException {
        if (args.length != 7) {
            System.out
                    .println("DistanceVector usage : g1 g2 seedFile bipartSize numToKeep outputFileName mode");
            System.exit(0);
        }
        runDV(args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
    }

    public static void runDV(String g1, String g2, String seedFile, String BipartSize, String NumToKeep,
            String out, String mode) throws IOException {
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> seeds = Graph.getSeeds(seedFile);
        int bipartSize = Integer.parseInt(BipartSize);
        BufferedWriter w = new BufferedWriter(new FileWriter(out));
        int numToKeep = Integer.parseInt(NumToKeep);
        w.write("g2ToG1seeds : \n");
        for(int i : seeds.keySet()){
            w.write(i+" "+seeds.get(i)+"\n");
        }
        HashMap<Integer, Integer>  res = distanceVector(G1, G2, seeds,bipartSize,numToKeep,mode);
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

    private static HashMap<Integer, Integer> distanceVector(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Mapping, int bipartSize, int numToKeep, String mode) {
        HashMap<Integer,HashMap<Integer,Integer>> g2Distance = new HashMap<Integer,HashMap<Integer,Integer>>();
        HashMap<Integer,HashMap<Integer,Integer>> g1Distance = new HashMap<Integer,HashMap<Integer,Integer>>();

        HashSet<Integer> g2Landmark = new HashSet<Integer>();
        HashSet<Integer> g1Landmark = new HashSet<Integer>();
        LinkedList<Integer> matchedG1Nodes = new LinkedList<Integer>();
        LinkedList<Integer> matchedG2Nodes = new LinkedList<Integer>();
        int numLandmark = g1ToG2Mapping.keySet().size();
        
        for(int g1Node : g1ToG2Mapping.keySet()){
            int g2Node = g1ToG2Mapping.get(g1Node);
            g2Landmark.add(g1Node);
            g1Landmark.add(g2Node);
            matchedG1Nodes.add(g1Node);
            matchedG2Nodes.add(g2Node);
            g2Distance.put(g2Node, new HashMap<Integer,Integer>());
            g1Distance.put(g1Node, new HashMap<Integer,Integer>());
            span(g1,g1Node,g1Distance.get(g1Node));
            span(g2,g2Node,g2Distance.get(g2Node));
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
                    score[g2Cand][g1Cand]=calcCosWeight(numLandmark,g1Distance,g2Distance,candidateG1.get(g1Cand),candidateG2.get(g2Cand));
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
    
    private static float calcCosWeight(int numLandmark,
            HashMap<Integer, HashMap<Integer, Integer>> sDistance,
            HashMap<Integer, HashMap<Integer, Integer>> cDistance,
            int sCand, int cCand) {
        ArrayList<Integer> I = new ArrayList<Integer>();
        ArrayList<Integer> J = new ArrayList<Integer>();
        for(int k : sDistance.keySet()){
            I.add(sDistance.get(k).get(sCand));
            J.add(cDistance.get(k).get(cCand));
        }
        float score = (float) Utilities.cosSimInt(I, J);
        return -1*score;
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
