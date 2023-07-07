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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClusterCenter{
    int id;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public ArrayList<Integer> getNeiborDegrees() {
        return neiborDegrees;
    }

    public void setNeiborDegrees(ArrayList<Integer> neiborDegrees) {
        this.neiborDegrees = neiborDegrees;
    }

    int degree;
    ArrayList<Integer> neiborDegrees;
    public ClusterCenter(int id, int d ,ArrayList<Integer> n){
        this.id=id;
        degree=d;
        neiborDegrees=n;
    }
    
    public static int getDistance(ClusterCenter u, ClusterCenter v, boolean zeroHopFlag){
        int numUNei = u.neiborDegrees.size();
        int numVNei = v.neiborDegrees.size();
        int zeroHop = Math.abs(numUNei - numVNei);
        if(zeroHopFlag){
            return zeroHop;
        }
        int numInNeiDegreeSet = Math.max(numUNei, numVNei);
        
        ArrayList<Integer> uNeiDegree = new ArrayList<Integer>();
        ArrayList<Integer> vNeiDegree = new ArrayList<Integer>();
        for(int i = 0;i<numInNeiDegreeSet;i++){
            if(u.neiborDegrees.size()>i){
                uNeiDegree.add(u.neiborDegrees.get(i));
            }else{
                uNeiDegree.add(0);
            }
            if(v.neiborDegrees.size()>i){
                vNeiDegree.add(v.neiborDegrees.get(i));
            }else{
                vNeiDegree.add(0);
            }
        }
        Collections.sort(uNeiDegree);
        Collections.sort(vNeiDegree);
        int oneHop=zeroHop;
        for(int i = 0;i< uNeiDegree.size();i++){
            oneHop+=Math.abs(uNeiDegree.get(i)-vNeiDegree.get(i));
        }
        return oneHop;
    }
    
    public boolean equals(ClusterCenter o) {
        boolean eq = false;
        if(id==o.id && degree==o.degree && neiborDegrees.equals(o.neiborDegrees)){
            eq=true;
        }
        return eq;
    }
    public static ClusterCenter getClusterCenter(
            Map<Integer, HashSet<Integer>> graph, ArrayList<Integer> here, int id) {
        int totalNeiDegree=0;
        for(int i : here){
            totalNeiDegree+=graph.get(i).size();
        }
        int clusterDegree = totalNeiDegree/here.size();
        HashMap<Integer, ArrayList<Integer>> oneHopNeiDegree = new HashMap<Integer, ArrayList<Integer>>();
        for(int i : here){
            oneHopNeiDegree.put(i, new ArrayList<Integer>());
            for(int j : graph.get(i)){
                oneHopNeiDegree.get(i).add(graph.get(j).size());
            }
        }
        ArrayList<Integer> neiDegrees = new ArrayList<Integer>();
        for(int i = 0;i< clusterDegree;i++){
            int mostCommonDegree = mostCommonDegree(oneHopNeiDegree);
            for(int j : oneHopNeiDegree.keySet()){
                oneHopNeiDegree.get(j).remove((Integer) mostCommonDegree);
            }
            neiDegrees.add(mostCommonDegree);
        }
        ClusterCenter ret = new ClusterCenter(id,clusterDegree, neiDegrees);
        return ret;
    }
    public static ClusterCenter getClusterCenter(
            Map<Integer, HashSet<Integer>> graph, HashSet<Integer> here, int id) {
        int totalNeiDegree=0;
        for(int i : here){
            totalNeiDegree+=graph.get(i).size();
        }
        int clusterDegree = totalNeiDegree/here.size();
        HashMap<Integer, ArrayList<Integer>> oneHopNeiDegree = new HashMap<Integer, ArrayList<Integer>>();
        for(int i : here){
            oneHopNeiDegree.put(i, new ArrayList<Integer>());
            for(int j : graph.get(i)){
                oneHopNeiDegree.get(i).add(graph.get(j).size());
            }
        }
        ArrayList<Integer> neiDegrees = new ArrayList<Integer>();
        for(int i = 0;i< clusterDegree;i++){
            int mostCommonDegree = mostCommonDegree(oneHopNeiDegree);
            for(int j : oneHopNeiDegree.keySet()){
                oneHopNeiDegree.get(j).remove((Integer) mostCommonDegree);
            }
            neiDegrees.add(mostCommonDegree);
        }
        ClusterCenter ret = new ClusterCenter(id,clusterDegree, neiDegrees);
        return ret;
    }
    private static int mostCommonDegree(
            HashMap<Integer, ArrayList<Integer>> oneHopNeiDegree) {
        HashMap<Integer, Integer> freq = new HashMap<Integer, Integer>();
        for(int i : oneHopNeiDegree.keySet()){
            for(int j : oneHopNeiDegree.get(i)){
                int f = 1;
                if(freq.containsKey(j)){
                    f+=freq.get(j);
                }
                freq.put(j, f);
            }
        }
        int mostCommonDegree=0, numOfDegree = 0;
        for(int i : freq.keySet()){
            if(numOfDegree<freq.get(i)){
                numOfDegree=freq.get(i);
                mostCommonDegree=i;
            }
        }
        return mostCommonDegree;
    }
}
