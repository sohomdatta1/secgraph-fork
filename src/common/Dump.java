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
package common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class Dump {
    public static void dump(Map<Integer, Integer> map, BufferedWriter w,String header) throws IOException{
        w.write("################"+header+"###############\n");
        for(int i : map.keySet()){
            w.write(i+" "+map.get(i)+"\n");
        }
        w.flush();
    }
    
    public static void dump(TreeMap<Integer, HashSet<Integer>> map, BufferedWriter w,String header) throws IOException{
        w.write("################"+header+"###############\n");
        for(int i : map.keySet()){
            w.write("key: "+i+" with "+map.get(i).size()+" values: ");
            for(int j : map.get(i)){
                w.write(j+" ");
            }
            w.write("\n");
        }
        w.flush();
    }

    public static <K> void dump(
            HashMap<Integer, HashMap<Integer,K>> map,
            BufferedWriter w, String header) throws IOException {
        w.write("################"+header+"###############\n");
        for(int i : map.keySet()){
            for(int j : map.keySet()){
                w.write(i+" "+j+" "+map.get(i).get(j)+"\n");
            }
        }
        w.flush();
    }


    public static void dumpGraph(HashMap<Integer, HashSet<Integer>> g1,
            BufferedWriter debug, String string) {
        // TODO Auto-generated method stub
        
    }
}
