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

public class newDVRolling{
    public static void main(String[] args) throws IOException {
        if(args.length!=7){
            System.out.println("DistanceVectorRolling usage : g1 g2 numSeeds bipartSize numToKeep outputFileName numWrongSeeds");
            System.exit(0);
        }
        String g1 = args[0];
        String g2 = args[1];
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);//Graph.sampleGraph(G, prob);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> cTosMapping = Graph.getSeeds(G1,Integer.parseInt(args[2]),Integer.parseInt(args[10]));
        int bipartSize = Integer.parseInt(args[3]);
        int numToKeep = Integer.parseInt(args[4]);
        double degSimW=Double.parseDouble(args[5]),neiSimW=Double.parseDouble(args[6]),disSimW=Double.parseDouble(args[7]);
        int numNeiToCons = Integer.parseInt(args[8]);
        HashMap<Integer, Integer> res = DVRollingWindow(G1, G2, cTosMapping, bipartSize,numToKeep, degSimW, neiSimW, disSimW, numNeiToCons);
        /*int r = 0;
        for(int i : res.keySet()){
            if(i==res.get(i)){
                r++;
            }
           //System.out.print(i+" "+res.get(i)+"\n");
        }
        System.out.println(r+" "+res.size());*/
        /*BufferedWriter w = new BufferedWriter(new FileWriter(args[9]+args[10]));
        int r = 0;
        for(int i : res.keySet()){
            if(i==res.get(i)){
                r++;
            }
            w.write(i+" "+res.get(i)+"\n");
        }
        w.write(args[9]+" "+r+"/"+res.size()+" "+G1.size()+" "+G2.size()+"\n");
        w.flush();w.close();*/
        //System.out.println(r);
    }
    public static HashMap<Integer, Integer> runCCSSlidingWindow(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Seeds, int bipartSize,int numToKeep ,double degSimW,double neiSimW,double disSimW,int numNeiToCons) throws IOException {
        HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance = new HashMap<Integer, HashMap<Integer, Integer>>();
        HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance = new HashMap<Integer, HashMap<Integer, Integer>>();
        /*
         * HashMap<Integer,Integer> sIndexMap = new HashMap<Integer, Integer>();
         * HashMap<Integer,Integer> cIndexMap = new HashMap<Integer, Integer>();
         * HashMap<Integer,Integer> rsIndexMap = new HashMap<Integer,
         * Integer>(); HashMap<Integer,Integer> rcIndexMap = new
         * HashMap<Integer, Integer>();
         */
        HashMap<Integer, Integer> g2ToG1Res = new HashMap<Integer, Integer>();
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
        if (sortedG1Nodes.size() != sortedG2Nodes.size()) {
            throw new RuntimeException("total unmatched size are not the same");
        }
        ArrayList<Integer> candidateG2 = new ArrayList<Integer>();
        ArrayList<Integer> candidateG1 = new ArrayList<Integer>();
        while (sortedG2Nodes.size() > 0 && sortedG1Nodes.size() > 0) {
            if(candidateG2.size()+sortedG2Nodes.size()>bipartSize && candidateG1.size()+sortedG1Nodes.size()>bipartSize){
                ArrayList<Integer> unMatchedG2 = new ArrayList<Integer>();
                ArrayList<Integer> unMatchedG1 = new ArrayList<Integer>();
                while(candidateG2.size()<bipartSize){
                    candidateG2.add(sortedG2Nodes.pollFirst());
                    candidateG1.add(sortedG1Nodes.pollFirst());
                }
                float[][] score = new float[candidateG2.size()][candidateG2.size()];
                float[][] matrix = new float[candidateG2.size()][candidateG2.size()];
                for(int g2Cand = 0;g2Cand<candidateG2.size();g2Cand++){
                    for(int g1Cand = 0;g1Cand<candidateG1.size();g1Cand++){
                        score[g2Cand][g1Cand]= calcSim(g1,g2,candidateG1.get(g1Cand),candidateG2.get(g2Cand),
                                                       g1seedsToAllNodeToDistance,
                                                       g2seedsToAllNodeToDistance,
                                                       g1ToG2Seeds, degSimW, neiSimW, disSimW, numNeiToCons);
                        matrix[g2Cand][g1Cand]=score[g2Cand][g1Cand];
                    }
                }
                MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
                int[][] result = match.computeAssignments(matrix);
                /*for(int i = 0;i<result.length;i++){
                    System.out.println(candidateS.get(result[i][1])+" "+candidateC.get(result[i][0])+" "+score[result[i][0]][result[i][1]]);
                }*/
                PriorityQueue<Pair<Pair<Integer,Integer>,Float>> queue = createNodePairToScoreQueue();
                for(int i = 0;i<result.length;i++){
                    int nodeG1 = candidateG1.get(result[i][1]);
                    int nodeG2 = candidateG2.get(result[i][0]);
                    queue.add(new Pair<Pair<Integer,Integer>, Float>(new Pair<Integer,Integer>(nodeG1, nodeG2),score[result[i][0]][result[i][1]]));
                }
                int count = 0;
                int r = 0;
                while(!queue.isEmpty()){
                    Pair<Pair<Integer, Integer>, Float> here = queue.poll();
                    if(count<numToKeep){
                        if(here.getKey().getKey().equals(here.getKey().getValue())){
                            r++;
                        }
                        matchedG1Nodes.add(here.getKey().getKey());
                        matchedG2Nodes.add(here.getKey().getValue());
                        //System.out.println("matched "+here.getKey().getKey()+" to "+here.getKey().getValue());
                    }else{
                        unMatchedG1.add(here.getKey().getKey());
                        unMatchedG2.add(here.getKey().getValue());
                    }
                    count++;
                }
                System.out.println(r);
                candidateG2=unMatchedG2;
                candidateG1=unMatchedG1;
            }else{
                for(int i = 0;i<sortedG2Nodes.size();i++){
                    candidateG2.add(sortedG2Nodes.pollFirst());
                    candidateG1.add(sortedG1Nodes.pollFirst());
                }                
                float[][] score = new float[candidateG2.size()][candidateG2.size()];
                float[][] matrix = new float[candidateG2.size()][candidateG2.size()];
                for(int g2Cand = 0;g2Cand<candidateG2.size();g2Cand++){
                    for(int g1Cand = 0;g1Cand<candidateG2.size();g1Cand++){
                        //matrix[i][j]=-1*calcWeight(numLandmark,sDistance,cDistance,sLandmarkMap,cLandmarkMap,i,j);
                        score[g2Cand][g1Cand]= calcSim(g1,g2,candidateG1.get(g1Cand),candidateG2.get(g2Cand),
                                                       g1seedsToAllNodeToDistance,
                                                       g2seedsToAllNodeToDistance,
                                                       g1ToG2Seeds, degSimW, neiSimW, disSimW, numNeiToCons);
                        matrix[g2Cand][g1Cand]=score[g2Cand][g1Cand];
                    }
                }
                MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
                int[][] result = match.computeAssignments(matrix);
                /*for(int i = 0;i<result.length;i++){
                    System.out.println(candidateS.get(result[i][1])+" "+candidateC.get(result[i][0])+" "+score[result[i][0]][result[i][1]]);
                }*/
                for(int i = 0;i<result.length;i++){
                    int nodeG1 = candidateG1.get(result[i][1]);
                    int nodeG2 = candidateG2.get(result[i][0]);
                    matchedG1Nodes.add(nodeG1);
                    matchedG2Nodes.add(nodeG2);
                }
            }
        }
        
        for(int i = 0;i<matchedG1Nodes.size();i++){
            g2ToG1Res.put(matchedG2Nodes.get(i),   matchedG1Nodes.get(i));
        }
        /*int right = 0;
        for(int i = 0;i<matchedSNodes.size();i++){
            if(matchedSNodes.get(i).equals(matchedCNodes.get(i))){
                right++;
            }
        }*/
        //System.out.println("numRight = "+right);
        return g2ToG1Res;
    }
    public static HashMap<Integer, Integer> DVRollingWindow(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Seeds, int bipartSize,int numToKeep ,double degSimW,double neiSimW,double disSimW,int numNeiToCons) throws IOException {
        LinkedList<Integer> matchedG1Nodes = new LinkedList<Integer>();
        LinkedList<Integer> matchedG2Nodes = new LinkedList<Integer>();
        HashSet<Integer> g1Landmark = new HashSet<Integer>();
        HashSet<Integer> g2Landmark = new HashSet<Integer>();
        HashMap<Integer, Integer> g1ToG2Nodes = new HashMap<Integer, Integer>();
        
        for (int g1Node : g1ToG2Seeds.keySet()) {
            int g2Node = g1ToG2Seeds.get(g1Node);
            matchedG1Nodes.add(g1Node);
            matchedG2Nodes.add(g2Node);
            g1Landmark.add(g1Node);
            g2Landmark.add(g2Node);
            g1ToG2Nodes.put(g1Node, g2Node);
        }
        HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance = calcDistanceToSeeds(g1, g1Landmark);
        HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance = calcDistanceToSeeds(g2, g2Landmark);
        LinkedList<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1,g1Landmark);
        LinkedList<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2,g2Landmark);
        if (sortedG1Nodes.size() != sortedG2Nodes.size()) {
            throw new RuntimeException("total unmatched size are not the same");
        }
        while (sortedG2Nodes.size() > 0 && sortedG1Nodes.size() > 0) {
            LinkedList<Integer> unMatchedG1 = new LinkedList<Integer>();
            LinkedList<Integer> unMatchedG2 = new LinkedList<Integer>();

            while (sortedG2Nodes.size() > bipartSize && sortedG1Nodes.size() > bipartSize) {
                //System.out.println(unMatchedG1.size()+" "+sortedG1Nodes.size()+" "+matchedG1Nodes.size()+" "+(unMatchedG1.size()+sortedG1Nodes.size()+matchedG1Nodes.size()));
                ArrayList<Integer> candidateG1 = new ArrayList<Integer>();
                ArrayList<Integer> candidateG2 = new ArrayList<Integer>();
                for (int i = 0; i < bipartSize; i++) {
                    candidateG1.add(sortedG1Nodes.pollFirst());
                    candidateG2.add(sortedG2Nodes.pollFirst());
                }
                float maxScore = 0;
                float[][] score = new float[bipartSize][bipartSize];
                float[][] matrix = new float[bipartSize][bipartSize];
                for (int g2Cand = 0; g2Cand < bipartSize; g2Cand++) {
                    for (int g1Cand = 0; g1Cand < bipartSize; g1Cand++) {
                        float scoreHere = calcSim(g1,g2,candidateG1.get(g1Cand),candidateG2.get(g2Cand),
                                g1seedsToAllNodeToDistance,
                                g2seedsToAllNodeToDistance,
                                g1ToG2Seeds, degSimW, neiSimW, disSimW, numNeiToCons);
                        score[g2Cand][g1Cand] = scoreHere;
                        matrix[g2Cand][g1Cand] = scoreHere;
                        if(maxScore<-1*scoreHere){
                            maxScore=scoreHere; 
                        }
                    }
                }
                /*System.out.println(candidateG1);
                System.out.println(candidateG2);
                System.out.println();
                for (int g2Cand = 0; g2Cand < bipartSize; g2Cand++) {
                    for (int g1Cand = 0; g1Cand < bipartSize; g1Cand++) {
                        System.out.print(score[g2Cand][g1Cand]+" ");
                    }
                    System.out.println();
                }*/
                MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
                int[][] result = match.computeAssignments(matrix);
                /*for(int i = 0;i<result.length;i++){
                    System.out.println(candidateG1.get(result[i][1])+" "+candidateG2.get(result[i][0])+" "+score[result[i][0]][result[i][1]]);
                }*/
                PriorityQueue<Pair<Pair<Integer, Integer>, Float>> queue = createNodePairToScoreQueue();
                for (int i = 0; i < result.length; i++) {
                    int nodeG1 = candidateG1.get(result[i][1]);
                    int nodeG2 = candidateG2.get(result[i][0]);
                    //System.out.println(nodeG1+" "+nodeG2+score[result[i][0]][result[i][1]]);
                    queue.add(new Pair<Pair<Integer, Integer>, Float>(
                            new Pair<Integer, Integer>(nodeG1, nodeG2),
                            score[result[i][0]][result[i][1]]));
                }
                int r = 0;
                int count = 0;
                /*int kept = 0;*/
                while (!queue.isEmpty()) {
                    Pair<Pair<Integer, Integer>, Float> here = queue.poll();
                    if(count<numToKeep){
                    //if (here.getValue()*-1 > maxScore*0.5) {
                        if (here.getKey().getKey().equals(here.getKey().getValue())) {
                            r++;
                        }
                       /* kept++;*/
                        int g1Cand =here.getKey().getKey();
                        int g2Cand = here.getKey().getValue();
                        matchedG1Nodes.add(g1Cand);
                        matchedG2Nodes.add(g2Cand);
                        g1ToG2Nodes.put(g1Cand, g2Cand);
                    } else {
                        unMatchedG1.add(here.getKey().getKey());
                        unMatchedG2.add(here.getKey().getValue());
                    }
                    count++;
                }
               System.out.println(r/*+"/"+kept*/);
            }
            if (unMatchedG2.size() + sortedG2Nodes.size() > bipartSize) {
                while(!sortedG2Nodes.isEmpty()){
                    unMatchedG2.add(sortedG2Nodes.pollFirst());
                }
                while(!sortedG1Nodes.isEmpty()){
                    unMatchedG1.add(sortedG1Nodes.pollFirst());
                }
                sortedG2Nodes = unMatchedG2;
                sortedG1Nodes = unMatchedG1;
            } else {
                ArrayList<Integer> candidateG2 = new ArrayList<Integer>();
                ArrayList<Integer> candidateG1 = new ArrayList<Integer>();
                for (int i = 0; i < sortedG2Nodes.size(); i++) {
                    candidateG2.add(sortedG2Nodes.pollFirst());
                    candidateG1.add(sortedG1Nodes.pollFirst());
                }
                float[][] score = new float[candidateG2.size()][candidateG2.size()];
                float[][] matrix = new float[candidateG2.size()][candidateG2.size()];
                for (int g2Cand = 0; g2Cand < candidateG2.size(); g2Cand++) {
                    for (int g1Cand = 0; g1Cand < candidateG2.size(); g1Cand++) {
                        // matrix[i][j]=-1*calcWeight(numLandmark,sDistance,cDistance,sLandmarkMap,cLandmarkMap,i,j);
                        score[g2Cand][g1Cand] = calcSim(g1,g2,candidateG1.get(g1Cand),candidateG2.get(g2Cand),
                                        g1seedsToAllNodeToDistance,
                                        g2seedsToAllNodeToDistance,
                                        g1ToG2Seeds, degSimW, neiSimW, disSimW, numNeiToCons);
                        matrix[g2Cand][g1Cand] = score[g2Cand][g1Cand];
                    }
                }
                MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
                int[][] result = match.computeAssignments(matrix);
                for (int i = 0; i < result.length; i++) {
                    int g1Cand = candidateG1.get(result[i][1]);
                    int g2Cand = candidateG2.get(result[i][0]);
                    matchedG1Nodes.add(g1Cand);
                    matchedG2Nodes.add(g2Cand);
                    g1ToG2Nodes.put(g1Cand, g2Cand);
                }
            }
        }
        int right = 0;
        for (int i = 0; i < matchedG1Nodes.size(); i++) {
            if (matchedG1Nodes.get(i).equals(matchedG2Nodes.get(i))) {
                right++;
            }
        }
        System.out.println("numRight = " + right+" "+matchedG1Nodes.size());
        return g1ToG2Nodes;
    }
    
    private static float calcSim(HashMap<Integer, HashSet<Integer>> g1, HashMap<Integer, HashSet<Integer>> g2, 
                                 int g1Cand, int g2Cand, 
                                 HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance, 
                                 HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance, HashMap<Integer, Integer> g1ToG2Seeds,
                                 double degSimW,double neiSimW,double disSimW,int numNeiToCons) {
        double degSim = calcDegreeSim(g1,g2,g1Cand,g2Cand);
        double neiSim = calcNeighSim(g1,g2,g1Cand,g2Cand,numNeiToCons);
        double disSim = calcRefDisSim(g1seedsToAllNodeToDistance,g2seedsToAllNodeToDistance,g1ToG2Seeds,g1Cand,g2Cand);
        return (float) ((disSimW*disSim+degSimW*degSim+neiSimW*neiSim)*-1);
    }
    private static double calcRefDisSim(HashMap<Integer, HashMap<Integer, Integer>> g1seedsToAllNodeToDistance, HashMap<Integer, HashMap<Integer, Integer>> g2seedsToAllNodeToDistance, HashMap<Integer, Integer> g1ToG2Seeds, int g1Cand, int g2Cand) {
        ArrayList<Integer> g1Vec = new ArrayList<Integer>();
        ArrayList<Integer> g2Vec = new ArrayList<Integer>();
        for(int i : g1ToG2Seeds.keySet()){
            g1Vec.add(g1seedsToAllNodeToDistance.get(i).get(g1Cand));
            g2Vec.add(g2seedsToAllNodeToDistance.get(g1ToG2Seeds.get(i)).get(g2Cand));
        }
        return Utilities.cosSimInt(g1Vec, g2Vec);
    }
    private static double calcNeighSim(HashMap<Integer, HashSet<Integer>> g1, HashMap<Integer, HashSet<Integer>> g2, int g1Cand, int g2Cand,int numNeiToCons) {
        ArrayList<Integer> g1Vec = getBNeiArray(g1,g1Cand,numNeiToCons);
        ArrayList<Integer> g2Vec = getBNeiArray(g2,g2Cand,numNeiToCons);
        return Utilities.cosSimInt(g1Vec, g2Vec);
    }
    private static ArrayList<Integer> getBNeiArray(HashMap<Integer, HashSet<Integer>> g, int cand, int numNei) {
        PriorityQueue<Pair<Integer,Integer>> neiQueueByDegree = new PriorityQueue<Pair<Integer,Integer>>(g.get(cand).size(), new Comparator<Pair<Integer,Integer>>(){

            @Override
            public int compare(Pair<Integer, Integer> o1,
                    Pair<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
            
        });
        for(int i : g.get(cand)){
            neiQueueByDegree.add(new Pair<Integer, Integer>(i,g.get(i).size()));
        }
        ArrayList<Integer> res = new ArrayList<Integer>();
        while(res.size()<numNei){
            if(!neiQueueByDegree.isEmpty()){
                Pair<Integer, Integer> here = neiQueueByDegree.poll();
                res.add(here.getValue());
            }else{
                res.add(0);
            }
        }
        return res;
    }

    private static double calcDegreeSim(HashMap<Integer, HashSet<Integer>> g1, HashMap<Integer, HashSet<Integer>> g2, int g1Cand, int g2Cand) {
        return Math.sqrt(1.0/(Math.abs(g1.get(g1Cand).size()-g2.get(g2Cand).size())+1));
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
    private static HashMap<Integer, HashMap<Integer, Integer>> calcDistanceToSeeds(HashMap<Integer, HashSet<Integer>> g, HashSet<Integer> seeds){
        HashMap<Integer, HashMap<Integer, Integer>> seedToAllNodeToDistance = new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int i : seeds){
            seedToAllNodeToDistance.put(i, new HashMap<Integer, Integer>());
            span(g, i, seedToAllNodeToDistance.get(i));
        }
        return seedToAllNodeToDistance;
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
    

}
