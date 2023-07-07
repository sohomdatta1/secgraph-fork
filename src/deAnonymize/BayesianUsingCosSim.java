package deAnonymize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import common.Graph;
import common.MinWeightHungarianAlgorithm;
import common.Pair;
import common.Utilities;

public class BayesianUsingCosSim {
    public static void main(String[] args) throws IOException {
        bayesianUsingCosSim(args[0],args[1],args[2]);
    }
    public static void bayesianUsingCosSim(String G1,String G2,String out
            ) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(out));
        HashMap<Integer, HashSet<Integer>> g1 = Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2 = Graph.readUndirectedGraph(G2);
        if(g1.size()!=g2.size()){
            w.close();
            throw new RuntimeException("Graph node size does not match");
        }
        List<Integer> sortedG1Nodes = getNodesInDegreeSortedOrder(g1);
        List<Integer> sortedG2Nodes = getNodesInDegreeSortedOrder(g2);
        int n = 2;
        HashMap<Integer, HashMap<Integer,Integer>> g1DistnaceMap = new HashMap<Integer, HashMap<Integer,Integer>>();
        HashMap<Integer, HashMap<Integer,Integer>> g2DistnaceMap = new HashMap<Integer, HashMap<Integer,Integer>>();
        List<Integer> g1Matched = new ArrayList<Integer>();
        List<Integer> g2Matched = new ArrayList<Integer>();
        List<Integer> g1Anchor = new ArrayList<Integer>();
        List<Integer> g2Anchor = new ArrayList<Integer>();
        while(g1Matched.size()<g1.size() && g2Matched.size()<g2.size()){
            System.out.println(g1Matched.size());
            List<Integer> g1NodesConsider = copyNHighestNodesToConsiderList(sortedG1Nodes, n);
            List<Integer> g2NodesConsider = copyNHighestNodesToConsiderList(sortedG2Nodes, n);
            HashMap<Integer, ArrayList<Integer>> g1CandFinger = getCandFingerPrint(g1Anchor,g1NodesConsider,g1,g1DistnaceMap);
            HashMap<Integer, ArrayList<Integer>> g2CandFinger = getCandFingerPrint(g2Anchor,g2NodesConsider,g2,g2DistnaceMap);
            
            HashMap<Integer, HashMap<Integer, Float>> g1ToG2ToNormRMap = normalize(
                                                                            calcG1ToG2R(
                                                                                    g1, g2, 
                                                                                    g1NodesConsider, g2NodesConsider,
                                                                                    g1Anchor,g2Anchor,
                                                                                    g1DistnaceMap,g2DistnaceMap,
                                                                                    g1CandFinger,g2CandFinger));
            
            float[][] score  = new float[g2NodesConsider.size()][g1NodesConsider.size()];
            float[][] matrix = new float[g2NodesConsider.size()][g1NodesConsider.size()];
            
            for(int g2Cand = 0;g2Cand<g2NodesConsider.size();g2Cand++){
                for(int g1Cand = 0;g1Cand<g1NodesConsider.size();g1Cand++){
                    score[g2Cand][g1Cand]=g1ToG2ToNormRMap.get(g1NodesConsider.get(g1Cand)).get(g2NodesConsider.get(g2Cand));
                    matrix[g2Cand][g1Cand]=-1*score[g2Cand][g1Cand];
                }
            }
           
            MinWeightHungarianAlgorithm match = new MinWeightHungarianAlgorithm();
            int[][] result = match.computeAssignments(matrix);
            PriorityQueue<Pair<Pair<Integer,Integer>,Float>> queue = createNodePairToScoreQueue();
            for(int i = 0;i<result.length;i++){
                int nodeG1 = g1NodesConsider.get(result[i][1]);
                int nodeG2 = g2NodesConsider.get(result[i][0]);
                queue.add(new Pair<Pair<Integer,Integer>, Float>(new Pair<Integer,Integer>(nodeG1, nodeG2),score[result[i][0]][result[i][1]]));
            }
            int count = 0;
            g1Anchor = new ArrayList<Integer>();
            g2Anchor = new ArrayList<Integer>();
            g1Matched = new ArrayList<Integer>();
            g2Matched = new ArrayList<Integer>();
            while(!queue.isEmpty()){
                Pair<Pair<Integer, Integer>, Float> here = queue.poll();
                if(count<n/2){
                    g1Anchor.add(here.getKey().getKey());
                    g2Anchor.add(here.getKey().getValue());                    
                }
                g1Matched.add(here.getKey().getKey());
                g2Matched.add(here.getKey().getValue());
                count++;
            }
            n=n*2;
            if(n>g1.size()){
                n=g1.size();
            }
           /* int right = 0;
            for(int i = 0;i<g1Matched.size();i++){
                if(g1Matched.get(i).equals(g2Matched.get(i))){
                    right++;
                }
            }*/
            //System.out.println("numRight = "+right+"/"+g1Matched.size()+" "+g2Matched.size()+" "+g1.size()+" "+g2.size()+"\n");
        }
        HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
        for(int i = 0;i<g1Matched.size();i++){
            res.put(g2Matched.get(i),   g1Matched.get(i));
        }
        int r = 0;
        for(int i =0;i<g1Matched.size();i++){
            if(g1Matched.get(i).equals(g2Matched.get(i))){
                r++;
            }
            w.write(g1Matched.get(i)+" "+g2Matched.get(i)+"\n");
        }
        w.write(out+" "+r+"/"+g1Matched.size()+" "+g1.size()+" "+g2.size()+"\n");
        w.flush();w.close();
        
    }

    private static HashMap<Integer, ArrayList<Integer>> getCandFingerPrint(
            List<Integer> anchor, List<Integer> nodesConsider,
            HashMap<Integer, HashSet<Integer>> g,
            HashMap<Integer, HashMap<Integer, Integer>> distnaceMap) {
        HashMap<Integer, ArrayList<Integer>> candFinger = new HashMap<Integer, ArrayList<Integer>>();
        for(int i : nodesConsider){
            candFinger.put(i, getFingerPrint(g, i, anchor, distnaceMap));
        }
        return candFinger;
    }

    private static HashMap<Integer, HashMap<Integer, Float>> calcG1ToG2R(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            List<Integer> g1NodesConsider, List<Integer> g2NodesConsider, List<Integer> g1Anchor, List<Integer> g2Anchor,
            HashMap<Integer, HashMap<Integer,Integer>> g1DistnaceMap,HashMap<Integer, HashMap<Integer,Integer>> g2DistnaceMap,
            HashMap<Integer, ArrayList<Integer>> g1CandFinger,HashMap<Integer, ArrayList<Integer>> g2CandFinger) {
        HashMap<Integer,HashMap<Integer, Float>> g1ToG2ToRMap = new HashMap<Integer,HashMap<Integer, Float>> ();
        for(int g1Node : g1NodesConsider){
            for(int g2Node : g2NodesConsider){
                float R = (float) Utilities.cosSimInt(g1CandFinger.get(g1Node), g2CandFinger.get(g2Node));//calcRValue(g1,g2,g1Node,g2Node,g1Anchor,g2Anchor, g1DistnaceMap, g2DistnaceMap,g1CandFinger,g2CandFinger);
                if(!g1ToG2ToRMap.containsKey(g1Node)){
                    g1ToG2ToRMap.put(g1Node, new HashMap<Integer,Float>());
                }
                g1ToG2ToRMap.get(g1Node).put(g2Node, R);
            }
        }
        return g1ToG2ToRMap;
    }
    
    private static HashMap<Integer, HashMap<Integer, Float>> normalize(
            HashMap<Integer, HashMap<Integer, Float>> g1ToG2ToRMap) {
        HashMap<Integer, Float> g1NodeSums = new HashMap<Integer,Float>();
        HashMap<Integer, Float> g2NodeSums = new HashMap<Integer,Float>();
        HashMap<Integer, HashMap<Integer, Float>> normRValMap = new HashMap<Integer, HashMap<Integer, Float>>();
        for(int i : g1ToG2ToRMap.keySet()){
            for(int j : g1ToG2ToRMap.get(i).keySet()){
                if(!g1NodeSums.containsKey(i)){
                    g1NodeSums.put(i, 0.0f);
                }
                if(!g2NodeSums.containsKey(j)){
                    g2NodeSums.put(j, 0.0f);
                }
                float g1Sum = g1NodeSums.get(i)+g1ToG2ToRMap.get(i).get(j);
                float g2Sum = g2NodeSums.get(j)+g1ToG2ToRMap.get(i).get(j);
                g1NodeSums.put(i, g1Sum);
                g2NodeSums.put(j, g2Sum);
                
            }
        }
        for(int i : g1ToG2ToRMap.keySet()){
            normRValMap.put(i, new HashMap<Integer,Float>());
            for(int j : g1ToG2ToRMap.get(i).keySet()){
                float r = g1ToG2ToRMap.get(i).get(j);
                float g1Sum = g1NodeSums.get(i);
                float g2Sum = g2NodeSums.get(j);
                normRValMap.get(i).put(j, (float) (r/Math.sqrt(g1Sum*g2Sum)));
            }
        }
        return normRValMap;
    }

    private static PriorityQueue<Pair<Pair<Integer, Integer>, Float>> createNodePairToScoreQueue() {
        return new PriorityQueue<Pair<Pair<Integer,Integer>,Float>>(100*100, new Comparator<Pair<Pair<Integer,Integer>,Float>>(){
            @Override
            public int compare(Pair<Pair<Integer,Integer>, Float> o1,
                    Pair<Pair<Integer,Integer>, Float> o2) {
                return Float.compare(o1.getValue(), o2.getValue());
            }
        });
    }   
    
    /*private static float calcRValue(HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2, int g1Node, int g2Node, List<Integer> g1Anchor, List<Integer> g2Anchor,
            HashMap<Integer, HashMap<Integer,Integer>> g1DistnaceMap,HashMap<Integer, HashMap<Integer,Integer>> g2DistnaceMap,
            HashMap<Integer, ArrayList<Integer>> g1CandFinger,HashMap<Integer, ArrayList<Integer>> g2CandFinger) {
       
        return 0f;
    }*/
    private static ArrayList<Integer> getFingerPrint(
            HashMap<Integer, HashSet<Integer>> g, int node,
            List<Integer> anchor,HashMap<Integer, HashMap<Integer,Integer>> distnaceMap) {
        ArrayList<Integer> finger = new ArrayList<Integer>();
        finger.add(g.get(node).size());
        for(int anchorNode : anchor){
            finger.add(distance(anchorNode,node,g,distnaceMap));
        }
        return finger;
    }

    private static Integer distance(int anchorNode, int node,
            HashMap<Integer, HashSet<Integer>> g, HashMap<Integer, HashMap<Integer, Integer>> distnaceMap) {
        if(!distnaceMap.containsKey(anchorNode)){
            distnaceMap.put(anchorNode, new HashMap<Integer,Integer>());
            span(g, anchorNode, distnaceMap);
        }
        return distnaceMap.get(anchorNode).get(node);
    }

    private static ArrayList<Integer> copyNHighestNodesToConsiderList(
            List<Integer> sortedNodes, int n) {
        ArrayList<Integer> nodesConsider = new ArrayList<Integer>();
        for(int i = 0;i<n;i++){
            if(i<sortedNodes.size()){
                nodesConsider.add(sortedNodes.get(i));
            }
        }
        return nodesConsider;
    }
    
    private static void span(HashMap<Integer, HashSet<Integer>> graph, int id,
            HashMap<Integer, HashMap<Integer, Integer>> disntace) {
            LinkedList<Pair<Integer,Integer>> queue = new LinkedList<Pair<Integer,Integer>>();
            HashSet<Integer> visited = new HashSet<Integer>();
            Pair<Integer, Integer> start = new Pair<Integer, Integer>(id, 0);
            disntace.get(id).put(id, 0);
            visited.add(id);
            queue.addLast(start);
            while(!queue.isEmpty()){
                Pair<Integer, Integer> here = queue.pollFirst();
                int dist = here.getValue()+1;
                for(int i : graph.get(here.getKey())){
                    if (!visited.contains(i)) {
                        visited.add(i);
                        disntace.get(id).put(i, dist);  
                        queue.add(new Pair<Integer, Integer>(i, dist));
                    }
                }
            }
        }
    
    private static LinkedList<Integer> getNodesInDegreeSortedOrder(
            HashMap<Integer, HashSet<Integer>> s) {
        LinkedList<Integer> sortedNode = new LinkedList<Integer>();
        TreeMap<Integer, HashSet<Integer>> sortedSDegreeToNodes = Graph.getDegreeToNodeMap(s);
        for(int i : sortedSDegreeToNodes.descendingKeySet()){
            for(int j : sortedSDegreeToNodes.get(i)){
                sortedNode.add(j);
            }
        }
        return sortedNode;
    }
}
