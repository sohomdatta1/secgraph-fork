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

import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import common.Graph;

public class Distance {
    public static HashMap<Integer, Integer>  getDistDist(String g, int n) throws IOException{
        HashMap<Integer, HashSet<Integer>> graph = Graph.readUndirectedGraph(g);
        Integer[] nodes = graph.keySet().toArray(new Integer[]{});
        HashSet<Integer> randomNodes = new HashSet<>();
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        while(randomNodes.size()<n){
            randomNodes.add(nodes[rand.nextInt(nodes.length)]);
        }
        //sb.append("\\\"");
        for(int i : randomNodes){
            sb.append(" "+i+" ");
        }
        //sb.append("\\\"");
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"Snap-2.4/examples/secGraphTools/Distance",g,sb.toString()};
        //System.out.println(commands);
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(proc.getInputStream()));
        HashMap<Integer, Integer> distanceFreq = new HashMap<Integer, Integer>();

        String l = null;
        while ((l = stdInput.readLine()) != null) {
            String[] stringArray= l.split(" ");
            int freq = 1;
            int distance = Integer.parseInt(stringArray[2]);
            if(distanceFreq.containsKey(distance)){
                freq += distanceFreq.get(distance);
            }
            distanceFreq.put(distance, freq);
        }
        return distanceFreq;
    }
    public static double compareDistanceDist(HashMap<Integer, Integer> g1,HashMap<Integer, Integer> g2){
        HashSet<Integer> key = new HashSet<Integer>(g1.keySet());
        key.addAll(g2.keySet());
        double aa=0,ab=0,bb=0;
        for(int i : key){
            if(g1.containsKey(i)){
                aa+=g1.get(i)*g1.get(i);
            }
            if(g2.containsKey(i)){
                bb+=g2.get(i)*g2.get(i);
            }
            if(g1.containsKey(i)&&g2.containsKey(i)){
                ab+=g1.get(i)*g2.get(i);
            }
        }
        return ab/(Math.sqrt(aa)*Math.sqrt(bb));
    }
    public static double compareDistance(HashMap<String, Double> c1,HashMap<String, Double>c2){
        if(!c1.keySet().containsAll(c2.keySet()) ||!c2.keySet().containsAll(c1.keySet())){
            throw new RuntimeException("keyset unmatched");
        }
        double ab=0,aa=0,bb=0;
       /* for(String i :c1.keySet()){
            aa+=c1.get(i)*c1.get(i);
            bb+=c2.get(i)*c2.get(i);
            ab+=c1.get(i)*c2.get(i);
        }*/
        for(String i :c1.keySet()){
            aa+=c1.get(i)/11*c1.get(i)/11;
            bb+=c2.get(i)/11*c2.get(i)/11;
            ab+=c1.get(i)/11*c2.get(i)/11;
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("usage G1 G2 n");
            System.exit(0);
        }
        System.out.println(getDistance(args[0],args[1],args[2]));
    }
    public static double getDistance(String G1, String G2, String n) throws IOException {
        HashMap<Integer, Integer> d1=getDistDist(G1,Integer.parseInt(n));
        HashMap<Integer, Integer> d2=getDistDist(G2,Integer.parseInt(n));
        return compareDistanceDist(d1,d2);
    }
}
