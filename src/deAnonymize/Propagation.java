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
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import common.Graph;

public class Propagation {
    private static double theta = 0.8;
    //static BufferedWriter debug;
    public static void propagationMethod(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> seed_mapping, String outFile) throws IOException {
        //debug = new BufferedWriter(new FileWriter("debug/propagation.txt"));
        int prevMatchingSize = seed_mapping.size();
        HashMap<Integer, Integer> lgraphInDegreeMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> lgraphOutDegreeMap = new HashMap<Integer, Integer>();
        Graph.getDirectedGraphDegreeMap(lgraph,lgraphInDegreeMap,lgraphOutDegreeMap);
        HashMap<Integer, Integer> rgraphInDegreeMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> rgraphOutDegreeMap = new HashMap<Integer, Integer>();
        Graph.getDirectedGraphDegreeMap(rgraph,rgraphInDegreeMap,rgraphOutDegreeMap);
        HashMap<Integer, Integer> invertedMapping = invert(seed_mapping);
        int roundCount = 0;
        BufferedWriter w = new BufferedWriter(new FileWriter(outFile));
        do {
            w.write("starting round "+roundCount+++"\n");
            prevMatchingSize = seed_mapping.size();
            //debug.write("current mapping of "+ prevMatchingSize+"nodes : "+seed_mapping);
            propagationStep(lgraph, rgraph, lgraphInDegreeMap,lgraphOutDegreeMap,rgraphInDegreeMap,rgraphOutDegreeMap,seed_mapping,invertedMapping);
            //debug.write("end of current propagaton new mapping of "+seed_mapping.size()+" nodes :"+seed_mapping);
            int countRight = 0;
            for(int i : seed_mapping.keySet()){
                w.write(i+" "+seed_mapping.get(i)+"\n");
                if(i==seed_mapping.get(i)){
                    countRight++;
                }
            }
            w.write("number of right mapping : "+countRight+"\n");
        } while (prevMatchingSize != seed_mapping.size());
    }

    private static void propagationStep(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> lgraphInDegreeMap,
            HashMap<Integer, Integer> lgraphOutDegreeMap,
            HashMap<Integer, Integer> rgraphInDegreeMap,
            HashMap<Integer, Integer> rgraphOutDegreeMap,
            HashMap<Integer, Integer> mapping,
            HashMap<Integer, Integer> invertedMapping) throws IOException {
        System.out.println("start propatation");
        //debug.write("start propatation");
        //HashMap<Integer, HashMap<Integer, Double>> lnodeScores = new HashMap<Integer, HashMap<Integer, Double>>();
        //HashMap<Integer, HashMap<Integer, Double>> rnodeScores = new HashMap<Integer, HashMap<Integer, Double>>();
       
        for (int lnode : lgraph.keySet()) {
            if(mapping.containsKey(lnode)){
                continue;
            }
            System.out.println("start node "+lnode);
            HashMap<Integer, Double> lnodeScore = matchScores(lgraph, rgraph,lgraphInDegreeMap,lgraphOutDegreeMap,rgraphInDegreeMap,rgraphOutDegreeMap, mapping,invertedMapping, lnode);
            //System.out.println("got score");
            if (eccentricity(lnodeScore) < theta) {
                continue;
            }
            int rnode = findNode(lnodeScore);
            System.out.println("start reverse");
            HashMap<Integer, Double> rnodeScore = matchScores(rgraph, lgraph,rgraphInDegreeMap,rgraphOutDegreeMap,lgraphInDegreeMap,lgraphOutDegreeMap, invertedMapping,mapping, rnode);

            if (eccentricity(rnodeScore) < theta) {
                continue;
            }
            int reverseMatch = findNode(rnodeScore);
            if (reverseMatch != lnode) {
                continue;
            }
            invertedMapping.put(rnode, lnode);
            mapping.put(lnode, rnode);
            System.out.println("end node "+lnode+" "+rnode+"mapped");
        }
        System.out.println("end propatation");
    }

    private static int findNode(HashMap<Integer, Double> score) {
        double maxScore = 0;
        int maxPosition = -1;
        for(int i : score.keySet()){
            if(score.get(i)>=maxScore){
                maxScore=score.get(i);
                maxPosition=i;
            }
        }
        return maxPosition;
    }

    private static HashMap<Integer, Integer> invert(
            HashMap<Integer, Integer> seedMapping) {
        HashMap<Integer,Integer> inverted= new HashMap<Integer, Integer>();
        for(int i : seedMapping.keySet()){
            inverted.put(seedMapping.get(i), i);
        }
        return inverted;
    }


    private static double eccentricity(HashMap<Integer, Double> score) {
        double[] scores = new double[score.keySet().size()];
        int posHere = 0;
        double maxScore = Integer.MIN_VALUE;
        double maxScore2 = Integer.MIN_VALUE;
        for(int i : score.keySet()){
            double here =score.get(i);
            scores[posHere]=here;
            posHere++;
            if (here > maxScore) {
                maxScore2 = maxScore;
                maxScore = here;
              } else if (here > maxScore2) {
                  maxScore2 = here;
              }
        }
        StandardDeviation std = new StandardDeviation();
        double standardDeviation = std.evaluate(scores);
        return (maxScore-maxScore2)/standardDeviation;
    }
    
    private static HashMap<Integer, Double> matchScores(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> lgraphInDegreeMap,
            HashMap<Integer, Integer> lgraphOutDegreeMap,
            HashMap<Integer, Integer> rgraphInDegreeMap,
            HashMap<Integer, Integer> rgraphOutDegreeMap,
            HashMap<Integer, Integer> mapping,
            HashMap<Integer, Integer> inverseMapping, int lnode) {
        //System.out.println("start scoring");
        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
        for(int nodeA : rgraph.keySet()){
            scores.put(nodeA, 0.0D);
            for(int nodeB : rgraph.get(nodeA)){
                scores.put(nodeB, 0.0D);
            }
        }
        //System.out.println("done init");
        //int c = 0;
        for(int lnbr: mapping.keySet()){
            //System.out.println("looped "+(c++));
            for(int node : lgraph.get(lnbr)){
                if (node == lnode) {
                    int rnbr = mapping.get(lnbr);
                    for (int rnode : rgraph.get(rnbr)) {
                        if (inverseMapping.containsKey(rnode)) {
                            continue;
                        }
                        double newScore = scores.get(rnode) + 1 / Math.sqrt(rgraphInDegreeMap.get(rnode));
                        scores.put(rnode, newScore);
                    }
                } 
            }
        }
        
        //System.out.println("done forward score");
        for (int lnbr : lgraph.get(lnode)) {
            if (!mapping.containsKey(lnbr)) {
                continue;
            }
            int rnbr = mapping.get(lnbr);
            for (int rnode : rgraph.keySet()) {
                for (int rNodeB : rgraph.get(rnode)) {
                    if (rNodeB == rnbr) {
                        if (inverseMapping.containsKey(rnode)) {
                            continue;
                        }
                        double newScore = scores.get(rnode) + 1
                                / Math.sqrt(rgraphOutDegreeMap.get(rnode));
                        scores.put(rnode, newScore);
                    }
                }
            }
        }
        //System.out.println("done reverse score");
        return scores;
    }
    
    public static void main(String[] args) throws IOException {
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(args[0]);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(args[1]);
        HashMap<Integer, Integer> seed_mapping = Graph.getSeeds(args[2]);
        String outFile = args[3];
        theta=Double.parseDouble(args[4]);
        Propagation.propagationMethod(G1, G2, seed_mapping,outFile);
    }
}
