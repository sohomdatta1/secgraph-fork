package utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import common.Graph;

public class getRandomPairs {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("usage g numPairs outFile");
            System.exit(0);
        }
        HashMap<Integer, HashSet<Integer>> g = Graph
                .readUndirectedGraph(args[0]);
        int numpairs = Integer.parseInt(args[1]);
        HashSet<String> pairs = new HashSet<String>();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        Random rand = new Random();
        while(pairs.size()<numpairs){
            int a = allNodes[rand.nextInt(allNodes.length)];
            int b = a;
            while(a==b){
                b = allNodes[rand.nextInt(allNodes.length)];
            }
            if(a<b){
                int temp=a;
                a=b;
                b=temp;
            }
            String here = a +" "+b;
            pairs.add(here);
        }
        BufferedWriter w = new BufferedWriter(new FileWriter(args[2]));
        for(String s : pairs){
            w.write(s+"\n");
        }
        w.flush();w.close();
    }
}
