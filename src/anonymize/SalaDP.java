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
package anonymize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;

import common.Graph;
import common.Pair;
import org.apache.commons.math3.distribution.LaplaceDistribution;

public class SalaDP {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("usage inputGraph outputGraph epselon ");
            System.exit(0);
        }
        DP(args[0], args[1], args[2]);
    }
    public static void DP(String inputGraph,String outputGraph, String epselon) throws IOException {
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(inputGraph);
        int cons = Integer.parseInt(epselon);
        int bucketSize = 1;
        getDP(g, cons, bucketSize);
        Graph.writeGraph(g, outputGraph);
    }
    
    public static HashMap<Integer, HashMap<Integer, Double>> DP(double epslon,
            HashMap<Integer, HashSet<Integer>> g,
            HashMap<Integer, HashMap<Integer, Integer>> jdd, int tuples) {
        PriorityQueue<Pair<Integer, Pair<Integer, Integer>>> jddPQ = pq(jdd);
        HashMap<Integer, HashMap<Integer, Double>> dp = new HashMap<Integer, HashMap<Integer, Double>>();
        int totalPairs = jddPQ.size();
        int iteamsPerTuple = (int) Math.ceil(totalPairs / tuples);
        while (!jddPQ.isEmpty()) {
            ArrayList<Pair<Integer, Pair<Integer, Integer>>> currentTuple = new ArrayList<Pair<Integer, Pair<Integer, Integer>>>();
            int maxDegree = 0;
            int count = 0;
            double valsLessThanZero = 0;
            int numOfZero = 0;
            while (!jddPQ.isEmpty() && count < iteamsPerTuple) {
                Pair<Integer, Pair<Integer, Integer>> here = jddPQ.poll();
                if (here.getKey() > maxDegree) {
                    maxDegree = here.getKey();
                }
                if (here.getValue().getKey() > maxDegree) {
                    maxDegree = here.getValue().getKey();
                }
                currentTuple.add(here);
                count++;
            }
            LaplaceDistribution lrv = new LaplaceDistribution(0,(4 * maxDegree + 1) / epslon);
            for (Pair<Integer, Pair<Integer, Integer>> here : currentTuple) {
                if (!dp.containsKey(here.getKey())) {
                    dp.put(here.getKey(), new HashMap<Integer, Double>());
                }
                double randVal = lrv.sample();
                double newVal = here.getValue().getValue() + randVal;
                if (newVal < 0) {
                    valsLessThanZero += -1 * newVal;
                    numOfZero++;
                    newVal = 0;
                }
                dp.get(here.getKey()).put(here.getValue().getKey(), newVal);
            }
            while (valsLessThanZero > 1E-10) {
                double valIfEvenDist = valsLessThanZero
                        / (totalPairs - numOfZero);
                numOfZero = 0;
                for (int i : dp.keySet()) {
                    for (int j : dp.get(i).keySet()) {
                        if (dp.get(i).get(j) == 0) {
                            numOfZero++;
                            continue;
                        }
                        double newVal = dp.get(i).get(j) - valIfEvenDist;
                        valsLessThanZero -= valIfEvenDist;
                        if (newVal < 0) {
                            valsLessThanZero += -1 * newVal;
                            numOfZero++;
                            newVal = 0;
                        }
                        dp.get(i).put(j, newVal);
                        if (valsLessThanZero <= 0) {
                            break;
                        }
                    }
                }
            }
        }

        /*
         * HashMap<Integer, HashMap<Integer, Double>> dp = new HashMap<Integer,
         * HashMap<Integer, Double>>();
         * 
         * int maxDegree = g.get(Graph.findLargestNode(g)).size();
         * LaplaceRandomVariable lrv = new LaplaceRandomVariable(0, (4 *
         * maxDegree + 1) / epslon); for (int i : jdd.keySet()) { dp.put(i, new
         * HashMap<Integer, Double>()); for (int j : jdd.get(i).keySet()) { int
         * val = jdd.get(i).get(j); double randVal = lrv.nextRandomVariable();
         * 
         * System.out.println( i+" "+j+" "+ (randVal) + " " + (val + randVal));
         * dp.get(i).put(j, val + randVal); } }
         */

        return dp;
    }

    public static void getDP(HashMap<Integer, HashSet<Integer>> g,double epselon,int tuple ) throws IOException {
       
        /*
         * for(int i : g.keySet()){ System.out.println(i+" "+g.get(i).size()); }
         * System.out.println("###########");
         */
        /*
         * for(int i : g.get(77)){ System.out.println(g.get(i).size()); }
         */
        HashMap<Integer, HashMap<Integer, Integer>> jdd = Graph.joinDegreeDistr(g);
        /*
         * for(int i : jdd.keySet()){ for(int j : jdd.get(i).keySet()){
         * System.out.println(i+" "+j+" "+jdd.get(i).get(j)); } }
         */
        HashMap<Integer, HashMap<Integer, Double>> dp = DP(epselon, g, jdd, tuple);
        TreeMap<Integer, Integer> degreeDistForG = Graph.getDegreeDist(g);
        int totalNodes = g.keySet().size();
        HashMap<Integer, Integer> degreeFreqWithDP = generateDegreeDistFromDP(dp, totalNodes);
        ArrayList<Integer> newDegreeSeq = getDegreeSeqFromDegreeFreq(
                degreeFreqWithDP, g.size());
        //TreeMap<Integer, Integer> differenceInDistrub = new TreeMap<Integer, Integer>();
        //HashMap<Integer, Integer> degreeDistWithJDD = generateDegreeDistFromJDD(jdd);

         /*System.out.println("####DegreeFreqForG###");
          for(int i : degreeDistForG.keySet()){
              System.out.println(i+" "+degreeDistForG.get(i)); 
         }
          System.out.println("####DegreeFreqWithDP###");
          for(int i : degreeFreqWithDP.keySet()){
              System.out.println(i+" "+degreeFreqWithDP.get(i)); 
         }*/
        HashSet<Integer> keys = new HashSet<Integer>(degreeDistForG.keySet());
        keys.addAll(degreeFreqWithDP.keySet());
        /*w.write("DegreeDist\nDegree\tnumInG\tnumInDPG\n");
        for(int i : degreeFreqWithDP.keySet()){
            w.write(i+" "+degreeDistForG.get(i)+" "+degreeFreqWithDP.get(i)+"\n"); 
       }*/

        /*
         * for(int i : degreeDistForG.keySet()){ differenceInDistrub.put(i,
         * degreeDistWithDP.get(i)-degreeDistForG.get(i)); }
         */

        PriorityQueue<Pair<Integer, HashSet<Integer>>> sortedGraph = Graph.getSortedGraphPQ(g);
       // Integer[] nodeList = g.keySet().toArray(new Integer[] {});
        Random rand = new Random();
        HashMap<Integer, Pair<Integer,Integer>> nodeToDegreeInMapToDegreeInDP = new HashMap<Integer, Pair<Integer,Integer>>();
        int i = 0;
        while(!sortedGraph.isEmpty()){
            Pair<Integer, HashSet<Integer>> here = sortedGraph.poll();
            int nodeHere = here.getKey();
            int degreeHere = g.get(nodeHere).size();
            nodeToDegreeInMapToDegreeInDP.put(nodeHere, new Pair<Integer,Integer>(degreeHere, newDegreeSeq.get(i)));
            i++;
        }
        /*w.write("########NodeToDegreeInGTODegreeInDP##########\n");
        w.write("node\tdegreeInG\tdegreeInDP");
        for(int j : nodeToDegreeInMapToDegreeInDP.keySet()){
            w.write(j+"\t"+nodeToDegreeInMapToDegreeInDP.get(j).getKey()+"\t"+nodeToDegreeInMapToDegreeInDP.get(j).getValue()+"\n");
        }*/
        sortedGraph.clear();
        sortedGraph = Graph.getSortedGraphPQ(g);
        while(!sortedGraph.isEmpty()){
            Pair<Integer, HashSet<Integer>> here = sortedGraph.poll();
            int nodeHere = here.getKey();
            int degreeHere = g.get(nodeHere).size();
            int degreeInDP = nodeToDegreeInMapToDegreeInDP.get(nodeHere).getValue();
            if(degreeHere<=degreeInDP){
                continue;
            }
            HashSet<Integer> neiOfNodeHere = new HashSet<Integer>(g.get(nodeHere));
            for(int nei : neiOfNodeHere){
                int neiDegreeInG = nodeToDegreeInMapToDegreeInDP.get(nei).getKey();
                int neiDegreeInNewG = nodeToDegreeInMapToDegreeInDP.get(nei).getValue();
                if(neiDegreeInG>neiDegreeInNewG){
                    degreeHere--;
                    g.get(nodeHere).remove(nei);
                    g.get(nei).remove(nodeHere);
                    nodeToDegreeInMapToDegreeInDP.put(nei, new Pair<Integer,Integer>(neiDegreeInG-1, neiDegreeInNewG));
                    if(degreeHere==degreeInDP){
                        break;
                    }
                }
            }
            nodeToDegreeInMapToDegreeInDP.put(nodeHere, new Pair<Integer,Integer>(degreeHere, degreeInDP));
            if(degreeHere==degreeInDP){
                continue;
            }
            Integer[] neiOfHere = g.get(nodeHere).toArray(new Integer[]{});
            int pos = 0;
            while(degreeHere>degreeInDP && pos<neiOfHere.length){
                if(g.get(neiOfHere[pos]).size()==1){
                    pos++;
                    continue;
                }
                g.get(nodeHere).remove(neiOfHere[pos]);
                g.get(neiOfHere[pos]).remove(nodeHere);
                int sizeInDp = nodeToDegreeInMapToDegreeInDP.get(neiOfHere[pos]).getValue();
                nodeToDegreeInMapToDegreeInDP.put(neiOfHere[pos], new Pair<Integer, Integer>(g.get(neiOfHere[pos]).size(),sizeInDp));
                degreeHere--;
                pos++;
            }
            nodeToDegreeInMapToDegreeInDP.put(nodeHere, new Pair<Integer, Integer>(degreeHere, degreeInDP));
        }
        sortedGraph = Graph.getSortedGraphPQ(g);
        Integer[] nodesInG =g.keySet().toArray(new Integer[]{});
        while(!sortedGraph.isEmpty()){
            Pair<Integer, HashSet<Integer>> here = sortedGraph.poll();
            int nodeHere = here.getKey();
            int degreeHere = g.get(nodeHere).size();
            int degreeInDP = nodeToDegreeInMapToDegreeInDP.get(nodeHere).getValue();
            if(degreeHere>=degreeInDP){
                continue;
            }
            
            for(int node :nodesInG){
                int nodeDegreeInG = nodeToDegreeInMapToDegreeInDP.get(node).getKey();
                int nodeDegreeInNewG = nodeToDegreeInMapToDegreeInDP.get(node).getValue();
                if(node!=nodeHere && !g.get(nodeHere).contains(node) && nodeDegreeInG<nodeDegreeInNewG){
                    g.get(nodeHere).add(node);
                    g.get(node).add(nodeHere);
                    nodeToDegreeInMapToDegreeInDP.put(node, new Pair<Integer, Integer>(nodeDegreeInG+1,nodeDegreeInNewG));
                    degreeHere++;
                    if(degreeHere==degreeInDP){
                        break;
                    }
                }
            }
        }
       //System.out.println(newDegreeSeq);
        /*w.write("the graph \n");
        for(int n : g.keySet()){
            w.write(n+" "+g.get(n).size()+"\n");
        }
        w.write("###Distrb##");
        TreeMap<Integer, Integer> newDist = Graph.getDegreeDist(g);
        for(int n : newDist.keySet()){
            w.write(n+" "+newDist.get(n)+"\n");
        }
        w.flush();w.close();*/
    }

    
    private static ArrayList<Integer> getDegreeSeqFromDegreeFreq(
            HashMap<Integer, Integer> degreeFreqWithDP, int size) {
        Random rand = new Random();
        ArrayList<Integer> degreeArray = new ArrayList<Integer>();
        int dpCount = 0;
        for (int i : degreeFreqWithDP.keySet()) {
            dpCount += degreeFreqWithDP.get(i);
        }
        for (int degree : degreeFreqWithDP.keySet()) {
            int degreeFreq = degreeFreqWithDP.get(degree);
            double pOfDegree = ((double) degreeFreq) / dpCount;
            int newDegreeCount = (int) (pOfDegree * size);
            for (int i = 0; i < newDegreeCount; i++) {
                degreeArray.add(degree);
            }
        }
        while (degreeArray.size() > size) {
            int randPos = rand.nextInt(degreeArray.size());
            degreeArray.remove(randPos);
        }
        while (degreeArray.size() < size) {
            int randPos = rand.nextInt(degreeArray.size());
            degreeArray.add(degreeArray.get(randPos));
        }
        Collections.sort(degreeArray, Collections.reverseOrder());
        //System.out.println(degreeArray);
        return degreeArray;
    }

    

    public static HashMap<Integer, Integer> generateDegreeDistFromDP(
            HashMap<Integer, HashMap<Integer, Double>> dp, int totalNodes) {
        HashMap<Integer, Integer> multiDegreeToCount = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> degreeToCount = new HashMap<Integer, Integer>();
        for (int i : dp.keySet()) {
            for (int j : dp.get(i).keySet()) {
                int freq = Math.round(Math.round(dp.get(i).get(j)));
                int numOfDegreeI = freq;
                int numOfDegreeJ = freq;
                if (multiDegreeToCount.containsKey(i)) {
                    numOfDegreeI += multiDegreeToCount.get(i);
                }
                if (multiDegreeToCount.containsKey(j)) {
                    numOfDegreeJ += multiDegreeToCount.get(j);
                }
                if (i == j) {
                    if (multiDegreeToCount.containsKey(j)) {
                        numOfDegreeI = multiDegreeToCount.get(i) + freq + freq;
                    } else {
                        numOfDegreeI = freq + freq;
                    }
                    numOfDegreeJ = numOfDegreeI;
                }
                multiDegreeToCount.put(i, numOfDegreeI);
                multiDegreeToCount.put(j, numOfDegreeJ);
            }
        }
        for (int i : multiDegreeToCount.keySet()) {
            degreeToCount.put(i, multiDegreeToCount.get(i) / i);
        }
        return degreeToCount;
    }

    public static HashMap<Integer, Integer> generateDegreeDistFromJDD(
            HashMap<Integer, HashMap<Integer, Integer>> jdd) {
        HashMap<Integer, Integer> multiDegreeToCount = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> degreeToCount = new HashMap<Integer, Integer>();
        for (int i : jdd.keySet()) {
            for (int j : jdd.get(i).keySet()) {
                int freq = jdd.get(i).get(j);
                int numOfDegreeI = freq;
                int numOfDegreeJ = freq;
                if (multiDegreeToCount.containsKey(i)) {
                    numOfDegreeI += multiDegreeToCount.get(i);
                }
                if (multiDegreeToCount.containsKey(j)) {
                    numOfDegreeJ += multiDegreeToCount.get(j);
                }
                if (i == j) {
                    if (multiDegreeToCount.containsKey(j)) {
                        numOfDegreeI = multiDegreeToCount.get(i) + freq + freq;
                    } else {
                        numOfDegreeI = freq + freq;
                    }
                }
                multiDegreeToCount.put(j, numOfDegreeJ);
                multiDegreeToCount.put(i, numOfDegreeI);
            }
        }
        for (int i : multiDegreeToCount.keySet()) {
            if (multiDegreeToCount.get(i) % i != 0) {
                System.out.println();
            }
            degreeToCount.put(i, multiDegreeToCount.get(i) / i);
        }
        return degreeToCount;
    }

    public static PriorityQueue<Pair<Integer, Pair<Integer, Integer>>> pq(
            HashMap<Integer, HashMap<Integer, Integer>> jdd) {
        PriorityQueue<Pair<Integer, Pair<Integer, Integer>>> pq = new PriorityQueue<Pair<Integer, Pair<Integer, Integer>>>(
                10, new Comparator<Pair<Integer, Pair<Integer, Integer>>>() {
                    public int compare(
                            Pair<Integer, Pair<Integer, Integer>> o1,
                            Pair<Integer, Pair<Integer, Integer>> o2) {
                        int a = Math.max(o1.getKey(), o1.getValue().getKey());
                        int b = Math.max(o2.getKey(), o2.getValue().getKey());
                        if (a > b)
                            return -1;
                        if (a < b)
                            return 1;
                        if (a == b) {
                            int c = Math.min(o1.getKey(), o1.getValue()
                                    .getKey());
                            int d = Math.min(o2.getKey(), o2.getValue()
                                    .getKey());
                            if (c > d)
                                return -1;
                            if (c < d)
                                return 1;
                        }
                        return 0;
                    }
                });
        for (int i : jdd.keySet()) {
            for (int j : jdd.get(i).keySet()) {
                Pair<Integer, Integer> innerTemp = new Pair<Integer, Integer>(
                        j, jdd.get(i).get(j));
                Pair<Integer, Pair<Integer, Integer>> temp = new Pair<Integer, Pair<Integer, Integer>>(
                        i, innerTemp);
                pq.add(temp);
            }
        }
        return pq;
    }
}