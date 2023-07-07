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

import common.Graph;

public class UserMatching {
    public static void main(String[] args) throws IOException {
        if(args.length!=5){
            System.out.println("UserMatching usage : g1 g2 seedFile numToTest outputFileName");
            System.exit(0);
        }
        runUM(args[0],args[1],args[2],args[3],args[4]);
    }
    public static void runUM(String g1, String g2, String SeedFile, String NumToTest, String out) throws IOException {
        HashMap<Integer, HashSet<Integer>> G1 = Graph.readUndirectedGraph(g1);//Graph.sampleGraph(G, prob);
        HashMap<Integer, HashSet<Integer>> G2 = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> g1ToG2Mapping = Graph.getSeeds(SeedFile);//Graph.getSeeds(G1,Integer.parseInt(args[2]),Integer.parseInt(args[5]));
        HashMap<Integer, Integer> res = UserMatching.userMatching(G1, G2, g1ToG2Mapping,Integer.parseInt(NumToTest));
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
    public static HashMap<Integer, Integer> userMatching(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> g1ToG2Mapping,int numToTest){
        //HashMap<Integer, Integer> g1DegreeMap = Graph.getGraphDegreeMap(g1);
        //HashMap<Integer, Integer> g2DegreeMap = Graph.getGraphDegreeMap(g2);
        HashSet<Integer> nodesInG1Matched = new HashSet<Integer>();
        HashSet<Integer> nodesInG2Matched = new HashSet<Integer>();
        for(int i : g1ToG2Mapping.keySet()){
            nodesInG1Matched.add(i);
            nodesInG2Matched.add(g1ToG2Mapping.get(i));
        }
        LinkedList<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1,nodesInG1Matched);
        LinkedList<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2,nodesInG2Matched);
        if (sortedG1Nodes.size() != sortedG2Nodes.size()) {
            throw new RuntimeException("total unmatched size are not the same");
        }
        ArrayList<Integer> candidateG1 = new ArrayList<Integer>();
        ArrayList<Integer> candidateG2 = new ArrayList<Integer>();
        while (sortedG1Nodes.size() > 0 && sortedG2Nodes.size() > 0) {
            if(candidateG1.size()+sortedG1Nodes.size()>numToTest && candidateG2.size()+sortedG2Nodes.size()>numToTest){
                while(candidateG1.size()<numToTest){
                    candidateG1.add(sortedG1Nodes.pollFirst());
                    candidateG2.add(sortedG2Nodes.pollFirst());
                }
                int largestG1Cand=-1,largestG2Cand=-1,largestScore=-1;
                for(int g1Cand : candidateG1){
                    for(int g2Cand : candidateG2){
                        int numCommonNei = findCommonNei(g1, g2, g1Cand, g2Cand, g1ToG2Mapping);
                        if(numCommonNei>largestScore){
                            largestScore=numCommonNei;
                            largestG1Cand=g1Cand;
                            largestG2Cand=g2Cand;
                        }
                    }
                }
                //System.out.println(largestG1Cand+" "+largestG2Cand);
                g1ToG2Mapping.put(largestG1Cand, largestG2Cand);
                sortedG1Nodes.remove(new Integer(largestG1Cand));
                sortedG2Nodes.remove(new Integer(largestG2Cand));
                candidateG1.remove(new Integer(largestG1Cand));
                candidateG2.remove(new Integer(largestG2Cand));
            }else{
                for(int i = 0;i<sortedG1Nodes.size();i++){
                    candidateG1.add(sortedG1Nodes.pollFirst());
                    candidateG2.add(sortedG2Nodes.pollFirst());
                }   
                while(candidateG1.size()>0){
                    int largestG1Cand=-1,largestG2Cand=-1,largestScore=-1;
                    for(int g1Cand : candidateG1){
                        for(int g2Cand : candidateG2){
                            int numCommonNei = findCommonNei(g1, g2, g1Cand, g2Cand, g1ToG2Mapping);
                            if(numCommonNei>largestScore){
                                largestScore=numCommonNei;
                                largestG1Cand=g1Cand;
                                largestG2Cand=g2Cand;
                            }
                        }
                    }
                    g1ToG2Mapping.put(largestG1Cand, largestG2Cand);
                    sortedG1Nodes.remove(new Integer(largestG1Cand));
                    sortedG2Nodes.remove(new Integer(largestG2Cand));
                    candidateG1.remove(new Integer(largestG1Cand));
                    candidateG2.remove(new Integer(largestG2Cand));
                }
            }
        }
        
        /*int numRight = 0;
        for(int a : g1ToG2Mapping.keySet()){
            if(g1ToG2Mapping.get(a)==a){
                numRight++;
            }
            System.out.println(a+" "+g1ToG2Mapping.get(a));
        }
        System.out.println(numRight+"/"+g1ToG2Mapping.size()+"mapped correctly");*/
        return g1ToG2Mapping;
    }

    private static int findCommonNei(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, int g1Cand, int g2Cand,
            HashMap<Integer, Integer> g1ToG2Mapping) {
        int commonNei = 0;
        HashSet<Integer> g2Nei = g2.get(g2Cand);
        for(int i : g1.get(g1Cand)){
            if(g1ToG2Mapping.containsKey(i) && g2Nei.contains(g1ToG2Mapping.get(i))){
                commonNei++;
            }
        }
        return commonNei;
    }

    /*private static PriorityQueue<Pair<Pair<Integer, Integer>, Integer>> createNodePairToScoreQueue() {
        return new PriorityQueue<Pair<Pair<Integer,Integer>,Integer>>(100*100, new Comparator<Pair<Pair<Integer,Integer>,Integer>>(){
            @Override
            public int compare(Pair<Pair<Integer,Integer>, Integer> o1,
                    Pair<Pair<Integer,Integer>, Integer> o2) {
                return -1*Integer.compare(o1.getValue(), o2.getValue());
            }
        });
    }   */
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
}
