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
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class TmeanClustering {
    public static void main(String[] args) throws IOException {
        HashMap<Integer,HashSet<Integer>> graph = Graph.readUndirectedGraph("graphs/Email-Enron.txt");
        ArrayList<ArrayList<Integer>> clusters = TmeanClustering.tMeanClustering(5, graph, true);
        System.out.println("graph size "+graph.size());
        for(ArrayList<Integer> i : clusters){
            System.out.println("cluster of size "+i.size()+" "+i);
        }
    }
    public static ArrayList<ArrayList<Integer>> tMeanClustering(int k,
            Map<Integer, HashSet<Integer>> graph, boolean trueForZeroHop) {
        ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>(67000);
        ArrayList<ClusterCenter> clusterCenters = new ArrayList<ClusterCenter>(67000);
        HashSet<Integer> nodesInClusterCenter = new HashSet<Integer>(67000);
        Random rand = new Random();
        int t = graph.size() / k;
        int extraVerticiesStarts = graph.size() - graph.size() % k;
        Integer[] nodes = graph.keySet().toArray(new Integer[] {});
        for (int i = 0; i < t; i++) {
            int cc;
            do {
                cc = nodes[rand.nextInt(nodes.length)];
            } while (nodesInClusterCenter.contains(cc));
            nodesInClusterCenter.add(cc);
            int ccDegree = graph.get(cc).size();
            ArrayList<Integer> ccNeiDegree = new ArrayList<Integer>(6000);
            for (int here : graph.get(cc)) {
                ccNeiDegree.add(graph.get(here).size());
            }
            clusterCenters.add(new ClusterCenter(i, ccDegree, ccNeiDegree));
        }
        boolean clusterCenterIsTheSame;
        do {
            clusters.clear();
            for (int i = 0; i < t; i++) {
                clusters.add(new ArrayList<Integer>(10000));
            }
            int numVisitedNode = 0;
            for (int node : graph.keySet()) {
                int lowestDistance = Integer.MAX_VALUE;
                ClusterCenter lowestCluster = null;
                numVisitedNode++;
                for (ClusterCenter cc : clusterCenters) {
                    int dist = Graph.getDistance(graph, node, cc.getDegree(),cc.getNeiborDegrees(), trueForZeroHop);
                    if (dist < lowestDistance) {
                        lowestDistance = dist;
                        lowestCluster = cc;
                    }
                }
                clusters.get(lowestCluster.getId()).add(node);
                if (clusters.get(lowestCluster.getId()).size() > k && numVisitedNode <= extraVerticiesStarts) {
                    int minDistance = Integer.MAX_VALUE;
                    ClusterCenter mindistCluster = null;
                    for (ClusterCenter c : clusterCenters) {
                        if (c.getId() == lowestCluster.getId()) {
                            continue;
                        }
                        if (clusters.get(c.getId()).size() >= k) {
                            continue;
                        }
                        int dist = ClusterCenter.getDistance(lowestCluster, c,trueForZeroHop);
                        if (dist < minDistance) {
                            minDistance = dist;
                            mindistCluster = c;
                        }
                    }
                    int minElement = -1, minScore = Integer.MAX_VALUE;
                    for (int i : clusters.get(lowestCluster.getId())) {
                        int dist = Math.abs(Graph.getDistance(graph, i,lowestCluster.getDegree(),  lowestCluster.getNeiborDegrees(), trueForZeroHop)
                                          - Graph.getDistance(graph, i,mindistCluster.getDegree(),mindistCluster.getNeiborDegrees(), trueForZeroHop));
                        if (dist < minScore) {
                            minScore = dist;
                            minElement = i;
                        }
                    }
                    clusters.get(lowestCluster.getId()).remove((Integer) minElement);
                    clusters.get(mindistCluster.getId()).add(minElement);
                }
            }
            ArrayList<ClusterCenter> newClusterCenters = new ArrayList<ClusterCenter>();
            for(int i =0;i<clusters.size();i++){
                newClusterCenters.add(ClusterCenter.getClusterCenter(graph,clusters.get(i),i));
            }
            clusterCenterIsTheSame=true;
            for(int i = 0;i<clusterCenters.size();i++){
                if(!newClusterCenters.get(i).equals(clusterCenters.get(i))){
                    clusterCenterIsTheSame=false;
                }
            }
            clusterCenters=newClusterCenters;
        } while (clusterCenterIsTheSame);
        return clusters;
    }
}
