package quant;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import common.Graph;

public class getDegreeDistFile {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("usage g degreeDistFileName");
            System.exit(0);
        }
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(args[0]);
        BufferedWriter w = new BufferedWriter(new FileWriter(args[1]));
        TreeMap<Integer, Integer>  d = Graph.getDegreeDist(g);
        for(int i : d.keySet()){
            w.write(i+","+d.get(i)+"\n");
        }
        w.flush();w.close();
    }
}
