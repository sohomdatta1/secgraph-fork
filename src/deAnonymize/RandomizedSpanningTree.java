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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import common.Graph;
import common.Pair;
import convenience.RTED;

public class RandomizedSpanningTree {
	public static void randomSpanTree(HashMap<Integer, HashSet<Integer>> social, HashMap<Integer, HashSet<Integer>> comm,int numOfSpanTree, HashMap<Integer, Integer> cTosMapping, String outFile) throws IOException {
		HashSet<String> spanningTreesOfSocial = new HashSet<String>();
		HashSet<String> spanningTreesOfComm   = new HashSet<String>();
		Graph.getNSpanningTrees(numOfSpanTree,comm,spanningTreesOfSocial);
		Graph.getNSpanningTrees(numOfSpanTree,social,spanningTreesOfComm);
		double lowestEditDistance = Double.MAX_VALUE;
		BufferedWriter w = new BufferedWriter(new FileWriter(outFile));
		LinkedList<int[]> lowestMatching = new LinkedList<int[]>();
		LinkedList<int[]> matching = new LinkedList<int[]>();
		for(String t1 : spanningTreesOfSocial){
			for(String t2 : spanningTreesOfComm){
				Pair<Double, LinkedList<int[]>> pair = RTED.computeDistanceAndMapping(t1, t2,cTosMapping);
				matching = pair.getValue();
				double score = pair.getKey();
				if(score<lowestEditDistance){
					lowestEditDistance=score;
					lowestMatching=matching;
				}
			}
		}
		int numRight = 0;
		for(int[] i : lowestMatching){
			w.write(i[0]+" "+i[1]+"\n");
			if(i[0]==i[1]){
				numRight++;
			}
		}
		w.write("number correct = "+numRight+"\n");
		w.flush();w.close();
	}
	
	public static void main(String[] args) throws IOException {
        runRSP(args[0],args[1],args[2],args[3],args[4]);
    }

    public static void runRSP(String g1, String g2, String seed,String numberOfTrees, String out) throws IOException {
        HashMap<Integer, HashSet<Integer>> commG = Graph.readUndirectedGraph(g1);
        HashMap<Integer, HashSet<Integer>> socialG = Graph.readUndirectedGraph(g2);
        HashMap<Integer, Integer> cTosMapping = Graph.getSeeds(seed);
        RandomizedSpanningTree.randomSpanTree(socialG, commG, Integer.parseInt(numberOfTrees),cTosMapping,out);
    }
}
