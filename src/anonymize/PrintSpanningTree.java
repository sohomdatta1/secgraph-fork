package anonymize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import common.Graph;

public class PrintSpanningTree {
    public static void main(String[] args) throws IOException {
        String inputFile = "graphs/deAnaEron/Email-EnronLCC.txt";
        HashMap<Integer, HashSet<Integer>>  g = Graph.readUndirectedGraph(inputFile);
        int largestNode = Graph.findLargestNode(g);
        span(g,largestNode);
    }
    private static HashSet<Integer> span(HashMap<Integer, HashSet<Integer>> g,
            int start) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter("graphs/deAnaEron/t/SpanTree"));
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(start);
        HashSet<Integer> visited = new HashSet<Integer>();
        visited.add(start);
        while(!queue.isEmpty()){
            int here = queue.poll();
            for(int i : g.get(here)){
                if(!visited.contains(i)){
                    queue.add(i);
                    visited.add(i);
                    w.write(here+" "+i+"\n");
                }
            }
        }
        w.flush();w.close();
        return visited;
    }
}
