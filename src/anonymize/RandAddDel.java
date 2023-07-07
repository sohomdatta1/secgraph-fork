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
import java.util.Random;

import common.Graph;

public class RandAddDel {
    public static void main(String[] args) throws IOException {
        args=new String[]{"exampleWithIDRemoved","exampleRandSW","0.25"};
        randAddAndDeleteEdges(args[0], args[1], Double.parseDouble(args[2]));
    }
    
    public static void randAddAndDeleteEdges(String graphIn,String graphOut,double k) throws IOException{
        HashMap<Integer,HashSet<Integer>> g = Graph.readUndirectedGraph(graphIn);
        int numChange = (int) (Graph.getNumberOfEdges(g)*k);
        randAddAndDeleteEdges(numChange, g);
        Graph.writeGraph(g, graphOut);
    }
    public static void randAddAndDeleteEdges(int k, HashMap<Integer, HashSet<Integer>> graph){
        LinkedList<LinkedList<Integer>> edgeList = new LinkedList<LinkedList<Integer>>();
        Random rand = new Random();
        for(int i : graph.keySet()){
            for(int j : graph.get(i)){
                if(i>j){
                    continue;
                }
                LinkedList<Integer> tmp = new LinkedList<Integer>();
                tmp.add(i);tmp.add(j);
                edgeList.add(tmp);
            }
        }
        Integer[] nodeList = graph.keySet().toArray(new Integer[]{});
        int start = Graph.findLargestNode(graph);
        HashMap<Integer, HashSet<Integer>> spanningTree = new HashMap<Integer, HashSet<Integer>>();
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.addFirst(start);
        while(!queue.isEmpty()){
            int here = queue.pollLast();
            for(int i : graph.get(here)){
                if(!spanningTree.containsKey(here)){
                    spanningTree.put(here, new HashSet<Integer>());
                }
                if(!spanningTree.containsKey(i)){
                    spanningTree.get(here).add(i);
                    spanningTree.put(i, new HashSet<Integer>());
                    spanningTree.get(i).add(here);
                    queue.add(i);
                }
            }
        }
        System.out.println(k);
        for(int i = 0;i<k;i++){
            if(i%10000==0){
                System.out.println(i);
            }
            int randPos;
            int oldNodeA,oldNodeB;
            int c = 0;
            do{
                randPos = rand.nextInt(edgeList.size());
                LinkedList<Integer> edge = edgeList.get(randPos);
                c++;
                if(c>=2*edgeList.size()){
                    System.out.println("tried all edges");
                    System.exit(0);
                }
                oldNodeA =edge.get(0);
                oldNodeB = edge.get(1);
            }while(!graph.get(oldNodeA).contains(oldNodeB) || 
                    (spanningTree.containsKey(oldNodeA) && spanningTree.get(oldNodeA).contains(oldNodeB))||
                    (spanningTree.containsKey(oldNodeB) && spanningTree.get(oldNodeB).contains(oldNodeA)));
            int newNodeA, newNodeB;
            do{
                newNodeA=nodeList[rand.nextInt(nodeList.length)];
                newNodeB=nodeList[rand.nextInt(nodeList.length)];
            }while(newNodeA==newNodeB||graph.get(newNodeA).contains(newNodeB));
            graph.get(oldNodeA).remove(oldNodeB);
            graph.get(oldNodeB).remove(oldNodeA);
            graph.get(newNodeA).add(newNodeB);
            graph.get(newNodeB).add(newNodeA);
        }
    }
}
