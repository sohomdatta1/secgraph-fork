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
package deAnonymize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import common.Graph;
import common.Pair;
import common.TreeNode;
import common.Utilities;

public class TheCutBasedAttack {
    static Random rand = new Random();
    public static HashMap<Integer, HashSet<Integer>> constructSubgraph(
            HashMap<Integer, HashSet<Integer>> originalGraphG,
            ArrayList<Integer> targets) {
        HashMap<Integer, HashSet<Integer>> addedGraph = new HashMap<Integer, HashSet<Integer>>();
        int numberOfNodesInOriginalGraph = originalGraphG.keySet().size();
        int nodeId = numberOfNodesInOriginalGraph + 1;
        int numberOfTargets = targets.size();
        int numberOfAddedNodes = 3*numberOfTargets+3;
        Integer[] addedNodeArray = new Integer[numberOfAddedNodes];
        for (int i = 0; i < numberOfAddedNodes; i++) {
            while (originalGraphG.keySet().contains(nodeId)) {
                nodeId++;
            }
            addedGraph.put(nodeId, new HashSet<Integer>());
            addedNodeArray[i] = nodeId;
            nodeId++;
        }
        for(int i =0; i < numberOfAddedNodes-1;i++){
            for(int j =i+1; j < numberOfAddedNodes;j++){
                if (rand.nextDouble() < 0.5) {
                    addedGraph.get(addedNodeArray[j]).add(addedNodeArray[i]);
                    addedGraph.get(addedNodeArray[i]).add(addedNodeArray[j]);
                }
            }
        }
        HashSet<Integer> bArbNodes = new HashSet<Integer>();
        while(bArbNodes.size()<numberOfTargets){
            bArbNodes.add(addedNodeArray[rand.nextInt(numberOfAddedNodes)]);
        }
        originalGraphG.putAll(addedGraph);
        int i = 0;
        for (int arbNode : bArbNodes) {
            originalGraphG.get(targets.get(i)).add(arbNode);
            originalGraphG.get(arbNode).add(targets.get(i));
            i++;
        }
        return addedGraph;
    }
   
    public static String recoverySubgraph(
            HashMap<Integer, HashSet<Integer>> modifiedGraphG,HashMap<Integer,
            HashSet<Integer>> addedGraph,int targetSize) throws IOException, InterruptedException
    {
    	int sz = addedGraph.keySet().size();
        HashSet<HashSet<Integer>> gomoryHuForest =
                gomoryHuAlgorithm(modifiedGraphG,sz,targetSize);
        HashMap<Integer, Integer> matching = bruteForceGraphIsomorphism(gomoryHuForest,addedGraph,modifiedGraphG);
        String ret = "";
        if(matching==null){
            ret="did not find a matching";
        } else {
            for (int i : matching.keySet()) {
                ret+="addedGraphId : " + i
                        + "recoveredGraphId : " + matching.get(i)+"\n";
            }
        }
        return ret;
    }
   
    private static HashMap<Integer, Integer> bruteForceGraphIsomorphism(
            HashSet<HashSet<Integer>> gomoryHuForest,
            HashMap<Integer, HashSet<Integer>> addedGraph, HashMap<Integer, HashSet<Integer>> modifiedGraphG) {
    	HashMap<Integer, Integer> matching=null;
        for(HashSet<Integer> tree : gomoryHuForest){
            HashMap<Integer, HashSet<Integer>> testGraph = findGraphFromTree(modifiedGraphG,tree);
            matching=Isomorphic(testGraph,addedGraph);
            if(matching!=null){
                break;
            }
        }
        return matching;
    }

    private static HashMap<Integer, Integer> Isomorphic(
            HashMap<Integer, HashSet<Integer>> testGraph,
            HashMap<Integer, HashSet<Integer>> addedGraph) {
    	List<Integer> ids = new ArrayList<Integer>();
        for(int i : addedGraph.keySet()){
            ids.add(i);
        }
        Integer[] testingNodes = testGraph.keySet().toArray(new Integer[]{});
        if(testingNodes.length!=ids.size()){
            throw new RuntimeException("The size of the testingGraph and the size of the addedGraph does not match");
        }
        List<List<Integer>> allPermutations = Utilities.generatePerm(ids);
        for(List<Integer> currentPermuation : allPermutations){
            HashMap<Integer,Integer> testToAddedMapping = new HashMap<Integer, Integer>();
            HashMap<Integer,Integer> addToTestMapping = new HashMap<Integer, Integer>();
            for(int i = 0;i< testingNodes.length;i++){
                addToTestMapping.put(currentPermuation.get(i), testingNodes[i]);
                testToAddedMapping.put(testingNodes[i],currentPermuation.get(i));
            }
            boolean testEq = testGraphAreEqualViaMapping(testGraph,addedGraph,testToAddedMapping);
            if(testEq){
            	System.out.println("end isomorphic");
                return addToTestMapping;
            }
        }
        throw new RuntimeException("Isomorphic failed");
        
    }

    private static boolean testGraphAreEqualViaMapping(
            HashMap<Integer, HashSet<Integer>> testGraph,
            HashMap<Integer, HashSet<Integer>> addedGraph,
            HashMap<Integer, Integer> testToAddedMapping) {
        for(int i : testGraph.keySet()){
            for(int j : testGraph.get(i)){
                int addedGraphI = testToAddedMapping.get(i);
                int addedGraphJ = testToAddedMapping.get(j);
                if(!addedGraph.get(addedGraphI).contains(addedGraphJ)){
                    return false;
                }
                if(!addedGraph.get(addedGraphJ).contains(addedGraphI)){
                    return false;
                }
            }
        }
        return true;
    }

    private static HashMap<Integer, HashSet<Integer>> findGraphFromTree(
            HashMap<Integer, HashSet<Integer>> modifiedGraphG,
            HashSet<Integer> tree) {
    	HashMap<Integer,HashSet<Integer>> newTree = new HashMap<Integer, HashSet<Integer>>();
        for(int i : modifiedGraphG.keySet()){
            for (int j : modifiedGraphG.get(i)) {
                if(tree.contains(i)&&tree.contains(j)){
                    if(!newTree.containsKey(i)){
                        newTree.put(i, new HashSet<Integer>());
                    }
                    if(!newTree.containsKey(j)){
                        newTree.put(j, new HashSet<Integer>());
                    }
                    newTree.get(i).add(j);
                    newTree.get(j).add(i);
                }
            }
        }
        return newTree;
    }

	public static HashSet<HashSet<Integer>> gomoryHuAlgorithm(
			HashMap<Integer, HashSet<Integer>> modifiedGraphG, int k, int b)
			throws IOException, InterruptedException {
		Graph.writeGraph(modifiedGraphG, "tmpGraph");
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("python python/cutTree.py tmpGraph out");
        pr.waitFor();
        BufferedReader br = new BufferedReader(new FileReader("out"));
        char[] tree = br.readLine().replaceAll("\\s+", "").toCharArray();
        System.out.println(tree);
        HashMap<Integer,HashSet<Integer>> forest = new HashMap<Integer,HashSet<Integer>>();
        br.close();
        System.out.println(b);
        for(int i = 0;i<tree.length;i++){
            String number="";
            int nodeA,nodeB,weight;
            if (tree[i] == '(') {
                i++;
                while (tree[i] != ',') {
                    number += tree[i];
                    i++;
                }
                nodeA = Integer.parseInt(number);
                i++;
                number = "";
                while (tree[i] != ')') {
                    number += tree[i];
                    i++;
                }
                nodeB = Integer.parseInt(number);
                i += 2;
                number = "";
                while (tree[i] != ',' && tree[i] != '}') {
                    number += tree[i];
                    i++;
                }
                weight = Integer.parseInt(number);
                if(weight>b){
                    if(!forest.containsKey(nodeA)){
                        forest.put(nodeA, new HashSet<Integer>());
                    }
                    if(!forest.containsKey(nodeB)){
                        forest.put(nodeB, new HashSet<Integer>());
                    }
                    forest.get(nodeA).add(nodeB);
                    forest.get(nodeB).add(nodeA);
                }
            }
        }
        HashMap<Integer, HashMap<Integer, HashSet<Integer>>> allTreesInForest =
                Graph.allConnectedCompentents(forest);
        System.out.println("num of trees "+allTreesInForest.size());
        HashSet<HashSet<Integer>> setOfTreeNodes = new HashSet<HashSet<Integer>>();
        for(int i : allTreesInForest.keySet()){
        	System.out.println("tree "+i+" in the forest have size "+allTreesInForest.get(i).keySet().size());
            if(allTreesInForest.get(i).keySet().size()==k){
                HashSet<Integer> thisTree = new HashSet<Integer>();
                for(int j : allTreesInForest.get(i).keySet()){
                    for(int j2 : allTreesInForest.get(i).get(j)){
                        thisTree.add(j);
                        thisTree.add(j2);
                    }
                }
                setOfTreeNodes.add(thisTree);
            }
        }
        System.out.println("number of set of trees returned "+setOfTreeNodes.size());
        File toDelete = new File("tmpGraph");
        toDelete.delete();
        toDelete = new File("out");
        toDelete.delete();
        return setOfTreeNodes;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        theWalkBasedAttack(args[0],args[1],args[2]);
    }
    public static void theWalkBasedAttack(String inputGraph,String targetFileName,String outputFileName) throws IOException, InterruptedException {
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(inputGraph);
        ArrayList<Integer> targets = new ArrayList<Integer>();
        BufferedReader r = new BufferedReader(new FileReader(targetFileName));
        String l = r.readLine();
        while(l!=null){
            targets.add(Integer.parseInt(l));
            l=r.readLine();
        }
        r.close();
        HashMap<Integer, HashSet<Integer>> addedGraph = TheCutBasedAttack.constructSubgraph(g, targets);
        String res = TheCutBasedAttack.recoverySubgraph(g, addedGraph, targets.size());
        BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName));
        w.write(res);
        w.flush();w.close();
    }
}