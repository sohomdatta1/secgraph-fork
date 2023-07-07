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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import common.Graph;
import common.TmeanClustering;

public class BoundedTMean {
    public static void main(String[] args) throws NumberFormatException, IOException {
        tMean(args[0], args[1], Integer.parseInt(args[2]));
    }
    public static void tMean(String in,String out,int i) throws IOException {
        HashMap<Integer, HashSet<Integer>> graph = Graph.readUndirectedGraph(in);
        BoundedTMean.interClusterMatching(graph, i);
        Graph.writeGraph(graph, out);
    }
    
    public static void interClusterMatching( Map<Integer, HashSet<Integer>> g,int k){
        HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>(g);
        ArrayList<ArrayList<Integer>> clusters = TmeanClustering.tMeanClustering(k, graph, true);
       // System.out.println("here");
       /* for(ArrayList<Integer> i : clusters){
            System.out.println((counter++)+" "+i);
        }*/
       // System.out.println("\n");
        LinkedList<Integer> avgDegreeOfClusters = new LinkedList<Integer>();
        Random rand = new Random();
        for(ArrayList<Integer> i : clusters){
            int sumOfDegree = 0;
            for(int j : i){
                sumOfDegree+=graph.get(j).size();
            }
            avgDegreeOfClusters.add(sumOfDegree/i.size());
            //System.out.println((counter++)+" "+sumOfDegree/i.size());
        }
        
        HashMap<Integer, Integer> nodeToAdjSize = new HashMap<Integer, Integer>();
        for(int i =0;i< clusters.size();i++){
            for(int j : clusters.get(i)){
                nodeToAdjSize.put(j, avgDegreeOfClusters.get(i)-graph.get(j).size());
            }
        }
        System.out.println("a");
        HashSet<Integer> allNodes = new HashSet<Integer>(graph.keySet());
        for(int i : allNodes){
            int adjSize=nodeToAdjSize.get(i);
            if(adjSize==0){
                continue;
            }else if(adjSize<0){
                HashSet<Integer> nodesToRemove = new HashSet<Integer>();
                HashSet<Integer> NeiOfI = new HashSet<Integer>(graph.get(i));
                for(int j : NeiOfI){
                    int neiAdjSize= nodeToAdjSize.get(j);
                    if(neiAdjSize<0){
                        adjSize++;
                        neiAdjSize++;
                        nodeToAdjSize.put(j, neiAdjSize);
                        nodeToAdjSize.put(i, adjSize);
                        graph.get(j).remove(i);
                        nodesToRemove.add(j);
                        if(adjSize==0){
                            break;
                        }
                    }
                }
                graph.get(i).removeAll(nodesToRemove);
                if(adjSize<0){
                    Integer[] neigh = graph.get(i).toArray(new Integer[]{});
                    for(int l = 0;l< adjSize;l++){
                        int toRemove =neigh[rand.nextInt(neigh.length)];
                        graph.get(toRemove).remove(i);
                        graph.get(i).remove(toRemove);
                    }
                    nodeToAdjSize.put(i, 0);
                }
            }else if(adjSize>0){
                while (adjSize > 0) {
                    int nodeNeedsEdge = findNodeNeedsEdge(graph, i,nodeToAdjSize);
                    if (nodeNeedsEdge == -1 && !graph.containsKey(-1)) {
                        graph.put(-1, new HashSet<Integer>());
                    }
                    if(nodeNeedsEdge==-1){
                        nodeToAdjSize.put(-1, 1);
                    }
                    graph.get(nodeNeedsEdge).add(i);
                    graph.get(i).add(nodeNeedsEdge);
                    adjSize--;
                    int newAdjSize = nodeToAdjSize.get(nodeNeedsEdge)-1;
                    nodeToAdjSize.put(nodeNeedsEdge, newAdjSize);
                }
                nodeToAdjSize.put(i, adjSize);
            }
        }
    }

    private static int findNodeNeedsEdge(
            HashMap<Integer, HashSet<Integer>> graph, int here,
            HashMap<Integer, Integer> nodeToAdjSize) {

        for(int i : graph.keySet()){
            if(i!=here &&1!=-1&& nodeToAdjSize.get(i)>0){
                return i;
            }
        }
        return -1;
    }
    
   
}
