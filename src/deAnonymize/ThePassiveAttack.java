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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import common.Graph;
import common.TreeNode;
public class ThePassiveAttack {
    static Random rand = new Random();
    
    public static void main(String[] args) throws IOException, InterruptedException {
        thePassiveAttack(args[0], args[1], args[2], args[3]);
       
    }
 
    public static void thePassiveAttack(String graphName, String coliationGraphFile,String coliationDegFile,String out) throws IOException, InterruptedException {
        System.out.println("Start reading g");
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(graphName);
        System.out.println("Done reading g");
        HashMap<Integer, HashSet<Integer>> coliationInternalGraph = Graph.readUndirectedGraph(coliationGraphFile);
        HashMap<Integer, Integer> coliationDeg = new HashMap<Integer,Integer>();
        HashMap<Integer, Integer> coliationDegFreq = new HashMap<Integer,Integer>();
        fillColiationDegAndDegFreq(coliationDegFile, coliationDeg,coliationDegFreq);
        HashSet<Integer> candidatesWithRightDeg = getCandidatesWithRightDeg(g,coliationDeg.get(0));
/*        System.out.print("candidatesWithRightDeg: [");
        for(int i : candidatesWithRightDeg){
            System.out.print(i+", ");
        }
        System.out.println("]");
*/
        int temp = coliationDegFreq.get(coliationDeg.get(0))-1;
        coliationDegFreq.put(coliationDeg.get(0), temp);
        HashSet<Integer> candidateWithRightDegAndNeiDeg = getCandidateWithRightDegAndNeiDeg(g, coliationDegFreq,candidatesWithRightDeg);
        
/*        System.out.print("candidateWithRightDegAndNeiDeg: [");
        for(int i : candidateWithRightDegAndNeiDeg){
            System.out.print(i+", ");
        }
        System.out.println("]");
*/        
        getCandWithRightStruc(g,coliationDeg,candidateWithRightDegAndNeiDeg,coliationInternalGraph,out);
    }

    private static void fillColiationDegAndDegFreq(String coliationDegFile,
            HashMap<Integer, Integer> coliationDeg,
            HashMap<Integer, Integer> coliationDegFreq)
            throws FileNotFoundException, IOException {
        BufferedReader r = new BufferedReader(new FileReader(coliationDegFile));
        String l =  r.readLine();       
        while(l!=null){
            String[] lArray = l.split("[\\s,;:]+");
            if(lArray.length!=2){
                r.close();
                throw new IOException(coliationDegFile+" format error");
            }
            int node = Integer.parseInt(lArray[0]);
            int deg  = Integer.parseInt(lArray[1]);
            coliationDeg.put(node, deg);
            if(!coliationDegFreq.containsKey(deg)){
                coliationDegFreq.put(deg, 0);
            }
            int newFreq = coliationDegFreq.get(deg)+1;
            coliationDegFreq.put(deg, newFreq);
            l=r.readLine();
        }
        r.close();
    }
    public static void getCandWithRightStruc(
            HashMap<Integer, HashSet<Integer>> g,HashMap<Integer, Integer> coliationDeg, HashSet<Integer> candidateWithRightDegAndNeiDeg,
            HashMap<Integer, HashSet<Integer>> coliationInternalGraph,String out) throws IOException {
        TreeNode<Integer> root = new TreeNode<>();
        root.data=-1;root.level=-1;root.parent=null;
        
        LinkedList<TreeNode<Integer>> stack = new LinkedList<TreeNode<Integer>>();
        ArrayList<Integer> orderedNodesInColiation = new ArrayList<Integer>();
        orderedNodesInColiation.add(0);
        for(int i : coliationInternalGraph.keySet()){
            if(i!=0){
                orderedNodesInColiation.add(i);
            }
        }
        for(int i : candidateWithRightDegAndNeiDeg){
            TreeNode<Integer> here = new TreeNode<Integer>(i,0,root);
            root.children.add(here);
            stack.push(here);
        }
        HashSet<TreeNode<Integer>> lastNodesFound = new HashSet<>();
        while(!stack.isEmpty()){
            TreeNode<Integer> here = stack.pop();
            if(here.data==0||here.data==13||here.data==102||here.data==17){
                System.out.println();
            }
            int currentLevel = here.level+1;
            if(currentLevel+1>orderedNodesInColiation.size()){
                continue;
            }
            int currentSeekingDeg = coliationDeg.get(orderedNodesInColiation.get(currentLevel));
            TreeNode<Integer> levl0Node = findLevel0Node(here);
            HashSet<Integer> seekingCandidates = g.get(levl0Node.data);
            for(int i : seekingCandidates){
                if(g.get(i).size()==currentSeekingDeg &&(structureCheck(here,orderedNodesInColiation,i,g,orderedNodesInColiation.get(currentLevel),coliationInternalGraph))){
                    
                    TreeNode<Integer> candidate = new TreeNode<Integer>(i,currentLevel,here);
                    here.children.add(candidate);
                    stack.push(candidate);
                    if(candidate.level+1==orderedNodesInColiation.size()){
                        lastNodesFound.add(candidate);
                    }
                }
            }
            
        }
        BufferedWriter w = new BufferedWriter(new FileWriter(out));
        int k = 0;
        for(TreeNode<Integer> i : lastNodesFound){
            TreeNode<Integer> t = i;
            k++;
            w.write("Solution: "+k+"\n");
            while(t.level>=0){
                w.write("ColiationID: " + orderedNodesInColiation.get(t.level) +" GId "+t.data+"\n");
                t=t.parent;
            }
            w.write("\n");
        }
        w.flush();w.close();
    }
    private static boolean structureCheck(TreeNode<Integer> here, ArrayList<Integer> orderedNodesInColiation, int idInG,
            HashMap<Integer, HashSet<Integer>> g, Integer idInColiation,
            HashMap<Integer, HashSet<Integer>> coliationInternalGraph) {
        TreeNode<Integer> temp = here;
        while(temp.level>=0){
            int tempData = temp.data;
            int tempLevel = temp.level;
            int tempInColiation = orderedNodesInColiation.get(tempLevel);
            if(coliationInternalGraph.get(idInColiation).contains(tempInColiation)){
                if(!g.get(tempData).contains(idInG)){
                    return false;
                }
            }else{
                if(g.get(tempData).contains(idInG)){
                    return false;
                } 
            }
            temp=temp.parent;
        }
        return true;
    }

    private static TreeNode<Integer> findLevel0Node(TreeNode<Integer> here) {
        TreeNode<Integer> temp = here;
        do{
            if(temp.level==0) return temp;
            temp=temp.parent;
        }while(temp!=null);
        return null;
    }

    private static HashSet<Integer> getCandidatesWithRightDeg(
            HashMap<Integer, HashSet<Integer>> g,
            int deg) {
        HashSet<Integer> candidatesWithRightDeg = new HashSet<Integer>();
        for(int i : g.keySet()){
            if(g.get(i).size()==deg){
                candidatesWithRightDeg.add(i);
            }
        }
        return candidatesWithRightDeg;
    }

    private static HashSet<Integer> getCandidateWithRightDegAndNeiDeg(
            HashMap<Integer, HashSet<Integer>> g,
            HashMap<Integer, Integer> coliationDegFreq,
            HashSet<Integer> candidatesWithRightDeg
            ) {
        HashSet<Integer> candidatesWithRightDegAndNeiDeg = new HashSet<Integer>();
           for(int i : candidatesWithRightDeg){
               HashMap<Integer, Integer> tempColiationDegFreqMap = new HashMap<Integer,Integer>(coliationDegFreq);
               for(int j : g.get(i)){
                   int jDeg = g.get(j).size();
                   if(tempColiationDegFreqMap.containsKey(jDeg) && tempColiationDegFreqMap.get(jDeg)>0){
                       int newFreq = tempColiationDegFreqMap.get(jDeg)-1;
                       tempColiationDegFreqMap.put(jDeg, newFreq);
                   }
               }
               
               boolean foundCand = true;
               for(int j : tempColiationDegFreqMap.keySet()){
                   if(tempColiationDegFreqMap.get(j)!=0){
                       foundCand=false;
                   }
               }
               if(foundCand){
                   candidatesWithRightDegAndNeiDeg.add(i);
               }
              
           }
           return candidatesWithRightDegAndNeiDeg;
    }

    
}
