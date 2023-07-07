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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import common.Graph;

public class checkEdge {
    public static void main(String[] args) throws IOException {
        //args=new String[]{"FBLCC","countLCCUserMatchingRwalk6Res_0"};
        if (args.length != 2) {
            System.out.println("usage graph resFile");
            System.exit(0);
        }
        HashMap<Integer, HashSet<Integer>> g = Graph
                .readUndirectedGraph(args[0]);
        HashSet<Integer> m = readMatchedNodes(args[1]);
        
        int one = 0, two = 0;
        double numEdge=0;
        for (int i : g.keySet()) {
            for (int j : g.get(i)) {
                if (i > j) {
                    continue;
                }
                numEdge++;
                if (m.contains(i) && m.contains(j)) {
                    two++;
                    one++;
                } else if (m.contains(i) || m.contains(j)) {
                    one++;
                }
            }
        }
        System.out.println(one/numEdge + " " + two/numEdge);
    }

    private static HashSet<Integer> readMatchedNodes(String s) throws IOException {
        if(s.matches(".*walk.*")){
            return readRwalkRes(s);
        }
        return readRes(s);
    }

    private static HashSet<Integer> readRwalkRes(String s) throws IOException {
        HashSet<Integer> matched = new HashSet<Integer>();
        BufferedReader r = new BufferedReader(new FileReader(s));
        String l = r.readLine();
        while(l!=null){
            if (l.matches(".*[a-zA-Z/].*")) {
                l = r.readLine();
                continue;
            }
            String[] arr = l.split("\\s+");
            if(Integer.parseInt(arr[2])>0){
                matched.add(Integer.parseInt(arr[0]));
            }
            l = r.readLine();
        }
        r.close();
        return matched;
    }

    private static HashSet<Integer> readRes(String s) throws IOException {
        HashSet<Integer> matched = new HashSet<Integer>();
        BufferedReader r = new BufferedReader(new FileReader(s));
        String l = r.readLine();
        while(l!=null){
            if (l.matches(".*[a-zA-Z/].*")) {
                l = r.readLine();
                continue;
            }
            String[] arr = l.split("\\s+");
            if(Integer.parseInt(arr[1])==Integer.parseInt(arr[0])){
                matched.add(Integer.parseInt(arr[0]));
            }
            l = r.readLine();
        }
        r.close();
        return matched;
    }
}
