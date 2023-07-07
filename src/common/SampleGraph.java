package common;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class SampleGraph {
    public static void main(String[] args) throws IOException {
        String gName = args[0];
        String outName = args[1];
        double p = Double.parseDouble(args[2])/1000;
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(gName);
        HashMap<Integer, HashSet<Integer>> gOut= Graph.sampleGraph(g, p);
        Graph.writeGraph(gOut, outName);
    }
}
