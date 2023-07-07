#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
//#include <math.h>

#ifndef WIN32
#include <sys/resource.h>
#include <sys/times.h>
#include <unistd.h>
#endif



const int SZ_INT=sizeof(int);
const int SZ_DOUBLE=sizeof(double);
const int SZ_PTR=sizeof(void*);
const int SZ_LONG=sizeof(unsigned long);
//const int INT2STR_SZ=10;


struct Graph{
	//int unikEdgeNum;
	//int *unikEdgeAr;		//an array of unikEdges of the graph
										//size of each unikEdgeAr = EDGESZ = 3
										//(i * EDGESZ+0)-th position: fromNodeLabel
										//(i * EDGESZ+1)-th position: edgeLabel
										//(i * EDGESZ+2)-th position: toNodeLabel
	//int *unikEdgeFreqAr;		//(sz: unikEdgeNum) the frequency of each unikEdge in curUEAr 


	//int unikNodeLbNum;	//number of unique node labels
	//int *unikNodeLbAr;		//(size: unikNodeLbNum) an ordered array of unique node labels
	//int *unikNodePosAr;		//an array of starting positions of nodes having the unikNodeLb in edgePosAar
												//size: |unikNodePosAr| = unikNodeLbNum + 1
												//unikNodePosAr[unikNodeLbNum] = nodeNum
												//frequency of each unikNodeLb: freq(unikNodeLbAr[i]) = unikNodePosAr[i+1] - unikNodePosAr[i]

	int nodeNum;				//total number of nodes: commented because unikNodePosAr[unikNodeLbNum] = nodeNum
	//int *nodeLbAr;
	int *degreeAr;			//(size: nodeNum) an array of number of edges of each node
										//position of edgeNumAr is also ID of nodes
										//Nodes of the same NodeLb but with a greater edgeNum 
											//is ordered before those with a smaller edgeNum (i.e. ordered by degree) 
	int **edgePosAar;		//(size: nodeNum) (sz of edgePosAar[i]: degreeAr[i]) an array of nodeNum arrays of positions of edges of each node in edgeAr

	int edgeNum;			//total number of edges
	int *edgeAr;				//(size: edgeNum * EDGESZ) an array of edges
											//size of each edgeAr entry = edgeSz = 2
											
											//(i * edgeSz + 0)-th position: toNode ID (position of fromNode in edgeNumAr)
											//(i * edgeSz + 1)-th position: fromNode ID (position of toNode in edgeNumAr)

};
const int SZ_GRAPH=sizeof(Graph);
const int EDGESZ=2;

struct qHashSlot{
	Graph *thisGraph;

	//other fields

	qHashSlot *next;
};
struct Embedding{
	int* VM;
	int* nodeID;
	int nodeNum;
	int edgeNum;
	int* edgeID;
	Graph *thisgraph;
	Embedding *next;
};

const int SZ_EMBEDDING=sizeof(Embedding);

struct PAG{
	int frequent;
	Graph *thisgraph;
	Embedding *EmbeddingList;
	Embedding *End;
	Embedding *VDEmbeddingList;
};
const int SZ_PAG = sizeof(PAG);

struct PAGSlot{
	PAG *thisPAG;
	PAGSlot *next;
};
const int SZ_PAGSLOT = sizeof(PAGSlot);

struct PAGQueue{
	PAG *thisPAG;
	int frequent;
	PAGQueue *next;
};

struct AddEdge{
	int fromnode;
	int tonode;
	AddEdge *next;
};
const int SZ_AddEdge = sizeof(AddEdge);

struct Node{ //should be edge
	int node;
	Node *next;
};

const int SZ_Node = sizeof(Node);

struct KPartition{
	int nodeNum;
	int **nodeAr; //size of nodeAr is the number of nodes which are contained by the certain partition
	              //size of nodeAr[] is K
};
const int SZ_Partition = sizeof(KPartition) ;

class graph{
public:
	int* curNum;
	graph();
	~graph();
	void print_graph();
}gra;
