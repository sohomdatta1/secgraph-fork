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
package views;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import swing2swt.layout.BorderLayout;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Combo;

import deAnon.matching.Comm_Enhanced;
import deAnonymize.BayesianUsingCosSim;
import deAnonymize.DV;
import deAnonymize.Percolation;
import deAnonymize.Propagation;
import deAnonymize.RandomizedSpanningTree;
import deAnonymize.SJCCS2014;
import deAnonymize.SJISC2014;
import deAnonymize.SJISC2014ADA;
import deAnonymize.TheCutBasedAttack;
import deAnonymize.ThePassiveAttack;
import deAnonymize.TheWalkBasedAttack;
import deAnonymize.UserMatching;
import swing2swt.layout.FlowLayout;

public class DeAnonymizationView {
    public static void deAnonymizationView(TabFolder tabFolder,final Shell shell) {
        TabItem tbtmDeAnonymization = new TabItem(tabFolder, SWT.NONE);
        tbtmDeAnonymization.setText("DeAnonymization");
        
        final Composite deAnonymizationComposite = new Composite(tabFolder, SWT.NONE);
        tbtmDeAnonymization.setControl(deAnonymizationComposite);
        deAnonymizationComposite.setLayout(new BorderLayout(0, 0));
        
        Composite composite = new Composite(deAnonymizationComposite, SWT.NONE);
        composite.setLayoutData(BorderLayout.NORTH);
        composite.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        Label lbldeAnonymizationAlgo = new Label(composite, SWT.NONE);
        lbldeAnonymizationAlgo.setText("DeAnonymization Techniques:");
        
        final Combo deAnonymizationCombo = new Combo(composite, SWT.READ_ONLY);
        
        final String[] deAnonymizationAlgoNames = { 
                "",
                "Walk-Based Attack",
                "Cut-Based Attack",
                "Backstrom-Passive Attack",
                "Narayanan-Shmatikov Attack",
                "Link Prediction",
                "Community Enhanced Attack",
                "Distance Vector",
                "Random Spanning Tree",
                //"Recursive Subgraph Matching",
                "Bayesian Attack",
                "Percolation Attack",
                "ISC De-Anonymization attack",
                "ISC Adaptive De-Anonymization Attack",
                "reconciliation Attack",
                "CCS Attack"
                };
        for(String s : deAnonymizationAlgoNames){
            deAnonymizationCombo.add(s);
        }
        final Composite algoComposit=new Composite(deAnonymizationComposite, SWT.NONE);
        deAnonymizationCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String algo=deAnonymizationAlgoNames[deAnonymizationCombo.getSelectionIndex()];
                switch (algo) {
                case "Walk-Based Attack":
                    walkBasedAttack(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Cut-Based Attack":
                    cutBasedAttack(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Backstrom-Passive Attack":
                    backstormPassiveAttack(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Narayanan-Shmatikov Attack":
                    narayananShmatikovAttack(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Link Prediction":
                    linkPrediction(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Community Enhanced Attack":
                    community(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Distance Vector":
                    distanceVector(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Random Spanning Tree":
                    randSpanningTree(shell, deAnonymizationComposite, algoComposit);
                    break;
                //case "Recursive Subgraph Matching":
                    //recursiveSubgraphMatching(shell, deAnonymizationComposite, algoComposit);
                    //break;
                case "Bayesian Attack":
                    bayesian(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "Percolation Attack":
                    precolation(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "ISC De-Anonymization attack":
                    ISCDeAnony(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "ISC Adaptive De-Anonymization Attack":
                    ISCAdaptive(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "reconciliation Attack":
                    reconciliation(shell, deAnonymizationComposite, algoComposit);
                    break;
                case "CCS Attack":
                    CCS(shell, deAnonymizationComposite, algoComposit);
                    break;
                
                default:
                    if(algoComposit!=null){
                        for(Control c : algoComposit.getChildren()) {
                            c.dispose();
                        }
                    }
                    algoComposit.setLayoutData(BorderLayout.CENTER);
                    algoComposit.setLayout(new GridLayout(3, false));
                    break;
                } 
                if(algoComposit!=null){
                    algoComposit.layout(true);
                    algoComposit.redraw();
                    deAnonymizationComposite.layout(true);
                    deAnonymizationComposite.redraw();
                }
            }

            private void CCS(Shell shell, Composite deAnonymizationComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,//0
                                SecGraphConstants.OPEN,//1
                                SecGraphConstants.OPEN,//2
                                SecGraphConstants.SAVE,//3
                                SecGraphConstants.NONE,//4
                                SecGraphConstants.NONE,//5
                                SecGraphConstants.NONE,//6
                                SecGraphConstants.NONE,//7
                                SecGraphConstants.NONE,//8
                                SecGraphConstants.NONE,//9
                                SecGraphConstants.NONE//10
                                };
                String[]  Names={"Input Graph File A:",//0
                                 "Input Graph File B:",//1
                                 "Seeds:",//2
                                 "Output File:",//3
                                 "Bipart Size",//4
                                 "Number Kept from Bipart",//5
                                 "Degree Simiularity Weight",//6
                                 "Neighbor Simiularity Weight",//7
                                 "Number of Neighbor to Consier",//8
                                 "Distance Simiularity Weight",//9
                                 "mode"//10
                                 };
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        //0   1  2        4         5                   6       7       9       8           3
                        //g1 g2 seedFile BipartSize numToKeepPerBipart degSimW neiSimW disSimW numNeiToCons outputFileName
                        try {
                            SJCCS2014.runSJCCS2014(Text.get(0).getText(), //1
                                    Text.get(1).getText(), //2
                                    Text.get(2).getText(), //3
                                    Text.get(4).getText(),//4
                                    Text.get(5).getText(), //5
                                    Text.get(6).getText(), //6
                                    Text.get(7).getText(), //7
                                    Text.get(9).getText(), //8
                                    Text.get(8).getText(),//9
                                    Text.get(3).getText(), //10
                                    Text.get(10).getText());//11
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void reconciliation(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                //User matching
                Integer[] Type={SecGraphConstants.OPEN,
                                SecGraphConstants.OPEN,
                                SecGraphConstants.OPEN,
                                SecGraphConstants.SAVE,
                                SecGraphConstants.NONE
                                };
                String[]  Names={"Input Graph File A:",
                                 "Input Graph File B:",
                                 "Seeds:",
                                 "Output File:",
                                 "Number to test"
                                };
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // g1 g2 seedFile numToTest outputFileName
                        try {
                            UserMatching.main(new String[]{Text.get(0).getText(),
                                                           Text.get(1).getText(),
                                                           Text.get(2).getText(),
                                                           Text.get(4).getText(),
                                                           Text.get(3).getText(),});
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void ISCAdaptive(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type = {  SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN, 
                        SecGraphConstants.OPEN,
                        SecGraphConstants.SAVE, 
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        };
    String[] Names = {  "Input Graph File A:",
                        "Input Graph File B:", 
                        "Seeds:", 
                        "Output File:",
                        "Graph A Centraility File:",
                        "Graph B Centraility File:", 
                        "Bipart Size",
                        "Bipart to Keep", 
                        "distance simuliarty weight",
                        "structure simuliarty weight",
                        "inherted simuliarty weight" ,
                        "mode"
                        };
    final ArrayList<Text> Text=new ArrayList<>();
    
    Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
    Start.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                SJISC2014ADA.runISCADA(Text.get(0).getText(), Text.get(1).getText(), 
                        Text.get(4).getText(), Text.get(5).getText(), 
                        Text.get(11).getText(), 
                        Text.get(2).getText(), 
                        Text.get(6).getText(), Text.get(7).getText(), 
                        Text.get(8).getText(), Text.get(9).getText(), Text.get(10).getText(), Text.get(3).getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    });                
}

            private void ISCDeAnony(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type = {  SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN, 
                        SecGraphConstants.OPEN,
                        SecGraphConstants.SAVE, 
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE, 
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        };
    String[] Names = {  "Input Graph File A:",
                        "Input Graph File B:", 
                        "Seeds:", 
                        "Output File:",
                        "Graph A Centraility File:",
                        "Graph B Centraility File:", 
                        "Bipart Size",
                        "Bipart to Keep", 
                        "distance simuliarty weight",
                        "structure simuliarty weight",
                        "inherted simuliarty weight" ,
                        "mode"
                        };
    final ArrayList<Text> Text=new ArrayList<>();
    
    Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
    Start.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                SJISC2014.runISC(Text.get(0).getText(), Text.get(1).getText(), 
                        Text.get(4).getText(), Text.get(5).getText(), 
                        Text.get(11).getText(), 
                        Text.get(2).getText(), 
                        Text.get(6).getText(), Text.get(7).getText(), 
                        Text.get(8).getText(), Text.get(9).getText(), Text.get(10).getText(), Text.get(3).getText());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    });                
}

            private void precolation(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Seeds:","Output File:","Number Correct to Keep"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            //g1 g2 numCredToKeep seedFile outputFileName
                            Percolation.main(new String[]{Text.get(0).getText(),
                                                          Text.get(1).getText(),
                                                          Text.get(4).getText(),
                                                          Text.get(2).getText(),
                                                          Text.get(3).getText()});
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void bayesian(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Output File:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent e) {
                           try {
                            BayesianUsingCosSim.bayesianUsingCosSim(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void randSpanningTree(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Seeds:","Output File:","Number of Spanning Trees"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            RandomizedSpanningTree.runRSP(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText(),
                                    Text.get(4).getText(), Text.get(3).getText());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void distanceVector(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
              //Distance vector
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Seeds:","Output File:","Bipart Size:","Number to keep From Bipart:","Mode"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            DV.runDV(Text.get(0).getText(),
                                    Text.get(1).getText(),  Text.get(2).getText(),  Text.get(4).getText(), Text.get(5).getText(), Text.get(3).getText(), Text.get(6).getText());
       
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void community(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.SAVE,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,};
                String[] Names = {  "ResultsDirectory", 
                                    "NumSeeds", 
                                    "Noise",
                                    "Exp", 
                                    "FileName1.net", 
                                    "FileName2.net",
                                    "FileName.clique", 
                                    "Prob/DegAnon(False/True)",
                                    "Overlapped(False/True)", 
                                    "FileName.avgProb (Optional)" };
                
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            String ResultsDirectory = Text.get(0).getText();
                            String NumSeeds   = Text.get(1).getText();
                            String Noise  = Text.get(2).getText();
                            String Exp  = Text.get(3).getText();
                            String FileName1Net  = Text.get(4).getText();
                            String FileName2Net  = Text.get(5).getText();
                            String FileNameClique  = Text.get(6).getText();
                            boolean ProbDegAnon = Boolean.parseBoolean(Text.get(7).getText());
                            String Overlapped  = Text.get(7).getText();
                            String FileNameAvgProb  = Text.get(9).getText();
                            if(!ProbDegAnon || FileNameAvgProb.isEmpty()){
                                Comm_Enhanced.main(new String[]{ResultsDirectory,NumSeeds,Noise,Exp,FileName1Net,FileName2Net,
                                        FileNameClique,ProbDegAnon+"",Overlapped});
                            }else{
                                Comm_Enhanced.main(new String[]{ResultsDirectory,NumSeeds,Noise,Exp,FileName1Net,FileName2Net,
                                        FileNameClique,ProbDegAnon+"",Overlapped,FileNameAvgProb});
                            }
                            
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void linkPrediction(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                narayananShmatikovAttack(shell, deAnonymizationComposite, algoComposit);
            }

            private void narayananShmatikovAttack(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                //Propagation method
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Seeds:","Output File:","Theta:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Propagation.main(new String[]{Text.get(0).getText(),Text.get(1).getText(),Text.get(2).getText(),Text.get(3).getText(),Text.get(4).getText()});
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void backstormPassiveAttack(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE};
                String[]  Names={"Input Graph File:","Input Coalition Graph File:","Input Coalition Degree File:","Output File:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            ThePassiveAttack.thePassiveAttack(Text.get(0).getText(), Text.get(1).getText(), 
                                    Text.get(2).getText(), Text.get(3).getText());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                
            }

            private void cutBasedAttack(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.SAVE};
                String[]  Names={"Input Graph File:",
                                 "Target List File:",
                                 "Output:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                      try {
                        TheCutBasedAttack.main(new String[]{
                                  Text.get(0).getText(),Text.get(1).getText(),Text.get(2).getText(),
                          });
                    } catch (IOException | InterruptedException e1) {
                        e1.printStackTrace();
                    }  
                    }
                });
                
            }

            private void walkBasedAttack(Shell shell,
                    Composite deAnonymizationComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,
                                SecGraphConstants.OPEN,
                                SecGraphConstants.SAVE,
                                SecGraphConstants.SAVE,
                                SecGraphConstants.SAVE,
                                SecGraphConstants.SAVE
                                };
                String[]  Names={"Input Graph File:",
                                 "Input Target File",
                                 "Output File Degree File:",
                                 "Modified Graph File:",
                                 "Internal Edges File",
                                 "Recovered Graph File:"
                                  };
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(deAnonymizationComposite, algoComposit, shell, Names, Text, Type);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            TheWalkBasedAttack.theWalkBasedAttack(Text.get(0).getText(), Text.get(2).getText(), Text.get(1).getText(), Text.get(3).getText(), Text.get(4).getText(), Text.get(5).getText());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            

        });
        deAnonymizationCombo.select(0);
    }
    
    
    
    
}
