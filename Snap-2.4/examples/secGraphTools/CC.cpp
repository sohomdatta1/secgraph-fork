#include <iostream>
#include <string>
#include <fstream>
#include "Snap.h"
using namespace std;
//g++ -std=c++98 -Wall -O3 -DNDEBUG -o CCen CCen.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp  -lrt
int main(int argc, char* argv[]) {
    if(argc!=2) {cout<<"Usage ./CC <GraphName>"<<endl; return 0;}
    PNGraph Graph = TSnap::LoadEdgeList<PNGraph>(argv[1]);
    PUNGraph UGraph = TSnap::ConvertGraph<PUNGraph>(Graph);
    TIntFltH CcfH;
    TSnap::GetNodeClustCf(UGraph, CcfH);
    for (TUNGraph::TNodeI NI = UGraph->BegNI(); NI < UGraph->EndNI(); NI++) {
        const int NId = NI.GetId();
        cout<<NId;
        cout<<" "<<CcfH.GetDat(NId)<<endl;
    }
	return 0;
}
