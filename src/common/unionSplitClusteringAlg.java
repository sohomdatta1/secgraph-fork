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
package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class unionSplitClusteringAlg {
    public static void main(String[] args) throws IOException {
        HashMap<Integer,HashSet<Integer>> graph = Graph.readUndirectedGraph("G");
        System.out.println(graph.size());
        ArrayList<HashSet<Integer>> list = unionSplitClustering(5, graph, true);
        int i = 0;
        for(HashSet<Integer> l : list){
            System.out.println((i++)+" : "+l.size()+" : "+l);
        }
    }
    public static ArrayList<HashSet<Integer>> unionSplitClustering(int k,
            HashMap<Integer, HashSet<Integer>> g, boolean trueForZeroHop){
        ArrayList<HashSet<Integer>> listOfNodesInCluster =new  ArrayList<HashSet<Integer>>();
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        Random rand = new Random();
        for(int a : g.keySet()){
            Cluster tmp = new Cluster();
            ArrayList<Integer> neiDegree = new ArrayList<Integer>();
            for(int b : g.get(a)){
                neiDegree.add(g.get(b).size());
            }
            tmp.setCenter(new ClusterCenter(a, g.get(a).size(), neiDegree));
            HashSet<Integer> nodesInCluster = new HashSet<Integer>();
            nodesInCluster.add(a);
            tmp.setNodesInCluster(nodesInCluster);
            clusters.add(tmp);
        }
        for(Cluster a : clusters){
            for(Cluster b : clusters){
                if(a.getCenter().getId()==b.getCenter().getId()){
                    continue;
                }
                a.getNearestNeighbors().add(b);
            }
        }
        boolean allClusterLargerThanK=false;
        while(!allClusterLargerThanK){
            allClusterLargerThanK=true;
            Cluster randomCluster = clusters.get(rand.nextInt(clusters.size()));
            while(randomCluster.getNodesInCluster().size()>k){
                randomCluster = clusters.get(rand.nextInt(clusters.size()));
            }
            Cluster nearestCluster = randomCluster.getNearestNeighbors().poll();
            
            clusters.remove(randomCluster);
            clusters.remove(nearestCluster);
            for(Cluster here : clusters){
                here.getNearestNeighbors().remove(nearestCluster);
                here.getNearestNeighbors().remove(randomCluster);
            }
            if(randomCluster.getNodesInCluster().size()
                    + nearestCluster.getNodesInCluster().size()<k){
                allClusterLargerThanK=false;
            }
            if (randomCluster.getNodesInCluster().size()
                    + nearestCluster.getNodesInCluster().size() < 2 * k) {
                randomCluster.combine(nearestCluster);
                randomCluster.setCenter(ClusterCenter.getClusterCenter(g,
                        randomCluster.getNodesInCluster(), randomCluster.getCenter().getId()));
                randomCluster.getNearestNeighbors().clear();
                for (Cluster here : clusters) {
                    if (here.getNodesInCluster().size() < k) {
                        allClusterLargerThanK = false;
                    }
                   here.getNearestNeighbors().add(randomCluster);
                   randomCluster.getNearestNeighbors().add(here);
                }
                clusters.add(randomCluster);
            }
            else{
                ArrayList<Integer> nodesToSplit = new ArrayList<Integer>();
                nodesToSplit.addAll(randomCluster.getNodesInCluster());
                nodesToSplit.addAll(nearestCluster.getNodesInCluster());
                int maxDistance = -1;
                int nodeI = nodesToSplit.get(0);
                int nodeJ = nodesToSplit.get(1);
                for(int i =0; i < nodesToSplit.size()-1;i++){
                    for(int j =i+1; j < nodesToSplit.size()-1;j++){
                        int distance =Graph.getDistance(g, nodesToSplit.get(i), nodesToSplit.get(j), trueForZeroHop);
                        if(maxDistance<distance){
                            maxDistance=distance;
                            nodeI=nodesToSplit.get(i);
                            nodeJ=nodesToSplit.get(j);
                        }
                    }
                }
                HashSet<Integer> setI = new HashSet<Integer>();
                HashSet<Integer> setJ = new HashSet<Integer>();
                setI.add(nodeI);
                setJ.add(nodeJ);
                for(int i : nodesToSplit){
                    if(i==nodeI || i==nodeJ){
                        continue;
                    }
                    int disToI = Graph.getDistance(g, i, nodeI, trueForZeroHop);
                    int disToJ = Graph.getDistance(g, i, nodeJ, trueForZeroHop);
                    if(disToI>disToJ){
                        setJ.add(i);
                        if(setJ.size()>k){
                            int nodeToBump = findNodeToBump(setJ,g,nodeI,trueForZeroHop,nodeJ);
                            setJ.remove((Integer) nodeToBump);
                            setI.add(nodeToBump);
                        }
                    }else{
                        setI.add(i);
                        if(setI.size()>k){
                            int nodeToBump = findNodeToBump(setI,g,nodeI,trueForZeroHop,nodeJ);
                            setI.remove((Integer) nodeToBump);
                            setJ.add(nodeToBump);
                        }
                    }
                }
                Cluster a = new Cluster();
                a.setCenter(ClusterCenter.getClusterCenter(g, setI, randomCluster.getCenter().getId()));
                a.setNodesInCluster(setI);
                Cluster b = new Cluster();
                b.setCenter(ClusterCenter.getClusterCenter(g, setJ, nearestCluster.getCenter().getId()));
                b.setNodesInCluster(setJ);
                for (Cluster here : clusters) {
                    if (here.getNodesInCluster().size() < k) {
                        allClusterLargerThanK = false;
                    }
                    here.getNearestNeighbors().add(a);
                    here.getNearestNeighbors().add(b);
                    a.getNearestNeighbors().add(here);
                    b.getNearestNeighbors().add(here);
                }
                a.getNearestNeighbors().add(b);
                b.getNearestNeighbors().add(a);
                clusters.add(a);
                clusters.add(b);
            }
        }
        for(Cluster c : clusters){
            listOfNodesInCluster.add(c.getNodesInCluster());
        }
        return listOfNodesInCluster;
    }
    
    private static int findNodeToBump(HashSet<Integer> set, HashMap<Integer, HashSet<Integer>> g, int nodeI, boolean trueForZeroHop, int nodeJ) {
        int lowestMarginCost = Integer.MAX_VALUE;
        int nodeWithLowestMarginCost = 0;
        for(int n : set){
            int marginCost = Math.abs(Graph.getDistance(g, n, nodeI, trueForZeroHop)-Graph.getDistance(g, n, nodeJ, trueForZeroHop));
            if(marginCost < lowestMarginCost){
                lowestMarginCost=marginCost;
                nodeWithLowestMarginCost=n;
            }
        }
        return nodeWithLowestMarginCost;
    }

   
}
