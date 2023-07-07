package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class RenumberAllGraphs {
    public static void main(String[] args) throws IOException {
        /*HashMap<Integer, HashSet<Integer>> ori = Graph.readUndirectedGraph("graphs/n2/Email-EnronLCC.txt");
        HashMap<Integer, HashSet<Integer>> addRem10 = Graph.readUndirectedGraph("graphs/n2/randAddAndDelete-10Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> addRem20 = Graph.readUndirectedGraph("graphs/n2/randAddAndDelete-20Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> addRem30 = Graph.readUndirectedGraph("graphs/n2/randAddAndDelete-30Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> swap10 = Graph.readUndirectedGraph("graphs/n2/randSw-10Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> swap20 = Graph.readUndirectedGraph("graphs/n2/randSw-20Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> swap30 = Graph.readUndirectedGraph("graphs/n2/randSw-30Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> kda5 = Graph.readUndirectedGraph("graphs/n2/priority5Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> kda10 = Graph.readUndirectedGraph("graphs/n2/priority10Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> kda15 = Graph.readUndirectedGraph("graphs/n2/priority15Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> cluster5 = Graph.readUndirectedGraph("graphs/n2/intercluster5Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> cluster10 = Graph.readUndirectedGraph("graphs/n2/intercluster10Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> cluster15 = Graph.readUndirectedGraph("graphs/n2/intercluster15Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> randWalk2 = Graph.readUndirectedGraph("graphs/n2/link2Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> randWalk4 = Graph.readUndirectedGraph("graphs/n2/link4Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> randWalk6 = Graph.readUndirectedGraph("graphs/n2/link6Email-Enron.txt");
        HashMap<Integer, HashSet<Integer>> dp100 = Graph.readUndirectedGraph("graphs/n2/Email-EnronWith10TuplesEpslon100");
        HashMap<Integer, HashSet<Integer>> dp200 = Graph.readUndirectedGraph("graphs/n2/Email-EnronWith10TuplesEpslon200");
        HashMap<Integer, HashSet<Integer>> dp300 = Graph.readUndirectedGraph("graphs/n2/Email-EnronWith10TuplesEpslon300");
        
        HashMap<Integer, Integer> reNumSeq = new HashMap<Integer, Integer>();
        int newId = 0;
        for (int i : ori.keySet()) {
            reNumSeq.put(i, newId);
            newId++;
        }
        BufferedWriter w;
        w=new BufferedWriter(new FileWriter("graphs/reNum/Email-EnronLCC.txt")); reNum( ori,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randAddAndDelete-10Email-Enron.txt")); reNum( addRem10,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randAddAndDelete-20Email-Enron.txt")); reNum( addRem20,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randAddAndDelete-30Email-Enron.txt")); reNum( addRem30,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randSw-10Email-Enron.txt")); reNum( swap10,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randSw-20Email-Enron.txt")); reNum( swap20,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/randSw-30Email-Enron.txt")); reNum( swap30,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/priority5Email-Enron.txt")); reNum( kda5,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/priority10Email-Enron.txt")); reNum( kda10,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/priority15Email-Enron.txt")); reNum( kda15,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/intercluster5Email-Enron.txt")); reNum( cluster5,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/intercluster10Email-Enron.txt")); reNum( cluster10,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/intercluster15Email-Enron.txt")); reNum( cluster15,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/link2Email-Enron.txt")); reNum( randWalk2,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/link4Email-Enron.txt")); reNum( randWalk4,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/link6Email-Enron.txt")); reNum( randWalk6,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/Email-EnronWith10TuplesEpslon100")); reNum( dp100,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/Email-EnronWith10TuplesEpslon200")); reNum( dp200,reNumSeq,w); w.flush(); w.close();
        w=new BufferedWriter(new FileWriter("graphs/reNum/Email-EnronWith10TuplesEpslon300")); reNum( dp300,reNumSeq,w); w.flush(); w.close();
    */
        
        HashMap<Integer, HashSet<Integer>> ori = Graph.readUndirectedGraph(args[0]);
        HashMap<Integer, Integer> reNumSeq = new HashMap<Integer, Integer>();
        int newId = 0;
        for (int i : ori.keySet()) {
            reNumSeq.put(i, newId);
            newId++;
        }
        BufferedWriter w=new BufferedWriter(new FileWriter(args[1])); 
        reNum(ori,reNumSeq,w);
        w.flush();w.close();
    }
    public static void reNum(HashMap<Integer, HashSet<Integer>> g,HashMap<Integer, Integer> reNumSeq,BufferedWriter w) throws IOException{
        for(int i : g.keySet()){
            for(int j : g.get(i)){
                w.write(reNumSeq.get(i)+" "+reNumSeq.get(j)+"\n");
            }
        }
    }
}
