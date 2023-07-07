package quant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class calculateSeedQuantification {
    public static void main(String[] args) throws IOException {
        //args=new String[]{"AstroPhDegree.csv","0.05","0.6","0","10"};
        if (args.length != 5) {
            System.out.println("usage degreeDist theta sampling_rate epsilon numberOfSeeds");
            System.exit(0);
        }
        int numSeed = Integer.parseInt(args[4]);
        ArrayList<Integer> degreeList = new ArrayList<Integer>();
        BufferedReader r = new BufferedReader(new FileReader(args[0]));
        int m = 0;
        double epsilon=Double.parseDouble(args[3]);
        double theta = Double.parseDouble(args[1]);
        double s = Double.parseDouble(args[2]);
        String line = r.readLine();
        while(line!=null){
            String[] strArray = line.split(",");
            int degree = Integer.parseInt(strArray[0]);
            int freq = Integer.parseInt(strArray[1]);
            for (int i = 0; i < freq; i++) {
                degreeList.add(degree);
            }
            m+=(degree*freq);
            line=r.readLine();
        }
        r.close();
        m=m/2;
        Collections.sort(degreeList,Collections.reverseOrder()); //System.out.println(degreeList);
        int n = degreeList.size();
        
        int thetaN = (int) (theta*n);
        int epsilonN = (int) epsilon*n;
        //epsilonN=(int) (0.1*n);
        ArrayList<Integer> degreeSumList = new ArrayList<Integer>();
        int sum = 0;
        for(int i = 0;i<degreeList.size();i++){
            sum+=degreeList.get(i);
            degreeSumList.add(sum);
        }
        double k = epsilonN+1;
        //double p = (degreeList.get(thetaN-epsilonN)*(((double) degreeSumList.get(numSeed-1)/numSeed)))/(2*m-1);
        double p = 2*m/((double)n*((double)n-1));
        /*System.out.println((p*s*s*s*(1-p)*(1-p)/(4*(2-s-p*s)))+">="+"Math.min("+(2*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(numSeed)+","+ 
                (((k+2)*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(k*(n-k/2-1)))+")");
        System.out.println("\t p: "+p);
        */
        
        while(!((p*s*s*s*(1-p)*(1-p)/(4*(2-s-p*s)))>=Math.min((2*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(numSeed), 
                                                              ((k+2)*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(k*(n-k/2-1))))){
            epsilonN++;
            epsilon=epsilonN/n;
            if(epsilonN>=thetaN){
                System.out.println("need to choose more seeds than avaliable");
                return;
            }
            double newN = thetaN-epsilonN, newM = degreeSumList.get(thetaN-epsilonN)/2;
            k = epsilonN+1;
            p = 2*newM/((double)newN*((double)newN-1));
            //p = (degreeList.get(thetaN-epsilonN)*(((double) degreeSumList.get(numSeed-1)/numSeed)))/(2*m-1);
            /*System.out.println((p*s*s*s*(1-p)*(1-p)/(4*(2-s-p*s)))+">="+"Math.min("+(2*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(numSeed)+","+ 
                    (((k+2)*Math.log(n)+Math.log(2*(n-epsilonN-numSeed)))/(k*(n-k/2-1)))+")");*/
            //System.out.println("\t p: "+p);
        }
        System.out.print((double)(thetaN-epsilonN)/thetaN);
        /*double hi = (((double) degreeSumList.get(numSeed-1)/numSeed)*
                (((double) (degreeSumList.get(thetaN-1)-degreeSumList.get(numSeed-1)))/(thetaN-numSeed)))/(2*m-1);
        double li = hi;
        
        double hj  = (((double) degreeSumList.get(numSeed-1)/numSeed)*
                (((double) (degreeSumList.get(n-1)-degreeSumList.get(thetaN-1)))/(n-thetaN)))/(2*m-1);
        double lj= hj;*/
        //double hi = ((double) (degreeList.get(thetaN-epsilonN-1)*degreeList.get(0)))/(2*m-1);
        //double hj = ((double) (degreeList.get(thetaN-epsilonN-1+1)*degreeList.get(0)))/(2*m-1);
        //double li = ((double) (degreeList.get(thetaN-epsilonN-1)*degreeList.get(numSeed-1)))/(2*m-1);
        //double lj = ((double) (degreeList.get(thetaN-epsilonN-1+1)*degreeList.get(numSeed-1)))/(2*m-1);
        //hj=2*m/((double)n*((double)n-1));
        //lj=2*m/((double)n*((double)n-1));
        /*double sBound = (2*hi-li-lj)/(2*hi-li*hj-lj*hi);
        double fss = (s*(li*(1-hj*s)+lj*(1-hi*s)-2*hi*(1-s))*(li*(1-hj*s)+lj*(1-hi*s)-2*hi*(1-s)))/
                     (8*(li*(1-hj*s)+lj*(1-hi*s)+2*hi*(1-s)));
        double seedBound = (2*Math.log(n)+Math.log(2*(theta-epsilon)*n))/fss;
        System.out.println(s+">"+sBound+" && "+numSeed +">="+seedBound);
        System.out.println("\t hi: "+hi+" hj: "+hj+" li: "+li+" lj: "+lj+" fss:  "+fss);
        while(!(s>sBound && numSeed>=seedBound)){
            
            numSeed++;
            if(numSeed>thetaN){
                System.out.println("need to choose more seeds than avaliable");
                return;
            }
            hi = (((double) degreeSumList.get(numSeed-1)/numSeed)*
                    (((double) (degreeSumList.get(thetaN-1)-degreeSumList.get(numSeed-1)))/(thetaN-numSeed)))/(2*m-1);
            li = hi;
            
            hj  = (((double) degreeSumList.get(numSeed-1)/numSeed)*
                    (((double) (degreeSumList.get(n-1)-degreeSumList.get(thetaN-1)))/(n-thetaN)))/(2*m-1);
            lj= hj;
            //lj = ((double) (degreeList.get(thetaN-epsilonN-1+1)*degreeList.get(numSeed-1)))/(2*m-1);
            //li = ((double) (degreeList.get(thetaN-epsilonN-1)*degreeList.get(numSeed-1)))/(2*m-1);
            sBound = (2*hi-li-lj)/(2*hi-li*hj-lj*hi);
            fss = (s*(li*(1-hj*s)+lj*(1-hi*s)-2*hi*(1-s))*(li*(1-hj*s)+lj*(1-hi*s)-2*hi*(1-s)))/
                  (8*(li*(1-hj*s)+lj*(1-hi*s)+2*hi*(1-s)));
            seedBound = (2*Math.log(n)+Math.log(2*(theta-epsilon)*n))/fss;
            System.out.println(s+">"+sBound+" && "+numSeed +">="+seedBound);
            System.out.println("\t hi: "+hi+" hj: "+hj+" li: "+li+" lj: "+lj+" fss:  "+fss);
        }
        System.out.println(numSeed);*/
    }
}
