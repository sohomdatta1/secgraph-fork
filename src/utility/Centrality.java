package utility;

import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Centrality {
    public static HashMap<Integer, Double> readCentrality(String g,int id) throws IOException{
        HashMap<Integer, Double> cen = new HashMap<Integer, Double>();
        BufferedReader r = new BufferedReader(new FileReader(g));
        String l = r.readLine();
        while(l!=null){
            if(l.contains("#")||l.equals("")){
                if(l.contains("Closeness")){
                    String[] split= l.split("\t");
                    //System.out.println(split[id]);
                }
                l=r.readLine();
                continue;
            }
            String[] stringArray= l.split("\t");
            cen.put(Integer.parseInt(stringArray[0]), Double.parseDouble(stringArray[id]));            
            l=r.readLine();
        }
        r.close();
        return cen;
    }
    public static ArrayList<Double> readCentralityToArray(String g,int id) throws IOException{
        ArrayList<Double>  cen = new  ArrayList<Double>();
        BufferedReader r = new BufferedReader(new FileReader(g));
        String l = r.readLine();
        while(l!=null){
            if(l.contains("#")||l.equals("")){
                if(l.contains("Closeness")){
                    String[] split= l.split("\t");
                    //System.out.println(split[id]);
                }
                l=r.readLine();
                continue;
            }
            String[] stringArray= l.split("\t");
            cen.add(Double.parseDouble(stringArray[id]));
            l=r.readLine();
        }
        r.close();
        Collections.sort(cen);
        return cen;
    }
    public static double compareCentrality(HashMap<Integer, Double> c1,HashMap<Integer, Double>c2){
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
    public static double compareCentrality(ArrayList<Double> c1,ArrayList<Double> c2){
        
        double ab=0,aa=0,bb=0;
        for(int i = 0;i<c1.size();i++){
            aa+=c1.get(i)*c1.get(i);
            bb+=c2.get(i)*c2.get(i);
            ab+=c1.get(i)*c2.get(i);
        }
        return ab/(Math.sqrt(bb)*Math.sqrt(aa));
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("usage cenFile1 cenFile2 cenPos");
            System.exit(0);
        }
        int cenPos = Integer.parseInt(args[2]);
        ArrayList<Double>  cen1=readCentralityToArray(args[0],cenPos);
        ArrayList<Double>  cen2=readCentralityToArray(args[1],cenPos);
        System.out.println(compareCentrality(cen1, cen2));
    }
}
