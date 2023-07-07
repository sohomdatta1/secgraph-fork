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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;

import common.TreeNode;

public class Graph {
    public static HashMap<Integer, Integer> getSeeds(String fn) throws IOException{
        HashMap<Integer, Integer> seeds = new HashMap<Integer, Integer>();
        BufferedReader r = new BufferedReader(new FileReader(fn));
        String l = r.readLine();
        while(l!=null){
            String[] relations = l.split("[\\s,;:]+");
            if(relations.length!=2){
                r.close();
                throw new IOException("From file "+fn+" the line "+l+" did not split correct when reading seed file");
            }
            int a = Integer.parseInt(relations[0]);
            int b = Integer.parseInt(relations[1]);
            seeds.put(a, b);
            l=r.readLine();
        }
        r.close();
        return seeds;
    }
	public static HashMap<Integer, Integer> getSeedsNew(
	        HashMap<Integer, HashSet<Integer>> g,int numSeeds) {
        int numTopDegSeeds = 10;
        HashMap<Integer, Integer> seeds = new HashMap<Integer, Integer>();
        TreeMap<Integer, HashSet<Integer>> degreeToNodeMap = Graph.getDegreeToNodeMap(g);
        Random r = new Random();
        Integer[] nodeList = g.keySet().toArray(new Integer[]{});
        for (int degree : degreeToNodeMap.descendingKeySet()) {
            for (int node : degreeToNodeMap.get(degree)) {
                if (seeds.size() < numTopDegSeeds) {
                    seeds.put(node, node);
                    //System.out.println(node);
                } else {
                    break;
                }
            }
            if (seeds.size() >= numTopDegSeeds) {
                break;
            }
        }
        while(seeds.size()<numSeeds){
            int id = r.nextInt(g.size());
            seeds.put(nodeList[id], nodeList[id]);
        }
        return seeds;
    }
    public static HashMap<Integer, Integer> getSeeds(
            HashMap<Integer, HashSet<Integer>> g, int numSeeds, int numWrongSeeds) {
        int numRightSeeds = numSeeds-numWrongSeeds;
        HashMap<Integer, Integer> seeds = new HashMap<Integer, Integer>();
        TreeMap<Integer, HashSet<Integer>> degreeToNodeMap = Graph
                .getDegreeToNodeMap(g);
        ArrayList<Integer> nodesWithWrongSeeds = new ArrayList<Integer>();
        for (int degree : degreeToNodeMap.descendingKeySet()) {
            for (int node : degreeToNodeMap.get(degree)) {
                if (seeds.size() < numSeeds) {
                    seeds.put(node, node);
                    //System.out.println(node);
                    if(seeds.size()>numRightSeeds){
                        nodesWithWrongSeeds.add(node);
                    }
                } else {
                    break;
                }
            }
            if (seeds.size() >= numSeeds) {
                break;
            }
        }
        if(numWrongSeeds==0){
            return seeds;
        }
        int largestWrongNode = nodesWithWrongSeeds.get(0);
        for(int i = 0;i<numWrongSeeds-1;i++){
            seeds.put(nodesWithWrongSeeds.get(i), nodesWithWrongSeeds.get(i+1));
        }
        seeds.put(nodesWithWrongSeeds.get(numWrongSeeds-1), largestWrongNode);
        return seeds;
    }
    public static int getNumberOfEdges(HashMap<Integer, HashSet<Integer>> graph) {
        int c = 0;
        for(int n: graph.keySet()){
            for(int k : graph.get(n)){
                if(n<k){
                    c++;
                }
            }
        }
        return c;
    }
	public static HashMap<Integer, HashSet<Integer>> readUndirectedGraph(String fn) throws IOException
	{
		HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>> ();
		BufferedReader reader = new BufferedReader(new FileReader(fn));
		String relation = null;
		relation = reader.readLine();		
		while(relation!=null){
			
			String[] relations = relation.split("[\\s,;:]+");
			if(relations.length!=2){
				reader.close();
				throw new IOException("From file "+fn+" the line "+relation+" did not split correct when creating the graph with readUndirectedGraph");
			}
			
			int a = Integer.parseInt(relations[0]);
			int b = Integer.parseInt(relations[1]);
			
			if(!graph.containsKey(a)){
				graph.put(a, new HashSet<Integer>());
			}
			if(!graph.containsKey(b)){
				graph.put(b, new HashSet<Integer>());
			}
			graph.get(a).add(b);
			graph.get(b).add(a);
			relation=reader.readLine();
		}
		reader.close();
		return graph;
	}
	
	public static HashMap<String, HashSet<String>> readUndirectedStringGraph(String fn) throws IOException
    {
        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>> ();
        BufferedReader reader = new BufferedReader(new FileReader(fn));
        String relation = null;
        relation = reader.readLine();       
        while(relation!=null){
            
            String[] relations = relation.split("[\\s,;:]+");
            if(relations.length!=2){
                reader.close();
                throw new IOException("From file "+fn+" the line "+relation+" did not split correct when creating the graph with readUndirectedGraph");
            }
            
            String a = relations[0];
            String b = relations[1];
            
            if(!graph.containsKey(a)){
                graph.put(a, new HashSet<String>());
            }
            if(!graph.containsKey(b)){
                graph.put(b, new HashSet<String>());
            }
            graph.get(a).add(b);
            graph.get(b).add(a);
            relation=reader.readLine();
        }
        reader.close();
        return graph;
    }
	
	public static void readCommSocialGraph(String fn,HashMap<Integer, HashSet<Integer>> comm, HashMap<Integer, HashSet<Integer>> social) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(fn));
		String relation = null;
		HashMap<Integer, HashSet<Integer>> graph =null;
		relation = reader.readLine();		
		while(relation!=null){
			if(relation.contains("COMM")){
				graph= comm;
			}else if(relation.contains("SOCIAL")){
				graph = social;
			}
			
			String[] relations = relation.split("[\\s,;:]+");
			if(relations.length!=3){
				reader.close();
				throw new IOException("From file "+fn+" the line "+relation+" did not split correct when creating the graph with readUndirectedGraph");
			}
			
			int a = Integer.parseInt(relations[0]);
			int b = Integer.parseInt(relations[1]);
			
			if(!graph.containsKey(a)){
				graph.put(a, new HashSet<Integer>());
			}
			if(!graph.containsKey(b)){
				graph.put(b, new HashSet<Integer>());
			}
			graph.get(a).add(b);
			graph.get(b).add(a);
			relation=reader.readLine();
		}
		reader.close();
	}
	public static void readDirectedGraph(String fn,HashMap<Integer, HashSet<Integer>> inMap, HashMap<Integer, HashSet<Integer>> outMap) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(fn));
		String relation = null;
		relation = reader.readLine();
		while(relation!=null){
			
			String[] relations = relation.split("[\\s,;:]+");
			if(relations.length!=2){
				reader.close();
				throw new IOException("from file "+fn+" the line "+relation+" did not split correct when creating the graph with readDirectedGraph");
			}
			
			int a = Integer.parseInt(relations[0]);//from  out
			int b = Integer.parseInt(relations[1]);//to    in
			
			if(!outMap.containsKey(a)){
			    outMap.put(a, new HashSet<Integer>());
			}
			if(!inMap.containsKey(b)){
			    inMap.put(b, new HashSet<Integer>());
			}
			outMap.get(a).add(b);
			inMap.get(b).add(a);
			relation=reader.readLine();
		}
		reader.close();
	}
	
	
	public static void writeGraph( HashMap<Integer, HashSet<Integer>> g, String fn) throws IOException{
		BufferedWriter w = new BufferedWriter(new FileWriter(fn));
		for(int i : g.keySet()){
			for(int j : g.get(i)){
				w.write(i+" "+j+"\n");
			}
		}
		w.flush();
		w.close();
	}
	
	public static HashMap<Integer, HashSet<Integer>>  getLargestConnectedCompententSnap(HashMap<Integer, HashSet<Integer>> graph) throws IOException, InterruptedException
    {
	    Graph.writeGraph(graph, "temp");
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("./getLCC temp tempOut");
        pr.waitFor();
        HashMap<Integer, HashSet<Integer>> lcc = Graph.readUndirectedGraph("tempOut");
        File toDelete = new File("temp");
        toDelete.delete();
        toDelete = new File("tempOut");
        toDelete.delete();
        return lcc;
    }
	public static int findLargestNode(HashMap<Integer, HashSet<Integer>> graph){
		int largestNode = 0, largestSize = 0;
		for(int i : graph.keySet()){
			if(graph.get(i).size()>largestSize){
				largestSize = graph.get(i).size();
				largestNode = i;
			}
		}
		return largestNode;
	}

	public static void spanFrom(int from, HashMap<Integer, HashSet<Integer>> graph, HashSet<Integer> compentent) {
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.addLast(from);
		while(!queue.isEmpty()){
			int here = queue.pollFirst();
			compentent.add(here);
			for(int i : graph.get(here)){
				if(!compentent.contains(i)) {
					queue.addLast(i);
				}
			}
		}
	}
	public static PriorityQueue<Pair<Integer, HashSet<Integer>>> getSortedGraphPQ(
            HashMap<Integer, HashSet<Integer>> g) {
        PriorityQueue<Pair<Integer, HashSet<Integer>>> sortedG = new PriorityQueue<Pair<Integer, HashSet<Integer>>>(
                10, new Comparator<Pair<Integer, HashSet<Integer>>>() {

                    @Override
                    public int compare(Pair<Integer, HashSet<Integer>> o1,
                            Pair<Integer, HashSet<Integer>> o2) {
                        if (o1.getValue().size() > o2.getValue().size()) {
                            return -1;
                        }
                        if (o1.getValue().size() < o2.getValue().size()) {
                            return 1;
                        }
                        if (o1.getKey() > o2.getKey()) {
                            return -1;
                        }
                        if (o1.getKey() < o2.getKey()) {
                            return 1;
                        }
                        // if (o1.getValue().containsAll(o2.getValue())) {
                        return 0;
                        // }
                        /*
                         * Iterator<Integer> it1 = o1.getValue().iterator();
                         * Iterator<Integer> it2 = o2.getValue().iterator();
                         * while (it1.hasNext() && it2.hasNext()) { int a =
                         * it1.next(); int b = it1.next(); if (a > b) return -1;
                         * if (a < b) return 1; } return 0;
                         */
                    }
                });
        for (int i : g.keySet()) {
            sortedG.add(new Pair<Integer, HashSet<Integer>>(i, g.get(i)));
        }
        return sortedG;
    }
	public static HashMap<Integer,HashSet<Integer>> getLargestConnectedCompentent(HashMap<Integer, HashSet<Integer>> graph)
	{
		HashMap<Integer,HashSet<Integer>> component = new  HashMap<Integer,HashSet<Integer>>();
		HashSet<Integer> visited = new HashSet<Integer>();
		int spanFromNode = findLargestNode(graph);
		int compId = 0;
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.addLast(spanFromNode);
		for (int i : graph.keySet()) {
			if (!visited.contains(i)) {
				component.put(compId, new HashSet<Integer>());
				spanFrom(i, graph,component.get(compId));
				visited.addAll(component.get(compId));
				compId++;
			}
		}
		int largestComponentSize = 0;
		int largestComponentId = 0;
		for(int i : component.keySet()){
			if(component.get(i).size()>largestComponentSize){
				largestComponentId=i;
				largestComponentSize=component.get(i).size();
			}
		}
		HashSet<Integer> nodesInLargestConnectedComponent=component.get(largestComponentId);
		HashMap<Integer,HashSet<Integer>> largestConnectedComponentGraph = new HashMap<Integer, HashSet<Integer>>();
		for(int i : graph.keySet()){
			for(int j : graph.get(i)){
				if(nodesInLargestConnectedComponent.contains(j)&&!nodesInLargestConnectedComponent.contains(i)){
					throw new RuntimeException("Connected nodes in different components");
				}
				
				if(nodesInLargestConnectedComponent.contains(i)){
					if(nodesInLargestConnectedComponent.contains(j)){
						if(!largestConnectedComponentGraph.containsKey(i)){
							largestConnectedComponentGraph.put(i, new HashSet<Integer>());
						}
						if(!largestConnectedComponentGraph.containsKey(j)){
							largestConnectedComponentGraph.put(j, new HashSet<Integer>());
						}
						largestConnectedComponentGraph.get(i).add(j);
						largestConnectedComponentGraph.get(j).add(i);
					}else{
						throw new RuntimeException("Connected nodes in different components");
					}
				}
			}
		}
		return largestConnectedComponentGraph;
	}
	
	public static HashMap<Integer, HashMap<Integer, HashSet<Integer>>> allConnectedCompentents(HashMap<Integer, HashSet<Integer>> graph)
	{
		HashMap<Integer,HashMap<Integer,HashSet<Integer>>> components = new  HashMap<Integer,HashMap<Integer,HashSet<Integer>>>();
		HashSet<Integer> visited = new HashSet<Integer>();
		int spanFromNode = findLargestNode(graph);
		int compId = 0;
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.addLast(spanFromNode);
		for (int i : graph.keySet()) {
			if (!visited.contains(i)) {
				components.put(compId, new HashMap<Integer,HashSet<Integer>>());
				
				HashSet<Integer> visitedThisRound = spanFrom(i, graph,components.get(compId));
				visited.addAll(visitedThisRound);
				compId++;
			}
		}
		return components;
	}
	
	public static HashSet<Integer> spanFrom(int from,
			HashMap<Integer, HashSet<Integer>> graph,
			HashMap<Integer, HashSet<Integer>> compentent) {
		LinkedList<Integer> queue = new LinkedList<Integer>();
		HashSet<Integer> visited = new HashSet<Integer>();
		queue.addLast(from);
		while(!queue.isEmpty()){
			int here = queue.pollFirst();
			if(!compentent.containsKey(here)){
				compentent.put(here, new HashSet<Integer>());
			}
			visited.add(here);
			for(int i : graph.get(here)){
				if(!compentent.containsKey(i)){
					compentent.put(i, new HashSet<Integer>());
				}
				compentent.get(here).add(i);
				compentent.get(i).add(here);
				if(!visited.contains(i)) {
					queue.addLast(i);
				}
			}
		}
		return visited;
	}
	public static TreeNode<Integer> randomSpanningTree(HashMap<Integer, HashSet<Integer>> graph,int root){
		LinkedList<TreeNode<Integer>> stack = new LinkedList<TreeNode<Integer>>();
		HashSet<Integer> visited = new HashSet<Integer>();
		TreeNode<Integer> rootNode = new TreeNode<Integer>(root,0);
		Random rand = new Random();
		stack.addFirst(rootNode);
		while(!stack.isEmpty()){
			TreeNode<Integer> here = stack.pollFirst();
			if (!visited.contains(here.data)) {
				if (here.parent != null) {
					TreeNode.add(rootNode, here);
				}
				visited.add(here.data);
			}
			if(!visited.containsAll(graph.get(here.data))){
				stack.add(here);
				Integer[] candiates = graph.get(here.data).toArray(new Integer[]{});
				int randPos = rand.nextInt(candiates.length);
				//int randPos = 0;
				while(visited.contains(candiates[randPos])){
					randPos = rand.nextInt(candiates.length);
					//randPos ++;
				}
				stack.add(new TreeNode<Integer>(candiates[randPos], here.level+1,here));
			}
		}
		return rootNode;
	}
	
	public static TreeNode<Integer> randomSpanningTree(HashMap<Integer, HashSet<Integer>> graph) {
		Random rand = new Random();
		Integer[] nodes = graph.keySet().toArray(new Integer[]{});
		return randomSpanningTree(graph,nodes[rand.nextInt(nodes.length)]);
	}
	
	public static void getNSpanningTrees(int n,
			HashMap<Integer, HashSet<Integer>> graph,
			HashSet<String> setOfSpanTrees) {
		while(setOfSpanTrees.size()<n){
			TreeNode<Integer> root = Graph.randomSpanningTree(graph);
			setOfSpanTrees.add(TreeNode.toString(root));
		}
		
	}
	
	public static HashMap<Integer, Integer> getGraphDegreeMap(
			HashMap<Integer, HashSet<Integer>> g) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i : g.keySet()) {
			map.put(i, g.get(i).size());
		}
		return map;
	}

    public static HashMap<Integer, HashSet<Integer>> genRandomGraphSnap(int n, int e) throws IOException, InterruptedException {
        HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>();
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("python endos-renyi.py "+n+" "+e+" "+"tempOutGraph");
        pr.waitFor();
        BufferedReader b = new BufferedReader(new FileReader("tempOutGraph"));
        String line = b.readLine();
        while(line.contains("#")){
            line=b.readLine();
        }
        while(line!=null){
            String[] nodes = line.split("\\s+");
            int na = Integer.parseInt(nodes[0]);
            int nb = Integer.parseInt(nodes[1]);
            if(!graph.containsKey(na)){
                graph.put(na, new HashSet<Integer>());
            }
            if(!graph.containsKey(nb)){
                graph.put(nb, new HashSet<Integer>());
            }
            graph.get(na).add(nb);
            graph.get(nb).add(na);
            line=b.readLine();
        }
        b.close();
        File toDelete = new File("tempOutGraph");
        toDelete.delete();
        return graph;
    }
	public static HashMap<Integer, HashMap<Integer, Integer>> getNodeNodeDistanceMapSnap(HashMap<Integer, HashSet<Integer>> g) throws IOException, InterruptedException {
	    HashMap<Integer, HashMap<Integer, Integer>> nodeNodeDistanceMap = new HashMap<Integer, HashMap<Integer,Integer>>();
	    Graph.writeGraph(g, "tmpGraph");
	    BufferedWriter w = new BufferedWriter(new FileWriter("nodes"));
	    for(int i : g.keySet()){
	        w.write(i+"\n");
	    }
	    w.flush();w.close();
	    Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("./DistanceToNodesInFile tmpGraph nodes");
        pr.waitFor();
        BufferedReader r = new BufferedReader(new FileReader("DistancetmpGraph"));
        String line = r.readLine();
        while(line!=null){
            String[] lineSeg=line.split("\\s+");
            int a = Integer.parseInt(lineSeg[0]);
            int b = Integer.parseInt(lineSeg[1]);
            int dis = Integer.parseInt(lineSeg[2]);
            if(!nodeNodeDistanceMap.containsKey(a)){
                nodeNodeDistanceMap.put(a, new HashMap<Integer,Integer>());
            }
            if(!nodeNodeDistanceMap.containsKey(b)){
                nodeNodeDistanceMap.put(b, new HashMap<Integer,Integer>());
            }
            nodeNodeDistanceMap.get(a).put(b, dis);
            nodeNodeDistanceMap.get(b).put(a, dis);
            line=r.readLine();
        }
        File toDelete = new File("DistancetmpGraph");
        toDelete.delete();
        toDelete = new File("tmpGraph");
        toDelete.delete();
        toDelete = new File("nodes");
        toDelete.delete();
        r.close();
	    return nodeNodeDistanceMap;
	}
	
	public static HashMap<Integer, HashMap<Integer, Integer>> getNodeNodeDistanceMap(
			HashMap<Integer, HashSet<Integer>> g) {
		HashMap<Integer, HashMap<Integer, Integer>> nodeNodeDistanceMap = new HashMap<Integer, HashMap<Integer,Integer>>();
		for(int i : g.keySet()){
			nodeNodeDistanceMap.put(i, new HashMap<Integer, Integer>());
		}
		//int c = 0;
		for(int i : g.keySet()){
		    //System.out.println(c++);
			HashSet<Integer> visited = new HashSet<Integer>();
			LinkedList<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>> ();
			queue.addLast(new Pair<Integer, Integer>(i,0));
			while(!queue.isEmpty()){
				Pair<Integer, Integer> here = queue.pollFirst();
				if (!visited.contains(here.key)) {
					visited.add(here.key);
					nodeNodeDistanceMap.get(i).put(here.key, here.value);
					for (int j : g.get(here.key)) {
					    if (!visited.contains(j)) {
					        queue.addLast(new Pair<Integer, Integer>(j, here.value + 1));
					    }
					}
				}
			}
		}
		return nodeNodeDistanceMap;
	}
	
	public static TreeMap<Integer, HashSet<Integer>> getDegreeToNodeMap(
			HashMap<Integer, HashSet<Integer>> g) {
		TreeMap<Integer,  HashSet<Integer>> map = new TreeMap<Integer,  HashSet<Integer>>();
		for (int i : g.keySet()) {
			if(!map.containsKey(g.get(i).size())){
				map.put(g.get(i).size(), new HashSet<Integer>());
			}
			map.get(g.get(i).size()).add(i);
		}
		return map;
	}
	public static void renum(HashMap<Integer, HashSet<Integer>> g,
			HashMap<Integer, Integer> gRenum,
			HashMap<Integer, Integer> rGRenum) {
		int inx = 0;
		for(int i : g.keySet()){
			gRenum.put(i, inx);
			rGRenum.put(inx, i);
			inx++;
		}
	}
	public static HashMap<Integer, HashSet<Integer>> genRandomGraph(int n,int e){
	    HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>> ();
	    graph.put(0, new HashSet<Integer>());
	    Random rand = new Random();
	    int numE  = 0;
	    for(int i = 1;i< n; i++){
	        Integer[] currentSet = graph.keySet().toArray(new Integer[]{});
	        int randPosition = rand.nextInt(currentSet.length);
	        if(!graph.containsKey(i)){
	            graph.put(i, new HashSet<Integer>());
	        }
	        graph.get(currentSet[randPosition]).add(i);
	        graph.get(i).add(currentSet[randPosition]);
	        numE++;
        }
	    while(numE<e){
	        int a, b;
	        do{
	            a = rand.nextInt(n);
	            b = rand.nextInt(n);
	        }while(graph.get(a).contains(b));
	        
	        graph.get(a).add(b);
	        graph.get(b).add(a);
	        numE++;
	    }
	    return graph;
	}
	
    public static HashMap<Integer, HashSet<Integer>> sampleGraph(HashMap<Integer, HashSet<Integer>> g,double p){
	    HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>> ();
	    HashMap<Integer, HashSet<Integer>> visitedGraph = new HashMap<Integer, HashSet<Integer>> ();
        int largestNode = findLargestNode(g);
	    LinkedList<Pair<Integer,Integer>> queue = new LinkedList<Pair<Integer,Integer>>();
	    HashSet<Integer> visited = new HashSet<Integer>();
	    queue.addFirst(new Pair<Integer, Integer>(largestNode,-1));
	    while(!queue.isEmpty()){
            Pair<Integer, Integer> here = queue.pollLast();
            if (visited.contains(here.key)) {
                continue;
            } else {
                visited.add(here.key);
            }
	        
	        if(here.value!=-1){
	            if(!graph.containsKey(here.key)){
	                graph.put(here.key, new HashSet<Integer>());
	            }
	            if(!graph.containsKey(here.value)){
	                graph.put(here.value, new HashSet<Integer>());
	            }
	            graph.get(here.key).add(here.value);
	            graph.get(here.value).add(here.key);
	            
	            if(!visitedGraph.containsKey(here.key)){
	                visitedGraph.put(here.key, new HashSet<Integer>());
                }
                if(!visitedGraph.containsKey(here.value)){
                    visitedGraph.put(here.value, new HashSet<Integer>());
                }
                visitedGraph.get(here.key).add(here.value);
                visitedGraph.get(here.value).add(here.key);
	        }
	        for(Integer i : g.get(here.key)){
	            if(!visited.contains(i)){
	                queue.addFirst(new Pair<Integer, Integer>(i,here.key));
	            }
	        }
	    }
	    
        for (int i : g.keySet()) {
            for (int j : g.get(i)) {
                if ((!visitedGraph.containsKey(i))
                        || (!visitedGraph.get(i).contains(j))) {
                    if (Math.random() < p) {
                        if (!graph.containsKey(i)) {
                            graph.put(i, new HashSet<Integer>());
                        }
                        if (!graph.containsKey(j)) {
                            graph.put(j, new HashSet<Integer>());
                        }
                        graph.get(i).add(j);
                        graph.get(j).add(i);
                    }
                    if (!visitedGraph.containsKey(i)) {
                        visitedGraph.put(i, new HashSet<Integer>());
                    }
                    if (!visitedGraph.containsKey(j)) {
                        visitedGraph.put(j, new HashSet<Integer>());
                    }
                    visitedGraph.get(i).add(j);
                    visitedGraph.get(j).add(i);
                }
            }
        }
        return graph;
    }
    public static HashMap<Integer, HashMap<Integer, Integer>> getTopNodeNodeDistanceMapSnap(
            HashMap<Integer, HashSet<Integer>> g,
            TreeMap<Integer, HashSet<Integer>> degreeToNodeMap, int n, HashSet<Integer> anchorCandidates) throws IOException, InterruptedException {
        HashMap<Integer, HashMap<Integer, Integer>> nodeNodeDistanceMap = new HashMap<Integer, HashMap<Integer,Integer>>();
        Graph.writeGraph(g, "tmpGraph");
        BufferedWriter w = new BufferedWriter(new FileWriter("nodes"));
        int count = 0;
        for(int i : degreeToNodeMap.descendingKeySet()){
            for(int j : degreeToNodeMap.get(i)){
                if(count > n){
                    break;
                }
                anchorCandidates.add(j);
                w.write(j+"\n");
                count++;
            }
            if(count > n){
                break;
            }
        }
        w.flush();w.close();
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("./DistanceToNodesInFile tmpGraph nodes");
        pr.waitFor();
        BufferedReader r = new BufferedReader(new FileReader("DistancetmpGraph"));
        String line = r.readLine();
        while(line!=null){
            String[] lineSeg=line.split("\\s+");
            int a = Integer.parseInt(lineSeg[0]);
            int b = Integer.parseInt(lineSeg[1]);
            int dis = Integer.parseInt(lineSeg[2]);
            if(!nodeNodeDistanceMap.containsKey(a)){
                nodeNodeDistanceMap.put(a, new HashMap<Integer,Integer>());
            }
            if(!nodeNodeDistanceMap.containsKey(b)){
                nodeNodeDistanceMap.put(b, new HashMap<Integer,Integer>());
            }
            nodeNodeDistanceMap.get(a).put(b, dis);
            nodeNodeDistanceMap.get(b).put(a, dis);
            line=r.readLine();
        }
        File toDelete = new File("DistancetmpGraph");
        toDelete.delete();
        toDelete = new File("tmpGraph");
        toDelete.delete();
        toDelete = new File("nodes");
        toDelete.delete();
        r.close();
        return nodeNodeDistanceMap;
    }
    public static double getInDegree(
            HashMap<Integer, HashSet<Integer>> rgraph, int rnode) {
        int degree = 0;
        for(int a :rgraph.keySet()){
            for(int b : rgraph.get(a)){
                if(b==rnode){
                    degree+=1;
                }
            }
        }
        return degree;
    }
    public static void getDirectedGraphDegreeMap(
            HashMap<Integer, HashSet<Integer>> graph,
            HashMap<Integer, Integer> graphInDegreeMap,
            HashMap<Integer, Integer> graphOutDegreeMap) {
        for(int nodeA : graph.keySet()){
            graphInDegreeMap.put(nodeA, 0);
            graphOutDegreeMap.put(nodeA, 0);
            for(int nodeB : graph.get(nodeA)){
                graphInDegreeMap.put(nodeB, 0);
                graphOutDegreeMap.put(nodeB, 0);
            }
        }
        for(int nodeA : graph.keySet()){
            for(int nodeB : graph.get(nodeA)){
                int curNodeAOutDegree = graphOutDegreeMap.get(nodeA)+1;
                int curNodeBInDegree = graphInDegreeMap.get(nodeB)+1;
                graphOutDegreeMap.put(nodeA, curNodeAOutDegree);
                graphInDegreeMap .put(nodeB, curNodeBInDegree );
            }
        }
        
    }
    public static int getDistance(HashMap<Integer, HashSet<Integer>> graph, int u, int v,boolean zeroHopFlag){
        int numUNei = graph.get(u).size();
        int numVNei = graph.get(v).size();
        int zeroHop = Math.abs(numUNei -numVNei );
        if(zeroHopFlag){
            return zeroHop;
        }
        Integer[] neiU = graph.get(u).toArray(new Integer[]{});
        Integer[] neiV = graph.get(v).toArray(new Integer[]{});
        ArrayList<Integer> uNeiDegree = new ArrayList<Integer>();
        ArrayList<Integer> vNeiDegree = new ArrayList<Integer>();
        int numInNeiDegreeSet = Math.max(numVNei, numUNei);
        for(int i = 0;i<numInNeiDegreeSet;i++){
            if(neiU.length>i){
                uNeiDegree.add(graph.get(neiU[i]).size());
            }else{
                uNeiDegree.add(0);
            }
            if(neiV.length>i){
                vNeiDegree.add(graph.get(neiV[i]).size());
            }else{
                vNeiDegree.add(0);
            }
        }
        Collections.sort(uNeiDegree,Collections.reverseOrder());
        Collections.sort(vNeiDegree,Collections.reverseOrder());
        int oneHop=zeroHop;
        for(int i = 0;i< uNeiDegree.size();i++){
            oneHop+=Math.abs(uNeiDegree.get(i)-vNeiDegree.get(i));
        }
        return oneHop;
    }
    public static int getDistance(Map<Integer, HashSet<Integer>> graph, int u, int degree, List<Integer> neighborDegrees,boolean zeroHopFlag){
        int numUNei = graph.get(u).size();
        int zeroHop = Math.abs(numUNei - degree);
        if(zeroHopFlag){
            return zeroHop;
        }
        Integer[] neiU = graph.get(u).toArray(new Integer[]{});
        ArrayList<Integer> uNeiDegree = new ArrayList<Integer>();
        ArrayList<Integer> vNeiDegree = new ArrayList<Integer>();
        int numInNeiDegreeSet = Math.max(neighborDegrees.size(), numUNei);
        for(int i = 0;i<numInNeiDegreeSet;i++){
            if(neiU.length>i){
                uNeiDegree.add(graph.get(neiU[i]).size());
            }else{
                uNeiDegree.add(0);
            }
            if(neighborDegrees.size()>i){
                vNeiDegree.add(neighborDegrees.get(i));
            }else{
                vNeiDegree.add(0);
            }
        }
        Collections.sort(uNeiDegree,Collections.reverseOrder());
        Collections.sort(vNeiDegree,Collections.reverseOrder());
        int oneHop=zeroHop;
        for(int i = 0;i< uNeiDegree.size();i++){
            oneHop+=Math.abs(uNeiDegree.get(i)-vNeiDegree.get(i));
        }
        return oneHop;
    }
    public static HashMap<Integer, HashMap<Integer, Integer>> joinDegreeDistr(Map<Integer, HashSet<Integer>> g){
        //System.out.println("size"+g.get(591).size());
        HashMap<Integer, HashMap<Integer, Integer>> jdd = new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int i : g.keySet()){
            //System.out.println("size"+g.get(591).size());
            int degreeI = g.get(i).size();
            //System.out.println(degreeI+"=g.get("+i+").size();");
            //System.out.println("after"+g.get(591).size());
            if(!jdd.containsKey(degreeI)){
                jdd.put(degreeI, new HashMap<Integer,Integer>());
            }
            for(int j : g.get(i)){
                degreeI = g.get(i).size();
                int degreeJ = g.get(j).size();
                if(j<i){
                    continue;
                }
                
                if(degreeI>degreeJ){
                    int tmp = degreeJ;
                    degreeJ=degreeI;
                    degreeI=tmp;
                    if(!jdd.containsKey(degreeI)){
                        jdd.put(degreeI, new HashMap<Integer,Integer>());
                    }
                }
                int numWithSameJoinDegreeDist=1;
                if(jdd.get(degreeI).containsKey(degreeJ)){
                    numWithSameJoinDegreeDist+=jdd.get(degreeI).get(degreeJ);
                }
                jdd.get(degreeI).put(degreeJ, numWithSameJoinDegreeDist);

                
            }
        }
        return jdd;
    }
    public static void printJoinDegreeDist(HashMap<Integer, HashMap<Integer, Integer>> jdd){
        int total = 0;
        for(int i : jdd.keySet()){
            for(int j : jdd.get(i).keySet()){
                total+=jdd.get(i).get(j);
            }
        }
        for(int i : jdd.keySet()){
            for(int j : jdd.get(i).keySet()){
                System.out.println(i+" "+j+" "+jdd.get(i).get(j)+" "+(double)jdd.get(i).get(j)/total);
            }
        }
    }
    public static TreeMap<Integer, Integer> getDegreeDist(HashMap<Integer, HashSet<Integer>> g){
        TreeMap<Integer,Integer> degreeDist = new TreeMap<Integer, Integer>();
        for(int i : g.keySet()){
            int count = 1;
            if(degreeDist.containsKey(g.get(i).size())){
                count+=degreeDist.get(g.get(i).size());
            }
            degreeDist.put(g.get(i).size(), count);
        }
        return degreeDist;
    }
    
}
