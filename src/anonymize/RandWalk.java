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
import java.util.Random;

import common.Graph;

public class RandWalk {
    //static BufferedWriter logger;
    public static void main(String[] args) throws IOException {
        rWalk(args[0], args[1], args[2], args[3]);
    }
    public static void rWalk(String inG, String outG, String NumStep, String MaxRetry) throws IOException {
        HashMap<Integer, HashSet<Integer>> g = Graph.readUndirectedGraph(inG);
        int numStep = Integer.parseInt(NumStep);
        int maxRetry = Integer.parseInt(MaxRetry);
        HashMap<Integer, HashSet<Integer>> ng = pertubationAlgorithm(g, numStep, maxRetry);
        Graph.writeGraph(ng, outG);
    }
    public static HashMap<Integer, HashSet<Integer>> pertubationAlgorithm(HashMap<Integer, HashSet<Integer>> g, int t , int M) throws IOException{
        Random rand = new Random();
        //logger = new BufferedWriter(new FileWriter("o"));
        HashMap<Integer, HashSet<Integer>> ng =new  HashMap<Integer, HashSet<Integer>>();
        for(int u : g.keySet()){
            if(!ng.containsKey(u)){
                ng.put(u, new HashSet<Integer>());
            }
            int count = 1;
            for(int v : g.get(u)){
                int loop = 1;
                int z = v;
                do{
                    for(int i = 0;i<t-1;i++){
                        z=randomWalk(g,z);                        
                    }
                    loop++;
                }while(loop<=M &&( u==z || ng.get(u).contains(z) ) );
                if(loop<=M){
                    if(count==1){
                        if(!ng.containsKey(z)){
                            ng.put(z, new HashSet<Integer>());
                        }
                        ng.get(u).add(z);
                        ng.get(z).add(u);
                        //logger.write(u+" "+v+" changed to "+u+" "+z+"\n");
                    }else{
                        int degU = g.get(u).size();
                        double p = (0.5*degU-1)/(degU-1);
                        if(rand.nextDouble()<p){
                            if(!ng.containsKey(z)){
                                ng.put(z, new HashSet<Integer>());
                            }
                            ng.get(u).add(z);
                            ng.get(z).add(u);
                            //logger.write(u+" "+v+" changed to "+u+" "+z+"\n");
                        }
                    }
                    count++;
                }
            }
        }
        //logger.flush();logger.close();
        return ng;
    }

    private static int randomWalk(HashMap<Integer, HashSet<Integer>> g, int v) {
        Random rand = new Random();
        Integer[] vNei = g.get(v).toArray(new Integer[]{});
        return vNei[rand.nextInt(vNei.length)];
    }

}
