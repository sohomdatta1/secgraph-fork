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
import java.util.Random;

import common.Graph;

public class RandSw {
    public static void main(String[] args) throws NumberFormatException, IOException {
        
        RandSwitch(args[0], args[1], Double.parseDouble(args[2]));
    }
    public static void RandSwitch(String in, String out, double k) throws IOException {
        System.out.println(in+" "+out+" "+k);
        HashMap<Integer,HashSet<Integer>> graph = Graph.readUndirectedGraph(in);
        double fractionEdgeToSw = k;
        int numEdgeToSw = (int)(fractionEdgeToSw*graph.keySet().size());
        System.out.println(numEdgeToSw);
        RandSw.RandSwitch(numEdgeToSw, graph);
        Graph.writeGraph(graph, out);
    }
    public static void RandSwitch(int k, HashMap<Integer, HashSet<Integer>> graph){
        /*int numEdges = 0;
        for(int n: graph.keySet()){
            for(int z : graph.get(n)){
                if(n<z){
                    numEdges++;
                }
            }
        }*/
        HashMap<Integer, HashSet<Integer>> originalGraph = new HashMap<Integer, HashSet<Integer>>();
        int max = originalGraph.size()*(originalGraph.size()-1)/2;
        for(int z : graph.keySet()){
            originalGraph.put(z, new HashSet<Integer>(graph.get(z)));
        }
        ArrayList<ArrayList<Integer>> edgeList = new ArrayList<ArrayList<Integer>>();
        
        Random rand = new Random();
        for(int i : graph.keySet()){
            for(int j : graph.get(i)){
                if(i>j){
                    continue;
                }
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(i);tmp.add(j);
                edgeList.add(tmp);
            }
        }
        System.out.println(k);
        for(int i = 0;i<k;i++){
            if(i%10000==0)
            System.out.println(i);
            int t=-1,w=-1,v=-1,u=-1;
            int randPos1=-1,randPos2=-1;
            int c = 0;
            do{
               randPos1=rand.nextInt(edgeList.size());
               randPos2=rand.nextInt(edgeList.size());
               ArrayList<Integer> e1 = edgeList.get(randPos1);
               t=e1.get(0); w=e1.get(1);
               ArrayList<Integer> e2 = edgeList.get(randPos2);
               u=e2.get(0); v=e2.get(1);
               c++;
               if(c>max){
                   System.out.println("no avaliable");
                   return;
               }
            }while(originalGraph.get(t).contains(v) || originalGraph.get(u).contains(w) 
                    ||t==v ||w==u||graph.get(t).contains(v) || graph.get(u).contains(w) );
            

            graph.get(t).remove(w);
            graph.get(w).remove(t);
            graph.get(v).remove(u);
            graph.get(u).remove(v);

            graph.get(t).add(v);
            graph.get(v).add(t);
            graph.get(u).add(w);
            graph.get(w).add(u);
            edgeList.remove(randPos1);
            if (randPos1 < randPos2) {
                edgeList.remove(randPos2 - 1);
            } else {
                edgeList.remove(randPos2);
            }

        }
    }
}
