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
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;

public class CalcDeAnaRandWalk {
    public static void main(String[] args) throws IOException {
        if(args.length!=4){
            System.out.println("usage g1 g2 resFile outFile");
            System.exit(0);
        }
        String resFile = args[2];
        HashMap<Integer, HashSet<Integer>> g1 = Graph.readUndirectedGraph(args[0]);
        HashMap<Integer, HashSet<Integer>> g2 = Graph.readUndirectedGraph(args[1]);
        HashMap<Integer, Integer> g2ToG1Nodes = new HashMap<Integer, Integer>();
        BufferedReader br = new BufferedReader(new FileReader(resFile));
        BufferedWriter w  = new BufferedWriter(new FileWriter(args[3]));
        String l = br.readLine();
        int correctAbove1=0;int correctAbove2=0;
        HashSet<Integer> n1=new HashSet<Integer>(), n2 = new HashSet<Integer>();
        while(l!=null){
            if (l.matches(".*[a-zA-Z/].*")) {
                l = br.readLine();
                continue;
            }
            String[] sArray = l.split("\\s+");
            g2ToG1Nodes.put(Integer.parseInt(sArray[1]), Integer.parseInt(sArray[0]));
            l=br.readLine();
            n1.add(Integer.parseInt(sArray[1]));n2.add(Integer.parseInt(sArray[0]));
        }
        br.close();
        
        
        HashMap<Integer, Integer> nodeToNumRightEdge = new HashMap<Integer, Integer>();
        for(int i : g2.keySet()){
            int count = 0;
            for(int j : g2.get(i)){
                if(!g2ToG1Nodes.containsKey(i) || !g2ToG1Nodes.containsKey(j)){
                    continue;
                }
                int a = g2ToG1Nodes.get(i);
                int b = g2ToG1Nodes.get(j);
                if(g1.get(a).contains(b)||g1.get(b).contains(a)){
                    count++;
                }
            }
            nodeToNumRightEdge.put(i, count);
        }
        for(int i : nodeToNumRightEdge.keySet()){
            w.write(i+" "+g1.get(i).size()+" "+nodeToNumRightEdge.get(i)+"\n");
            if(nodeToNumRightEdge.get(i)>=1){
                correctAbove1++;
            }
            if(nodeToNumRightEdge.get(i)>=2){
                correctAbove2++;
            }
        }
        w.write("correctOneAndAbove "+correctAbove1+"\n");
        w.write("correctTwoAndAbove "+correctAbove2+"\n");
        w.flush();w.close();
    }
}
