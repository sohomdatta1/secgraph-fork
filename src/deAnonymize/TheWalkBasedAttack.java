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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import common.Graph;
import common.Pair;
import common.TreeNode;
public class TheWalkBasedAttack {
	static Random rand = new Random();

	public static ArrayList<Pair<Integer, Integer>> constructSubgraph(
			HashMap<Integer, HashSet<Integer>> originalGraphG, int d0, int d1,
			double delta, ArrayList<Integer> targets, int smallConstantC,int k, 
			HashMap<Integer, HashSet<Integer>> internalEdges)
			throws IOException {
		HashMap<Integer, HashSet<Integer>> addedGraph = new HashMap<Integer, HashSet<Integer>>();
		if (d0 > d1) {
			throw new IOException("d0 should be less than d1");
		}
		/*System.out.println(originalGraphG.keySet().size());*/
		Integer[] originalNodeArray = originalGraphG.keySet().toArray(
				new Integer[] {});
		HashMap<Integer, Integer> addedGraphExternalDegrees = new HashMap<Integer, Integer>();

		int numberOfNodesInOriginalGraph = originalGraphG.keySet().size();
		int nodeId = numberOfNodesInOriginalGraph + 1;
		int numberOfAddedNodes = k;
		
		/*System.out.println("numberOfNodesInOriginalGraph="+numberOfNodesInOriginalGraph);
		System.out.println("numberOfAddedNodes="+numberOfAddedNodes);*/
		Integer[] addedNodeArray = new Integer[numberOfAddedNodes];
		for (int i = 0; i < numberOfAddedNodes; i++) {
			while (originalGraphG.keySet().contains(nodeId)) {
				nodeId++;
			}
			addedGraph.put(nodeId, new HashSet<Integer>());
			addedGraphExternalDegrees.put(nodeId, rand.nextInt(d1 - d0) + d0);
			addedNodeArray[i] = nodeId;
			nodeId++;
		}
		/*System.out.println("finished adding new nodes");*/
		createEdgeToTargets(originalGraphG, targets, smallConstantC, addedGraph, addedGraphExternalDegrees, addedNodeArray);
		/*System.out.println("created edge to targets");*/
		fillEdgeCountWithNoneTargetNodes(originalGraphG, targets, addedGraph,
				addedGraphExternalDegrees, numberOfNodesInOriginalGraph,
				originalNodeArray, addedNodeArray);
		/*System.out.println("filled edge count");*/
		createInternalEdges(addedGraph, numberOfAddedNodes, addedNodeArray,internalEdges);
		/*System.out.println(internalEdges.keySet().size());
		System.out.println("created internal edges");*/
		originalGraphG.putAll(addedGraph);

		ArrayList<Pair<Integer, Integer>> addedDegrees = new ArrayList<Pair<Integer, Integer>>();
		for (int i : addedNodeArray) {
			addedDegrees.add(new Pair<Integer, Integer>(i, addedGraph.get(i).size()));
		}
		return addedDegrees;
	}

	public static TreeNode<Integer> recoveringHGivenG(
			HashMap<Integer, HashSet<Integer>> modifiedG,
			ArrayList<Pair<Integer, Integer>> addedDegrees, 
			HashMap<Integer, HashSet<Integer>> internalEdges) {
		TreeNode<Integer> root = new TreeNode<Integer>(0);
		root.level = -1;
		LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer>>();
		for(int i: modifiedG.keySet()){
			if(modifiedG.get(i).size()==addedDegrees.get(0).getValue()){
				TreeNode<Integer> t = new TreeNode<Integer>();
				t.data=i;
				t.parent=root;
				t.level=root.level+1;
				root.children.add(t);
				queue.addLast(t);
			}
		}
		while(!queue.isEmpty()){
			TreeNode<Integer> here = queue.pollFirst();
			int nodeId = here.data;
			int seekDegree = addedDegrees.get(here.level+1).getValue();
			for(int i : modifiedG.get(nodeId)){
				if(modifiedG.get(i).size()==seekDegree){
					TreeNode<Integer> t = new TreeNode<Integer>();
					t.data=i;
					t.parent=here;
					t.level=here.level+1;
					here.children.add(t);
					if(t.level+1<addedDegrees.size()){
						queue.add(t);
					}
				}
			}
		}
		
		cleanTree(root,addedDegrees.size()-1,internalEdges,addedDegrees,modifiedG);
		return root;
	}

	private static void cleanTree(
			TreeNode<Integer> root, int depth,
			HashMap<Integer, HashSet<Integer>> internalEdges,
			ArrayList<Pair<Integer, Integer>> addedDegrees, HashMap<Integer, HashSet<Integer>> modifiedG) {
		if(!internalEdges.containsKey(-1)){
			internalEdges.put(-1, new HashSet<Integer>());
		}
		LinkedList<TreeNode<Integer>> stack = new LinkedList<TreeNode<Integer>>();
		LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer>>();
		HashMap<Integer,HashSet<TreeNode<Integer>>> levelToNodes = new HashMap<Integer,HashSet<TreeNode<Integer>>>(); 	
		queue.addLast(root);
		while(!queue.isEmpty()){
			TreeNode<Integer> n = queue.poll();
			stack.addFirst(n);
			for(TreeNode<Integer> i : n.children){
				queue.add(i);
			}
		}
		while(!stack.isEmpty()){
			TreeNode<Integer> i = stack.pollFirst();
			if(i.children.size()==0 && i.level!=depth&& i.level!=-1){
				i.parent.children.remove(i);
			}else{
				if(!levelToNodes.containsKey(i.level)){
					levelToNodes.put(i.level, new HashSet<TreeNode<Integer>>());
				}
				levelToNodes.get(i.level).add(i);
			}
		}
		int size1 = 0;
		int size2 = -1;
		
		
		while(size1!=size2){
			size2=size1;
			size1=0;
			queue.addLast(root);
			while (!queue.isEmpty()) {
				TreeNode<Integer> n = queue.poll();
				for (TreeNode<Integer> i : n.children) {
					queue.add(i);
				}
				for (int level : internalEdges.get(n.level)) {
					boolean notFound = true;
					for (TreeNode<Integer> i : levelToNodes.get(level)) {
						if (modifiedG.get(i.data).contains(n.data)) {
							notFound = false;
						}
					}
					if (notFound) {
						n.parent.children.remove(n);
					}else{
						size1++;
					}
				}

			}
		}
		return;
	}

	private static void createInternalEdges(
			HashMap<Integer, HashSet<Integer>> addedGraph,
			int numberOfAddedNodes, Integer[] addedNodeArray,
			HashMap<Integer, HashSet<Integer>> internalEdges) {
		for (int i = 0; i < numberOfAddedNodes - 1; i++) {
			if(!internalEdges.containsKey(i)){
				internalEdges.put(i, new HashSet<Integer>());
			}
			if(!internalEdges.containsKey(i+1)){
				internalEdges.put(i+1, new HashSet<Integer>());
			}
			
			addedGraph.get(addedNodeArray[i]).add(addedNodeArray[i + 1]);
			addedGraph.get(addedNodeArray[i + 1]).add(addedNodeArray[i]);
			internalEdges.get(i).add(i + 1);
			internalEdges.get(i + 1).add(i);
			
			for (int j = i + 2; j < numberOfAddedNodes; j++) {
				if (rand.nextDouble() < 0.5) {
					if(!internalEdges.containsKey(addedNodeArray[j])){
						internalEdges.put(j, new HashSet<Integer>());
					}
					addedGraph.get(addedNodeArray[i]).add(addedNodeArray[j]);
					addedGraph.get(addedNodeArray[j]).add(addedNodeArray[i]);
					internalEdges.get(i).add(j);
					internalEdges.get(j).add(i);
				}
			}
		}
	}

	private static void fillEdgeCountWithNoneTargetNodes(
			HashMap<Integer, HashSet<Integer>> originalGraphG,
			ArrayList<Integer> targets,
			HashMap<Integer, HashSet<Integer>> addedGraph,
			HashMap<Integer, Integer> addedGraphDegrees,
			int numberOfNodesInOriginalGraph, Integer[] originalNodeArray,
			Integer[] addedNodeArray) {
		for (int i : addedNodeArray) {
			while (addedGraph.get(i).size() < addedGraphDegrees.get(i)) {
				int position = rand.nextInt(numberOfNodesInOriginalGraph);
				if (!targets.contains(originalNodeArray[position])) {
					addedGraph.get(i).add(originalNodeArray[position]);
					originalGraphG.get(originalNodeArray[position]).add(i);
				}
			}
		}
	}

	private static HashMap<Integer, HashSet<Integer>> createEdgeToTargets(
			HashMap<Integer, HashSet<Integer>> originalGraphG,
			ArrayList<Integer> targets, int smallConstantC,
			HashMap<Integer, HashSet<Integer>> addedGraph,
			HashMap<Integer, Integer> addedGraphDegrees,
			Integer[] addedNodeArray) {
		HashSet<HashSet<Integer>> choosenNodes = new HashSet<HashSet<Integer>>();
		HashMap<Integer, HashSet<Integer>> Nj = new HashMap<Integer, HashSet<Integer>>();
		for (int target : targets) {
			/*System.out.println("now processing target "+target);*/
			Nj.put(target, new HashSet<Integer>());
			while (Nj.get(target).size() < smallConstantC) {
				int randomPosition = rand.nextInt(addedGraph.keySet().size());
				int cand = addedNodeArray[randomPosition];
				if (addedGraph.get(cand).size() < addedGraphDegrees.get(cand)) {
					Nj.get(target).add(cand);
					addedGraph.get(cand).add(target);
				}
				if(Nj.get(target).size() == smallConstantC){
					if(choosenNodes.contains(Nj.get(target))){
						/*System.out.println("redo finding nodes to connect to");*/
						for(int toRemove : Nj.get(target)){
							addedGraph.get(toRemove).remove(target);
						}
						Nj.get(target).clear();
					}else{
						choosenNodes.add(Nj.get(target));
					}
				}
			}
		}
		for(int target : targets){
			for(int to : Nj.get(target)){
				originalGraphG.get(target).add(to);
				if(!originalGraphG.containsKey(to)){
					originalGraphG.put(to, new HashSet<Integer>());
				}
				originalGraphG.get(to).add(target);
			}
		}
		return Nj;				
	}
	
	 public static void main(String[] args) throws IOException, InterruptedException {
	        theWalkBasedAttack(args[0], args[1], args[2], args[3], args[4], args[5]);
	    }
	 
	    public static void theWalkBasedAttack(String graphName, String addedDegreeFileName,String targetFileName,
	            String outGraphName,String internalEdgeFileName,String recoverNodesFileName) throws IOException, InterruptedException {
	        BufferedWriter w = new BufferedWriter(new FileWriter(addedDegreeFileName));
	        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(graphName);
	        HashMap<Integer, HashSet<Integer>> internalEdges = new HashMap<Integer, HashSet<Integer>>();
	        BufferedReader r = new BufferedReader(new FileReader(targetFileName));
	        ArrayList<Integer> targets = new ArrayList<Integer>();
	        String l = r.readLine();
	        while(l!=null){
	            targets.add(Integer.parseInt(l));
	            l=r.readLine();
	        }
	        r.close();
	        ArrayList<Pair<Integer, Integer>>  degrees = TheWalkBasedAttack.constructSubgraph(g,20,60,0.5,targets,3,20,internalEdges);
	        //System.out.println(internalEdges.keySet().size());
	        Graph.writeGraph(g, outGraphName);
	        /*for(int i : g.keySet()){
	            w4.write(i+" "+g.get(i).size()+"\n");
	        }
	        w4.flush();w4.close();*/
	        Graph.writeGraph(internalEdges, internalEdgeFileName);
	        for(Pair<Integer, Integer> i : degrees){
	            w.write(i.getKey()+" "+i.getValue()+"\n");
	        }
	        w.flush();w.close();
	        System.out.println("start recovery");
	        TreeNode<Integer> recovered = TheWalkBasedAttack.recoveringHGivenG(g, degrees,internalEdges);
	        TreeNode.writeTree(recovered, recoverNodesFileName,g);
	    }
}
