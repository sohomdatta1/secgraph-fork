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

import java.util.HashMap;
import java.util.HashSet;

public class improvedUserMatching {
    public static void userMatching(
            HashMap<Integer, HashSet<Integer>> g1,
            HashMap<Integer, HashSet<Integer>> g2,
            HashMap<Integer, Integer> seed_mapping,int D, int T , int k){
        
        HashMap<Integer, HashMap<Integer, Integer>> nodeToNodeToScore =new HashMap<Integer, HashMap<Integer,Integer>>();
        for(int seedA : seed_mapping.keySet()){
            int seedB = seed_mapping.get(seedA);
            for(int i : g1.get(seedA)){
                if(!nodeToNodeToScore.containsKey(i)){
                    nodeToNodeToScore.put(i, new HashMap<Integer,Integer>());
                }
                for(int j : g2.get(seedB)){
                    int numCommon = 1;
                    if(nodeToNodeToScore.get(i).containsKey(j)){
                        numCommon=nodeToNodeToScore.get(i).get(j)+1;
                    }
                    nodeToNodeToScore.get(i).put(j, numCommon);
                }
            }
        }
        
        HashSet<Integer> nodesInG1Matched = new HashSet<Integer>();
        HashSet<Integer> nodesInG2Matched = new HashSet<Integer>();
        for(int i : seed_mapping.keySet()){
            nodesInG1Matched.add(i);
            nodesInG2Matched.add(seed_mapping.get(i));
        }
        
        for(int i = 0;i<k;i++){
            for(int j = (int) (Math.log(D)/Math.log(2)); j>0;j--){
                
                for(int a : nodeToNodeToScore.keySet()){
                    for(int b : nodeToNodeToScore.get(a).keySet()){
                        
                    }
                }
            }
        }
    }
}
