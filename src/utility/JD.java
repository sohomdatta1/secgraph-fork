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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;
import common.Utilities;

public class JD {
    public static double compareJD(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2){
        HashMap<Integer, HashMap<Integer, Integer>> g1Res = Graph.joinDegreeDistr(g1);
        HashMap<Integer, HashMap<Integer, Integer>> g2Res = Graph.joinDegreeDistr(g2);
        HashMap<Integer, HashSet<Integer>> keys = new HashMap<Integer, HashSet<Integer>>();
        for(int i : g1Res.keySet()){
            if(!keys.containsKey(i)){
                keys.put(i, new HashSet<Integer>());
            }
            for(int j : g1Res.get(i).keySet()){
                keys.get(i).add(j);
            }
        }
        for(int i : g2Res.keySet()){
            if(!keys.containsKey(i)){
                keys.put(i, new HashSet<Integer>());
            }
            for(int j : g2Res.get(i).keySet()){
                keys.get(i).add(j);
            }
        }
        ArrayList<Integer> g1List = new ArrayList<Integer>();
        ArrayList<Integer> g2List = new ArrayList<Integer>();
        for(int i : keys.keySet()){
            for(int j : keys.get(i)){
                if(g1Res.containsKey(i) && g1Res.get(i).containsKey(j)){
                    g1List.add(g1Res.get(i).get(j));
                }else{
                    g1List.add(0);
                }
                if(g2Res.containsKey(i) && g2Res.get(i).containsKey(j)){
                    g2List.add(g2Res.get(i).get(j));
                }else{
                    g2List.add(0);
                }
            }
        }
        return Utilities.cosSimInt(g1List, g2List);
    }
    public static double joinDeg(String G1,String G2) throws IOException {
        
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(G2);
        return compareJD(g1,g2);
    }
    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("usage g1 g2");
            System.exit(0);
        }
        System.out.println(joinDeg(args[0],args[1]));;
    }
}
