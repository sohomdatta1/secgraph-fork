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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utilities {
	public static <E> List<List<E>> generatePerm(List<E> original) {
	     if (original.size() == 0) { 
	       List<List<E>> result = new ArrayList<List<E>>();
	       result.add(new ArrayList<E>());
	       return result;
	     }
	     E firstElement = original.remove(0);
	     List<List<E>> returnValue = new ArrayList<List<E>>();
	     List<List<E>> permutations = generatePerm(original);
	     for (List<E> smallerPermutated : permutations) {
	       for (int index=0; index <= smallerPermutated.size(); index++) {
	         List<E> temp = new ArrayList<E>(smallerPermutated);
	         temp.add(index, firstElement);
	         returnValue.add(temp);
	       }
	     }
	     return returnValue;
	   }
	public static double cosSimInt(List<Integer> g1List, List<Integer> g2List){
	    double aa=0,ab=0,bb=0;
        for(int i=0; i<g1List.size();i++){
            aa+=g1List.get(i)*g1List.get(i);
            bb+=g2List.get(i)*g2List.get(i);
            ab+=g2List.get(i)*g1List.get(i);
        }
        return ab/(Math.sqrt(aa)*Math.sqrt(bb));  
	}
	public static double cosSimDou(List<Double> g1List, List<Double> g2List){
        double aa=0,ab=0,bb=0;
        for(int i=0; i<g1List.size();i++){
            aa+=g1List.get(i)*g1List.get(i);
            bb+=g2List.get(i)*g2List.get(i);
            ab+=g2List.get(i)*g1List.get(i);
        }
        return ab/(Math.sqrt(aa)*Math.sqrt(bb));  
    }
	public static float cosSimFloat(List<Float> g1List, List<Float> g2List){
	    float aa=0,ab=0,bb=0;
        for(int i=0; i<g1List.size();i++){
            aa+=g1List.get(i)*g1List.get(i);
            bb+=g2List.get(i)*g2List.get(i);
            ab+=g2List.get(i)*g1List.get(i);
        }
        return (float) (ab/(Math.sqrt(aa)*Math.sqrt(bb)));  
    }
	public static double mapCosSim(HashMap<Integer, Double> c1,HashMap<Integer, Double>c2){
        if(!c1.keySet().containsAll(c2.keySet()) ||!c2.keySet().containsAll(c1.keySet())){
            throw new RuntimeException("keyset unmatched");
        }
        double ab=0,aa=0,bb=0;
        for(int i :c1.keySet()){
            aa+=c1.get(i)*c1.get(i);
            bb+=c2.get(i)*c2.get(i);
            ab+=c1.get(i)*c2.get(i);
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
}
