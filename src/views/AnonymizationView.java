/*
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

import anonymize.BoundedTMean;
import anonymize.IdRemoval;
import anonymize.KDa;
import anonymize.RandAddDel;
import anonymize.RandWalk;
import anonymize.SalaDP;
import anonymize.UnionSplit;
import anonymize.kIso;
import anonymize.xiaoDP;
import swing2swt.layout.FlowLayout;

public class AnonymizationView {
    public static void anonymizationView(TabFolder tabFolder,final Shell shell) {
        TabItem tbtmAnonymization = new TabItem(tabFolder, SWT.NONE);
        tbtmAnonymization.setText("Anonymization");
        
        final Composite anonymizationComposite = new Composite(tabFolder, SWT.NONE);
        tbtmAnonymization.setControl(anonymizationComposite);
        anonymizationComposite.setLayout(new BorderLayout(0, 0));
        
        Composite composite = new Composite(anonymizationComposite, SWT.NONE);
        composite.setLayoutData(BorderLayout.NORTH);
        composite.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        Label lblAnonymizationAlgo = new Label(composite, SWT.NONE);
        lblAnonymizationAlgo.setText("Anonymization Techniques:");
        
        final Combo anonymizationCombo = new Combo(composite, SWT.READ_ONLY);
        
        final String[] anonymizationAlgoNames = { 
                "",
                "ID removal",
                "Add/Del Edge Editing", 
                "Switch Edge Editing", 
                "k-DA", 
                "k-iso",
                "bounded t-means clustering", 
                "union-split clustering",
                "Sala et al.’s DP based algorithm",
                "Proserpio et al.’s DP based algorithm",
                "Xiao et al.’s DP based algorithm",
                "Random Walk based algorithm"};
        for(String s : anonymizationAlgoNames){
            anonymizationCombo.add(s);
        }
        final Composite algoComposit=new Composite(anonymizationComposite, SWT.NONE);
        anonymizationCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String algo=anonymizationAlgoNames[anonymizationCombo.getSelectionIndex()];
                switch (algo) {
                case "ID removal":
                    IDRemoval(shell, anonymizationComposite, algoComposit);
                    break;
                case "Add/Del Edge Editing":
                    addDel(shell, anonymizationComposite, algoComposit);
                    break;
                case "Switch Edge Editing":
                    randSwitch(shell, anonymizationComposite, algoComposit);
                    break;
                case "k-DA": 
                    kDa(shell, anonymizationComposite, algoComposit);
                    break;
                case "k-iso":
                    kIso(shell, anonymizationComposite, algoComposit);
                    break;
                case "bounded t-means clustering":
                    tMean(shell, anonymizationComposite, algoComposit);
                    break;
                case "union-split clustering":
                    unionSplit(shell, anonymizationComposite, algoComposit);
                    break;
                case "Sala et al.’s DP based algorithm":
                    salaDP(shell, anonymizationComposite, algoComposit);
                    break;
                case "Proserpio et al.’s DP based algorithm": 
                    proserpioDP(shell, anonymizationComposite, algoComposit);
                    break;
                case "Xiao et al.’s DP based algorithm":
                    xiaoDP(shell, anonymizationComposite, algoComposit);
                    break;
                case "Random Walk based algorithm":
                    rWalk(shell, anonymizationComposite, algoComposit);
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
                    anonymizationComposite.layout(true);
                    anonymizationComposite.redraw();
                }
            }

            private void rWalk(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] rWalkType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  rWalkNames={"Input Graph File:",
                                      "Output File:",
                                      "Number of Random Steps:",
                                      "Max Number of Retries:"};
                final ArrayList<Text> rWalkText=new ArrayList<>();
                
                Button rWalkStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, rWalkNames, rWalkText, rWalkType);
                rWalkStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            RandWalk.main(new String[]{rWalkText.get(0).getText(),
                                                       rWalkText.get(1).getText(),
                                                       rWalkText.get(2).getText(),
                                                       rWalkText.get(3).getText(),});
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void xiaoDP(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                //String gName, String epsilonHRG, String epsilonE, String eq, String stop
                Integer[] xiaoDPType={SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.NONE
                        ,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  xiaoDPNames={"Input .pairs graph file:",
                                       "Input privacy budget for HRG: (optional input group 1)",
                                       "Input privacy budget for edge perturbation: (optional input group 1)",
                                       "Threshold for manually forcing MCMC stop after eq*n steps and reaching convergence (optional input group 2)",
                                       "Threshold for manually stop MCMC stop after stop*n (optional input group 2)"};
                final ArrayList<Text> xiaoDPText=new ArrayList<>();
                
                Button xiaoDPStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, xiaoDPNames, xiaoDPText, xiaoDPType);
                xiaoDPStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String g = xiaoDPText.get(0).getText();
                        String epsilonHRG = xiaoDPText.get(1).getText();
                        String epsilonE = xiaoDPText.get(2).getText();
                        String eq = xiaoDPText.get(3).getText();
                        String stop = xiaoDPText.get(4).getText();
                        if(!epsilonE.isEmpty() && !epsilonHRG.isEmpty()&&!eq.isEmpty()&&!stop.isEmpty()){
                            try {
                                xiaoDP.runXiao(g, epsilonHRG, epsilonE, eq, stop);
                            } catch (IOException | InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        else if(!epsilonE.isEmpty() && !epsilonHRG.isEmpty()){
                            try {
                                xiaoDP.runXiao(g, epsilonHRG, epsilonE);
                            } catch (IOException | InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        else{
                            try {
                                xiaoDP.runXiao(g);
                            } catch (IOException | InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }

            private void proserpioDP(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] proserpioDPType={SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.SAVE,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  proserpioDPNames={"Input Graph File:","parallel:","outputFile","mcmcIterations","epsilon"};
                final ArrayList<Text> proserpioDPText=new ArrayList<>();
                
                Button proserpioDPStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, proserpioDPNames, proserpioDPText, proserpioDPType);
                proserpioDPStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            anonymize.proserpioDP.runproserpioDP(proserpioDPText.get(0).getText(), proserpioDPText.get(1).getText(), proserpioDPText.get(2).getText(), proserpioDPText.get(3).getText(), proserpioDPText.get(4).getText());
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void salaDP(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] salaDPType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  salaDPNames={"Input Graph File:","Output File:","Epselon"};
                final ArrayList<Text> salaDPText=new ArrayList<>();
                
                Button salaDPStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, salaDPNames, salaDPText, salaDPType);
                salaDPStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            SalaDP.main(new String[]{salaDPText.get(0).getText(),
                                                     salaDPText.get(1).getText(),
                                                     salaDPText.get(2).getText()});
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void unionSplit(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] unionSplitType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  unionSplitNames={"Input Graph File:","Output File:","Cluster Size"};
                final ArrayList<Text> unionSplitText=new ArrayList<>();
                
                Button unionSplitStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, unionSplitNames, unionSplitText, unionSplitType);
                unionSplitStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            UnionSplit.unionSplit(unionSplitText.get(0).getText(), unionSplitText.get(1).getText(), Integer.parseInt(unionSplitText.get(2).getText()));
                        } catch (NumberFormatException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void tMean(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] tMeanType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  tMeanNames={"Input Graph File:","Output File:","t:"};
                final ArrayList<Text> tMeanText=new ArrayList<>();
                
                Button tMeanStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, tMeanNames, tMeanText, tMeanType);
                tMeanStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            BoundedTMean.tMean(tMeanText.get(0).getText(), tMeanText.get(1).getText(), Integer.parseInt(tMeanText.get(2).getText()));
                        } catch (NumberFormatException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void kIso(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] kIsoType={SecGraphConstants.NONE,SecGraphConstants.NONE,SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.SAVE };
                String[]  kIsoNames={"K:","curNNum:","Input Graph:","Status Result File:","Edge Result File:"};
                final ArrayList<Text> kIsoText=new ArrayList<Text>();
                
                Button kIsoStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, kIsoNames, kIsoText, kIsoType);
                kIsoStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String k=kIsoText.get(0).getText(),curNNum=kIsoText.get(1).getText(),
                                g=kIsoText.get(2).getText(),result=kIsoText.get(3).getText(),resultEdge=kIsoText.get(4).getText();
                        try {
                            kIso.runKISO(k, curNNum, g, result, resultEdge);
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void kDa(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] kDaType={SecGraphConstants.OPEN,SecGraphConstants.SAVE};
                String[]  kDaNames={"Input Graph File:","Output File:"};
                final ArrayList<Text> kDaText=new ArrayList<>();
                
                Button kDaStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, kDaNames, kDaText, kDaType);
                kDaStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            KDa.kDa(kDaText.get(0).getText(), kDaText.get(1).getText(), Integer.parseInt(kDaText.get(2).getText()));
                        } catch (NumberFormatException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void randSwitch(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] SwType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  SwNames={"Input Graph File:","Output File:","Fraction Edge Swaped:"};
                final ArrayList<Text> SwText=new ArrayList<>();
                
                Button SwStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, SwNames, SwText, SwType);
                SwStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            anonymize.RandSw.RandSwitch(SwText.get(0).getText(), SwText.get(1).getText(), Double.parseDouble(SwText.get(2).getText()));
                        } catch (NumberFormatException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

            private void addDel(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] AddDelType={SecGraphConstants.OPEN,SecGraphConstants.SAVE,SecGraphConstants.NONE};
                String[]  AddDelNames={"Input Graph File:","Output File:","Fraction Edge Edited:"};
                final ArrayList<Text> AddDeltext=new ArrayList<>();
                
                Button AddDelStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, AddDelNames, AddDeltext, AddDelType);
                AddDelStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                            try {
                                RandAddDel.randAddAndDeleteEdges(AddDeltext.get(0).getText(), AddDeltext.get(1).getText(), Double.parseDouble(AddDeltext.get(2).getText()));
                            } catch (NumberFormatException | IOException e1) {
                                e1.printStackTrace();
                            }
                         
                    }
                });
            }

            private void IDRemoval(final Shell shell,
                    final Composite anonymizationComposite,
                    final Composite algoComposit) {
                Integer[] IDRemoveType={SecGraphConstants.OPEN,SecGraphConstants.SAVE};
                String[]  IDRemovelabelNames={"Input Graph File:","Output File:"};
                final ArrayList<Text> IDRemoveText=new ArrayList<>();
                
                Button IDRemoveStart = CreateAlgoComposit.createAlgoComposit(anonymizationComposite, algoComposit, shell, IDRemovelabelNames, IDRemoveText, IDRemoveType);
                IDRemoveStart.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            IdRemoval.idRemoval(IDRemoveText.get(0).getText(), IDRemoveText.get(1).getText());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }

        });
        anonymizationCombo.select(0);
    }
    
    
    
    
}
