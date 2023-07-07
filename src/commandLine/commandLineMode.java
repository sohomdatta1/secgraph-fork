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
package commandLine;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.DefaultParser;

import utility.AuthoritiesScore;
import utility.Betweenness;
import utility.Closeness;
import utility.Community;
import utility.Deg;
import utility.Distance;
import utility.EffDiam;
import utility.EigenVectorCentr;
import utility.HubsScore;
import utility.Infectiousness;
import utility.InfluenceMax;
import utility.JD;
import utility.LocalClusterCoefficient;
import utility.MINS;
import utility.NetConstraint;
import utility.NetworkResilience;
import utility.NodeClustCf;
import utility.PageRank;
import utility.ReliableEmail;
import utility.Rolx;
import utility.Sprout;
import utility.Sybil;
import deAnon.matching.Comm_Enhanced;
import deAnonymize.BayesianUsingCosSim;
import deAnonymize.DV;
import deAnonymize.Percolation;
import deAnonymize.RandomizedSpanningTree;
import deAnonymize.SJCCS2014;
import deAnonymize.SJISC2014;
import deAnonymize.SJISC2014ADA;
import deAnonymize.TheCutBasedAttack;
import deAnonymize.ThePassiveAttack;
import deAnonymize.TheWalkBasedAttack;
import deAnonymize.UndirectionedPropagation;
import deAnonymize.UserMatching;
import anonymize.BoundedTMean;
import anonymize.IdRemoval;
import anonymize.KDa;
import anonymize.RandAddDel;
import anonymize.RandSw;
import anonymize.RandWalk;
import anonymize.SalaDP;
import anonymize.UnionSplit;
import anonymize.kIso;
import anonymize.xiaoDP;

public class commandLineMode {
    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption("m", "mode", true, "Select Mode");
        options.addOption("a", "algorithm", true, "Select algorithm");
        options.addOption("gA", "graphA", true, "Graph A");
        options.addOption("gB", "graphB", true, "Graph B");
        options.addOption("gO", "graphOut", true, "Output Graph");
        options.addOption("f", "fractionChange", true, "Fraction of Change");
        options.addOption("k", true, "k-value");
        options.addOption("t", true, "t-value");
        options.addOption("e", true, "epsilon-value");
        options.addOption("n", true, "number of nodes to try");
        options.addOption("rn", true, "randwalk number of step");
        options.addOption("rm", true, "randwalk max retry");

        options.addOption("cD", true, "Coalition Degree File");
        options.addOption("cG", true, "Coalition Graph File");
        
        
        options.addOption("l", "Limit", true, "limit for algorithm");

        options.addOption("am", "algorithmMode", true,"algorithm Mode (stack or queue)");
        options.addOption("pMINS", true, "p value for MINS");
        options.addOption("mcmcIt", true, "number of MCMC Iterations");
        options.addOption("gASRE", true, "graphA spam node sequence");
        options.addOption("gBSRE", true, "graphB spam node sequence");
        options.addOption("naRE", "neiToAcceptRE", true,"number of neighbor to accept for RE");
        options.addOption("thRE", "twoHopRE", true,"use two hop for RE (true/false)");

        options.addOption("nInf", true, "number of repeates for infectiousness");

        options.addOption("addDegFN", true, "addedDegreeFileName");
        options.addOption("trgFN", true, "targetFileName");
        options.addOption("intEFN", true, "internalEdgeFileName");
        options.addOption("recFN", true, "recoverNodesFileName");
        options.addOption("seed", true, "seedFileName");
        options.addOption("theta", true, "Theta");
        options.addOption("bSize", true, "BipartSize");
        options.addOption("nKeep", true, "NumToKeep");
        
        options.addOption("gACen", true, "Graph A Centrality File ");
        options.addOption("gBCen", true, "Graph B Centrality File ");
        
        options.addOption("DisSimW", true, "Distance Similarity Weight");
        options.addOption("StrSimW", true, "Structual Similarity Weight");
        options.addOption("InhSimW", true, "Inherited Similarity Weight");
        
        options.addOption("DegSimW", true, "Degree Similarity Weight");
        options.addOption("NeiSimW", true, "Neighborhood Similarity Weight");
        options.addOption("nNei", true, "max number of Neighbor to consider");
        options.addOption("nTest", true, "Number of Nodes to Test");
    
        
        options.addOption("sbn", true, "Sybil Detection numSInstances");
        options.addOption("sbs", true, "Sybil Detection rseed");
        options.addOption("sbl", true, "Sybil Detection walkLengh");
        options.addOption("sbh", true, "Sybil Detection balanceCondHva");
        
        options.addOption("RD", true, "Results Directory");
        options.addOption("sN", true, "Num Seeds");
        options.addOption("noise", true, "Noise");
        options.addOption("exp", true, "Exp");
        options.addOption("cliqueFN", true, "FileNameClique");
        options.addOption("probDeg", true, "Prob/DegAnon");
        options.addOption("Overlap", true, "Overlapped");
        options.addOption("avgProb", true, "FileNameAvgProb");
        
        options.addOption("eHRG", true, "epsilonHRG");
        options.addOption("eE", true, "epsilonE/DegAnon");
        options.addOption("eq", true, "eq");
        options.addOption("stop", true, "stop");
        
        options.addOption("parallel", true, "parallel");
        
        options.addOption("curNNum", true, "curNNum");
        options.addOption("statusResult", true, "statusResult");
        options.addOption("edgeResult", true, "edgeResult");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        if(!cmd.hasOption("m")){
            System.out.println("Usage java -jar secGraph -m <d/a/u> -a <algorithm> <algorithm inputs>");
            System.exit(1);
        }
        switch (cmd.getOptionValue("m")) {
        case "d":
            deanonymizeCLI(cmd,options);
            break;
        case "a":
            anonymizeCLI(cmd,options);
            break;
        case "u":
            utilityCLI(cmd,options);
            break;
        default:
            printHelp(options);
            break;
        }
    }

    

    private static void anonymizeCLI(CommandLine cmd, Options options) {
        switch (cmd.getOptionValue("a")) {
        case "id":
            try {
				IdRemoval.idRemoval(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"));
			} catch (Exception e) {
				System.out.println("Usage java -jar secGraph -m a -a id -gA <Graph A File Name> -gO <Output Graph Name>");
				System.exit(1);
			}
            break;
        case "addDel":
            try {
				RandAddDel.randAddAndDeleteEdges(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), 
						Double.parseDouble(cmd.getOptionValue("f")));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a addDel -gA <Graph A File Name> -gO <Output Graph Name>  -f <fraction of edges to modify>");
				System.exit(1);
			}
            break;
        case "sw":
            try {
				RandSw.RandSwitch(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), 
						Double.parseDouble(cmd.getOptionValue("f")));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a sw -gA <Graph A File Name> -gO <Output Graph Name>  -f <fraction of edges to modify>");
				System.exit(1);
			}
            break;
        case "kDa":
            try {
				KDa.kDa(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), 
						Integer.parseInt(cmd.getOptionValue("k")));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a kDa -gA <Graph A File Name> -gO <Output Graph Name>  -k <k-value>");
				System.exit(1);
			}
            break;
        case "kIso":
            try {

                kIso.runKISO(cmd.getOptionValue("k"), cmd.getOptionValue("curNNum"), cmd.getOptionValue("gA"), cmd.getOptionValue("statusResult"), cmd.getOptionValue("edgeResult"));
            } catch (IOException | InterruptedException e3) {
                System.out.println("Usage: java -jar secGraph -m a -a kIso -gA <Graph A File Name> -gO <Output Graph Name>  -k <k-value> -curNNum <curNNum> -statusResult <statusResult> -edgeResult <edgeResult>");
                System.exit(1);
            }
            break;
            
        case "tMean":
            try {
				BoundedTMean.tMean(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), 
						Integer.parseInt(cmd.getOptionValue("t")));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a tMean -gA <Graph A File Name> -gO <Output Graph Name>  -t <t-value>");
				System.exit(1);
			}
            break;
        case "salaDP":
            try {
				SalaDP.DP(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), cmd.getOptionValue("e"));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a salaDP -gA <Graph A File Name> -gO <Output Graph Name> -e <epsilon-value>");
				System.exit(1);
			}
            break;
        case "P_DP":
            try {
                anonymize.proserpioDP.runproserpioDP(cmd.getOptionValue("gA"), 
                        cmd.getOptionValue("parallel"),cmd.getOptionValue("gO"),
                        cmd.getOptionValue("mcmcIt"),cmd.getOptionValue("e"));
            } catch (IOException | InterruptedException e2) {
                System.out.println("Usage: java -jar secGraph -m a -a P_DP -gA <Graph A File Name> "
                        + "-parallel <parallel> -gO <Output Graph Name> -mcmcIt <number of MCMC Iterations> -e <epsilon-value>");
                System.exit(1);
            }
            break;
        case "XiaoDP":
            if(cmd.hasOption("eHRG") && cmd.hasOption("eE")&&cmd.hasOption("eq") && cmd.hasOption("stop")){
                try {
                    String g = cmd.getOptionValue("gA");
                    String epsilonHRG = cmd.getOptionValue("eHRG");
                    String epsilonE = cmd.getOptionValue("eE");
                    String eq = cmd.getOptionValue("eq");
                    String stop = cmd.getOptionValue("stop");
                    xiaoDP.runXiao(g, epsilonHRG, epsilonE, eq, stop);
                } catch (IOException | InterruptedException e1) {
                    System.out.println("Usage: java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> "
                            + "-eHRG <epsilonHRG> -eE <epsilonE> -eq <eq> -stop <stop>");
                    System.exit(1);
                }
            }
            else if(cmd.hasOption("eHRG") && cmd.hasOption("eE")){
                try {
                    String g = cmd.getOptionValue("gA");
                    String epsilonHRG = cmd.getOptionValue("eHRG");
                    String epsilonE = cmd.getOptionValue("eE");
                    xiaoDP.runXiao(g, epsilonHRG, epsilonE);
                } catch (IOException | InterruptedException e1) {
                    System.out.println("Usage: java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> "
                            + "-eHRG <epsilonHRG> -eE <epsilonE>");
                    System.exit(1);
                }
            }
            else{
                try {
                    String g = cmd.getOptionValue("gA");
                    xiaoDP.runXiao(g);
                } catch (IOException | InterruptedException e1) {
                    System.out.println("Usage: java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> ");
                    System.exit(1);
                }
            }
            break;
        case "rWalk":
            try {
				RandWalk.rWalk(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), cmd.getOptionValue("rn"), cmd.getOptionValue("rm"));
			} catch (Exception e) {
				System.out.println("Usage: java -jar secGraph -m a -a rWalk -gA <Graph A File Name> -gO <Output Graph Name> -rn <randwalk number of step> -rm <randwalk max retry>	");
				System.exit(1);
			}
            break;
        case "union":
            try {
                UnionSplit.unionSplit(cmd.getOptionValue("gA"), cmd.getOptionValue("gO"), Integer.parseInt(cmd.getOptionValue("k")));
            } catch (NumberFormatException | IOException e) {
                System.out.println("Usage: java -jar secGraph -m a -a union -gA <Graph A File Name> -gO <Output Graph Name> -k <cluster size>");
                System.exit(1);
            }
            break;
        default:
            printHelp(options);
            break;
        }
        
    }

    private static void deanonymizeCLI(CommandLine cmd, Options options) {
       switch (cmd.getOptionValue("a")) {
       case "Walk":
           
        try {
            TheWalkBasedAttack.theWalkBasedAttack(cmd.getOptionValue("gA"), 
                    cmd.getOptionValue("addDegFN"), cmd.getOptionValue("trgFN"), cmd.getOptionValue("gO"), 
                    cmd.getOptionValue("intEFN"), cmd.getOptionValue("recFN"));
        } catch (Exception e) {
           System.out.println("Usage: java -jar secGraph -m d -a Walk -gA <Graph A File Name> -addDegFN <addedDegreeFileName> -trgFN <targetFileName> -gO <Output Graph Name> -intEFN <internalEdgeFileName> -recFN <recoverNodesFileName>");
           System.exit(1);
        }
            break;
        case "Cut":
            
        try {
            TheCutBasedAttack.theWalkBasedAttack(cmd.getOptionValue("gA"),
                    cmd.getOptionValue("trgFN"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
            System.out.println("Usage: java -jar secGraph -m d -a Cut -gA <Input Graph File Name> -trgFN <targetFileName> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "Passive":
        try {
            ThePassiveAttack.thePassiveAttack(cmd.getOptionValue("gA"), cmd.getOptionValue("cG"), 
                    cmd.getOptionValue("cD"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
            System.out.println("Usage: java -jar secGraph -m d -a Passive -gA <Input Graph File Name> "
                    + "-cG <Coalition Graph File> -cG <Coalition Degree File> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "NS":
            
        try {
            UndirectionedPropagation.undirectionedPropagation(
            		cmd.getOptionValue("gA"), 
                    cmd.getOptionValue("gB"), cmd.getOptionValue("seed"), 
                    cmd.getOptionValue("theta"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a NS -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -theta <Theta (double)> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "Link":
            try {
                UndirectionedPropagation.undirectionedPropagation(
                        cmd.getOptionValue("gA"), 
                        cmd.getOptionValue("gB"), cmd.getOptionValue("seed"), 
                        cmd.getOptionValue("theta"), cmd.getOptionValue("gO"));
            } catch (Exception e) {
                System.out.println("Usage: java -jar secGraph -m d -a Link -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -theta <Theta (double)> -gO <Output Graph Name>");
                System.exit(1);
            }
            break;
        case "Community":
            try {
                String ResultsDirectory = cmd.getOptionValue("RD");
                String NumSeeds   = cmd.getOptionValue("sN");
                String Noise  = cmd.getOptionValue("noise");
                String Exp  = cmd.getOptionValue("exp");
                String FileName1Net  = cmd.getOptionValue("gA");
                String FileName2Net  = cmd.getOptionValue("gB");
                String FileNameClique  = cmd.getOptionValue("cliqueFN");
                boolean ProbDegAnon = Boolean.parseBoolean(cmd.getOptionValue("probDeg"));
                String Overlapped  = cmd.getOptionValue("Overlap");
                if(!ProbDegAnon || !cmd.hasOption("avgProb")){
                    Comm_Enhanced.main(new String[]{ResultsDirectory,NumSeeds,Noise,Exp,FileName1Net,FileName2Net,
                            FileNameClique,ProbDegAnon+"",Overlapped});
                }else{
                    String FileNameAvgProb  = cmd.getOptionValue("avgProb");
                    Comm_Enhanced.main(new String[]{ResultsDirectory,NumSeeds,Noise,Exp,FileName1Net,FileName2Net,
                            FileNameClique,ProbDegAnon+"",Overlapped,FileNameAvgProb});
                }
                
            } catch (Exception e1) {
                System.out.println("Usage: java -jar secGraph -m d -a Community "
                        + "-RD <ResultsDirectory> -sN <NumSeeds> "
                        + "-noise <Noise (double)> -exp <Exp (double)> -gA <Input Graph A> "
                        + "-gB <Input Graph B> -cliqueFN <Clique File Name> -probDeg <ProbDegAnon boolean> Overlap <Overlap (boolean)>"
                        + "avgProb <avgProb Optional use if want to get degree of anonymity>");
                System.exit(1);
            }
            break;
        case "DV":
        try {
            DV.runDV(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), cmd.getOptionValue("seed"), 
                    cmd.getOptionValue("bSize"), cmd.getOptionValue("nKeep"),
                    cmd.getOptionValue("gO"), cmd.getOptionValue("am"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a DV -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -gO <Output Graph Name> -am <algorithm mode (queue or stack)>");
            System.exit(1);
        }
            break;
        case "RST":
        try {
            RandomizedSpanningTree.runRSP(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), cmd.getOptionValue("seed"), 
                    cmd.getOptionValue("n"), cmd.getOptionValue("gO"));
        } catch (Exception e1) {
            System.out.println("Usage: java -jar secGraph -m d -a RST -gA <Graph A File Name> -gB <Graph B File Name>"
                    + " -seed <Seed File Name> -n <number of trees> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "Bayesian":
        try {
            BayesianUsingCosSim.bayesianUsingCosSim(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"),cmd.getOptionValue("gO"));
        } catch (IOException e1) {
            System.out.println("Usage: java -jar secGraph -m d -a Bayesian -gA <Graph A File Name> -gB <Graph B File Name>"
                    +" -gO <Output Graph Name>");
            System.exit(1);
        }
              break;
        case "Percolation":
        try {
            Percolation.runPercolation(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"),
                    cmd.getOptionValue("nKeep"), 
                    cmd.getOptionValue("seed"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a Percolation -gA <Graph A File Name> -gB <Graph B File Name> -nKeep <number of elements to keep from each bipartite matching> -seed <Seed File Name> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "SJISC2014":
            
        try {
            SJISC2014.runISC(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), 
                    cmd.getOptionValue("gACen"), cmd.getOptionValue("gBCen"), cmd.getOptionValue("am"),
                    cmd.getOptionValue("seed"), cmd.getOptionValue("bSize"),
                    cmd.getOptionValue("nKeep"), cmd.getOptionValue("DisSimW"), cmd.getOptionValue("StrSimW"), 
                    cmd.getOptionValue("InhSimW"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a SJISC2014 -gA <Graph A File Name> -gB <Graph B File Name>  -gACen <Graph A Centrality File > -gBCen <Graph B Centrality File > -am <algorithm mode (queue or stack)> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DisSimW <Distance Similarity Weight> -StrSimW <Structual Similarity Weight>  -InhSimW <Inherited Similarity Weight> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "SJISCA2014":
        try {
            SJISC2014ADA.runISCADA(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), 
                    cmd.getOptionValue("gACen"), cmd.getOptionValue("gBCen"), cmd.getOptionValue("am"),
                    cmd.getOptionValue("seed"), cmd.getOptionValue("bSize"),
                    cmd.getOptionValue("nKeep"), cmd.getOptionValue("DisSimW"), cmd.getOptionValue("StrSimW"), 
                    cmd.getOptionValue("InhSimW"), cmd.getOptionValue("gO"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a SJISCA2014 -gA <Graph A File Name> -gB <Graph B File Name>  -gACen <Graph A Centrality File > -gBCen <Graph B Centrality File > -am <algorithm mode (queue or stack)> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DisSimW <Distance Similarity Weight> -StrSimW <Structual Similarity Weight>  -InhSimW <Inherited Similarity Weight> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "Reconciliation":
            
        try {
            UserMatching.runUM(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"),
                    cmd.getOptionValue("seed"), cmd.getOptionValue("nTest"),
                    cmd.getOptionValue("gO"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a Reconciliation -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -nTest <Number of Nodes to Test> -gO <Output Graph Name>");
            System.exit(1);
        }
            break;
        case "SJCCS2014":
        try {
            SJCCS2014.runSJCCS2014(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"),
                    cmd.getOptionValue("seed"), cmd.getOptionValue("bSize"), cmd.getOptionValue("nKeep"),
                    cmd.getOptionValue("DegSimW"), cmd.getOptionValue("NeiSimW"),
                    cmd.getOptionValue("DisSimW"), cmd.getOptionValue("nNei"),
                    cmd.getOptionValue("gO"), cmd.getOptionValue("am"));
        } catch (Exception e) {
        	System.out.println("Usage: java -jar secGraph -m d -a SJCCS2014 -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DegSimW <Degree Similarity Weight> -NeiSimW <Neighborhood Similarity Weight> -DisSimW <Distance Similarity Weight> -nNei <max number of Neighbor to consider> -gO <Output Graph Name> -am <algorithm mode (queue or stack)>");
            System.exit(1);
        }
            break;  
        default:
        	printHelp(options);
            break;
        }
        
    }
    private static void utilityCLI(CommandLine cmd, Options options) {
        switch (cmd.getOptionValue("a")) {
        case "deg":
            try {
                System.out.println(Deg.getDeg(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e6) {
            	System.out.println("Usage: java -jar secGraph -m u -a deg -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "JD":
            try {
                System.out.println(JD.joinDeg(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e5) {
            	System.out.println("Usage: java -jar secGraph -m u -a JD -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "ED":
            try {
                System.out.println(EffDiam.runED(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e5) {
            	System.out.println("Usage: java -jar secGraph -m u -a ED -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "PL":
            try {
                System.out.println(Distance.getDistance(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), cmd.getOptionValue("n")));
            } catch (Exception e5) {
            	System.out.println("Usage: java -jar secGraph -m u -a PL -gA <Graph A File Name> -gB <Graph B File Name> -n <number of nodes used for path length>");
                System.exit(1);
            }
            break;
        case "LCC":
            try {
                System.out.println(LocalClusterCoefficient.LCC(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a LCC -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "CC":
            try {
                System.out.println(NodeClustCf.runCC(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a CC -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "CCen":
            try {
               System.out.println( Closeness.runCCen(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a CCen -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "BC":
            try {
                System.out.println(Betweenness.runBC(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a BC -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "EV":
            try {
                System.out.println(EigenVectorCentr.runEV(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a EV -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "NC":
            try {
                System.out.println(NetConstraint.runNC(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e4) {
            	System.out.println("Usage: java -jar secGraph -m u -a NC -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "NR":
            try {
                System.out.println(NetworkResilience.GetNR(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), cmd.getOptionValue("l")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a NR -gA <Graph A File Name> -gB <Graph B File Name> -l <limit>");
                System.exit(1);
            }
            break;
        case "Infec":
            
            try {
                System.out.println(Infectiousness.calcInfec(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"),  cmd.getOptionValue("nInf")));
            } catch (Exception e2) {
            	System.out.println("Usage: java -jar secGraph -m u -a Infec -gA <Graph A File Name> -gB <Graph B File Name>  -nInf <number of repeats for infectiousness>");
                System.exit(1);
            }
            break;
        case "Rolx":
            try {
                System.out.println(Rolx.calcRolxSim(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e2) {
            	System.out.println("Usage: java -jar secGraph -m u -a Rolx -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "RE":
            
            try {
                System.out.println(ReliableEmail.compareRE(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), 
                        cmd.getOptionValue("gASRE"), cmd.getOptionValue("gBSRE"), 
                        cmd.getOptionValue("naRE"), cmd.getOptionValue("thRE")));
            } catch (Exception e1) {
            	System.out.println("Usage: java -jar secGraph -m u -a RE -gA <Graph A File Name> -gB <Graph B File Name>  -gASRE <graphA spam node sequence file> -gBSRE <graphB spam node sequence file>  -naRE <number of neighbor to accept for RE> -thRE <use two hop for RE (true/false)>");
                System.exit(1);
            }
            break;
        case "Infl":
            try {
               System.out.println( InfluenceMax.runInfluxMax(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e1) {
            	System.out.println("Usage: java -jar secGraph -m u -a Infl -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "MINS":
            try {
                System.out.println(MINS.compareMINS(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), cmd.getOptionValue("pMINS"), cmd.getOptionValue("l")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a MINS -gA <Graph A File Name> -gB <Graph B File Name> -pMINS <p value for MINS (double<=1)> -l <threshold (double<=1)>");
                System.exit(1);
            }
            break;
        case "CD":
            try {
                System.out.println(Community.calcCommunitySim( cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a CD  -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "SR":
            try {
                System.out.println(Sprout.runSprout(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a SR -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
            
        case "SD":
            
            try {
                System.out.println(Sybil.runSybil(cmd.getOptionValue("gA"), cmd.getOptionValue("gB"), 
                        cmd.getOptionValue("sbn"), cmd.getOptionValue("sbs"), cmd.getOptionValue("sbl"), 
                        cmd.getOptionValue("sbh")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a SD -gA <Graph A File Name> -gB <Graph B File Name>"
            	        + "  -sbn <Sybil Detection numSInstances> -sbs <Sybil Detection rseed> -sbl <Sybil Detection walkLengh>  "
            	        + "-sbh <Sybil Detection balanceCondHva>");
                System.exit(1);
            }
            break;
        case "AS":
            try {
                System.out.println(AuthoritiesScore.runAS(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a AS -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
        case "HS":
            try {
                System.out.println(HubsScore.runHS(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a HS -gA <Graph A File Name> -gB <Graph B File Name> ");
                System.exit(1);
            }
            break;
        case "PR":
            try {
                System.out.println(PageRank.runPR(cmd.getOptionValue("gA"), cmd.getOptionValue("gB")));
            } catch (Exception e) {
            	System.out.println("Usage: java -jar secGraph -m u -a PR -gA <Graph A File Name> -gB <Graph B File Name>");
                System.exit(1);
            }
            break;
            
        default:
        	printHelp(options);
            break;
        }
    }
    private static void printHelp(Options options) {
        System.out.println("Usage java -jar secGraph -m <d/a/u> -a <algorithm> <algorithm inputs>");
        System.exit(1);
    }
}
