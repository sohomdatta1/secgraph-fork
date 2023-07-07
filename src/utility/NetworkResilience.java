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
package utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import common.Graph;
import common.Pair;
import common.Utilities;

public class NetworkResilience {
    public static ArrayList<Integer> getNetworkResilience(HashMap<Integer, HashSet<Integer>> g ,int limit) throws IOException, InterruptedException{
        ArrayList<Integer> res = new ArrayList<Integer>();
        PriorityQueue<Pair<Integer, HashSet<Integer>>> pq = Graph.getSortedGraphPQ(g);
        int tried = 0;
        while(!pq.isEmpty()){
            Pair<Integer, HashSet<Integer>> here = pq.poll();
            g.remove(here.getKey());
            for(int i : g.keySet()){
                g.get(i).remove(here.getKey());
            }
            int size = getLargestConnectCompSize(g);
            if(size==0){size=1;}
            res.add(size);
            tried++;
            //System.out.println(tried);
            if(tried>limit){
                break;
            }
        }
        return res;
    }
    private static int getLargestConnectCompSize(
            HashMap<Integer, HashSet<Integer>> g) {
        LinkedList<HashSet<Integer>> listOfConnectComp = new LinkedList<HashSet<Integer>>();
        HashSet<Integer> visitedNodes = new HashSet<Integer>();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        while(visitedNodes.size()!=g.size()){
            int i = 0;
            int start = allNodes[i];
            while(visitedNodes.contains(start)){
                i++;
                start = allNodes[i];
            }
            HashSet<Integer> spanNodes = span(g,start);
            visitedNodes.addAll(spanNodes);
            listOfConnectComp.add(spanNodes);
        }
        int largestSize = 0;
        for(int i = 0;i<listOfConnectComp.size();i++){
            if(listOfConnectComp.get(i).size()>largestSize){
                largestSize=listOfConnectComp.get(i).size();
            }
        }
        return largestSize;
    }
    private static HashSet<Integer> span(HashMap<Integer, HashSet<Integer>> g,
            int start) {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(start);
        HashSet<Integer> visited = new HashSet<Integer>();
        visited.add(start);
        while(!queue.isEmpty()){
            int here = queue.poll();
            for(int i : g.get(here)){
                if(!visited.contains(i)){
                    queue.add(i);
                    visited.add(i);
                }
            }
        }
        return visited;
    }
    public static double GetNR(String G1,String G2,String Limit) throws IOException, InterruptedException {
        ArrayList<Integer> g1=getNetworkResilience(Graph.readUndirectedGraph(G1),Integer.parseInt(Limit));
        ArrayList<Integer> g2=getNetworkResilience(Graph.readUndirectedGraph(G2),Integer.parseInt(Limit));
        return Utilities.cosSimInt(g1, g2);
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(GetNR(args[0], args[1], args[2]));;
    }
}
