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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet; 
import java.util.TreeMap;

import common.Graph;
import common.Utilities;

public class ReliableEmail {
    public static int getReliableEmail(
            HashMap<Integer, HashSet<Integer>> g, int numNeiToAccept,
            HashSet<Integer> spammingUsers, boolean twoHop) {
        HashSet<Integer> neiOfSpammingUser = new HashSet<Integer>();
        for (int i : spammingUsers) {
            for (int oneHopNei : g.get(i)) {
                neiOfSpammingUser.add(oneHopNei);
                if (twoHop) {
                    for (int twoHopNei : g.get(oneHopNei)) {
                        neiOfSpammingUser.add(twoHopNei);
                    }
                }
            }
        }
        HashSet<Integer> spammedUser = new HashSet<Integer>();
        for (int here : neiOfSpammingUser) {
            int numOfSpammingNei = 0;
            for (int nei : g.get(here)) {
                if (spammingUsers.contains(nei)) {
                    numOfSpammingNei++;
                }
                if (twoHop) {
                    for (int twoHopNei : g.get(nei)) {
                        if (spammingUsers.contains(twoHopNei)) {
                            numOfSpammingNei++;
                        }
                        if (numOfSpammingNei > numNeiToAccept) {
                            spammedUser.add(nei);
                            break;
                        }
                    }
                }
                if (numOfSpammingNei > numNeiToAccept) {
                    spammedUser.add(nei);
                    break;
                }
            }
        }

        return spammedUser.size();
    }
    public static double compareRE(HashMap<Integer, HashSet<Integer>> g1,
                                   HashMap<Integer, HashSet<Integer>> g2,
                                   ArrayList<Integer> g1SpamNodesSeq,
                                   ArrayList<Integer> g2SpamNodesSeq,
                                   int numNeiToAccept,
                                   boolean twoHop){
        TreeMap<Integer, Double> c1 = new TreeMap<Integer, Double>();
        TreeMap<Integer, Double> c2 = new TreeMap<Integer, Double>();
        
        HashSet<Integer> g1SpammingUsers = new HashSet<Integer>();
        HashSet<Integer> g2SpammingUsers = new HashSet<Integer>();
        int g1NodePerPercent = g1.keySet().size()/100;
        int g2NodePerPercent = g2.keySet().size()/100;        
        int g1Position = 0,g2Position=0;
        for(int i = 0;i< 10;i++){
            while(g1SpammingUsers.size()<g1NodePerPercent*i){
                g1SpammingUsers.add(g1SpamNodesSeq.get(g1Position));
                g1Position++;
            }
            while(g2SpammingUsers.size()<g2NodePerPercent*i){
                g2SpammingUsers.add(g2SpamNodesSeq.get(g2Position));
                g2Position++;
            }
            
            c1.put(i, ((double) getReliableEmail(g1, numNeiToAccept, g1SpammingUsers, twoHop))/g1.size());
            c2.put(i, ((double) getReliableEmail(g2, numNeiToAccept, g2SpammingUsers, twoHop))/g2.size());
        }
        double aa = 0;
        double bb = 0;
        double ab = 0;
        for(int percentToSpam : c1.keySet()){
           aa+=c1.get(percentToSpam)*c1.get(percentToSpam);
           bb+=c2.get(percentToSpam)*c2.get(percentToSpam);
           ab+=c1.get(percentToSpam)*c2.get(percentToSpam);
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    
    public static double compareRE(String G1,String G2,
                                 String g1SpamNodesSeqFN,
                                 String g2SpamNodesSeqFN,
                                 String neiToAccept,
                                 String twoHopStr) throws IOException{
        HashMap<Integer, HashSet<Integer>> g1=Graph.readUndirectedGraph(G1);
        HashMap<Integer, HashSet<Integer>> g2=Graph.readUndirectedGraph(G2);
        ArrayList<Integer> g1SpamNodesSeq = new ArrayList<Integer>(70000);
        ArrayList<Integer> g2SpamNodesSeq = new ArrayList<Integer>(70000);
        BufferedReader r = new BufferedReader(new FileReader(g1SpamNodesSeqFN));
        String l= r.readLine();
        while(l!=null){
            g1SpamNodesSeq.add(Integer.parseInt(l));
            l=r.readLine();
        }
        r.close();
        
        r = new BufferedReader(new FileReader(g2SpamNodesSeqFN));
        l= r.readLine();
        while(l!=null){
            g2SpamNodesSeq.add(Integer.parseInt(l));
            l=r.readLine();
        }
        r.close();
        return compareRE(g1,g2,
                g1SpamNodesSeq,g2SpamNodesSeq,
                Integer.parseInt(neiToAccept),Boolean.parseBoolean(twoHopStr));
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 6) {
            System.out.println("usage ReliableEmail g1FileName g2FileName, "
                             + "g1SpamNodesSeqFile g2SpamNodesSeqFile numNeiToAccept twoHop");
            System.exit(0);
        }
        System.out.println(compareRE(args[0], args[1], args[2], args[3], args[4], args[5]));
    }
}
