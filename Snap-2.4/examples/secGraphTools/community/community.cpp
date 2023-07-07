#include <iostream>
#include <string>
#include <fstream>
#include "stdafx.h"

int main(int argc, char* argv[]) {
  if(argc!=2) {std::cout<<"Usage ./Community <GraphName>"<<std::endl; return 0;}

  PUNGraph Graph = TSnap::LoadEdgeList<PUNGraph>(argv[1], false);
  //PUNGraph Graph = TSnap::LoadEdgeList<PUNGraph>("../as20graph.txt", false);
  //PUNGraph Graph = TSnap::GenRndGnm<PUNGraph>(5000, 10000); // generate a random graph

  TSnap::DelSelfEdges(Graph);
  TCnComV CmtyV;
  TStr CmtyAlgStr;
  CmtyAlgStr = "Clauset-Newman-Moore";
  TSnap::CommunityCNM(Graph, CmtyV); 
  

  for (int c = 0; c < CmtyV.Len(); c++) {
    for (int i = 0; i < CmtyV[c].Len(); i++) {
      std::cout<<CmtyV[c][i].Val<<" "<<c<<std::endl;
    }
  }
  return 0;
}

