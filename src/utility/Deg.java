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
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;

public class Deg {
    public static double getDeg(HashMap<Integer, HashSet<Integer>> g1,HashMap<Integer, HashSet<Integer>> g2){
        HashMap<Integer, Integer> g1NodeToDegree = new HashMap<Integer,Integer>();
        HashMap<Integer, Integer> g2NodeToDegree = new HashMap<Integer,Integer>();
        if(!g1.keySet().containsAll(g2.keySet()) ||!g2.keySet().containsAll(g1.keySet())){
            throw new RuntimeException("keyset unmatched");
        }
        for(int i : g1.keySet()){
            g1NodeToDegree.put(i, g1.get(i).size());
        }
        for(int i : g2.keySet()){
            g2NodeToDegree.put(i, g2.get(i).size());
        }
        double ab=0,aa=0,bb=0;
        for(int i :g1.keySet()){
            aa+=g1NodeToDegree.get(i)*g1NodeToDegree.get(i);
            bb+=g2NodeToDegree.get(i)*g2NodeToDegree.get(i);
            ab+=g1NodeToDegree.get(i)*g2NodeToDegree.get(i);
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    public static double getDeg(String g1, String g2) throws IOException{
        HashMap<Integer, HashSet<Integer>> ga=Graph.readUndirectedGraph(g1);
        HashMap<Integer, HashSet<Integer>> gb=Graph.readUndirectedGraph(g2);
        return getDeg(ga,gb);
        
    }
    public static void main(String[] args) throws IOException {
        args=new String[]{"G","G1"};
        if(args.length!=2){
            System.out.println("usage: g1 g2");
            System.exit(0);
        }
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(args[0]);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(args[1]);
        System.out.print(getDeg(g1,g2));
        }    
}
