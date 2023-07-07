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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import common.Graph;

public class ConnectDisconnectedCompoents {
    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("usage inputFileName outputFileName");
            System.exit(0);
        }
        String inputFile = args[0];
        String outputFile = args[1];
        HashMap<Integer, HashSet<Integer>>  g = Graph.readUndirectedGraph(inputFile);
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
        for(HashSet<Integer> i : listOfConnectComp){
            System.out.println(i.size());
        }
        //System.out.println(listOfConnectComp.size());
        int largestSize = 0;
        int largestPos = -1;
        for(int i = 0;i<listOfConnectComp.size();i++){
            if(listOfConnectComp.get(i).size()>largestSize){
                largestPos= i;
                largestSize=listOfConnectComp.get(i).size();
            }
        }
        int largNode = largestNodeInSet(g,listOfConnectComp.get(largestPos));
        for(int i = 0;i<listOfConnectComp.size();i++){
            if(i==largestPos){
                continue;
            }
            int n = largestNodeInSet(g,listOfConnectComp.get(i));
            g.get(n).add(largNode);
            g.get(largNode).add(n);
        }
        Graph.writeGraph(g, outputFile);
    }

    private static int largestNodeInSet(HashMap<Integer, HashSet<Integer>> g, HashSet<Integer> set) {
        int largestNode = -1;
        int largestSize = -1;
        for(int i : set){
            if(g.get(i).size()>largestSize){
                largestNode=i;
                largestSize=g.get(i).size();
            }
        }
        return largestNode;
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
}
