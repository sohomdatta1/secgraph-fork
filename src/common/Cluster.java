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

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Cluster{
    ClusterCenter center;
    HashSet<Integer> nodesInCluster;
    PriorityQueue<Cluster> nearestNeighbors;
    boolean zeroHop;
    public Cluster() {
        zeroHop=true;
        center=null;
        nodesInCluster=new HashSet<Integer>(38000);
        nearestNeighbors=new PriorityQueue<Cluster>(38000,new Comparator<Cluster>() {
            public int compare(Cluster o1, Cluster o2) {
                return ClusterCenter.getDistance(getCenter(), o1.getCenter(),zeroHop)-
                        ClusterCenter.getDistance(getCenter(), o2.getCenter(),zeroHop);
            }
        });
    }
    public ClusterCenter getCenter() {
        return center;
    }
    public void setCenter(ClusterCenter center) {
        this.center = center;
    }
    public HashSet<Integer> getNodesInCluster() {
        return nodesInCluster;
    }
    public void setNodesInCluster(HashSet<Integer> nodesInCluster) {
        this.nodesInCluster = nodesInCluster;
    }
    public PriorityQueue<Cluster> getNearestNeighbors() {
        return nearestNeighbors;
    }
    public void setNearestNeighbors(PriorityQueue<Cluster> nearestNeighbors) {
        this.nearestNeighbors = nearestNeighbors;
    }
   
    public String toString(){
        return center.getId()+"";
    }
    public void combine(Cluster nearestCluster) {
        this.getNodesInCluster().addAll(nearestCluster.nodesInCluster);
    }
    
    public boolean equals(Cluster o){
        return center.getId()==o.getCenter().getId();
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof Cluster){
            Cluster c = (Cluster) o;
            return this.equals(c);
        }
        return false;
    }
    public static Cluster getClusterWithID(int id, LinkedList<Cluster> clusters){
        for(Cluster c : clusters){
            if(c.getCenter().getId()==id){
                return c;
            }
        }
        return null;
    }
}
