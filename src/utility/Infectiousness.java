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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import common.Graph;

public class Infectiousness {
    public static double getInfectiousnessSize(HashMap<Integer, HashSet<Integer>> g, double rate,int start){
        Random rand = new Random();
        HashSet<Integer> infected = new HashSet<Integer>();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        //Integer start = allNodes[rand.nextInt(allNodes.length)];
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.addLast(start);
        infected.add(start);
        //inf.put(index, infected.size());
        while(!queue.isEmpty()){
            Integer here = queue.removeFirst();
            for(int i : g.get(here)){
                if(infected.contains(i)){
                    continue;
                }
                if(rand.nextDouble()<rate){
                    infected.add(i);
                    queue.addLast(i);
                }
            }
        }
        return ((double )infected.size())/g.size();
    }
    public static double getInfectiousnessSize(HashMap<Integer, HashSet<Integer>> g, double rate){
        Random rand = new Random();
        HashSet<Integer> infected = new HashSet<Integer>();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        Integer start = allNodes[rand.nextInt(allNodes.length)];
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.addLast(start);
        infected.add(start);
        //inf.put(index, infected.size());
        while(!queue.isEmpty()){
            Integer here = queue.removeFirst();
            for(int i : g.get(here)){
                if(infected.contains(i)){
                    continue;
                }
                if(rand.nextDouble()<rate){
                    infected.add(i);
                    queue.addLast(i);
                }
            }
        }
        return ((double )infected.size())/g.size();
    }
    public static HashSet<Integer> getInfectiousness(HashMap<Integer, HashSet<Integer>> g, double rate,int start){
        Random rand = new Random();
        HashSet<Integer> infected = new HashSet<Integer>();
        //Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        //Integer start = allNodes[rand.nextInt(allNodes.length)];
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.addLast(start);
        infected.add(start);
        //inf.put(index, infected.size());
        while(!queue.isEmpty()){
            Integer here = queue.removeFirst();
            for(int i : g.get(here)){
                if(infected.contains(i)){
                    continue;
                }
                if(rand.nextDouble()<rate){
                    infected.add(i);
                    queue.addLast(i);
                }
            }
        }
        return infected;
    }
    public static double compareInfectionsSize(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2,ArrayList<Integer> start){
        ArrayList<Double> g1List = new ArrayList<Double>();
        ArrayList<Double> g2List = new ArrayList<Double>();
        for(int i = 0;i<=800;i+=5){
            double totalI1 = 0;
            double totalI2 = 0;
            for(int j = 0;j<start.size();j++){
                totalI1 += getInfectiousnessSize(g1,((double) i)/1000.0,start.get(j));
                totalI2 += getInfectiousnessSize(g2,((double) i)/1000.0,start.get(j));
            }
            g1List.add(totalI1/start.size());
            g2List.add(totalI2/start.size());
        }
        
        double aa=0,ab=0,bb=0;
        for(int i=0; i<g1List.size();i++){
            aa+=g1List.get(i)*g1List.get(i);
            bb+=g2List.get(i)*g2List.get(i);
            ab+=g2List.get(i)*g1List.get(i);
        }
        return ab/(Math.sqrt(aa)*Math.sqrt(bb));  
    }
    
    public static double compareInfectionsSize(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2,int numTry){
        ArrayList<Double> g1List = new ArrayList<Double>();
        ArrayList<Double> g2List = new ArrayList<Double>();
        for(int i = 0;i<=500;i+=5){
            double totalI1 = 0;
            double totalI2 = 0;
            for(int j = 0;j<numTry;j++){
                totalI1 += getInfectiousnessSize(g1,((double) i)/1000.0);
                totalI2 += getInfectiousnessSize(g2,((double) i)/1000.0);
            }
            g1List.add(totalI1/numTry);
            g2List.add(totalI2/numTry);
        }
        
        double aa=0,ab=0,bb=0;
        for(int i=0; i<g1List.size();i++){
            aa+=g1List.get(i)*g1List.get(i);
            bb+=g2List.get(i)*g2List.get(i);
            ab+=g2List.get(i)*g1List.get(i);
        }
        return ab/(Math.sqrt(aa)*Math.sqrt(bb));  
    }
    public static double compareInfectionsJarccard(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2,ArrayList<Integer> start){
        double sum = 0;
        int count = 0;
        for(int i = 0;i<=800;i+=5){
            double rateSum = 0;
            for(int j = 0;j<start.size();j++){
                HashSet<Integer> setA = getInfectiousness(g1,((double) i)/1000.0,start.get(j));
                HashSet<Integer> setB = getInfectiousness(g2,((double) i)/1000.0,start.get(j));
                rateSum += calcJaccard(setA,setB);
            }
            count++;
            sum+=(rateSum/start.size());
        }
        return sum/count;
    }
    private static double calcJaccard(HashSet<Integer> setInA,
            HashSet<Integer> setInB) {
        HashSet<Integer> union = new HashSet<Integer>(setInA);
        union.addAll(setInB);
        HashSet<Integer> intersect = new HashSet<Integer>(setInA);
        intersect.retainAll(setInB);
        return ((double)intersect.size())/union.size();
    }
    public static double calcInfec(String G1,String G2,String numRep) throws IOException {
        
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(G2);
        return compareInfectionsSize(g1, g2, Integer.parseInt(numRep));
      }
    public static void main(String[] args) throws IOException {
        if(args.length!=3){
            System.out.println("usage g1 g2 numOfRepeates");
            System.exit(0);
        }
        
        System.out.println(calcInfec(args[0], args[1], args[2])+"");
    }
}
