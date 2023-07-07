package utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import common.Graph;

public class GenRand {
    public static HashSet<String> generateRandomPairs(HashMap<Integer, HashSet<Integer>>g,int n){
        Random rand = new Random();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        HashSet<String> randPairs = new HashSet<String>();
        while(randPairs.size()<n){
            int a = allNodes[rand.nextInt(allNodes.length)];
            int b = a;
            while(b==a){
                b = allNodes[rand.nextInt(allNodes.length)];
            }
            if(a<b){
                randPairs.add(a+" "+b);
            }else{
                randPairs.add(b+" "+a);
            }
        }
        return randPairs;
    }
    public static HashSet<Integer> generateRandomNode(HashMap<Integer, HashSet<Integer>>g,int n){
        Random rand = new Random();
        Integer[] allNodes = g.keySet().toArray(new Integer[]{});
        HashSet<Integer> randNodes = new HashSet<Integer>();
        while(randNodes.size()<n){
            randNodes.add(allNodes[rand.nextInt(allNodes.length)]);
        }
        return randNodes;
    }
    public static void main(String[] args) throws IOException {
        HashMap<Integer, HashSet<Integer>>g=Graph.readUndirectedGraph("graphs/n/Email-EnronLCC.txt");
        BufferedWriter w = new BufferedWriter(new FileWriter("graphs/n2/allNodes.txt"));
        /*       HashSet<String> res = generateRandomPairs(g, 1000);
        for(String s : res ){
            System.out.println(s);
        }
 */       //HashSet<Integer> randNode = generateRandomNode(g, 1000);
    }
}
