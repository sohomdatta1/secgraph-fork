package quant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class calculateSeedlessQuantification {
    public static void main(String[] args) throws IOException {
        //args=new String[]{"EnronDegree.csv","0.5","0.90"};
        if (args.length != 3) {
            System.out.println("usage degreeDist theta sampling_rate");
            System.exit(0);
        }
        //System.out.print(args[0]+","+args[1]+","+args[2]+",");
        //System.out.println();
        ArrayList<Integer> degreeList = new ArrayList<Integer>();
        BufferedReader r = new BufferedReader(new FileReader(args[0]));
        int m = 0;
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
        int tolerence = 0;
        int n = degreeList.size();
        //System.out.print(n+","+m+",");
        double k = 2;
        int thetaN = (int) (theta*n);
        ArrayList<Integer> degreeSumList = new ArrayList<Integer>();
        int sum = 0;
        for(int i = 0;i<degreeList.size();i++){
            sum+=degreeList.get(i);
            degreeSumList.add(sum);
        }
        double l = ((double)degreeList.get(n-1)*degreeList.get(n-1-1))
                    /(2*m-1);
        double lTheta = ((double)degreeList.get(thetaN-1)*degreeList.get(thetaN-1-1))
                        /(2*m-1);
        double lThetaEpsilon = (((double)degreeList.get(thetaN-tolerence-1)*degreeList.get(thetaN-tolerence-1-1)))
                                /(2*m-1);
        double hTheta = ((double)degreeList.get(0)*degreeList.get(1))
                        /(2*m-1);
        double hThetaEpsilon = ((double)degreeList.get(0)*degreeList.get(1))
                                /(2*m-1);
        
        double epsilon = 0;
        double pd = 2*m/((double)n*((double)n-1));
        //double sBound = (2*hThetaEpsilon-lThetaEpsilon-l)/(hThetaEpsilon*(2-lThetaEpsilon-l));
        double sBound = (tolerence+1)/((1-pd)*(2*(tolerence+1)*n-(tolerence+1)*(tolerence+1))+(2*pd-1)*(tolerence+1));
        
        /*double fs = (s*((l+lTheta)*(1-hTheta*s)-2*hTheta*(1-s))*((l+lTheta)*(1-hTheta*s)-2*hTheta*(1-s)))/
                (8*((l+lTheta)*(1-hTheta*s)+(2*hTheta*(1-s))));*/
        double fs = pd*s*(s-pd*s)*(s-pd*s)/(2*(2-pd*s-s));
        double fBound = (2*Math.log(n)+k*Math.log(theta*n)+Math.log((theta-epsilon)*n)+1)
                /(theta*k*n-k*k/2-k);
        /*double fBound = (2*Math.log(n)+k*Math.log(theta*n)+Math.log((theta-epsilon)*n))
                /(theta*k*n);*/
        //double fBound = ((k+2)*Math.log(theta*n)+Math.log((theta-epsilon)*n))/(theta*k*n);
       // System.out.println(s+">"+sBound+" && "+fs +">="+fBound);
       // int c = 0;
        while(!(s>sBound && fs >= fBound)){
            /*c++;
            if(c==11232){
                System.out.println();
            }*/
            tolerence++;
            if(tolerence+1>=thetaN){
                tolerence++;
                System.out.print("0");
                /*System.out.println("(thetaN-tolerence)/thetaN\t: ("+thetaN+"-"+tolerence+")/"+thetaN+"="+(double)(thetaN-tolerence)/thetaN+"  "
                        + "(thetaN-tolerence)/n\t: ("+thetaN+"-"+tolerence+")/"+n+"="+(double)(thetaN-tolerence)/n);*/
                //System.out.print("(thetaN-tolerence)/thetaN\t: ("+thetaN+"-"+tolerence+")/"+thetaN+"="+(double)(thetaN-tolerence)/thetaN+" \ntolerence/n\t\t: "+tolerence+"/"+n+"="+(double)tolerence/n);
                return;
            }
            epsilon=((double)tolerence)/n;
            k=Math.max(2, tolerence+1);
            lThetaEpsilon = ((double)degreeList.get(thetaN-tolerence-1)*degreeList.get(thetaN-tolerence-1-1))/(2*m-1);
            
            /*for(int i =0;i<thetaN-tolerence;i++){
                newM+=degreeList.get(i);
            }*/
            double newN = thetaN-tolerence, newM = degreeSumList.get(thetaN-tolerence)/2;
            pd = 2*newM/((double)newN*((double)newN-1));
            fs = pd*s*(s-pd*s)*(s-pd*s)/(2*(2-pd*s-s));
            //sBound = (2*hThetaEpsilon-lThetaEpsilon-l)/(hThetaEpsilon*(2-lThetaEpsilon-l));
            /*if(fBound<((k+2)*Math.log(theta*n)+Math.log((theta-epsilon)*n))/(theta*k*n)){
                System.out.println();
            }*/
            sBound = (tolerence+1)/((1-pd)*(2*(tolerence+1)*n-(tolerence+1)*(tolerence+1))+(2*pd-1)*(tolerence+1));
            //fBound = ((k+2)*Math.log(theta*n)+Math.log((theta-epsilon)*n))/(theta*k*n);
            /*fBound = (2*Math.log(n)+k*Math.log(theta*n)+Math.log((theta-epsilon)*n))
                    /(theta*k*n);*/
            fBound = (2*Math.log(n)+k*Math.log(theta*n)+Math.log((theta-epsilon)*n)+1)
                    /(theta*k*n-k*k/2-k);
           //System.out.println(s+">"+sBound+" && "+fs +">="+fBound);
        }
        System.out.print(/*thetaN+","+tolerence+","+*/((double)(thetaN-tolerence))/thetaN);
    }
}
