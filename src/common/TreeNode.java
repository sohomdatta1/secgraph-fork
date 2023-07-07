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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class TreeNode<T extends Comparable<T>>  implements Comparable<TreeNode<T>>{
    public TreeNode(T i) {
        data = i;
        children = new TreeSet<TreeNode<T>>();
    }
    public TreeNode(T i,int l) {
        data = i;
        level= l;
        children = new TreeSet<TreeNode<T>>();
    }
    public TreeNode(T i,int l,TreeNode<T> pre) {
        data = i;
        level= l;
        parent=pre;
        children = new TreeSet<TreeNode<T>>();
    }
    public TreeNode(TreeNode<T> cop) {
        data = cop.data;
        level= cop.level;
        parent = null;
        children = new TreeSet<TreeNode<T>>(cop.children);
    }
    public TreeNode() {
        children = new TreeSet<TreeNode<T>>();
        level=0;
    }

    public T data;
    public TreeNode<T> parent;
    public Set<TreeNode<T>> children;
    public int level;

    public static void writeTree(TreeNode<?> n, String name,
            HashMap<Integer, HashSet<Integer>> g) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(name));
        LinkedList<TreeNode<?>> stack = new LinkedList<TreeNode<?>>();
        stack.addFirst(n);
        while (!stack.isEmpty()) {
            TreeNode<?> node = stack.pollFirst();
            if (node.parent != null) {
                w.write(node.parent.data + " ("
                        + g.get(node.parent.data).size() + ") " + node.data
                        + " (" + g.get(node.data).size() + ")+ -" + node.level
                        + "\n");
            }
            for (TreeNode<?> t : node.children) {
                stack.add(t);
            }
        }
        w.flush();
        w.close();
    }
    public static void add(TreeNode<Integer> n,TreeNode<Integer> addIn){
       
        Integer a  = addIn.parent.data;
        LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer>>();
        queue.addLast(n);
        while(!queue.isEmpty()){
            TreeNode<Integer> here =queue.pollFirst();
            if(here.data==a){
                addIn.parent=here;
                here.children.add(addIn);
                return;
            }
            for(TreeNode<Integer> t : here.children){
                queue.addLast(t);
            }
        }
    }
    public static void writeTree(TreeNode<?> n, String name) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(name));
        LinkedList<TreeNode<?>> stack = new LinkedList<TreeNode<?>>();
        stack.addFirst(n);
        while (!stack.isEmpty()) {
            TreeNode<?> node = stack.pollLast();
            if (node.parent != null) {
                w.write(node.parent.data + " " + node.data + " " + node.level + "\n");
            }
            for (TreeNode<?> t : node.children) {
                stack.addFirst(t);
            }
        }
        w.flush();
        w.close();
    }

   
    public static String toString(TreeNode<Integer> n) {   
        String res = "{" + (n.data);
        for(TreeNode<Integer> t : n.children){
            res += toString(t);
        }
        res += "}";
        return res;
    }
    public String toString() {   
        String res = "{" + (data);
        for(TreeNode<T> t : children){
            res += t.toString();
        }
        res += "}";
        return res;
    }
    @Override
    public int compareTo(TreeNode<T> o) {
       
        return this.data.compareTo(o.data);
    }
   
    public static HashSet<String> getFullDecomposition(
            TreeNode<Integer> root) {
       
        HashSet<String> fullDecomp = new HashSet<String>();
        if(root == null){
            return fullDecomp;
        }
        fullDecomp.add(root.toString());
        for(TreeNode<Integer> t : root.children){
            fullDecomp.addAll(getFullDecomposition(t));
        }
        return fullDecomp;
    }
    public static TreeNode<Integer> treeFromString(String s){
        LinkedList<TreeNode<Integer>> stack = new LinkedList<TreeNode<Integer>>();
        String here = "";
        TreeNode<Integer> root = null;
        int level = 0;
        for(char i : s.toCharArray()){
            if(i==' '){
                continue;
            }
            else if(i=='{'){
                if(!here.equals("")){
                    int nodeId = Integer.parseInt(here);
                    if(stack.isEmpty()){
                        TreeNode<Integer> t =new TreeNode<Integer>(nodeId, level, null);
                        //System.out.println("a "+nodeId+" "+level);
                        stack.addFirst(t);
                        root=t;
                        level++;
                    }else{
                        TreeNode<Integer> t = new TreeNode<Integer>(nodeId, level, stack.peekFirst());
                        stack.peekFirst().children.add(t);
                        stack.addFirst(t);
                        //System.out.println("b "+nodeId+" "+level);
                        level++;
                    }
                }
                here = "";
            }
            else if(i=='}'){
                if(here.equals("")){
                    stack.pollFirst();
                    level--;
                } else {
                    int nodeId = Integer.parseInt(here);
                    if(stack.isEmpty()){
                        TreeNode<Integer> t =new TreeNode<Integer>(nodeId, level, null);
                        root=t;
                    }else{
                        TreeNode<Integer> t = new TreeNode<Integer>(nodeId, level, stack.peekFirst());
                        stack.peekFirst().children.add(t);
                    }
                }
                here = "";
            }else{
                here += i;
            }
        }
        return root;
    }
    public static int numberOfTreeNodes(TreeNode<Integer> r,
            HashMap<Integer, TreeNode<Integer>> idToNodeMap,
            HashMap<TreeNode<Integer>, Integer> nodeToIdMap){
        int numNode = 0;
        LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer>>();
        queue.addFirst(r);
        while(!queue.isEmpty()){
            TreeNode<Integer> here = queue.pollLast();
            idToNodeMap.put(numNode, here);
            nodeToIdMap.put(here, numNode);
            numNode++;
            for(TreeNode<Integer> t : here.children){
                queue.addFirst(t);
            }
        }
        return numNode;
    }
    public static int numberOfTreeNodes(TreeNode<Integer> r){
        int numNode = 0;
        LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer>>();
        queue.addFirst(r);
        while(!queue.isEmpty()){
            TreeNode<Integer> here = queue.pollLast();
            numNode++;
            for(TreeNode<Integer> t : here.children){
                queue.addFirst(t);
            }
        }
        return numNode;
	}

	public static double editDistance(String t1, String t2,
			HashMap<Integer, Integer> t1Tot2SeedsMapping, String modfiedT1[][],
			HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> addedNodes, 
			HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> removedNodes) {
        TreeNode<Integer> tr1 = treeFromString(t1);
        TreeNode<Integer> tr2 = treeFromString(t2);
        HashMap<Integer, TreeNode<Integer>> idToT1NodeMap    = new HashMap<Integer, TreeNode<Integer>>();
        HashMap<Integer, TreeNode<Integer>> idToT2NodeMap    = new HashMap<Integer, TreeNode<Integer>>();
        HashMap<Integer, TreeNode<Integer>> idToT1SubtreeMap = new HashMap<Integer, TreeNode<Integer>>();
        HashMap<Integer, TreeNode<Integer>> idToT2SubtreeMap = new HashMap<Integer, TreeNode<Integer>>();
        HashMap<TreeNode<Integer>, Integer> nodeToIdT1Map    = new HashMap<TreeNode<Integer>,Integer>();
        HashMap<TreeNode<Integer>, Integer> nodeToIdT2Map    = new HashMap<TreeNode<Integer>,Integer>();
        HashMap<String, Integer> subtreeToT1IdMap = new HashMap<String,Integer>();
        HashMap<String, Integer> subtreeToT2IdMap = new HashMap<String,Integer>();
        int numNodeInT1 = numberOfTreeNodes(tr1,idToT1NodeMap,nodeToIdT1Map);
        int numNodeInT2 = numberOfTreeNodes(tr2,idToT2NodeMap,nodeToIdT2Map);
        int numSubtreeT1 = tr1.children.size();
        int numSubtreeT2 = tr2.children.size();
        int id = 1;
        for(TreeNode<Integer> i : tr1.children){
            idToT1SubtreeMap.put(id, i);
            subtreeToT1IdMap.put(i.toString(), id);
            id++;
        }
        id=1;
        for(TreeNode<Integer> i : tr2.children){
            idToT2SubtreeMap.put(id, i);
            subtreeToT2IdMap.put(i.toString(), id);
            id++;
        }
        HashSet<Integer> seedsInT1= new HashSet<Integer>();
        HashSet<Integer> seedsInT2 = new HashSet<Integer>();
        int[] t1Seeds = new int[t1Tot2SeedsMapping.size()];
        int[] t2Seeds = new int[t1Tot2SeedsMapping.size()];
        int inx = 0;
        for(int i : t1Tot2SeedsMapping.keySet()){
            t1Seeds[inx]=i;
            seedsInT1.add(i);
            t2Seeds[inx]=t1Tot2SeedsMapping.get(i);
            seedsInT2.add(t1Tot2SeedsMapping.get(i));
            inx++;
        }
        double[][] changeLableCost = new double[numNodeInT1][numNodeInT2];
        double[] insertionCost = new double[numSubtreeT2+1];
        double[] deletionCost = new double [numSubtreeT1+1];
        for(int i = 0; i<changeLableCost.length;i++){
            int t1NodeData = idToT1NodeMap.get(i).data;
            for(int j = 0; j<changeLableCost[i].length;j++){
                int t2NodeData = idToT2NodeMap.get(j).data;
				if (seedsInT1.contains(t1NodeData)
						|| seedsInT2.contains(t2NodeData)) {
					changeLableCost[i][j] = 0;
				} else {
					changeLableCost[i][j] = 1;
				}
            }
        }
       
        for(int i = 1; i<deletionCost.length;i++){
            deletionCost[i]=numberOfTreeNodes(idToT1SubtreeMap.get(i));
        }
        for(int i = 1; i<insertionCost.length;i++){
            insertionCost[i]=numberOfTreeNodes(idToT2SubtreeMap.get(i));
        }
        int m = tr1.children.size()+1;
        int n = tr2.children.size()+1;
        double[][] dp = new double[m][n];
        
        boolean found = false;
        dp[0][0]=changeLableCost[nodeToIdT1Map.get(tr1)][nodeToIdT2Map.get(tr2)];
        LinkedList<Integer> addInit = new LinkedList<Integer>();
        LinkedList<Integer> removeInit = new LinkedList<Integer>();
        addInit.add(tr2.data);
        removeInit.add(tr1.data);
        if(!addedNodes.containsKey(0)){
        	addedNodes.put(0, new HashMap<Integer,LinkedList<Integer>>());
        }
        if(!removedNodes.containsKey(0)){
        	removedNodes.put(0, new HashMap<Integer,LinkedList<Integer>>());
        }
        
        addedNodes.get(0).put(0, addInit);
        removedNodes.get(0).put(0, removeInit);
        TreeNode<Integer> tmp = treeFromString(t1);
        tmp.data=tr2.data;
        modfiedT1[0][0]=tmp.toString();
        tmp=null;
        for(int k = 1 ;k< numSubtreeT2+1;k++){
            dp[0][k] = dp[0][k-1]+insertionCost[k];        
            tmp = treeFromString(modfiedT1[0][k-1]);
            tmp.children.add(idToT2SubtreeMap.get(k));
            modfiedT1[0][k]=tmp.toString();
           
            LinkedList<Integer> add = new LinkedList<Integer>();
            LinkedList<Integer> remove = new LinkedList<Integer>();
            
            add.addAll(addedNodes.get(0).get(k-1));
            addSubtreeNodesToList(add, idToT2SubtreeMap.get(k));
            
			remove.addAll(removedNodes.get(0).get(k - 1));

			addedNodes.get(0).put(k, add);
			removedNodes.get(0).put(k, remove);
            

            found=false;
            tmp=null;
        }
        
        for(int k = 1 ;k< numSubtreeT1+1;k++){
        	if(!addedNodes.containsKey(k)){
            	addedNodes.put(k, new HashMap<Integer,LinkedList<Integer>>());
            }
            if(!removedNodes.containsKey(k)){
            	removedNodes.put(k, new HashMap<Integer,LinkedList<Integer>>());
            }
            
        	LinkedList<Integer> add = new LinkedList<Integer>();
            LinkedList<Integer> remove = new LinkedList<Integer>();
            add.addAll(addedNodes.get(k-1).get(0));
            remove.addAll(removedNodes.get(k-1).get(0));
            dp[k][0] = dp[k-1][0]+deletionCost[k];
            tmp = treeFromString(modfiedT1[k-1][0]);
            int removeTreeId=idToT1SubtreeMap.get(k).data;
            for(TreeNode<Integer> t : tmp.children){
            	if(t.data==removeTreeId){
            		found=true;
            		tmp.children.remove(t);
            		addSubtreeNodesToList(remove, t);
            		break;
            	}
            }
            if(!found || tmp==null){
            	throw new RuntimeException("did not find the node to remove");
            }
            modfiedT1[k][0]=tmp.toString();
            
            addedNodes.get(k).put(0, add);
            removedNodes.get(k).put(0, remove);
            found = false;
            tmp=null;
        }
        
        for(int i = 1; i < numSubtreeT1+1;i++){
            for(int j = 1; j< numSubtreeT2+1;j++){
            	int sz1 = idToT1SubtreeMap.get(i).children.size()+1;
            	int sz2 =idToT2SubtreeMap.get(j).children.size()+1;

            	String[][] modTree = new String[sz1][sz2];
            	HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> add = new HashMap<Integer, HashMap<Integer,LinkedList<Integer>>>();
                HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> remove= new HashMap<Integer, HashMap<Integer,LinkedList<Integer>>>();
                 
                double cost  = dp[i-1][j-1]+editDistance(idToT1SubtreeMap.get(i).toString(), idToT2SubtreeMap.get(j).toString(), t1Tot2SeedsMapping,modTree,add,remove);
                double cost2 = dp[i][j-1]+insertionCost[j];
                double cost3 = dp[i-1][j]+deletionCost[i];
                double minCost=Math.min(cost, Math.min(cost2, cost3)); 
                if (minCost == cost){
                	LinkedList<Integer> adds = new LinkedList<Integer>();
                    LinkedList<Integer> removes = new LinkedList<Integer>();
                    adds.addAll(addedNodes.get(i-1).get(j-1));
                    adds.addAll(add.get(sz1-1).get(sz2-1));
                    removes.addAll(removedNodes.get(i-1).get(j-1));
                    removes.addAll(remove.get(sz1-1).get(sz2-1));
                    
                    tmp = treeFromString(modfiedT1[i-1][j-1]);
                	int subTreeReplaceId = idToT1SubtreeMap.get(i).data;
                	for(TreeNode<Integer> t : tmp.children){
                		if(t.data==subTreeReplaceId){
                			tmp.children.remove(t);
                			tmp.children.add(treeFromString(modTree[sz1-1][sz2-1]));
                			found = true;
                			break;
                		}
                	}
                	if(!found){
                    	throw new RuntimeException("did not find the subTreeReplaceId");
                    }
                	found = false;
                	addedNodes.get(i).put(j, adds);
                    removedNodes.get(i).put(j, removes);
                	modfiedT1[i][j]=tmp.toString();
                	tmp=null;
                }else if (minCost == cost2) {
                	LinkedList<Integer> adds = new LinkedList<Integer>();
                    LinkedList<Integer> removes = new LinkedList<Integer>();
                	
                    tmp = treeFromString(modfiedT1[i][j-1]);
                	tmp.children.add(idToT2SubtreeMap.get(j));
                	
                	adds.addAll(addedNodes.get(i).get(j-1));
                	addSubtreeNodesToList(adds, idToT2SubtreeMap.get(j));
                	
                	removes.addAll(removedNodes.get(i).get(j - 1));
                	addedNodes.get(i).put(j, adds);
                	removedNodes.get(i).put(j, removes);
                	
					modfiedT1[i][j] = tmp.toString();
                	tmp=null;
                } else if (minCost == cost3) {
                	LinkedList<Integer> adds = new LinkedList<Integer>();
                    LinkedList<Integer> removes = new LinkedList<Integer>();
                    
                    adds.addAll(addedNodes.get(i-1).get(j));
                    removes.addAll(removedNodes.get(i-1).get(j));
                	
                    tmp = treeFromString(modfiedT1[i-1][j]);
                	int idToRemove = idToT1SubtreeMap.get(i).data;
                	for(TreeNode<Integer> t : tmp.children){
                		if(t.data==idToRemove){
                			tmp.children.remove(t);
                			addSubtreeNodesToList(removes, t);
                			found = true;
                			break;
                		}
                	}
                	if(!found){
                    	throw new RuntimeException("did not find the node to delete");
                    }
                	found = false;
                	addedNodes.get(i).put(j, adds);
                	removedNodes.get(i).put(j, removes);
                	modfiedT1[i][j]=tmp.toString();
                	tmp=null;
                }
                dp[i][j] = minCost;
            }
        }
        return dp[m-1][n-1];
    }
	public static void addSubtreeNodesToList(LinkedList<Integer> list,
			TreeNode<Integer> treeNode) {
		LinkedList<TreeNode<Integer>> queue = new LinkedList<TreeNode<Integer> >(); 
		queue.add(treeNode);
		while(!queue.isEmpty()){
			TreeNode<Integer> here = queue.pollLast();
			list.add(here.data);
			for(TreeNode<Integer>  t : here.children){
				queue.addFirst(t);
			}
		}
	}
}