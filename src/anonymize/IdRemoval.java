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
package anonymize;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;

public class IdRemoval {
    public static void main(String[] args) throws IOException {
        idRemoval(args[0],args[1]);
    }
    public static void idRemoval(String ga,String go) throws IOException{
        HashMap<String, HashSet<String>> g = Graph.readUndirectedStringGraph(ga);
        int counter = 0;
        HashMap<String,Integer> mapping = new HashMap<String,Integer>();
        HashMap<Integer, HashSet<Integer>> o = new HashMap<Integer, HashSet<Integer>>();
        for(String i : g.keySet()){
            mapping.put(i, counter);
            System.out.println(i+" "+counter);
            counter++;
        }
        for(String a : g.keySet()){
            for(String b :g.get(a)){
                if(!o.containsKey(mapping.get(a))){
                    o.put(mapping.get(a), new HashSet<Integer>());
                }
                if(!o.containsKey(mapping.get(b))){
                    o.put(mapping.get(b), new HashSet<Integer>());
                }
                o.get(mapping.get(a)).add(mapping.get(b));
                o.get(mapping.get(b)).add(mapping.get(a));
            }
        }
        Graph.writeGraph(o, go);
    }
}
