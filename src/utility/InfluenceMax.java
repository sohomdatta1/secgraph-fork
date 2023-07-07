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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;
import common.Utilities;

public class InfluenceMax {
    public static void makeGraphForInfluenceMax(HashMap<Integer, HashSet<Integer>> g, BufferedWriter w) throws IOException{
        HashSet<String> written = new HashSet<String>();
        for(int i :g.keySet()){
            w.write(i+","+i+"\n");
        }
        w.write("\n");
        for(int i : g.keySet()){
            for(int j : g.get(i)){
                String t = i+","+j+","+"0.7";
                if(!written.contains(t)){
                w.write(t+"\n");
                written.add(t);
                }
                String t2 = j+","+i+","+"0.7";
                if(!written.contains(t2)){
                    w.write(t2+"\n");
                    written.add(t2);}
            }
        }
        return ;
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(runInfluxMax(args[0],args[1]));
    }
    public static double runInfluxMax(String g1Name, String g2Name) throws IOException, InterruptedException {
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(g1Name);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(g2Name);
        BufferedWriter w = new BufferedWriter(new FileWriter("infMaxG1"));
        makeGraphForInfluenceMax(g1, w);
        w.flush();
        w.close();
        w = new BufferedWriter(new FileWriter("infMaxG2"));
        makeGraphForInfluenceMax(g2, w);
        w.flush();
        w.close();
        HashMap<Integer, Double> g1Inf = runInfMax(g1Name);
        HashMap<Integer, Double> g2Inf = runInfMax(g2Name);
        Files.delete(FileSystems.getDefault().getPath("infMaxG1"));
        Files.delete(FileSystems.getDefault().getPath("infMaxG2"));
        return Utilities.mapCosSim(g1Inf, g2Inf);
    }
    
    private static HashMap<Integer, Double> runInfMax(String g1Name) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String commands = "./influmax/influmax -n:"+g1Name+" -o:temp";
        Process proc = rt.exec(commands);
        proc.waitFor();
        HashMap<Integer, Double> nToProb = new HashMap<Integer, Double>();
        BufferedReader r = new BufferedReader(new FileReader("influence-info-temp.txt"));
        r.readLine();r.readLine();r.readLine();
        String s = null;
        while ((s = r.readLine()) != null) {
           if(s.equals("")){
               break;
           }
            String[] splitStr = s.split(",");
           int node = Integer.parseInt(splitStr[0]);
           double val = Double.parseDouble(splitStr[1]);
           nToProb.put(node, val);
        }
        r.close();
        Files.delete(FileSystems.getDefault().getPath("influence-info-temp.txt"));
        Files.delete(FileSystems.getDefault().getPath("influence-average-temp.txt"));
        return nToProb;
    }  
}
