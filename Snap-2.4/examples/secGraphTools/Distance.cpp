#include <iostream>
#include <vector>
#include <sstream>
#include <string>
#include <fstream>
#include "Snap.h"
using namespace std;
void FindPoint(PUNGraph& Graph, int &i){
		TIntH NDistH(Graph->GetNodes());
		TSnap::GetShortPath<PUNGraph>(Graph, i, NDistH, true, TInt::Mx);
		for (TIntH::TIter I = NDistH.BegI(); I < NDistH.EndI(); I++) {
			int dist =I->Dat();
			int to = I->Key();
			cout<<i<<" "<<to<<" "<<dist<<endl;
		}
}

int main(int argc, char* argv[]) {
	if(argc!=3) {cout<<"Usage ./Distance <GraphName> <Nodes>"<<endl; 
        for(int i = 0;i<argc;i++){
            cout<<argv[i]<<endl;
        }
        return 0;}	
	PUNGraph Graph = TSnap::LoadEdgeList<PUNGraph>(argv[1]);
	string fn=argv[1];
	string nodeList = argv[2];
	stringstream str(nodeList);
	vector<int> nodes;

	while(!str.eof()){
		string tmp;
		str>>tmp;
		int nodeI = atoi( tmp.c_str() );
		nodes.push_back(nodeI);
	}

	for (std::vector<int>::iterator i = nodes.begin() ; i != nodes.end(); ++i){
		
		FindPoint(Graph, *i);

	}
	return 0;
}


