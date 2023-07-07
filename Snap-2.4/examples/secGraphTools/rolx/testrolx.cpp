#include <string.h>
#include <iostream>
#include <string>
#include <fstream>
#include "stdafx.h"
#include "rolx.h"

int main(int argc, char* argv[]) {
    if(argc!=2) {std::cout<<"Usage ./rolx <GraphName>"<<std::endl; return 0;}
  const TStr InFNm = argv[1];
  const int MinRoles = 2;
  const int MaxRoles = 3;
  double Threshold = 1e-6;
  
  PUNGraph Graph = TSnap::LoadEdgeList<PUNGraph>(InFNm, 0, 1);
  TIntFtrH Features = ExtractFeatures(Graph);
  TIntIntH NodeIdMtxIdH = CreateNodeIdMtxIdxHash(Features);
  TFltVV V = ConvertFeatureToMatrix(Features, NodeIdMtxIdH);
  TFlt MnError = TFlt::Mx;
  TFltVV FinalG, FinalF;
  int NumRoles = -1;
  for (int r = MinRoles; r <= MaxRoles; ++r) {
    TFltVV G, F;
    CalcNonNegativeFactorization(V, r, G, F, Threshold);
    TFlt Error = CalcDescriptionLength(V, G, F);
    if (Error < MnError) {
      MnError = Error;
      FinalG = G;
      FinalF = F;
      NumRoles = r;
    }
  }
  TIntIntH Roles = FindRoles(FinalG, NodeIdMtxIdH);
  for (TIntIntH::TIter HI = Roles.BegI(); HI < Roles.EndI(); HI++) {
          std::cerr<<HI.GetKey()()<<" "<<HI.GetDat()()<<std::endl;
    }  
    return 0;
}
