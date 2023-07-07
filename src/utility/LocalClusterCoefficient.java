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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;

public class LocalClusterCoefficient {
    public static HashMap<Integer, Double> getLocalClusterCoeff(HashMap<Integer, HashSet<Integer>> g ){
        HashMap<Integer, Double> localClusterCoef = new HashMap<Integer, Double>();
        for(int here : g.keySet()){
            HashSet<Integer> nodes = new HashSet<Integer>(g.get(here));
            if(nodes.size()==1){
                localClusterCoef.put(here, ((double) 1));
                continue;
            }
            int totalNumberOfPath = 0;
            for(int i : nodes){
                for(int j : nodes){
                    if(i==j){
                        continue;
                    }
                    if(g.get(i).contains(j)){
                        totalNumberOfPath++;
                    }
                }
            }
            totalNumberOfPath/=2;
            int numOfEdgeInComplGraph = nodes.size()*(nodes.size()-1)/2;
            localClusterCoef.put(here, ((double) (totalNumberOfPath))/numOfEdgeInComplGraph);
        }
        return localClusterCoef;
    }
    public static double compareLCC(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2){
        HashMap<Integer, Double> g1Res = getLocalClusterCoeff(g1);
        HashMap<Integer, Double> g2Res = getLocalClusterCoeff(g2);
        double ab=0,aa=0,bb=0;
        for(int i :g1Res.keySet()){
            aa+=g1Res.get(i)*g1Res.get(i);
            bb+=g2Res.get(i)*g2Res.get(i);
            ab+=g1Res.get(i)*g2Res.get(i);
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    public static double LCC(String G1,String G2) throws IOException {
        
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(G2);
        return(compareLCC(g1,g2));
    }
    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("usage g1 g2");
            System.exit(0);
        }
        System.out.println(LCC(args[0],args[1]));
    }
}
