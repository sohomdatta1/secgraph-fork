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

import common.Graph;

public class MINS {
    public static void main(String[] args) throws IOException {
        if(args.length!=4){
            System.out.println("usage : java -jar MINS <graph1Name> <graph2Name> <p> <threshold>");
            System.exit(0);
        }
        System.out.println(compareMINS(args[0], args[1], args[2], args[3]));
    }
    public static double compareMINS(String G1, String G2, String P, String Threshold) throws IOException {
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(G2);
        ArrayList<Integer> r1 = mins(g1, Double.parseDouble(P), Double.parseDouble(Threshold));
        ArrayList<Integer> r2 = mins(g2, Double.parseDouble(P), Double.parseDouble(Threshold));
        HashSet<Integer> h1 = new HashSet<Integer>();
        HashSet<Integer> h2 = new HashSet<Integer>();
        for(int i : r1){
            h1.add(i);
        }
        for(int j : r2){
            h2.add(j);
        }
        return compareJaccard(h1, h2);
    }
    public static double compareJaccard(HashSet<Integer> a , HashSet<Integer> b){
        HashSet<Integer> or = new HashSet<Integer>(a);
        or.addAll(b);
        HashSet<Integer> and = new HashSet<Integer>(a);
        and.retainAll(b);
        
        return ((double)and.size())/((double)or.size());
    }
    public static ArrayList<Integer> mins(HashMap<Integer, HashSet<Integer>> g,double p,double threshold) {
        HashSet<Integer> setOfSelectedNodes = new HashSet<Integer>();
        ArrayList<Integer> listOfSelectedNodes = new ArrayList<Integer>();
        HashSet<Integer> setOfCandidate = new HashSet<Integer>(g.keySet());
        HashSet<Integer> setOfUninfluencedNodes = new HashSet<Integer>(g.keySet());
        int largestNode = Graph.findLargestNode(g);
        setOfSelectedNodes.add(largestNode);
        listOfSelectedNodes.add(largestNode);
        setOfCandidate.remove(largestNode);
        setOfUninfluencedNodes.remove(largestNode);
        HashMap<Integer, Integer> uninfToNeiInSel = new HashMap<Integer, Integer>();
        for(int i : setOfUninfluencedNodes){
            int SelectedAndIsNeiOfI = 0;
            for(int j : setOfSelectedNodes){
                if(g.get(i).contains(j)){
                    SelectedAndIsNeiOfI++;
                }
            }
            uninfToNeiInSel.put(i, SelectedAndIsNeiOfI);
        }
        while(!setOfUninfluencedNodes.isEmpty()){
            System.out.println(setOfUninfluencedNodes.size());
            double maxScore = -1;
            int nodeWithMax = -1;
            for(int candidate : setOfCandidate){
                double score = computeScore(p,setOfUninfluencedNodes,setOfSelectedNodes,candidate,g,uninfToNeiInSel);
                if(score>maxScore){
                    maxScore=score;
                    nodeWithMax=candidate;
                }
            }
            setOfCandidate.remove(nodeWithMax);
            setOfSelectedNodes.add(nodeWithMax);
            listOfSelectedNodes.add(nodeWithMax);
            setOfUninfluencedNodes.remove(nodeWithMax);
            uninfToNeiInSel.remove(nodeWithMax);
            updateUninfluencedNodes(p,threshold,setOfUninfluencedNodes,setOfSelectedNodes,g,uninfToNeiInSel);
            //System.out.println(setOfUninfluencedNodes.size());
        }
        return listOfSelectedNodes;

    }
    private static void updateUninfluencedNodes(double p,double threshold,
            HashSet<Integer> setOfUninfluencedNodes,
            HashSet<Integer> setOfSelectedNodes, HashMap<Integer, HashSet<Integer>> g, HashMap<Integer, Integer> uninfToNeiInSel) {
        HashSet<Integer> setOfNodesToRemove = new HashSet<Integer>();
        for(int i : setOfUninfluencedNodes){
            int neiOfIInSelected = 0;
            for(int j : setOfSelectedNodes){
                if(g.get(i).contains(j)){
                    neiOfIInSelected++;
                }
            }
            uninfToNeiInSel.put(i, neiOfIInSelected);
            double score = 1-Math.pow(1-p,neiOfIInSelected);
            if(score>threshold){
                setOfNodesToRemove.add(i);
            }
        }
        setOfUninfluencedNodes.removeAll(setOfNodesToRemove);
        uninfToNeiInSel.remove(setOfNodesToRemove);
    }
    private static double computeScore(double p, HashSet<Integer> setOfUninfluencedNodes, HashSet<Integer> setOfSelectedNodes, int candidate, HashMap<Integer, HashSet<Integer>> g, HashMap<Integer, Integer> uninfToNeiInSel) {
        double score = 0;
        for(int i : g.get(candidate)){
            if(!setOfUninfluencedNodes.contains(i)){
                continue;
            }
            if(i==candidate){
                score+=1;
                continue;
            }
            int SelectedAndIsNeiOfJ = 0;
            int SelectedAndIsNeiOfJOrI =0;
            /*for(int j : setOfSelectedNodes){
                if(g.get(i).contains(j)){
                    SelectedAndIsNeiOfJOrI++;
                    SelectedAndIsNeiOfJ++;
                }
            }
            if(g.get(i).contains(candidate)){
                SelectedAndIsNeiOfJOrI++;
            }*/
            SelectedAndIsNeiOfJ=uninfToNeiInSel.get(i);
            SelectedAndIsNeiOfJOrI=SelectedAndIsNeiOfJ;
            if(g.get(i).contains(candidate)){
                SelectedAndIsNeiOfJOrI++;
            }
            score+=(1-(Math.pow(1-p,SelectedAndIsNeiOfJOrI)))-(1-(Math.pow(1-p,SelectedAndIsNeiOfJ)));
        }
        return score;
    }
}
