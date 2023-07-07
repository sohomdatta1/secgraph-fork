#include <iostream>
#include <string>
#include <fstream>
#include "Snap.h"
using namespace std;
//g++ -std=c++98 -Wall -O3 -DNDEBUG -o CCen CCen.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp  -lrt
int main(int argc, char* argv[]) {
    if(argc!=2) {cout<<"Usage ./ED <GraphName>"<<endl; return 0;}
    PNGraph Graph = TSnap::LoadEdgeList<PNGraph>(argv[1]);
    PUNGraph UGraph = TSnap::ConvertGraph<PUNGraph>(Graph);
    
    TIntFltKdV DistNbrsV;
    TSnap::GetAnf(UGraph, DistNbrsV, -1, false, 32);
    const double EffDiam = TSnap::TSnapDetail::CalcEffDiam(DistNbrsV, 0.9);
    cout<<EffDiam<<endl;
	return 0;
}
