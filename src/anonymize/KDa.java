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
import java.util.Random;
import java.util.TreeMap;

import common.Graph;
import common.Pair;
import common.greedyDegreeAnonymization;

public class KDa {
    public static void main(String[] args) throws NumberFormatException, IOException {
        kDa(args[0], args[1], Integer.parseInt(args[0]));
    }
    public static void kDa(String in,String out,int k) throws IOException {
        HashMap<Integer, HashSet<Integer>> graph = Graph.readUndirectedGraph(in);

        HashMap<Integer, HashSet<Integer>> ng = priority(graph, k);
        Graph.writeGraph(ng, out);
    }
    public static HashMap<Integer,HashSet<Integer>> priority(HashMap<Integer,HashSet<Integer>> graph, int k){
        ArrayList<Pair<Integer, Integer>> nodeToDegree = new ArrayList<Pair<Integer,Integer>>();
        HashMap<Integer,HashSet<Integer>> newGraph = null;
        HashMap<Integer,Integer> nodeToId = new HashMap<Integer, Integer>();
        graphToNodeDegreeMap(graph, nodeToDegree);
        sortNodeToDegree(nodeToDegree,nodeToId);
        boolean firstThrough = true;
        Random rand = new Random();
        System.out.println("here");
        while (newGraph == null) {    
            if(!firstThrough){
                probe(nodeToDegree,nodeToDegree,nodeToId);
            }
            firstThrough=false;
            int[] newDeg = greedyDegreeAnonymization.greedy(nodeToDegree, k);
            /*String nodID ="";
            String nodeDegree = "";
            for(Pair<Integer, Integer> here : nodeToDegree){
                nodID+=here.getKey().toString()+"\t";
                nodeDegree+=here.getValue().toString()+"\t";
            }
            System.out.println(nodID);
            System.out.println(nodeDegree);
            HashMap<Integer, Integer> newDegFreq = new HashMap<Integer, Integer>();
            for(int i : newDeg){
                System.out.print(i+"\t");
                int deg = 1;
                if(newDegFreq.containsKey(i)){
                    deg+=newDegFreq.get(i);
                }
                newDegFreq.put(i, deg);
            }
            System.out.println();
            for(int i : newDegFreq.keySet()){
                System.out.println(i+" "+newDegFreq.get(i));
            }*/
            int sumOfDegree = 0;
            for (int i : newDeg) {
                sumOfDegree += i;
            }
            if (sumOfDegree % 2 != 0) {
                newGraph = null;
                continue;
            }
            newGraph = new HashMap<Integer, HashSet<Integer>>();
            for (int i : graph.keySet()) {
                newGraph.put(i, new HashSet<Integer>());
            }
            TreeMap<Integer, Integer> newDegDis = new TreeMap<Integer, Integer>();
            for(int i : newDeg){
                int numAtDegI = 1;
                if(newDegDis.containsKey(i)){
                    numAtDegI+=newDegDis.get(i);
                }
                newDegDis.put(i, numAtDegI);
            }
            System.out.println("newDegree Distrub ");
            for(int i : newDegDis.descendingKeySet()){
                System.out.println(i+" "+newDegDis.get(i));
            }
            while(true){
                boolean allDegPostive = allDegreesPostive(newDeg);
                if(!allDegPostive){newGraph = null;break;}
                boolean allDegZero = allDegreesZero(newDeg);
                if(allDegZero){return newGraph;}
               
                int numEdgeNeeded = 0;
                int nodePos=-1;
                while(numEdgeNeeded == 0){
                    nodePos = rand.nextInt(newDeg.length);
                    numEdgeNeeded = newDeg[nodePos];
                }
                int nodeID =nodeToDegree.get(nodePos).getKey();
                newDeg[nodePos]=0;
                HashSet<Integer> candidateSet = new HashSet<Integer>();
                for(int i : graph.get(nodeID)){
                    if(newDeg[nodeToId.get(i)]!=0 &&  !newGraph.get(nodeID).contains(i)){
                        candidateSet.add(i);
                    }
                }
                
                int extraEdgeNeeded=numEdgeNeeded-candidateSet.size();
                int position = 0;
                while(extraEdgeNeeded>0){
                    if (newDeg[position] > 0
                            && position != nodePos
                            && !candidateSet.contains(nodeToDegree
                                    .get(position).getKey())) {
                        candidateSet.add(nodeToDegree.get(position).getKey());
                        extraEdgeNeeded--;
                    }
                    position++;
                    if(position==newDeg.length){
                        break;
                    }
                }
                if(candidateSet.size()<numEdgeNeeded){
                    System.out.println("a");
                }
                for(int i : candidateSet){
                    newGraph.get(nodeID).add(i);
                    newGraph.get(i).add(nodeID);
                    newDeg[nodeToId.get(i)]--;
                    numEdgeNeeded--;
                    if(numEdgeNeeded==0){break;}
                    //System.out.println("from node add "+nodeID+" removed one from node i= "+i+" and index of"+nodeToId.get(i));
                }
            }
        }
        return null;
    }

    private static boolean allDegreesZero(int[] newDeg) {
        boolean allDegZero=true;
        for (int i : newDeg) {
            if(i!=0){
                allDegZero = false;
            }
        }
        return allDegZero;
    }

    private static boolean allDegreesPostive(int[] newDeg) {
        boolean allDegPos=true;
        for (int i : newDeg) {
            if(i<0){
                allDegPos = false;
            }
        }
        return allDegPos;
    }

    private static void probe(ArrayList<Pair<Integer, Integer>> nodeToDegree, ArrayList<Pair<Integer, Integer>> nodeToDegree2, HashMap<Integer, Integer> nodeToId) {
        Random rand = new Random();
        int nodeToAdjPos = nodeToDegree.size()/2+rand.nextInt(nodeToDegree.size()/2);
        int newDegree = nodeToDegree.get(nodeToAdjPos).getValue()+rand.nextInt(5)+1;
        nodeToDegree.get(nodeToAdjPos).setValue(newDegree);
        sortNodeToDegree(nodeToDegree,nodeToId);
    }

    private static void sortNodeToDegree(
            ArrayList<Pair<Integer, Integer>> nodeToDegree, HashMap<Integer, Integer> nodeToId) {
        Collections.sort(nodeToDegree, new Comparator<Pair<Integer,Integer>>(){
            public int compare(Pair<Integer, Integer> p0, Pair<Integer, Integer> p1) {
                return p1.getValue()-p0.getValue();
            }
        });
        nodeToId.clear();
        int index = 0;
        for(Pair<Integer, Integer> here : nodeToDegree){
            nodeToId.put(here.getKey(), index);
            index++;
        }
    }

    private static void graphToNodeDegreeMap(
            HashMap<Integer, HashSet<Integer>> graph,
            ArrayList<Pair<Integer, Integer>> nodeToDegree) {
        for(int i : graph.keySet()) {
            nodeToDegree.add(new Pair<Integer, Integer>(i, graph.get(i).size()));
        }
    }
}
