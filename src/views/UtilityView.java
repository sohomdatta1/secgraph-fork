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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;
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
import utility.PageRank;
import utility.ReliableEmail;
import utility.Rolx;
import utility.Sprout;
import utility.Sybil;

public class UtilityView {
    public static void utilityView(TabFolder tabFolder,final Shell shell) {
        TabItem tbtmUtility = new TabItem(tabFolder, SWT.NONE);
        tbtmUtility.setText("Utility");
        
        final Composite utilityComposite = new Composite(tabFolder, SWT.NONE);
        tbtmUtility.setControl(utilityComposite);
        utilityComposite.setLayout(new BorderLayout(0, 0));
        
        Composite composite = new Composite(utilityComposite, SWT.NONE);
        composite.setLayoutData(BorderLayout.NORTH);
        composite.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        Label lbldeAnonymizationAlgo = new Label(composite, SWT.NONE);
        lbldeAnonymizationAlgo.setText("Utility Techniques:");
        
        final Combo utilityCombo = new Combo(composite, SWT.READ_ONLY);
        
        final String[] utilityAlgoNames = { 
                "",
                "Degree",
                "Join Degree",
                "Effective Diameter",
                "Path Length",
                "Local Clustering Coefficient",
                "Node Clustering Coefficient",
                "Closeness Centrality",
                "Betweenness Centrality",
                "Eigen Vector",
                "Network Constraint",
                "Network Resilience",
                "Infectiousness",
                "Role eXtraction",
                "Reliable Email",
                "Influence Maximization",
                "Minimum-sized Influential Node Set",
                "Community Detection",
                "Secure Routing",
                "Sybil Detection",
                "Authorities Score",
                "Hubs Score",
                "Page Rank"
                };
        for(String s : utilityAlgoNames){
            utilityCombo.add(s);
        }
        final Composite algoComposit=new Composite(utilityComposite, SWT.NONE);
        utilityCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String algo=utilityAlgoNames[utilityCombo.getSelectionIndex()];
                switch (algo) {
                case "Degree":
                    degree(shell, utilityComposite, algoComposit);
                    break;
                case "Join Degree":
                    joinDegree(shell, utilityComposite, algoComposit);
                    break;
                case "Effective Diameter":
                    effectiveDiameter(shell, utilityComposite, algoComposit);
                    break;
                case "Path Length":
                    pathLength(shell, utilityComposite, algoComposit);
                    break;
                case "Local Clustering Coefficient":
                    localClusteringCoefficient(shell, utilityComposite, algoComposit);
                    break;
                case "Node Clustering Coefficient":
                    localClusteringCoefficient(shell, utilityComposite, algoComposit);
                    break;
                case "Closeness Centrality":
                    closenessCentrality(shell, utilityComposite, algoComposit);
                    break;
                case "Betweenness Centrality":
                    betweennessCentrality(shell, utilityComposite, algoComposit);
                    break;
                case "Eigen Vector":
                    eigenVector(shell, utilityComposite, algoComposit);
                    break;
                case "Network Constraint":
                    networkConstraint(shell, utilityComposite, algoComposit);
                    break;
                case "Network Resilience":
                    networkResilience(shell, utilityComposite, algoComposit);
                    break;
                case "Infectiousness":
                    infectiousness(shell, utilityComposite, algoComposit);
                    break;
                case "Role eXtraction":
                    role_eXtraction(shell, utilityComposite, algoComposit);
                    break;
                case "Reliable Email":
                    reliableEmail(shell, utilityComposite, algoComposit);
                    break;
                case "Influence Maximization":
                    influenceMaximization(shell, utilityComposite, algoComposit);
                    break;
                case "Minimum-sized Influential Node Set":
                    minimumInfluentialNodeSet(shell, utilityComposite, algoComposit);
                    break;
                case "Community Detection":
                    communityDetection(shell, utilityComposite, algoComposit);
                    break;
                case "Secure Routing":
                    secureRouting(shell, utilityComposite, algoComposit);
                    break;
                case "Sybil Detection":
                    sybilDetection(shell, utilityComposite, algoComposit);
                    break;
                    
                case "Authorities Score":
                    AS(shell, utilityComposite, algoComposit);
                    break;
                case "Hubs Score":
                    HS(shell, utilityComposite, algoComposit);
                    break;
                case "Page Rank":
                    PR(shell, utilityComposite, algoComposit);
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
                    utilityComposite.layout(true);
                    utilityComposite.redraw();
                }
            }

            private void PR(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(PageRank.runPR(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
                
            }

            private void HS(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(HubsScore.runHS(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });            
                
            }

            private void AS(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(AuthoritiesScore.runAS(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });            
                
            }

            private void sybilDetection(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Sprout.runSprout(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                 
            }

            private void secureRouting(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,
                                SecGraphConstants.OPEN,
                                SecGraphConstants.NONE,
                                SecGraphConstants.NONE,
                                SecGraphConstants.NONE,
                                SecGraphConstants.NONE,
                                SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:",
                                 "Input Graph File B:",
                                 "Sybil Detection numSInstances",
                                 "Sybil Detection rseed",
                                 "Sybil Detection walkLengh",
                                 "Sybil Detection balanceCondHva",  
                                 "Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(6).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(6).setText(Sybil.runSybil(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText(), Text.get(3).getText(), Text.get(4).getText(), Text.get(5).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                    
            }

            private void communityDetection(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Community.calcCommunitySim(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                             
            }

            private void minimumInfluentialNodeSet(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","P:","Threshold:","Result"};
               final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(4).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(6).setText(MINS.compareMINS(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText(), Text.get(3).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }

            private void influenceMaximization(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(InfluenceMax.runInfluxMax(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                   
            }

            private void reliableEmail(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.OPEN,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE,
                        SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Graph A Spamming Node Sequence File:",
                                 "Graph B Spamming Node Sequence File:","Number Of Neighbor to Accept Spam:","Use two hop?(true/false):","Results:"};
               final ArrayList<Text> Text=new ArrayList<>();
                
               Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
               Text.get(6).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(6).setText(ReliableEmail.compareRE(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText(), Text.get(3).getText(),
                                    Text.get(4).getText(), Text.get(5).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }

            private void role_eXtraction(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Rolx.calcRolxSim(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                               
            }

            private void infectiousness(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Number of Repeats:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(3).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(3).setText(Infectiousness.calcInfec(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }

            private void networkResilience(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Limit:","Result:"};
                
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(3).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(3).setText(NetworkResilience.GetNR(Text.get(0).getText(), Text.get(1).getText(), Text.get(2).getText())+"");
                        } catch (IOException | InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }

            private void networkConstraint(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(NetConstraint.runNC(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                             
            }

            private void eigenVector(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(EigenVectorCentr.runEV(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                             
            }

            private void betweennessCentrality(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Betweenness.runBC(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                 
            }

            private void closenessCentrality(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Closeness.runCCen(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                    
            }

            private void localClusteringCoefficient(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(LocalClusterCoefficient.LCC(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });             
            }

            private void pathLength(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","number of nodes","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(3).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(3).setText(Distance.getDistance(Text.get(0).getText(), Text.get(1).getText(),Text.get(2).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                            
            }

            private void effectiveDiameter(Shell shell,
                    Composite utilityComposite, Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(EffDiam.runED(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                      
            }

            private void joinDegree(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(JD.joinDeg(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }

            private void degree(Shell shell, Composite utilityComposite,
                    Composite algoComposit) {
                Integer[] Type={SecGraphConstants.OPEN,SecGraphConstants.OPEN,SecGraphConstants.NONE};
                String[]  Names={"Input Graph File A:","Input Graph File B:","Result:"};
                final ArrayList<Text> Text=new ArrayList<>();
                Button Start = CreateAlgoComposit.createAlgoComposit(utilityComposite, algoComposit, shell, Names, Text, Type);
                Text.get(2).setEnabled(false);
                Start.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        try {
                            Text.get(2).setText(Deg.getDeg(Text.get(0).getText(), Text.get(1).getText())+"");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });                                
            }
        });
        utilityCombo.select(0);
    }
}

