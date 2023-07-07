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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class greedyDegreeAnonymization {
    

    public static int I(ArrayList<Pair<Integer, Integer>> nodeToDegree,int i,int j){
        int sum = 0;
        for(int l = i;l<=j;l++){
            if(nodeToDegree.contains(l)){
                sum+=nodeToDegree.get(i).getValue()-nodeToDegree.get(l).getValue();
            }
        }
        return sum;
    }
    public static int[] greedy(ArrayList<Pair<Integer, Integer>> nodeToDegree,int k){
        int[] greedy = new int[nodeToDegree.size()];
        int degreeHere = nodeToDegree.get(0).getValue();
        int i = 0;
        while(i<nodeToDegree.size()){
            int counter = 0;
            while(counter<k){
                greedy[i]=degreeHere;
                i++;
                if(i==nodeToDegree.size()){
                    break;
                }
                counter++;
            }
            if(i==nodeToDegree.size()){
                break;
            }
            int costOfMerge = degreeHere - nodeToDegree.get(i).getValue()
                    + I(nodeToDegree, i + 1, i+k);
            int costOfNew   = I(nodeToDegree, i,  i +k-1);
            while(costOfMerge<=costOfNew){
                greedy[i]=degreeHere;
                i++;
                if(i==nodeToDegree.size()){
                    break;
                }
                costOfMerge = degreeHere
                        - nodeToDegree.get(i).getValue()
                        + I(nodeToDegree, i + 1, i+k);
                costOfNew = I(nodeToDegree, i,  i +k-1);

            }
            if(i==nodeToDegree.size()){
                break;
            }
            degreeHere = nodeToDegree.get(i).getValue();
            if(degreeHere==94){
                System.out.println();
            }
        }
        return greedy;
    }
   
    public static void main(String[] args) throws IOException {
        HashMap<Integer,HashSet<Integer>> graph = Graph.readUndirectedGraph("G");
        System.out.println(graph.get(591));
        ArrayList<Pair<Integer, Integer>> nodeToDegree = new ArrayList<Pair<Integer,Integer>>();
        for(int i : graph.keySet()){
            nodeToDegree.add(new Pair<Integer, Integer>(i,graph.get(i).size()));
        }
        Collections.sort(nodeToDegree, new Comparator<Pair<Integer,Integer>>(){
            public int compare(Pair<Integer, Integer> p0, Pair<Integer, Integer> p1) {
                return p1.getValue()-p0.getValue();
            }
        });
        //nodeToDegree.add(0, new Pair<Integer, Integer>(-1,-1));
        String nodeID ="";
        String nodeDegree = "";
        for(Pair<Integer, Integer> here : nodeToDegree){
            nodeID+=here.getKey().toString()+"\t";
            nodeDegree+=here.getValue().toString()+"\t";
        }
        System.out.println(nodeID);
        System.out.println(nodeDegree);
        HashMap<Integer, Integer> newDegFreq = new HashMap<Integer, Integer>();
        int[] newDeg = greedy(nodeToDegree, 1);
        System.out.println(graph.size());
        System.out.println(newDeg.length);
        System.out.println();
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
        }
    }
}
