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

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import common.Graph;

public class UndirectionedPropagation {
    private static double theta;
    //static BufferedWriter debug;
    
    public static void main(String[] args) throws IOException {
        if(args.length!=5){
            System.out.println("Propagation usage : g1 g2 seedFile theta outputFileName");
            System.exit(0);
        }
        undirectionedPropagation(args[0],args[1],args[2],args[3],args[4]);
    }

    public static void undirectionedPropagation(String g1, String g2, String seedFile, String Theta, String outputFile) throws IOException {
        theta = Double.parseDouble(Theta);
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);//Graph.sampleGraph(G, prob);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);//Graph.sampleGraph(G, prob);
        HashMap<Integer, Integer> seed_mapping = Graph.getSeeds(seedFile);//Graph.getSeeds(G1,Integer.parseInt(args[2]),Integer.parseInt(args[5]));
        HashMap<Integer, Integer> res = UndirectionedPropagation.propagationMethod(G1, G2, seed_mapping);
        BufferedWriter w = new BufferedWriter(new FileWriter(outputFile));
        int r = 0;
        for(int i : res.keySet()){
            if(i==res.get(i)){
                r++;
            }
            w.write(i+" "+res.get(i)+"\n");
        }
        w.write(outputFile+" "+r+"/"+res.size()+" "+G1.size()+" "+G2.size()+"\n");
        w.flush();w.close();
    }

    public static HashMap<Integer, Integer> propagationMethod(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> seed_mapping) throws IOException {
        //debug = new BufferedWriter(new FileWriter("debug/propagation.txt"));
        int prevMatchingSize = seed_mapping.size();
        HashMap<Integer, Integer> lgraphDegreeMap = Graph.getGraphDegreeMap(lgraph);
        HashMap<Integer, Integer> rgraphDegreeMap = Graph.getGraphDegreeMap(rgraph);
        HashMap<Integer, Integer> invertedMapping = invert(seed_mapping);
        //int roundCount = 0;
        do {
            //System.out.println("starting round "+roundCount++);
            prevMatchingSize = seed_mapping.size();
            //debug.write("current mapping of "+ prevMatchingSize+"nodes : "+seed_mapping);
            propagationStep(lgraph, rgraph, lgraphDegreeMap,rgraphDegreeMap,seed_mapping,invertedMapping);
            //debug.write("end of current propagaton new mapping of "+seed_mapping.size()+" nodes :"+seed_mapping);
            int countRight = 0;
            for(int i : seed_mapping.keySet()){
                //System.out.println(i+" "+seed_mapping.get(i));
                if(i==seed_mapping.get(i)){
                    countRight++;
                }
            }
            System.out.println("number of right mapping : "+countRight+"\n");
        } while (prevMatchingSize != seed_mapping.size());
        
       /* int r = 0;
        for(int i : seed_mapping.keySet()){
            if(i==seed_mapping.get(i)){
                r++;
            }
        }
        //System.out.println(r);
*/        return seed_mapping;
    }

    private static void propagationStep(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> lgraphDegreeMap,
            HashMap<Integer, Integer> rgraphDegreeMap,
            HashMap<Integer, Integer> mapping,
            HashMap<Integer, Integer> invertedMapping) throws IOException {
        //System.out.println("start propatation");
       
        for (int lnode : lgraph.keySet()) {
            if(mapping.containsKey(lnode)){
                continue;
            }
            //System.out.println("start node "+lnode);
            HashMap<Integer, Double> lnodeScore = matchScores(lgraph, rgraph,rgraphDegreeMap,mapping,invertedMapping, lnode);
            if (eccentricity(lnodeScore) < theta) {
                continue;
            }
            int rnode = findNode(lnodeScore);
            //System.out.println("start reverse");
            HashMap<Integer, Double> rnodeScore = matchScores(rgraph, lgraph,lgraphDegreeMap, invertedMapping,mapping, rnode);

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
        //return mapping;
        //System.out.println("end propatation");
    }

    

    private static HashMap<Integer, Integer> invert(
            HashMap<Integer, Integer> seedMapping) {
        HashMap<Integer,Integer> inverted= new HashMap<Integer, Integer>();
        for(int i : seedMapping.keySet()){
            inverted.put(seedMapping.get(i), i);
        }
        return inverted;
    }

    
    private static HashMap<Integer, Double> matchScores(
            HashMap<Integer, HashSet<Integer>> lgraph,
            HashMap<Integer, HashSet<Integer>> rgraph,
            HashMap<Integer, Integer> rgraphDegreeMap,
            HashMap<Integer, Integer> mapping,
            HashMap<Integer, Integer> inverseMapping, int lnode) {

        HashMap<Integer, Double> scores = new HashMap<Integer, Double>();
        for(int nodeA : rgraph.keySet()){
            scores.put(nodeA, 0.0D);
        }
       
        for (int lnbr : lgraph.get(lnode)) {
            if (!mapping.containsKey(lnbr)) {
                continue;
            }
            int rnbr = mapping.get(lnbr);
            for(int rnode : rgraph.get(rnbr)){
                if (inverseMapping.containsKey(rnode)) {
                    continue;
                }
                double newScore = scores.get(rnode) + 1 / Math.sqrt(rgraphDegreeMap.get(rnode));
                scores.put(rnode, newScore);
            }
        }
        return scores;
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
    

}
