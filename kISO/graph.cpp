#include "./graph.h"

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Parameters for tmp memory management (START)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
const int BLK_NUM=102400;
const int BLK_SZ=4194304;        //1024*1024*4

char *memBlkAr[BLK_NUM];        //An array of pointers to allocated blocks
char *curMemPos;        //The first free pos (startpos) in the first free block for writing
char *curMemEnd;        //The end pos of the first free block for writing
int curBlk,endBlk;        //curBlk: the ID of the current block
//endBlk: the ID of the last allocated block
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Parameters for tmp memory management (END)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Parameters for permanent memory management (START)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
const int BLK_NUM2=102400;
const int BLK_SZ2=4194304;        //1024*1024*4

char *memBlkAr2[BLK_NUM2];        //An array of pointers to allocated blocks
char *curMemPos2;        //The first free pos (startpos) in the first free block for writing
char *curMemEnd2;        //The end pos of the first free block for writing
int curBlk2;        //curBlk: the ID of the current block
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Parameters for permanent memory management (END)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

const int freeMemBlkSz=1024;

//const int HASHFNODE=820*311;
//const int HASHEDGE=820*1129*311;
//const int HASHTNODE=1129*311;
const int HASHFNODE=255020;
const int HASHEDGE=287917580;
const int HASHTNODE=351119;
//onst int HASHFNODE=11;
//const int HASHEDGE=41;
//const int HASHTNODE=311;

//EdgeHashSlot **EdgeHashTable;
//const int EDGEHASHTABLESZ=4096;
//const int EDGEHASHMASK=4095;
//        const int EDGEHASHCONST=16;


const int MAXUENUM=10240;
const int MAXUNNUM=10240;
const int MAXENUM=10240;


//        int **joinIDaar;
//        int *joinNumAr;
//        int *joinPosAr;
//        int low,middle,high;
const int BSstartSz=16;
const int qSortStartSz=1024;


const int QHASHTABLESZ=131072;
const int QHASHMASK=131071;
qHashSlot **qHashTable;



int *NIDar;
int PAGnodeNum;
int PAGedgeNum;
int PAGNum;
PAGSlot **MaxPAGHashTable;
const int PAGHASHTABLESZ=131072;

Graph *OriginalGraph;
PAGQueue *Queue;

int SubgraphSize;
int find;
AddEdge *Addedge;
int Add_edges;
AddEdge *endAddedge;

int K;
int MaxPAGSize;
int *TravelEdge;
int *EdgeMapping;
int *NodeMapping;
KPartition *Partition;
int Delete_edges;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//END OF VARIABLE DEFINITIONS
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

graph::graph()
{
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Initialization of memory blocks (START)
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Initialize memory blocks (START)
	curBlk2=0;
	curMemPos2=(char*)malloc(BLK_SZ2);
	memBlkAr2[0]=curMemPos2;
	curMemEnd2=curMemPos2+BLK_SZ2;
	//Initialize memory blocks (END)
	
	
	//Initialize memory blocks (START)
	curBlk=endBlk=0;
	curMemPos=(char*)malloc(BLK_SZ);
	memBlkAr[0]=curMemPos;
	curMemEnd=curMemPos+BLK_SZ;
	//Initialize memory blocks (END)
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Initialization of memory blocks (START)
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	//Declare and initialize hashtable
	qHashTable=(qHashSlot**)malloc(SZ_PTR * QHASHTABLESZ);
	memset(qHashTable,0,(SZ_PTR * QHASHTABLESZ));
	MaxPAGHashTable = (PAGSlot**)malloc(SZ_PTR * PAGHASHTABLESZ);
	memset(MaxPAGHashTable,0,(SZ_PTR * PAGHASHTABLESZ));
	
    Queue = (PAGQueue*)malloc(sizeof(PAGQueue));
	Queue -> next = NULL;
	Queue ->thisPAG = NULL;
	
	Addedge = endAddedge = NULL;
	
	Add_edges = 0;
	
	//(sz: maxNodeNum) an array of node IDs: 0, 1, 2, ... used for memcpy initialization
	NIDar=(int*)malloc(SZ_INT * (MAXUNNUM+1) * 2);
	int i=0;
	do{
		NIDar[i]=i;
	}while(++i <= MAXUNNUM);
}

graph :: ~graph()
{
	//free hashtable
	free(qHashTable);
	
	free(NIDar);
	
	int i=0;
	do{
		free(memBlkAr[i]);
	}while(++i <= endBlk);
	
	i=0;
	do{
		free(memBlkAr2[i]);
	}while(++i <= curBlk2);
	
	
	printf("All mem freed \n");
}
void graph :: print_graph()
{
	printf("%d\n",1);
}

void calculate_degree(int *degree, int curNNum, char* newEdge)
{
	FILE *infile;
	infile = fopen(newEdge,"r");
	
	char c;
	int curPos = 0;
	int fromnode, tonode;
	int k = 0;
	for( k = 0 ; k < curNNum ; k ++ ) degree[k] = 0;
	
	while( c = fgetc(infile)!=EOF)
	{
		if( c =='\n' ) break;
		fseek(infile,-1,SEEK_CUR);
		fscanf(infile,"%d",&fromnode);
		fscanf(infile,"%d",&tonode);
		while( (c = fgetc(infile)) != '\n');
		degree[fromnode] ++;
	}
	fclose(infile);
}

void create_graph(int curNNum, int curENum, int* degreeAr,char* newEdge)
{
	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;

	int SZ_EdgeMapping = sizeof(int)*curENum;
	int SZ_NodeMapping = sizeof(int)*curNNum;
	if((curMemEnd2 - curMemPos2) < (SZ_EdgeMapping+SZ_NodeMapping)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	EdgeMapping = (int*)curMemPos2;
	curMemPos2 += SZ_EdgeMapping;
	memset(EdgeMapping,-1,SZ_EdgeMapping);
	// in the EdgeMapping: -1 means in the Originalgraph, -2 means deletion, i(0<=i<=k-1) means in the ith partition
	NodeMapping = (int*)curMemPos2;
	curMemPos2 += SZ_NodeMapping;
	memset(NodeMapping,-1,SZ_NodeMapping);
	// in the NodeMapping: -1 means in the Originalgraph, i(0<=i<=k-1) means in the ith partition, no deletion.
	
	
	int SZ_edgeAr = curENum * sizeof(int) * 2;
	int SZ_degreeAr = curNNum * sizeof(int);
	if((curMemEnd2 - curMemPos2) < (SZ_GRAPH+SZ_edgeAr)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	OriginalGraph=(Graph*)curMemPos2;
	curMemPos2+=SZ_GRAPH;
	
	OriginalGraph->nodeNum = curNNum;
	OriginalGraph->edgeNum = curENum;
	
	OriginalGraph->edgeAr = (int*)curMemPos2;
	curMemPos2+=SZ_edgeAr;
	
	OriginalGraph->degreeAr = degreeAr;
	
	//test: print degreeAr
	//printf("nodenum is %d\nedgeNum is %d\n",OriginalGraph->nodeNum,OriginalGraph->edgeNum);
	//for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	//printf("degreee of node %d is %d\n",i,OriginalGraph->degreeAr[i]);
	//end of test
	
	//initialize the edgeAr
	FILE *infile;
	infile = fopen(newEdge,"r");
	
	char c;
	int curEdge = 0;
	int fromnode, tonode;
	
	while( c = fgetc(infile)!=EOF)
	{
		if( c =='\n' ) break;
		fseek(infile,-1,SEEK_CUR);
		fscanf(infile,"%d",&fromnode);
		fscanf(infile,"%d",&tonode);
		while( (c = fgetc(infile)) != '\n');
		if( fromnode < tonode )
		{
			if( curEdge > OriginalGraph->edgeNum ) printf("curEdge is greater than OriginalGraph->edgeNum\n");
			OriginalGraph->edgeAr[curEdge*EDGESZ] = fromnode;
			OriginalGraph->edgeAr[curEdge*EDGESZ+1] = tonode;
			curEdge ++;
		}
		else
			continue;
	}
	fclose(infile);
	
	//end of initialize the edgeAr
	
	//test: print the edgeAr
	//for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	//printf("the %d edge is %d %d\n", i,OriginalGraph->edgeAr[i*2],OriginalGraph->edgeAr[i*2+1]);
	//end of test
	
	//sort the node by discreasing order of degree
	int maxdegree = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( OriginalGraph->degreeAr[i] > maxdegree )
			maxdegree = OriginalGraph->degreeAr[i];
	}
	
	//printf( "maxdegree is %d\n", maxdegree);
	
	int SZ_newNIDAr = sizeof(int)*OriginalGraph->nodeNum;
	int SZ_newdegreeAr = sizeof(int)*OriginalGraph->nodeNum;
	if((curMemEnd - curMemPos) < SZ_newNIDAr + SZ_newdegreeAr){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* newNIDAr = (int*)curMemPos;
	curMemPos += SZ_newNIDAr;
	memset(newNIDAr,0,SZ_newNIDAr);
	int* newdegreeAr = (int*)curMemPos;
	curMemPos += SZ_newdegreeAr;
	memset(newdegreeAr,0,SZ_newdegreeAr);
	
	int curPos = 0;
	for ( int degree = maxdegree ; degree >= 0 ; degree -- )
	{
		for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
		{
			if( OriginalGraph->degreeAr[i] == degree )
			{
				newNIDAr[i] = curPos;
				newdegreeAr[curPos] = degree;
				curPos ++;
			}
		}
	}
	
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		OriginalGraph->degreeAr[i] = newdegreeAr[i];
	}
	for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	{
		fromnode = OriginalGraph->edgeAr[i*EDGESZ];
		tonode = OriginalGraph->edgeAr[i*EDGESZ+1];
		if( newNIDAr[fromnode] < newNIDAr[tonode])
		{
			OriginalGraph->edgeAr[i*EDGESZ] = newNIDAr[fromnode];
			OriginalGraph->edgeAr[i*EDGESZ+1] = newNIDAr[tonode];
		}
		else
		{
			OriginalGraph->edgeAr[i*EDGESZ] = newNIDAr[tonode];
			OriginalGraph->edgeAr[i*EDGESZ+1] = newNIDAr[fromnode];
		}
	}
	//printf("end of sort the nodes\n");
	
	//end of sort the node by discreasing order of degree
	
	//initialize the edgePosAar
	
	int SZ_edgePosAar = sizeof(int*)*curNNum;
	if((curMemEnd2 - curMemPos2) < (SZ_edgePosAar)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	OriginalGraph->edgePosAar = (int**)curMemPos2;
	curMemPos2 += SZ_edgePosAar;
	
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		int SZ_edgePosAar_i = sizeof(int)*OriginalGraph->degreeAr[i];
		if((curMemEnd2 - curMemPos2) < (SZ_edgePosAar_i)){
			//free mem in cur block is not enough
			//allocate a new block
			++curBlk2;
			curMemPos2=(char*)malloc(BLK_SZ2);
			memBlkAr2[curBlk2]=curMemPos2;
			curMemEnd2=curMemPos2+BLK_SZ2;
		}
		OriginalGraph->edgePosAar[i] = (int*)curMemPos2;
		curMemPos2 += SZ_edgePosAar_i;
	}
	
	
	int SZ_curPosAr = sizeof(int)*OriginalGraph->nodeNum;
	if((curMemEnd - curMemPos) < SZ_curPosAr){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* curPosAr = (int*)curMemPos;
	curMemPos += SZ_curPosAr;
	memset(curPosAr,0,SZ_curPosAr);
	
	
	
	
	for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	{
		fromnode = OriginalGraph->edgeAr[i*EDGESZ];
		tonode = OriginalGraph->edgeAr[i*EDGESZ+1];
		//test
		/*if( curPosAr[fromnode] == OriginalGraph->degreeAr[fromnode] ) 
		 {
		 printf("fromnode wrong!\n");
		 scanf("%d",&fromnode);
		 }
		 if( curPosAr[tonode] == OriginalGraph->degreeAr[tonode] ) 
		 {
		 printf("tonode wrong!\n");
		 scanf("%d",&tonode);
		 }*/
		//end of test
		int curPos_fromnode = curPosAr[fromnode];
		int curPos_tonode = curPosAr[tonode];
		
		curPosAr[fromnode]++;
		curPosAr[tonode]++;
		
		OriginalGraph->edgePosAar[fromnode][curPos_fromnode]=i;
		OriginalGraph->edgePosAar[tonode][curPos_tonode]=i;
	}
	
	//test
	/*for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	 {
	 if( curPosAr[i]!=OriginalGraph->degreeAr[i] ) printf(" node %d is wrong, the curPosAr[%d] is %d and the degreeAr[%d] is %d\n",i,i,curPosAr[i],i,OriginalGraph->degreeAr[i]);
	 }*/
	//end of test
	
	//test
	/*int tmpnode = 1000;
	 printf("the degree of node 1000 is %d\n", OriginalGraph->degreeAr[tmpnode]);
	 for( int i = 0 ; i < OriginalGraph->degreeAr[tmpnode] ; i ++ )
	 {
	 int curEdge = OriginalGraph->edgePosAar[tmpnode][i];
	 printf( " the %d edge of tmpnode is edgeAr[%d] : %d %d\n",i,curEdge,OriginalGraph->edgeAr[curEdge*EDGESZ],OriginalGraph->edgeAr[curEdge*EDGESZ+1]);
	 }*/
	//end of test
	
	//order the items in the edgePosAar[i] according to NID
	
	int SZ_tmpPosAr_i = sizeof(int)*maxdegree;
	if((curMemEnd - curMemPos) < SZ_tmpPosAr_i){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* tmpPosAr_i = (int*)curMemPos;
	curMemPos += SZ_tmpPosAr_i;
	memset(curPosAr,0,SZ_tmpPosAr_i);
	
	
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++)
	{
		int curtmpPos = 0;
		for( int j = 0 ; j < OriginalGraph->degreeAr[i]; j++)
		{
			int tmpmax = 0;
			int tmpmaxPos = 0;
			for( int k = 0; k < OriginalGraph->degreeAr[i]; k++)
			{
				int edge = OriginalGraph->edgePosAar[i][k];
				if( edge == -1 ) continue;
				fromnode = OriginalGraph->edgeAr[edge*EDGESZ];
				tonode = OriginalGraph->edgeAr[edge*EDGESZ+1];
				if( fromnode == i )
				{
					if( OriginalGraph->degreeAr[tonode] > tmpmax )
					{
						tmpmax = OriginalGraph->degreeAr[tonode];
						tmpmaxPos = k;
					}
				}
				else
				{
					if( OriginalGraph->degreeAr[fromnode] > tmpmax )
					{
						tmpmax = OriginalGraph->degreeAr[fromnode];
						tmpmaxPos = k;
					}
				}
				
			}
			tmpPosAr_i[curtmpPos] = OriginalGraph->edgePosAar[i][tmpmaxPos];
			OriginalGraph->edgePosAar[i][tmpmaxPos] = -1;
			curtmpPos ++;
		}
		//if( curtmpPos != OriginalGraph->degreeAr[i] ) printf(" i = %d is wrong!\n" , i );
		for( int j = 0 ; j < OriginalGraph->degreeAr[i] ; j ++ )
		{
			OriginalGraph->edgePosAar[i][j] = tmpPosAr_i[j];
		}
	}
	
	//test
	//int tmpnode = 1000;
	/*printf("**old NodeID is %d\n",newNIDAr[tmpnode]);
	printf("the degree of node 1000 is %d\n", OriginalGraph->degreeAr[tmpnode]);
	for( int i = 0 ; i < OriginalGraph->degreeAr[tmpnode] ; i ++ )
	{
		int curEdge = OriginalGraph->edgePosAar[tmpnode][i];
		printf( " the %d edge of tmpnode is edgeAr[%d] : %d(%d) %d(%d)\n",i,curEdge,OriginalGraph->edgeAr[curEdge*EDGESZ],OriginalGraph->degreeAr[OriginalGraph->edgeAr[curEdge*EDGESZ]],OriginalGraph->edgeAr[curEdge*EDGESZ+1],OriginalGraph->degreeAr[OriginalGraph->edgeAr[curEdge*EDGESZ+1]]);
	}*/
	//end of test
	
	//end of order the items in the edgePosAar[i]
//end of initialize the edgePosAar
	
	
	//Release the memory 
	curBlk=prevBlk0;
	curMemPos=prevMemPos0;
	curMemEnd=prevMemEnd0;
	
	return;
	
}

Graph* CreateGraphFromEdgeID(int edgeNum,int* edgeID,int nodeNum, int* nodeID)
{
	//test
	//printf("-begin of CreateGraphFromEdgeID()\n");
	//end of test

	Graph* newgraph;

	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;
	
	int SZ_edgeAr = edgeNum * sizeof(int) * EDGESZ;
	int SZ_degreeAr = nodeNum * sizeof(int);
	if((curMemEnd2 - curMemPos2) < (SZ_GRAPH+SZ_edgeAr+SZ_degreeAr)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	newgraph=(Graph*)curMemPos2;
	curMemPos2+=SZ_GRAPH;
	
	newgraph->nodeNum = nodeNum;
	newgraph->edgeNum = edgeNum;
	
	newgraph->edgeAr = (int*)curMemPos2;
	curMemPos2+=SZ_edgeAr;
	
	newgraph->degreeAr = (int*)curMemPos2;
	curMemPos2+=SZ_degreeAr;
	memset(newgraph->degreeAr,0,SZ_degreeAr);

	//calculate the degreeAr
	for( int i = 0 ; i < edgeNum ; i ++ )
	{
		int curEdge = edgeID[i];
		int fromnode = OriginalGraph->edgeAr[curEdge*EDGESZ];
		int tonode = OriginalGraph->edgeAr[curEdge*EDGESZ+1];
		for( int j = 0 ; j < nodeNum; j ++ )
		{
			if( fromnode == nodeID[j] )
				newgraph->degreeAr[j]++;
			if( tonode == nodeID[j])
				newgraph->degreeAr[j]++;
		}
	}
	//test
	//printf("--end of calculate the degreeAr\n");
	//end of calculate the degreeAr
	
	//initialize the edgeAr
	for( int i = 0 ; i < edgeNum ; i++ )
	{
		int curEdge = edgeID[i];
		int fromnode = OriginalGraph->edgeAr[curEdge*EDGESZ];
		int tonode = OriginalGraph->edgeAr[curEdge*EDGESZ+1];
		for( int j = 0 ; j < nodeNum ; j++ )
		{
			if( fromnode == nodeID[j])
			{
				newgraph->edgeAr[i*EDGESZ] = j;
				break;
			}
		}
		for( int j = 0 ; j < nodeNum ; j ++ )
		{
			if( tonode == nodeID[j] )
			{
				newgraph->edgeAr[i*EDGESZ+1] = j;
				break;
			}
		}
	}
	//test
	//printf("--end of initialize the edgeAr\n");
	//end of initialize the edgeAr
	
	//sort the node by discreasing order of degree
	int maxdegree = 0;
	for( int i = 0 ; i < newgraph->nodeNum ; i ++ )
	{
		if( newgraph->degreeAr[i] > maxdegree )
			maxdegree = newgraph->degreeAr[i];
	}
	
	//printf( "maxdegree is %d\n", maxdegree);
	
	int SZ_newNIDAr = sizeof(int)*newgraph->nodeNum;
	int SZ_newdegreeAr = sizeof(int)*newgraph->nodeNum;
	int SZ_tmpnodeID = sizeof(int) * nodeNum;
	if((curMemEnd - curMemPos) < SZ_newNIDAr + SZ_newdegreeAr+SZ_tmpnodeID){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* newNIDAr = (int*)curMemPos;
	curMemPos += SZ_newNIDAr;
	memset(newNIDAr,0,SZ_newNIDAr);
	int* newdegreeAr = (int*)curMemPos;
	curMemPos += SZ_newdegreeAr;
	memset(newdegreeAr,0,SZ_newdegreeAr);
	int* tmpnodeID = (int*)curMemPos;
	curMemPos += SZ_tmpnodeID;
	memset(tmpnodeID,-1,SZ_tmpnodeID);
	
	int curPos = 0;
	for ( int degree = maxdegree ; degree >= 0 ; degree -- )
	{
		for( int i = 0 ; i < newgraph->nodeNum ; i ++ )
		{
			if( newgraph->degreeAr[i] == degree )
			{
				newNIDAr[i] = curPos;
				newdegreeAr[curPos] = degree;
				curPos ++;
			}
		}
	}

	for( int i = 0 ; i < nodeNum; i ++ )
	{
		tmpnodeID[i] = nodeID[newNIDAr[i]];
	}
	for( int i = 0 ; i < nodeNum ; i++ )
	{
		nodeID[i] = tmpnodeID[i];
	}
	
	for( int i = 0 ; i < newgraph->nodeNum ; i ++ )
	{
		newgraph->degreeAr[i] = newdegreeAr[i];
	}
	for( int i = 0 ; i < newgraph->edgeNum ; i ++ )
	{
		int fromnode = newgraph->edgeAr[i*EDGESZ];
		int tonode = newgraph->edgeAr[i*EDGESZ+1];
		if( newNIDAr[fromnode] < newNIDAr[tonode])
		{
			newgraph->edgeAr[i*EDGESZ] = newNIDAr[fromnode];
			newgraph->edgeAr[i*EDGESZ+1] = newNIDAr[tonode];
		}
		else
		{
			newgraph->edgeAr[i*EDGESZ] = newNIDAr[tonode];
			newgraph->edgeAr[i*EDGESZ+1] = newNIDAr[fromnode];
		}
	}
	//test
	//printf("--end of sort the nodes\n");
	
	//end of sort the node by discreasing order of degree
	
	//initialize the edgePosAar
	
	int SZ_edgePosAar = sizeof(int*)*nodeNum;
	if((curMemEnd2 - curMemPos2) < (SZ_edgePosAar)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	newgraph->edgePosAar = (int**)curMemPos2;
	curMemPos2 += SZ_edgePosAar;
	
	for( int i = 0 ; i < newgraph->nodeNum ; i ++ )
	{
		int SZ_edgePosAar_i = sizeof(int)*newgraph->degreeAr[i];
		if((curMemEnd2 - curMemPos2) < (SZ_edgePosAar_i)){
			//free mem in cur block is not enough
			//allocate a new block
			++curBlk2;
			curMemPos2=(char*)malloc(BLK_SZ2);
			memBlkAr2[curBlk2]=curMemPos2;
			curMemEnd2=curMemPos2+BLK_SZ2;
		}
		newgraph->edgePosAar[i] = (int*)curMemPos2;
		curMemPos2 += SZ_edgePosAar_i;
	}
	
	
	int SZ_curPosAr = sizeof(int)*newgraph->nodeNum;
	if((curMemEnd - curMemPos) < SZ_curPosAr){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* curPosAr = (int*)curMemPos;
	curMemPos += SZ_curPosAr;
	memset(curPosAr,0,SZ_curPosAr);
	
	
	
	
	for( int i = 0 ; i < newgraph->edgeNum ; i ++ )
	{
		int fromnode = newgraph->edgeAr[i*EDGESZ];
		int tonode = newgraph->edgeAr[i*EDGESZ+1];
		//test
		/*if( curPosAr[fromnode] == OriginalGraph->degreeAr[fromnode] ) 
		 {
		 printf("fromnode wrong!\n");
		 scanf("%d",&fromnode);
		 }
		 if( curPosAr[tonode] == OriginalGraph->degreeAr[tonode] ) 
		 {
		 printf("tonode wrong!\n");
		 scanf("%d",&tonode);
		 }*/
		//end of test
		int curPos_fromnode = curPosAr[fromnode];
		int curPos_tonode = curPosAr[tonode];
		
		curPosAr[fromnode]++;
		curPosAr[tonode]++;
		
		newgraph->edgePosAar[fromnode][curPos_fromnode]=i;
		newgraph->edgePosAar[tonode][curPos_tonode]=i;
	}
	
	//test
	/*for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	 {
	 if( curPosAr[i]!=OriginalGraph->degreeAr[i] ) printf(" node %d is wrong, the curPosAr[%d] is %d and the degreeAr[%d] is %d\n",i,i,curPosAr[i],i,OriginalGraph->degreeAr[i]);
	 }*/
	//end of test
	
	//test
	/*int tmpnode = 1000;
	 printf("the degree of node 1000 is %d\n", OriginalGraph->degreeAr[tmpnode]);
	 for( int i = 0 ; i < OriginalGraph->degreeAr[tmpnode] ; i ++ )
	 {
	 int curEdge = OriginalGraph->edgePosAar[tmpnode][i];
	 printf( " the %d edge of tmpnode is edgeAr[%d] : %d %d\n",i,curEdge,OriginalGraph->edgeAr[curEdge*EDGESZ],OriginalGraph->edgeAr[curEdge*EDGESZ+1]);
	 }*/
	//end of test
	
	//order the items in the edgePosAar[i] according to NID
	
	int SZ_tmpPosAr_i = sizeof(int)*maxdegree;
	if((curMemEnd - curMemPos) < SZ_tmpPosAr_i){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int* tmpPosAr_i = (int*)curMemPos;
	curMemPos += SZ_tmpPosAr_i;
	memset(curPosAr,0,SZ_tmpPosAr_i);
	
	
	for( int i = 0 ; i < newgraph->nodeNum ; i ++)
	{
		int curtmpPos = 0;
		for( int j = 0 ; j < newgraph->degreeAr[i]; j++)
		{
			int tmpmax = 0;
			int tmpmaxPos = 0;
			for( int k = 0; k < newgraph->degreeAr[i]; k++)
			{
				int edge = newgraph->edgePosAar[i][k];
				if( edge == -1 ) continue;
				int fromnode = newgraph->edgeAr[edge*EDGESZ];
				int tonode = newgraph->edgeAr[edge*EDGESZ+1];
				if( fromnode == i )
				{
					if( newgraph->degreeAr[tonode] > tmpmax )
					{
						tmpmax = newgraph->degreeAr[tonode];
						tmpmaxPos = k;
					}
				}
				else
				{
					if( newgraph->degreeAr[fromnode] > tmpmax )
					{
						tmpmax = newgraph->degreeAr[fromnode];
						tmpmaxPos = k;
					}
				}
				
			}
			tmpPosAr_i[curtmpPos] = newgraph->edgePosAar[i][tmpmaxPos];
			newgraph->edgePosAar[i][tmpmaxPos] = -1;
			curtmpPos ++;
		}
		//if( curtmpPos != OriginalGraph->degreeAr[i] ) printf(" i = %d is wrong!\n" , i );
		for( int j = 0 ; j < newgraph->degreeAr[i] ; j ++ )
		{
			newgraph->edgePosAar[i][j] = tmpPosAr_i[j];
		}
	}
	
	//test
	//int tmpnode = 1000;
	/*printf("**old NodeID is %d\n",newNIDAr[tmpnode]);
	printf("the degree of node 1000 is %d\n", OriginalGraph->degreeAr[tmpnode]);
	for( int i = 0 ; i < OriginalGraph->degreeAr[tmpnode] ; i ++ )
	{
		int curEdge = OriginalGraph->edgePosAar[tmpnode][i];
		printf( " the %d edge of tmpnode is edgeAr[%d] : %d(%d) %d(%d)\n",i,curEdge,OriginalGraph->edgeAr[curEdge*EDGESZ],OriginalGraph->degreeAr[OriginalGraph->edgeAr[curEdge*EDGESZ]],OriginalGraph->edgeAr[curEdge*EDGESZ+1],OriginalGraph->degreeAr[OriginalGraph->edgeAr[curEdge*EDGESZ+1]]);
	}*/
	//end of test
	
	//end of order the items in the edgePosAar[i]
//end of initialize the edgePosAar
	
	
	//Release the memory 
	curBlk=prevBlk0;
	curMemPos=prevMemPos0;
	curMemEnd=prevMemEnd0;

	//test
	//printf("-end of CreateGraphFromEdgeID()\n");
	//end of test
	
	return newgraph;
}

Embedding* createEmbedding(int* PAGedge,int PAGedgeNum)
{
	//test
	//printf("begin createEmbedding()\n");
	//end of test
	Embedding *newEmbedding;

	//test
	/*if( PAGedgeNum != MaxPAGSize )
	{
		printf("PAGedgeNum is %d\n",PAGedgeNum);
	}
	char c;
	scanf("%c",&c);*/
	//end of test

	int SZ_edgeID = PAGedgeNum * sizeof(int);
	int SZ_nodeID = (PAGedgeNum+1) * sizeof(int);
	int SZ_VM = (PAGedgeNum + 1 )*sizeof(int);
	
	if((curMemEnd2 - curMemPos2) < (SZ_EMBEDDING+SZ_edgeID+SZ_nodeID)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	newEmbedding = (Embedding*)curMemPos2;
	curMemPos2 += SZ_EMBEDDING;

	newEmbedding->edgeNum = PAGedgeNum;
	newEmbedding->edgeID = (int*)curMemPos2;
	curMemPos2 += SZ_edgeID;

	newEmbedding->next = NULL;

	newEmbedding->nodeNum = 0;
	newEmbedding->nodeID = (int*)curMemPos2;
	curMemPos2 += SZ_nodeID;

	newEmbedding->VM = (int*)curMemPos2;
	curMemPos2 += SZ_VM;


	for( int i = 0 ; i < newEmbedding->edgeNum ; i ++ )
	{
		newEmbedding->edgeID[i] = PAGedge[i];
		int curEdge = newEmbedding->edgeID[i];
		int fromnode = OriginalGraph->edgeAr[curEdge*EDGESZ];
		int tonode = OriginalGraph->edgeAr[curEdge*EDGESZ+1];
		int exist_fromnode = 0;
		int exist_tonode = 0;
		for( int j = 0 ; j < newEmbedding->nodeNum ; j ++ )
		{
			if( fromnode == newEmbedding->nodeID[j] )
			{
				exist_fromnode = 1;
				break;
			}
		}
		for( int j = 0 ; j < newEmbedding->nodeNum ; j ++ )
		{
			if( tonode == newEmbedding->nodeID[j] )
			{
				exist_tonode = 1;
				break;
			}
		}
		if( exist_fromnode == 0 )
		{
			newEmbedding->nodeID[newEmbedding->nodeNum] = fromnode;
			newEmbedding->nodeNum ++;
		}
		if( exist_tonode == 0 )
		{
			newEmbedding->nodeID[newEmbedding->nodeNum] = tonode;
			newEmbedding->nodeNum ++;
		}

	}

	//test
	/*if( newEmbedding->nodeNum > 6 )
	{
		printf( "newEmbedding->nodeNum is %d    The PAGedge is : \n",newEmbedding->nodeNum);
		for( int i = 0 ; i < PAGedgeNum ; i ++ )
		{
			int edge = PAGedge[i];
			int fromnode = OriginalGraph->edgeAr[edge*EDGESZ];
			int tonode = OriginalGraph->edgeAr[edge*EDGESZ+1];
			printf( "(%d %d) ",fromnode, tonode );
		}
		printf("\n");
	}*/
	//end of test

	for( int i = 0 ; i < newEmbedding->nodeNum ; i ++ )
	{
		newEmbedding->VM[i] = i;
	}
	newEmbedding->thisgraph = CreateGraphFromEdgeID(newEmbedding->edgeNum,newEmbedding->edgeID,newEmbedding->nodeNum,newEmbedding->nodeID);

	//test
	//printf("end of createEmbedding()\n");
	//end of test
	return newEmbedding;
}


int testSameG(Graph* qGraph, Graph* supG, int curPos, int* VM)
{
	//test
	//for( int x = 0 ; x < curPos ; x ++ ) printf("-");
	//printf("~~~~~~begin testSameG(), curPos is %d    ",curPos);
	//char c;
	//scanf("%c",&c);
	//end of test
	int isSameG = 0; 
	int qNNum = qGraph->nodeNum;
	int qENum = qGraph->edgeNum;
	int *qEAr = qGraph->edgeAr;
	int *qDAr = qGraph->degreeAr;
	int **qEPosAar = qGraph->edgePosAar;

	int sNNum = supG->nodeNum;
	int sENum = supG->edgeNum;
	int *sEAr = supG->edgeAr;
	int *sDAr = supG->degreeAr;
	int **sEPosAar = supG->edgePosAar;

	int begin;
	int end;
	int j;
	for( j = 0 ; j < sNNum && sDAr[j] > sDAr[curPos] ; j ++ );
	begin = j;
	for( ; j < sNNum && sDAr[j] == sDAr[curPos] ; j ++ );
	end = j-1;

	//printf("sNNum is %d,qNNum is %d, sENum is %d, qENum is%d\n",sNNum, qNNum,sENum,qENum);
	//printf("degree is %d, begin is %d, end is %d  \n", sDAr[curPos],begin,end);


	for( j = begin ; j <= end; j ++ )
	{
		//printf("****j = %d****\n",j);
		int k;
		for( k = 0 ; k < curPos ; k ++ )
		{
			if( VM[k] == j )
				break;
		}
		if( k != curPos)//this node is unavailable
		{
			//test
			//printf("j is %d, is not available\n",j);
			continue;
		}
		else
		{
			//test
			//printf("in the first else\n");
			//test node j in qGraph and node curPos in supG
			int SZ_ND = sizeof(int) * sDAr[curPos];
			if((curMemEnd - curMemPos) < SZ_ND*2){
				//free mem in cur block is not enough
				if(curBlk < endBlk){
					//we have already allocated free blocks
					++curBlk;
					curMemPos=memBlkAr[curBlk];
					curMemEnd=curMemPos+BLK_SZ;
				}
				else{
					//allocate a new block
					++curBlk;
					++endBlk;
					curMemPos=(char*)malloc(BLK_SZ);
					memBlkAr[curBlk]=curMemPos;
					curMemEnd=curMemPos+BLK_SZ;
				}
			}
			int *qND = (int*)curMemPos;
			curMemPos += SZ_ND;
			int *sND = (int*)curMemPos;
			curMemPos += SZ_ND;

			for( int l = 0; l < sDAr[curPos]; l ++ )
			{
				int edge = sEPosAar[curPos][l];
				int fromnode = sEAr[edge*EDGESZ];
				int tonode = sEAr[edge*EDGESZ+1];
				if( fromnode == curPos )
					sND[l] = sDAr[tonode];
				else
					sND[l] = sDAr[fromnode];
			}

			//for( int l = 0 ; l < sDAr[curPos] ; l ++ )
				//printf("sND[%d] is %d	",l,sND[l]);

			for( int l = 0; l < qDAr[j]; l ++ )
			{
				int edge = qEPosAar[j][l];
				int fromnode = qEAr[edge*EDGESZ];
				int tonode = qEAr[edge*EDGESZ+1];
				if( fromnode == j )
					qND[l] = qDAr[tonode];
				else
					qND[l] = qDAr[fromnode];
			}
			//for( int l = 0 ; l < qDAr[j] ; l ++ )
				//printf("qND[%d] is %d	",l,qND[l]);

			int change = 1;
			while( change == 1 )
			{
				change=0;
				for( int l = 0 ; l < sDAr[curPos]-1 ; l++ )
				{
					if( sND[l+1] > sND[l] )
					{
						int tmp = sND[l];
						sND[l] = sND[l+1];
						sND[l+1] = tmp;
						change =1;
					}
				}
			}

			//printf( "After ordering, sND[] is \n");
			//for( int l = 0 ; l < sDAr[curPos] ; l ++ )
				//printf("sND[%d] is %d	",l,sND[l]);

			change = 1;
			while( change == 1 )
			{
				change=0;
				for( int l = 0 ; l < qDAr[j]-1 ; l++ )
				{
					if( qND[l+1] > qND[l] )
					{
						int tmp = qND[l];
						qND[l] = qND[l+1];
						qND[l+1] = tmp;
						change =1;
					}
				}
			}

			//printf( "After ordering, qND[] is \n");
			//for( int l = 0 ; l < qDAr[j] ; l ++ )
				//printf("qND[%d] is %d	",l,qND[l]);

			int m;
			for( m = 0 ; m < sDAr[curPos] && sND[m] == qND[m] ; m++ );
			if( m == sDAr[curPos] )
			{
				//printf(" m == sDAr[curPos]\n");
				VM[curPos] = j;
				curPos ++;
				if( curPos == sNNum )
				{
					//printf("curPos == sNNum, return\n");
					isSameG = -1;
					return isSameG;
				}
				else
				{
					//printf("call testSameG(), ");
					isSameG = testSameG(qGraph,supG,curPos,VM);
					if( isSameG == -1 )
					{
						//printf ( " return isSub = -1\n");
						return isSameG;
					}
					else
					{
						//printf( " continue find next j\n");
						curPos--;
					}
				}
			}
			else
			{
				//printf( "m!=sDAr[curPos]\n");
				continue;
			}
		}
	}
	//printf("return 0 in the end of testSubG()\n");
	return isSameG;
}
//return 0 if qGraph is not a subgraph of supG
//return -1 if qGraph is a subgraph of supG
int testSubG(Embedding *curEmbedding,Graph *qGraph, Graph *supG)
{
	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;



	int isSubG = 0; //qGraph is not a subgraph of supG
	int qNNum = qGraph->nodeNum;
	int qENum = qGraph->edgeNum;
	int *qEAr = qGraph->edgeAr;
	int *qDAr = qGraph->degreeAr;
	int **qEPosAar = qGraph->edgePosAar;

	int sNNum = supG->nodeNum;
	int sENum = supG->edgeNum;
	int *sEAr = supG->edgeAr;
	int *sDAr = supG->degreeAr;
	int **sEPosAar = supG->edgePosAar;

	//test
	/*printf("PAGNum is %d\n",PAGNum);
	printf("sNNum is %d,qNNum is %d, sENum is %d, qENum is%d\n",sNNum, qNNum,sENum,qENum);
	for( int i = 0 ; i < qNNum ; i ++ ) printf("qDAr[%d] = %d  ;",i,qDAr[i] );
	printf("\n");
	for( int i = 0 ; i < sNNum ; i ++ ) printf("sDAr[%d] = %d  ;",i,sDAr[i] );
	printf("\n");
	for( int i = 0 ; i < qENum ; i ++ ) printf("edge %d is %d, %d \n",i,qEAr[i*2],qEAr[i*2+1]);
	for( int i = 0 ; i < sENum ; i ++ ) printf("edge %d is %d, %d \n",i,sEAr[i*2],sEAr[i*2+1]);
	char c;
	scanf("%c",&c);*/
	//end of test

	if( qNNum == sNNum && qENum == sENum ) //is or not the same structure
	{
		int i;
		for( i = 0; i < qNNum && qDAr[i] == sDAr[i] ; i ++ );
		if( i != qNNum ) 
		{
			//printf("In testSubG(), i!= qNNum, return 0\n");
			return isSubG;
		}
		else
		{
			int *VM;
			int SZ_VM = sizeof(int) * qNNum;

			if((curMemEnd - curMemPos) < SZ_VM){
				//free mem in cur block is not enough
				if(curBlk < endBlk){
					//we have already allocated free blocks
					++curBlk;
					curMemPos=memBlkAr[curBlk];
					curMemEnd=curMemPos+BLK_SZ;
				}
				else{
					//allocate a new block
					++curBlk;
					++endBlk;
					curMemPos=(char*)malloc(BLK_SZ);
					memBlkAr[curBlk]=curMemPos;
					curMemEnd=curMemPos+BLK_SZ;
				}
			}
			VM = (int*)curMemPos;
			curMemPos+=SZ_VM;

			memset(VM,-1,SZ_VM);
			isSubG = testSameG(qGraph,supG,0,VM);

			//printf("In testSubG(), i== qNNum, call testSameG return %d\n",isSubG);

			if( isSubG == -1 )
				for( int j = 0 ; j < qNNum ; j ++ )
					curEmbedding->VM[j] = VM[j];
			//Release the memory 
			curBlk=prevBlk0;
			curMemPos=prevMemPos0;
			curMemEnd=prevMemEnd0;
			return isSubG;
		}
	}
	else if( qNNum > sNNum || qENum > sENum)
	{
		//printf("In testSubG(), else if, return 0\n");
		return isSubG;
	}
	else
	{
		//test whether qGraph is a subgraph of supG
		//printf("In testSubG(),else, return 0\n");

		return isSubG;
	}

}

void testtestSubG()
{
	Embedding *newembedding;
	
	int SZ_VM = sizeof(int)*6;
	int SZ_edgeAr = sizeof(int) * 5 * EDGESZ;
	int SZ_edgePosAar = sizeof(int*) * 5;
	int SZ_degreeAr = sizeof(int) * 5;
	int SZ_edgePosAar_i = sizeof(int) * 3 ;

	if((curMemEnd2 - curMemPos2) < SZ_EMBEDDING+SZ_VM+SZ_GRAPH*2+SZ_edgeAr*2+SZ_edgePosAar*2+SZ_degreeAr*2+SZ_edgePosAar*5){
		//free mem in cur block is not enough
						//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}

	newembedding = (Embedding*)curMemPos2;
	curMemPos2 += SZ_EMBEDDING;
	newembedding->VM = (int*)curMemPos2;
	curMemPos2 += SZ_VM;
	for( int i = 0 ; i < 6 ; i ++ )
		newembedding->VM[i] = i;
	newembedding->edgeNum = 5;
	newembedding->nodeNum = 5;

	Graph *g1;
	Graph *g2;
	
	g1 = (Graph*)curMemPos2;
	curMemPos2 += SZ_GRAPH;
	g2 = (Graph*)curMemPos2;
	curMemPos2 += SZ_GRAPH;

	newembedding->thisgraph = g1;

	g1->edgeNum = 5;
	g2->edgeNum = 5;
	g1->nodeNum = 5;
	g2->nodeNum = 5;

	g1->degreeAr= (int*) curMemPos2;
	curMemPos2+=SZ_degreeAr;
	g2->degreeAr = (int*) curMemPos2;
	curMemPos2+=SZ_degreeAr;

	g1->edgeAr = (int*) curMemPos2;
	curMemPos2 += SZ_edgeAr;
	g2->edgeAr = (int*) curMemPos2;
	curMemPos2 += SZ_edgeAr;

	g1->edgePosAar = (int**)curMemPos2;
	curMemPos2 += SZ_edgePosAar;
	g2->edgePosAar = (int**)curMemPos2;
	curMemPos2 += SZ_edgePosAar;

	g1->degreeAr[0] = g2->degreeAr[0] = 3;
	g1->degreeAr[1] = g2->degreeAr[1] = 2;
	g1->degreeAr[2] = g2->degreeAr[2] = 2;
	g1->degreeAr[3] = g2->degreeAr[3] = 2;
	g1->degreeAr[4] = g2->degreeAr[4] = 1;

	g1->edgeAr[0] = g1->edgeAr[2] = g1->edgeAr[4] = 0;
	g1->edgeAr[1] = g1->edgeAr[6] = 1;
	g1->edgeAr[3] = g1->edgeAr[7] = 2;
	g1->edgeAr[5] = g1->edgeAr[8] = 3;
	g1->edgeAr[9] = 4;

	g2->edgeAr[0] = g2->edgeAr[2] = g2->edgeAr[4] = 0;
	g2->edgeAr[1] = g2->edgeAr[6] = 1;
	g2->edgeAr[3] = g2->edgeAr[8] = 2;
	g2->edgeAr[5] = g2->edgeAr[9] = 3;
	g2->edgeAr[7] = 4;

	int* edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 1;
	edgePosAr_i[2] = 2;
	g1->edgePosAar[0] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 1;
	edgePosAr_i[2] = 2;
	g2->edgePosAar[0] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 3;
	g1->edgePosAar[1] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 3;
	g2->edgePosAar[1] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 3;
	g1->edgePosAar[2] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 0;
	edgePosAr_i[1] = 4;
	g2->edgePosAar[2] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 2;
	edgePosAr_i[1] = 4;
	g1->edgePosAar[3] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 2;
	edgePosAr_i[1] = 4;
	g2->edgePosAar[3] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 4;
	g1->edgePosAar[4] = edgePosAr_i;

	edgePosAr_i = (int*)curMemPos;
	curMemPos += SZ_edgePosAar_i;
	edgePosAr_i[0] = 3;
	g2->edgePosAar[4] = edgePosAr_i;


	int isSubG = testSubG( newembedding,g1,g2);


	//printf("In testtestSubG, isSubG is %d\n",isSubG );


}
int HashEmbeddingtoPAGHashTable(Embedding* curEmbedding)
{
	//printf("**begin HashEmbeddingtoPAGHashTable()\n");
	Graph *qGraph;
	qGraph = curEmbedding->thisgraph;
	int *qDAr=qGraph->degreeAr;		//(sz: qNNum)	an array of number of edges of each node of the graph
	int **qEPAar=qGraph->edgePosAar;	//(size: qNNum) (sz of qEPAar[i]: qDAr[i]) an array of qNNum arrays of positions of edges of each node in qEAr

	int qNNum=qGraph->nodeNum;
	int qENum=qGraph->edgeNum;
	int *qEAr=qGraph->edgeAr;	//(sz: qENum*EDGESZ) an array of edges of the graph  
	int hashID=1;

	hashID *= qNNum * qENum;
	int num[10]={3,5,7,13,17,19,23,29,31,37};
	//printf("hash ID is %d\n",hashID);

	for( int i = 0 ; i < qNNum; i++)
	{
		int tmp = 0;
		for( int j = 0 ; j < qDAr[i] ; j++ )
		{
			int edge = qEPAar[i][j];
			int fromnode = qEAr[edge*EDGESZ];
			int tonode = qEAr[edge*EDGESZ+1];
			int neighbour;
			if( fromnode == i)
				neighbour = tonode;
			else
				neighbour = fromnode;
			tmp += qDAr[neighbour];
		}
		hashID *= (tmp + 1) * num[i%10];
	}

	hashID=hashID&QHASHMASK;

	//printf("hash ID is %d\n",hashID);

	if(MaxPAGHashTable[hashID] != NULL)
	{
		PAGSlot *curPAGSlot = MaxPAGHashTable[hashID];
		do{	//while(curPAG != NULL);
			PAG *supPAG=curPAGSlot->thisPAG;
			Graph *supG = supPAG->thisgraph;
			
			//for testing graph isomorphism only
			//should skip the three testings in testSubG then

			//printf("call testSubG()\n");
			int isSubG = testSubG(curEmbedding,qGraph,supG);
			//printf("after call testSubG(), return %d\n",isSubG);
			if(qGraph->nodeNum == supG->nodeNum &&  qGraph->edgeNum == supG->edgeNum && isSubG == -1){
				//curPAG is in hashtable
				//test
				//printf("curPAG is in hashtable\n");
				Embedding *tmpEmbedding = supPAG->EmbeddingList;
				while(tmpEmbedding->next != NULL)
				{
					tmpEmbedding = tmpEmbedding->next;
				}
				tmpEmbedding->next = curEmbedding;
				supPAG->frequent ++;
				break;
			}
			else{
				if(curPAGSlot->next != NULL){
					curPAGSlot=curPAGSlot->next;
				}
				else{
					//qGraph is NOT in hashtable
					//may hash qGraph into the hashtable
					//test
					//printf("Create a new PAG to the exist PAGSlot\n");

					if((curMemEnd2 - curMemPos2) < SZ_PAGSLOT+SZ_PAG+SZ_GRAPH){
						//free mem in cur block is not enough
						//allocate a new block
						++curBlk2;
						curMemPos2=(char*)malloc(BLK_SZ2);
						memBlkAr2[curBlk2]=curMemPos2;
						curMemEnd2=curMemPos2+BLK_SZ2;
					}
					PAGSlot *newSlot=curPAGSlot->next=(PAGSlot*)curMemPos2;
					curMemPos2+=SZ_PAGSLOT;

					newSlot->thisPAG = (PAG*)curMemPos2;
					curMemPos2 += SZ_PAG;

					newSlot->thisPAG->thisgraph = (Graph*)curMemPos2;
					curMemPos2 += SZ_GRAPH;

					newSlot->next=NULL;
					//Initialize other fields of newSlot

					newSlot->thisPAG->EmbeddingList = newSlot->thisPAG->End = curEmbedding;
					newSlot->thisPAG->End->next = NULL;
					newSlot->thisPAG->VDEmbeddingList = NULL;
					newSlot->thisPAG->frequent = 1;

					//copy curEmbedding->thisgraph to newslot->thisPAG->thisgraph
					newSlot->thisPAG->thisgraph->nodeNum = curEmbedding->thisgraph->nodeNum;
					newSlot->thisPAG->thisgraph->edgeNum = curEmbedding->thisgraph->edgeNum;

					int SZ_degreeAr = sizeof(int)*newSlot->thisPAG->thisgraph->nodeNum;
					int SZ_edgeAr = sizeof(int) * newSlot->thisPAG->thisgraph->edgeNum * 2;
					int SZ_edgePosAar = sizeof(int*) * newSlot->thisPAG->thisgraph->nodeNum;

					if((curMemEnd2 - curMemPos2) < SZ_degreeAr+SZ_edgeAr+SZ_edgePosAar){
						//free mem in cur block is not enough
						//allocate a new block
						++curBlk2;
						curMemPos2=(char*)malloc(BLK_SZ2);
						memBlkAr2[curBlk2]=curMemPos2;
						curMemEnd2=curMemPos2+BLK_SZ2;
					}
					newSlot->thisPAG->thisgraph->degreeAr = (int*)curMemPos2;
					curMemPos2 += SZ_degreeAr;
					newSlot->thisPAG->thisgraph->edgeAr = (int*)curMemPos2;
					curMemPos2 += SZ_edgeAr;
					newSlot->thisPAG->thisgraph->edgePosAar = (int**)curMemPos2;
					curMemPos2 += SZ_edgePosAar;

					for( int i = 0 ; i < newSlot->thisPAG->thisgraph->nodeNum ; i ++ )
					{
						newSlot->thisPAG->thisgraph->degreeAr[i] = curEmbedding->thisgraph->degreeAr[i];
					}
					for( int i = 0 ; i < newSlot->thisPAG->thisgraph->edgeNum*2 ; i ++ )
					{
						newSlot->thisPAG->thisgraph->edgeAr[i] = curEmbedding->thisgraph->edgeAr[i];
					}
					for( int i = 0 ; i < newSlot->thisPAG->thisgraph->nodeNum ; i ++ )
					{
						int SZ_edgePosAar_i = sizeof(int) * newSlot->thisPAG->thisgraph->degreeAr[i];
						if((curMemEnd2 - curMemPos2) < SZ_edgePosAar_i){
							//free mem in cur block is not enough
							//allocate a new block
							++curBlk2;
							curMemPos2=(char*)malloc(BLK_SZ2);
							memBlkAr2[curBlk2]=curMemPos2;
							curMemEnd2=curMemPos2+BLK_SZ2;
						}

						int* tmp_edgePosAar_i = (int*)curMemPos2;
						curMemPos2 += SZ_edgePosAar_i;

						newSlot->thisPAG->thisgraph->edgePosAar[i] = tmp_edgePosAar_i;

						for( int j = 0 ; j < newSlot->thisPAG->thisgraph->degreeAr[i] ; j ++ )
						{
							newSlot->thisPAG->thisgraph->edgePosAar[i][j] = curEmbedding->thisgraph->edgePosAar[i][j];
						}
					}
					break;
				}
			}
		}while(1);
	}	//end: if(qHashTable[hashID] != NULL)
	else{
		//curPAG is NOT in hashtable
		//may hash qGraph into the hashtable
		//printf("Create a new PAGSlot\n");

		if((curMemEnd2 - curMemPos2) < SZ_PAGSLOT+SZ_PAG+SZ_GRAPH){
			//free mem in cur block is not enough
			//allocate a new block
			++curBlk2;
			curMemPos2=(char*)malloc(BLK_SZ2);
			memBlkAr2[curBlk2]=curMemPos2;
			curMemEnd2=curMemPos2+BLK_SZ2;
		}
		PAGSlot *newSlot=MaxPAGHashTable[hashID]=(PAGSlot*)curMemPos2;
		curMemPos2+=SZ_PAGSLOT;

		newSlot->thisPAG = (PAG*)curMemPos2;
		curMemPos2 += SZ_PAG;

		newSlot->thisPAG->thisgraph = (Graph*)curMemPos2;
		curMemPos2 += SZ_GRAPH;

		newSlot->next=NULL;
		//Initialize other fields of newSlot

		newSlot->thisPAG->EmbeddingList = newSlot->thisPAG->End = curEmbedding;
		newSlot->thisPAG->End->next = NULL;
		newSlot->thisPAG->VDEmbeddingList = NULL;
		newSlot->thisPAG->frequent = 1;

		//copy curEmbedding->thisgraph to newslot->thisPAG->thisgraph
		newSlot->thisPAG->thisgraph->nodeNum = curEmbedding->thisgraph->nodeNum;
		newSlot->thisPAG->thisgraph->edgeNum = curEmbedding->thisgraph->edgeNum;

		int SZ_degreeAr = sizeof(int)*newSlot->thisPAG->thisgraph->nodeNum;
		int SZ_edgeAr = sizeof(int) * newSlot->thisPAG->thisgraph->edgeNum * 2;
		int SZ_edgePosAar = sizeof(int*) * newSlot->thisPAG->thisgraph->nodeNum;

		if((curMemEnd2 - curMemPos2) < SZ_degreeAr+SZ_edgeAr+SZ_edgePosAar){
			//free mem in cur block is not enough
						//allocate a new block
			++curBlk2;
			curMemPos2=(char*)malloc(BLK_SZ2);
			memBlkAr2[curBlk2]=curMemPos2;
			curMemEnd2=curMemPos2+BLK_SZ2;
		}
		newSlot->thisPAG->thisgraph->degreeAr = (int*)curMemPos2;
		curMemPos2 += SZ_degreeAr;
		newSlot->thisPAG->thisgraph->edgeAr = (int*)curMemPos2;
		curMemPos2 += SZ_edgeAr;
		newSlot->thisPAG->thisgraph->edgePosAar = (int**)curMemPos2;
		curMemPos2 += SZ_edgePosAar;

		for( int i = 0 ; i < newSlot->thisPAG->thisgraph->nodeNum ; i ++ )
		{
			newSlot->thisPAG->thisgraph->degreeAr[i] = curEmbedding->thisgraph->degreeAr[i];
		}
		for( int i = 0 ; i < newSlot->thisPAG->thisgraph->edgeNum*2 ; i ++ )
		{
			newSlot->thisPAG->thisgraph->edgeAr[i] = curEmbedding->thisgraph->edgeAr[i];
		}
		for( int i = 0 ; i < newSlot->thisPAG->thisgraph->nodeNum ; i ++ )
		{
			int SZ_edgePosAar_i = sizeof(int) * newSlot->thisPAG->thisgraph->degreeAr[i];
			if((curMemEnd2 - curMemPos2) < SZ_edgePosAar_i){
				//free mem in cur block is not enough
							//allocate a new block
				++curBlk2;
				curMemPos2=(char*)malloc(BLK_SZ2);
				memBlkAr2[curBlk2]=curMemPos2;
				curMemEnd2=curMemPos2+BLK_SZ2;
			}

			int* tmp_edgePosAar_i = (int*)curMemPos2;
			curMemPos2 += SZ_edgePosAar_i;

			newSlot->thisPAG->thisgraph->edgePosAar[i] = tmp_edgePosAar_i;

			for( int j = 0 ; j < newSlot->thisPAG->thisgraph->degreeAr[i] ; j ++ )
			{
				newSlot->thisPAG->thisgraph->edgePosAar[i][j] = curEmbedding->thisgraph->edgePosAar[i][j];
			}
		}
	}
	//printf("**end HashEmbeddingtoPAGHashTable()\n");
	return 0;
}

void printPAGHash()
{
	int i;
	int count = 0;
	//use tmp to test the total number of PAGs in the Hashtable
	//int tmp=0;
	for( i = 0 ; i < QHASHTABLESZ ; i ++ )
	{
		if( MaxPAGHashTable[i] != NULL )
		{
			PAGSlot *curPAGSlot = MaxPAGHashTable[i];
			do
			{
				PAG *curPAG = curPAGSlot->thisPAG;
				printf("curPAG's frequency is %d\n",curPAG->frequent);
				count ++;
				
				//curPAGSlot->frequent = find_PAG_VDembed(count,curPAGSlot);
				if( curPAGSlot->next != NULL )
					curPAGSlot = curPAGSlot->next;
				else
					break;
			}while(1);
		}
	}
	printf("HashTable have %d PAGs\n",count);
}

int findMPAGs_edge(int* PAGedge, Node* oldList, Node** oldpointer, Node** oldend)
{
	//test
	//printf("()");
	//end of test
	//test
	/*printf("in function findMPAGs_edge: PAGedgeNum is %d\nPAGedge = ",PAGedgeNum);
	char c;
	
	for( int i = 0 ; i < PAGedgeNum ; i ++ )
	{
		printf("%d ",PAGedge[i]);
	}
	printf("\n");
	scanf("%c",&c);*/
	//end of test

	//printf("=======begin function finMPAGs_edge, PAGedgeNum is %d, MaxPAGSize is %d\n",PAGedgeNum,MaxPAGSize);
	int curEdge = PAGedge[PAGedgeNum-1];
	int fromnode;
	int tonode;
	int fromnode_degree, tonode_degree;
	
	tonode = OriginalGraph->edgeAr[curEdge*EDGESZ+1];
	fromnode = OriginalGraph->edgeAr[curEdge*EDGESZ];
	fromnode_degree = OriginalGraph->degreeAr[fromnode];
	tonode_degree = OriginalGraph->degreeAr[tonode];

	

	int *fromnode_edgePos = OriginalGraph->edgePosAar[fromnode];
	int *tonode_edgePos = OriginalGraph->edgePosAar[tonode];

	int curfromPos = 0;
	int curtoPos = 0;


	Node *List = NULL;
	Node *curListPos = NULL;
	if((curMemEnd - curMemPos) < SZ_Node * (fromnode_degree + tonode_degree - 2 + 2 * PAGedgeNum) ){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
		//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}

	for( curfromPos = 0 ; curfromPos < fromnode_degree ; curfromPos ++ )
	{
		int tmp = OriginalGraph->edgePosAar[fromnode][curfromPos];
		if( tmp == curEdge ) continue;
		if( TravelEdge[tmp] == 1 || EdgeMapping[tmp] != -1 ) continue;
		Node *tmpNode;
		tmpNode = List;
		int exist = 0;
		while( tmpNode!= NULL )
		{
			if( tmp == tmpNode->node )
			{
				exist = 1;
				break;
			}
			tmpNode = tmpNode->next;
		}
		tmpNode = oldList;
		while( tmpNode!= NULL && exist != 1 )
		{
			if( tmp == tmpNode->node )
			{
				exist = 1;
				break;
			}
			tmpNode = tmpNode->next;
		}
		for( int i = 0 ; i < PAGedgeNum && exist != 1 ; i ++ )
		{
			if( PAGedge[i] == tmp )
			{
				exist = 1;
				break;
			}
		}
		if( exist == 1 )
			continue;
		if( List == NULL )
		{
			//create first Node in the List
			List = (Node*)curMemPos;
			curMemPos += SZ_Node;
			List->node = tmp;
			List->next = NULL;
			curListPos = List;
		}
		else
		{
			Node *newNode = (Node*)curMemPos;
			curMemPos += SZ_Node;
			curListPos->next = newNode;
			curListPos = newNode;
			curListPos->node = tmp;
			curListPos->next = NULL;
		}
	}

	for( curtoPos = 0 ; curtoPos < tonode_degree ; curtoPos ++ )
	{
		int tmp = OriginalGraph->edgePosAar[tonode][curtoPos];
		if( tmp == curEdge ) continue;
		if( TravelEdge[tmp] == 1 || EdgeMapping[tmp] != -1 ) continue;
		Node *tmpNode;
		tmpNode = List;
		int exist = 0;
		while( tmpNode!= NULL )
		{
			if( tmp == tmpNode->node )
			{
				exist = 1;
				break;
			}
			tmpNode = tmpNode->next;
		}
		tmpNode = oldList;
		while( tmpNode!= NULL && exist != 1 )
		{
			if( tmp == tmpNode->node )
			{
				exist = 1;
				break;
			}
			tmpNode = tmpNode->next;
		}
		for( int i = 0 ; i < PAGedgeNum && exist != 1 ; i ++ )
		{
			if( PAGedge[i] == tmp )
			{
				exist = 1;
				break;
			}
		}
		if( exist == 1 )
			continue;
		if( List == NULL )
		{
			//create first Node in the List
			List = (Node*)curMemPos;
			curMemPos += SZ_Node;
			List->node = tmp;
			List->next = NULL;
			curListPos = List;
		}
		else
		{
			Node *newNode = (Node*)curMemPos;
			curMemPos += SZ_Node;
			curListPos->next = newNode;
			curListPos = newNode;
			curListPos->node = tmp;
			curListPos->next = NULL;
		}
	}

	if( List == NULL )
	{
		List = oldList;
		curListPos = List;
	}
	else
		curListPos->next = oldList;

	Node **pointer;
	pointer = (Node**)curMemPos;
	curMemPos += SZ_Node * PAGedgeNum;
	Node **end;
	end = (Node**)curMemPos;
	curMemPos += SZ_Node * PAGedgeNum;

	for( int i = 0 ; i < PAGedgeNum - 1 ; i ++ )
	{
		pointer[i] = oldpointer[i];
		end[i] = oldend[i];
	}

	pointer[PAGedgeNum-1] = List;
	end[PAGedgeNum-1] = curListPos;

	//test
			/*Node *testNode;
			testNode = List;
			printf("List = ");
			while(testNode != NULL )
			{
				printf("%d ",testNode->node );
				testNode = testNode->next;
				
			}
			printf("\n");
	//end of test
	
	//test
	
	for( int i = PAGedgeNum -1 ; i >= 0 ; i -- )
	{
		printf("pointer[%d] is %d, end[%d] is %d\n",i,pointer[i]->node,i,end[i]->node);

	}
	char c;
	scanf("%c",&c);*/
	//end of test

	for( int i = PAGedgeNum - 1 ; i >= 0 ; i -- )
	{
		if( pointer[i] == end[i] ) continue;

		while(pointer[i] != NULL)
		{
			
			PAGedge[PAGedgeNum] = pointer[i]->node;
			PAGedgeNum ++;

			if( pointer[i] == end[i] ) pointer[i]->next = NULL;
			else
				pointer[i] = pointer[i]->next;

			if(PAGedgeNum == MaxPAGSize )
			{
				//find a new embedding
				PAGNum ++;

				//test
				/*for( int k = 0 ; k < PAGedgeNum ; k ++ )
				{
					printf("%d ",PAGedge[k]);
				}
				printf("\n");
				char c;
				scanf("%c",&c);*/
				//end of test

				Embedding *newEmbedding;
				//test
				//printf("In findMPAG_edge(),find a new embedding, and call createEmbedding()\n");
				/*if( PAGNum %1000 == 0 )
				{
					printf( "PAGNum is %d\n",PAGNum);
					printPAGHash();
				}
				printf("PAGedge is : ");
				for( int x = 0 ; x < PAGedgeNum ; x ++ )
					printf("%d, ",PAGedge[x]);
				printf("\n");*/
				//end of test
				newEmbedding = createEmbedding(PAGedge,PAGedgeNum);
				if(HashEmbeddingtoPAGHashTable(newEmbedding)==1)
				{
					return 1;
				}
				else
				{
					PAGedgeNum --;
					return 0;
				}
			}
			else
			{
				int havenewedge = 0;
				for( int k = PAGedgeNum - 1 ; k >= 0 ; k -- )
				{
					if( pointer[k] != NULL && pointer[k] != end[k] )
					{
						havenewedge = 1;
					}
				}
				if ( havenewedge == 1 )
				{
					//printf("------------------------\n");
					return findMPAGs_edge(PAGedge,List,pointer,end);
				}
				else
				{
					//printf("++++++++++++++++++++++++++\n");
					PAGedgeNum --;
					return 0;
				}
			}	
		}
	}
	PAGedgeNum --;
	return 0;
}


void findMPAGs()
{
	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;

	PAGNum = 0;

	int SZ_PAGedge = sizeof(int)*MaxPAGSize;
	int SZ_TravelEdge = sizeof(int) * OriginalGraph->edgeNum;
	if((curMemEnd - curMemPos) < SZ_PAGedge+SZ_TravelEdge){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}        
	int *PAGedge = (int*)curMemPos;
    curMemPos += SZ_PAGedge;
	memset(PAGedge,-1,SZ_PAGedge);
	// in the Travel Edge, 0 means haven't been traveled, 1 means have been traveled.
	TravelEdge = (int*)curMemPos;
	curMemPos += SZ_TravelEdge;
	memset(TravelEdge,0,SZ_TravelEdge);


	int i;
	for( i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		PAGedgeNum = 0;
		//test
		
		printf( "node %d, there are %d edges\n",i,OriginalGraph->degreeAr[i]);
		printf("PAGNum is %d\n\n",PAGNum);
		/*if( i%1000 == 0 )
		{
			char c;
			scanf("%c",&c);
		}
		//char c;
		scanf("%c",&c);*/
		//end of test
		if( NodeMapping[i] != -1 )
			continue;
		for( int j = 0 ; j < OriginalGraph->degreeAr[i] ; j ++ )
		{
			//test
			//if( j % 50 == 0 )
			//printf("*************j = %d (%d)\n",j,OriginalGraph->degreeAr[i] );
			//char c;
			//scanf("%c",&c);
			//end of test
			int curEdge = OriginalGraph->edgePosAar[i][j];
			if( EdgeMapping[curEdge] != -1 || TravelEdge[curEdge] != 0 )
				continue;
			PAGedge[0] = curEdge;
			PAGedgeNum = 1;
			TravelEdge[curEdge] = 1;


			//initialize the List
			Node* List;
			int fromnode;
			int tonode;
			int fromnode_degree, tonode_degree;
			
			
			tonode = OriginalGraph->edgeAr[curEdge*EDGESZ+1];
			fromnode = OriginalGraph->edgeAr[curEdge*EDGESZ];
			fromnode_degree = OriginalGraph->degreeAr[fromnode];
			tonode_degree = OriginalGraph->degreeAr[tonode];

			//test
			//printf("++++++++++++++++curEdge is %d   fromnode is %d  tonode is %d\n",curEdge,fromnode,tonode);
			//end of test

			int SZ_pointer = sizeof(Node*);
			int SZ_end = sizeof(Node*);

			//test
			/*printf("fromnode_degree + tonode_degree = %d\n",fromnode_degree + tonode_degree);
			scanf("%c",&c);*/
			//end of test

			if(fromnode_degree + tonode_degree - 2 == 0 ) continue;


			if((curMemEnd - curMemPos) < SZ_Node * (fromnode_degree + tonode_degree - 2) + SZ_pointer + SZ_end){
				//free mem in cur block is not enough
				if(curBlk < endBlk){
					//we have already allocated free blocks
					++curBlk;
					curMemPos=memBlkAr[curBlk];
					curMemEnd=curMemPos+BLK_SZ;
				}
				else{
					//allocate a new block
					++curBlk;
					++endBlk;
					curMemPos=(char*)malloc(BLK_SZ);
					memBlkAr[curBlk]=curMemPos;
					curMemEnd=curMemPos+BLK_SZ;
				}
			}

			Node *curListPos;
			curListPos = List = NULL;

			int curfromPos = 0;
			int curtoPos = 0;
			for( curfromPos = 0 ; curfromPos < fromnode_degree ; curfromPos ++ )
			{
				int tmp = OriginalGraph->edgePosAar[fromnode][curfromPos];
				if( tmp == curEdge ) continue;
				if( TravelEdge[tmp] == 1 || EdgeMapping[tmp] != -1 ) continue;
				if( List == NULL )
				{
					//create first Node in the List
					List = (Node*)curMemPos;
					curMemPos += SZ_Node;
					List->node = tmp;
					List->next = NULL;
					curListPos = List;
				}
				else
				{
					Node *newNode = (Node*)curMemPos;
					curMemPos += SZ_Node;
					curListPos->next = newNode;
					curListPos = newNode;
					curListPos->node = tmp;
					curListPos->next = NULL;
				}
			}


			//test
			//printf("tonode is %d\n",tonode);
			//end of test
			for( curtoPos = 0 ; curtoPos < tonode_degree ; curtoPos ++ )
			{
				int tmp = OriginalGraph->edgePosAar[tonode][curtoPos];
				if( tmp == curEdge ) continue;
				if( TravelEdge[tmp] == 1 || EdgeMapping[tmp] != -1 ) continue;
				if( List == NULL )
				{
					//create first Node in the List
					List = (Node*)curMemPos;
					curMemPos += SZ_Node;
					List->node = tmp;
					List->next = NULL;
					curListPos = List;
				}
				else
				{
					Node *newNode = (Node*)curMemPos;
					curMemPos += SZ_Node;
					curListPos->next = newNode;
					curListPos = newNode;
					curListPos->node = tmp;
					curListPos->next = NULL;
				}
			}

			Node **pointer;
			pointer = (Node**)curMemPos;
			curMemPos += SZ_pointer;
			Node **end;
			end = (Node**)curMemPos;
			curMemPos += SZ_end;

			pointer[0] = List;
			end[0] = curListPos;

			//test
			/*printf("pointer[0] is %d\n",pointer[0]->node);
			printf("end[0] is %d\n",end[0]->node);
			printf("PAGedge[0] = %d (%d %d)\n",PAGedge[0],OriginalGraph->edgeAr[PAGedge[0]*EDGESZ],OriginalGraph->edgeAr[PAGedge[0]*EDGESZ+1]);
			printf("tonode is %d\n",tonode);
			printf("OriginalGraph->degreeAr[5] is %d\n",OriginalGraph->degreeAr[5]);
			Node *testNode;
			testNode = List;
			int testnum = 0;
			printf("List = ");
			while(testNode != NULL )
			{
				int fromnode = OriginalGraph->edgeAr[testNode->node*EDGESZ];
				int tonode = OriginalGraph->edgeAr[testNode->node*EDGESZ+1];
				printf("%d(%d %d), ",testNode->node,fromnode,tonode );
				testnum ++;
				testNode = testNode->next;
			}
			printf("\ntestnum is %d   fromnode_degree + tonode_degree - 2 = %d\n",testnum,fromnode_degree + tonode_degree - 2);*/
			
			//end of test

			while(pointer[0] != NULL)
			{
				PAGedge[1] = pointer[0]->node;
				PAGedgeNum = 2;

				//test
				/*int edge1 = PAGedge[0];
				int edge2 = PAGedge[1];
				int fromnode1 = OriginalGraph->edgeAr[edge1*EDGESZ];
				int tonode1 = OriginalGraph->edgeAr[edge1*EDGESZ+1];
				int fromnode2 = OriginalGraph->edgeAr[edge2*EDGESZ];
				int tonode2 = OriginalGraph->edgeAr[edge2*EDGESZ+1];
				if( fromnode1 != fromnode2 && fromnode1!= tonode2 && tonode1 != fromnode2 && tonode1 != tonode2)
				{
					printf("pointer[0] is %d\n",pointer[0]->node);
					Node *testNode;
					testNode = pointer[0];
					while( testNode != end[0] )
					{
						int fromnode = OriginalGraph->edgeAr[testNode->node*EDGESZ];
						int tonode = OriginalGraph->edgeAr[testNode->node*EDGESZ+1];
						printf("%d(%d %d), ",testNode->node,fromnode,tonode );
						testNode = testNode->next;
					}
					printf("\nPAGedge  : %d (%d %d), %d (%d %d)\n",edge1,fromnode1,tonode1,edge2,fromnode2,tonode2);
					char c;
					scanf("%c",&c);
				}*/
				//end of test


				if( findMPAGs_edge(PAGedge,List,pointer,end) == 1 ) //some PAG have k VD-Embeddings
				{
					//do the partition and update the NodeMapping[], EdgeMapping[] and TravelEdge[]
					//memset(TravelEdge,0,SZ_TravelEdge);
					//i = 0;
				}
				else; //do nothing;

				if( pointer[0] == end[0] ) pointer[0] = NULL;
				else
					pointer[0] = pointer[0]->next;
				
			}	
		}
		//test
		//printPAGHash();
		//end of test
		/*if( PAGedgeNum != 0 )
		{
			printf("not correct");
		}*/
	}
	// cannot find any PAG which frequency is greater than K.
	if( i == OriginalGraph->nodeNum )
	{
		//do the anonymous
	}

	//printPAGHash();

	//Release the memory 
	curBlk=prevBlk0;
	curMemPos=prevMemPos0;
	curMemEnd=prevMemEnd0;

	return;
}

int calculate_VDfrequency(PAG* curPAG)
{
	//test
	//return curPAG->frequent;
	//end of test
	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;

	int SZ_NM = sizeof(int) * OriginalGraph->nodeNum;

	if((curMemEnd - curMemPos) < SZ_NM){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	} 

	int VDfrequency = 0;
	
	int *NM = (int*)curMemPos;
	curMemPos += SZ_NM;
	memset(NM,0,SZ_NM);
	// 1 means be contained by other VD-embedding, 0 means not be contained.

	//printf("1111111111111111111\n");

	Embedding *preE;
	Embedding *curVDE;
	Embedding *curEmbedding = NULL;

	//int count = 0;

	do
	{
		//count ++;
		int i=0;
		//test
		//for( i = 0 ; i < curPAG->EmbeddingList->nodeNum ; i ++ )
		//{
			//printf("i = %d, NodeMapping[curPAG->EmbeddingList->nodeID[%d]] = %d\n",i,i,NodeMapping[curPAG->EmbeddingList->nodeID[i]]);
			//printf("NM[curPAG->EmbeddingList->nodeID[%d]] = %d\n",i,NM[curPAG->EmbeddingList->nodeID[i]]);
		//}
		//end of test
		for( i = 0 ; i < curPAG->EmbeddingList->nodeNum && NodeMapping[curPAG->EmbeddingList->nodeID[i]] == -1 && NM[curPAG->EmbeddingList->nodeID[i]] == 0;i++);
		//printf("222222222222\n");
		if( i == curPAG->EmbeddingList->nodeNum )
		{
			//printf("2222222222222\n");
			if( VDfrequency == 0 )//first item in the VDEmbeddingList
			{
				//printf("case 1 \n");
				curPAG->VDEmbeddingList = curPAG->EmbeddingList;
				curPAG->EmbeddingList = curPAG->EmbeddingList->next;
				curVDE = curPAG->VDEmbeddingList;
				curVDE->next = NULL;
				VDfrequency ++;
			}
			else
			{
				//printf("case 2 \n");
				curVDE->next = curPAG->EmbeddingList;
				curPAG->EmbeddingList = curPAG->EmbeddingList->next;
				curVDE = curVDE->next;
				curVDE->next = NULL;
				VDfrequency ++;
			}
			for( int j = 0 ; j < curVDE->nodeNum; j ++ )
			{
				NM[curVDE->nodeID[j]]=1;
			}
		}
		else
		{
			//printf("case 3");
			preE = curPAG->EmbeddingList;
			curEmbedding = curPAG->EmbeddingList->next;
			//printf("VDfrequency is %d, count is %d\n",VDfrequency, count);
			break;
		}
	}while( curPAG->EmbeddingList != NULL );

	//printf("33333333333\n");

	while( curEmbedding != NULL )
	{
		//count ++;
		int i;
		for( i = 0; i < curEmbedding->nodeNum && NodeMapping[curEmbedding->nodeID[i]] == -1 && NM[curEmbedding->nodeID[i]] == 0; i ++ );
		if( i == curEmbedding->nodeNum )//is VD-embedding
		{
			preE->next = curEmbedding->next;
			if( VDfrequency == 0 )//first item in the VDEbeddingList
			{
				curPAG->VDEmbeddingList = curEmbedding;
				curVDE = curPAG->VDEmbeddingList;
				curVDE->next =NULL;
				VDfrequency ++;
				
			}
			else
			{
				curVDE->next = curEmbedding;
				curVDE=curVDE->next;
				curVDE->next = NULL;
				VDfrequency ++;

			}
			for( int j = 0 ; j < curVDE->nodeNum; j ++ )
			{
				NM[curVDE->nodeID[j]]=1;
			}
			
			curEmbedding = preE->next;
		}
		else
		{
			preE = curEmbedding;
			curEmbedding = curEmbedding->next;
		}
	}
	//printf("count is %d\n",count);

	for( curPAG->End = curPAG->EmbeddingList ; curPAG->End->next != NULL ; curPAG->End = curPAG->End->next); 

	//test
	int testnum = 0;
	for( int j = 0 ; j < OriginalGraph->nodeNum ; j ++ )
	{
		if( NM[j] == 1 ) testnum ++;
	}
	//printf("VDfrequency is %d, testnum is %d, curPAG->nodeNum is %d\n",VDfrequency,testnum,curPAG->thisgraph->nodeNum);

	//Release the memory 
	curBlk=prevBlk0;
	curMemPos=prevMemPos0;
	curMemEnd=prevMemEnd0;

	return VDfrequency;
}
void print_KPartition(KPartition* Partition)
{
	printf("********************* Print Partition *******************************\n");
	printf("Partition->nodeNum is %d\n",Partition->nodeNum);
	for( int i = 0 ; i < K ; i ++ )
	{
		printf("Partition %d :", i);
		for( int j = 0 ; j < Partition->nodeNum ; j ++ )
		{
			/*if( NodeMapping[Partition->nodeAr[j][i]] != i )
			{
				printf("\ni is %d, j is %d\n",i,j);
				printf("It's wrong! node %d in Partition %d but NodeMapping[%d] is %d\n",Partition->nodeAr[j][i],i,Partition->nodeAr[j][i],NodeMapping[Partition->nodeAr[j][i]]);
				char c;
				scanf("%c",&c);
			}
			else*/
				printf("%d ",Partition->nodeAr[j][i]);
		}
		printf("\n");
	}

	int curNNum = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 )
			curNNum ++;
	}
	printf("There are %d nodes!",OriginalGraph->nodeNum-curNNum);

	printf("********************* End of Print Partition *******************************\n");
}
void distribution_notes()
{
	//use tmp to test the total number of PAGs in the Hashtable
	//int tmp=0;

	//test
	//printf("******************begin distribution_notes******************************\n");
	int count = 0;
	for( int i = 0 ; i < QHASHTABLESZ ; i ++ )
	{
		if( MaxPAGHashTable[i] != NULL )
		{
			PAGSlot *curPAGSlot = MaxPAGHashTable[i];
			do
			{
				PAG *curPAG = curPAGSlot->thisPAG;
				//test
				//printf("PAG %d,curPAG->nodeNum is %d, curPAG->edgeNum is %d\nfrequency is %d ",++count,curPAG->thisgraph->nodeNum,curPAG->thisgraph->edgeNum,curPAG->frequent);
				//end of test

				
				curPAG->frequent = calculate_VDfrequency(curPAG);

				//test
				//printf("VDfrequency is %d\n",curPAG->frequent );
				//char c;
				//scanf("%c",&c);
				//end of test

				while( curPAG->frequent >= K )
				{
					for( int i = 0 ; i < K ; i ++ )
					{
						//printf("Partition %d : ",i);
						Embedding* curEmbedding = curPAG->VDEmbeddingList;
						curPAG->VDEmbeddingList = curPAG->VDEmbeddingList->next;
						//put curEmbedding to Partition i
						for( int j = 0 ; j < curEmbedding->nodeNum ; j ++ )
						{
							int curnode = curEmbedding->nodeID[curEmbedding->VM[j]];
							//test
							/*printf(" %d",curnode);
							if( NodeMapping[curnode] != -1 )
							{
								printf("(in Partition %d) ",NodeMapping[curnode]);
							}*/
							//end of test
							
							NodeMapping[curnode] = i;
							Partition->nodeAr[Partition->nodeNum+j][i] = curnode;
						}
						
					}
					curPAG->frequent -= K;
					if( curPAG->VDEmbeddingList != NULL )
					{
						curPAG->End->next = curPAG->VDEmbeddingList;
						curPAG->VDEmbeddingList = NULL;
						curPAG->frequent = calculate_VDfrequency(curPAG);
					}

					Partition->nodeNum += curPAG->thisgraph->nodeNum;
					
				}
				//test of print KPartition
				//print_KPartition(Partition);
				//end of test

				//test
				//printf("VDfrequency is %d\n",curPAG->frequent );
				//end of test

				//test
				/*if( count == 5 )
				{
					Embedding *tmpE = curPAG->VDEmbeddingList;
					int m = 0;
					while( tmpE!=NULL)
					{
						printf("tmpE->nodeNum is %d\n",tmpE->nodeNum);
						printf("tmpE->thisgraph->nodeNum is %d\n",tmpE->thisgraph->nodeNum);
						printf( "%d VDEmbedding is : ",++m);
						for( int x = 0 ; x < tmpE->nodeNum ; x ++ )
						{
							printf("%d ",tmpE->nodeID[x]);
						}
						printf("\n");
						tmpE = tmpE->next;
					}
					tmpE = curPAG->EmbeddingList;
					m = 0;
					while( tmpE != NULL)
					{
						printf( "%d Embedding is : ",++m);
						for( int x = 0 ; x < tmpE->nodeNum ; x ++ )
						{
							printf("%d ",tmpE->nodeID[x]);
						}
						printf("\n");
						tmpE = tmpE->next;
					}
					char c;
					scanf("%c",&c);
				}*/
				//end of test

				if( curPAGSlot->next != NULL )
					curPAGSlot = curPAGSlot->next;
				else
					break;
			}while(1);
			
		}
	}
	//test
	//print_KPartition(Partition);
	//end of test
}

void distribution_MPAGSZ2()
{
	//test
	/*printf( "****************before handle MPAGSize = 2 *************************\n");
	int tcount1 = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 )
		{
			tcount1 ++;
		}
	}
	printf("there are %d nodes in the OriginalGraph\n",tcount1);
	printf("Partition->nodeNum = %d\n\n",Partition->nodeNum);*/
	//end of test

	//handle the MaxPAGSize = 2

	int PNum = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 && OriginalGraph->degreeAr[i] >= 2 )
		{
			int newdegree = 0;
			int newnode0;
			int newnode1;
			for( int j = 0 ; j < OriginalGraph->degreeAr[i] ; j ++ )
			{
				int tmpedge = OriginalGraph->edgePosAar[i][j];
				int tmpnode = OriginalGraph->edgeAr[tmpedge*EDGESZ];
				if( tmpnode == i )
					tmpnode = OriginalGraph->edgeAr[tmpedge*EDGESZ+1];
				if( NodeMapping[tmpnode] == -1 )
				{
					if( newdegree == 0 )
						newnode0 = tmpnode;
					else
						newnode1 = tmpnode;
					newdegree ++;
					if( newdegree == 2 )
					{
						Partition->nodeAr[Partition->nodeNum][PNum] = i;
						Partition->nodeAr[Partition->nodeNum+1][PNum] = newnode0;
						Partition->nodeAr[Partition->nodeNum+2][PNum] = newnode1;

						NodeMapping[i] = PNum;
						NodeMapping[newnode0] = PNum;
						NodeMapping[newnode1] = PNum;

						if( PNum == K - 1 )
						{
							PNum = 0;
							Partition->nodeNum+= 3;
						}
						else
							PNum ++;
						i --;
						break;
					}
				}
			}
			
		}
	}

	//test
	/*printf( "****************after handle MPAGSize = 2 *************************\n");
	int tcount2 = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 )
		{
			tcount2 ++;
		}
	}
	printf("there are %d nodes in the OriginalGraph\n",tcount2);
	printf("Partition->nodeNum = %d\n\n",Partition->nodeNum);*/
	//end of test
	
	if( PNum != 0 )
	{
		//test
		//printf( "****************PNum != 0 after handle MPAGSize = 2 *************************\nPNum is %d\n",PNum);

		//end of test
		int Pos = 0;
		for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
		{
			if( NodeMapping[i] == -1 )
			{
				Partition->nodeAr[Partition->nodeNum+Pos][PNum]=i;
				NodeMapping[i] = PNum;
				Pos ++;
				if( Pos == 3 )
				{
					Pos = 0;
					if( PNum == K - 1 )
					{
						Partition->nodeNum += 3;
						PNum = 0;
						break;
					}
					else
					{
						PNum ++;
					}
				}
			}
		}

		//test
		
		/*int tcount3 = 0;
		for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
		{
			if( NodeMapping[i] == -1 )
			{
				tcount3 ++;
			}
		}
		printf("there are %d nodes in the OriginalGraph\n",tcount3);
		printf("Partition->nodeNum = %d\n\n",Partition->nodeNum);*/
		//end of test
	}
    

	//test
	/*int i = 0;
	for( i = 0 ; i < OriginalGraph->nodeNum && OriginalGraph->degreeAr[i] > 1; i ++ );
	printf( "from node %d, the degree is 1\n",i );
	for( ; i < OriginalGraph->nodeNum && OriginalGraph->degreeAr[i] > 0 ; i ++ );
	printf( "from node %d, the degree is 0\n",i );
	int testcount = 0;
	for( i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 && OriginalGraph->degreeAr[i] >= 2 )
		{
			int newdegree = 0;
			for( int j = 0 ; j < OriginalGraph->degreeAr[i] ; j ++ )
			{
				int tmpedge = OriginalGraph->edgePosAar[i][j];
				int tmpnode = OriginalGraph->edgeAr[tmpedge*EDGESZ];
				if( tmpnode == i )
					tmpnode = OriginalGraph->edgeAr[tmpedge*EDGESZ+1];
				if( NodeMapping[tmpnode] == -1 )
					newdegree ++;
				
			}
			if( newdegree >= 2 )
			{
				testcount ++;
			}
		}
	}
	printf("there are %d nodes have newdegree greater than 2 \n",testcount);*/

	//end of test
	
	//handle the remand nodes
	int pcount = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 )
		{
			NodeMapping[i] = pcount%K;
			Partition->nodeAr[Partition->nodeNum][NodeMapping[i]]=i;
			if( pcount % K == K - 1 )
			{
				Partition->nodeNum ++;
			}
			pcount ++;
		}
	}
	//end of handle the remand nodes

	//test
	/*int tcount = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if( NodeMapping[i] == -1 )
		{
			tcount ++;
		}
	}
	printf("there are %d nodes in the OriginalGraph\n",tcount);
	printf("Partition->nodeNum = %d\n",Partition->nodeNum);*/

	/*int wrong = 0;
	for( int i = 0 ; i < Partition->nodeNum ; i ++ )
	{
		for( int j = 0 ; j < K ; j ++ )
		{
			if(NodeMapping[Partition->nodeAr[i][j]]  != j)
			{
				wrong ++;
			}
			if( Partition->nodeAr[i][j] == -1 )
			{
				printf("Partition->nodeAr[%d][%d] == -1\n",i,j);
				char c;
				scanf("%c",&c);
			}
		}
	}
	printf("there are %d wrong Position in Partition->nodeAr\n",wrong);

	wrong = 0;
	for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
	{
		if(NodeMapping[i]==-1)
			wrong++;
	}

	printf("there are %d wrong Position in NodeMapping array\n",wrong);*/
	
	//end of test
	
	
	//test
	/*int sum_degree = 0;
	 for( int j = 0 ; j < curNNum; j ++ ) 
	 {
	 sum_degree += degree[j];
	 fprintf(outfile,"%d %d\n", j, degree[j]);
	 }
	 printf("there are totally %d edges!\n",sum_degree/2);
	 printf("average degree is %f!\n",(float)sum_degree/(float)curNNum);*/
	//system("pause");
	//return 0;
	//end of test
}

void addordelete( int fromPos,int toPos )
{
	//remember the starting position of the temp mem buffer (will restore to this position after using the temp mem buffer)
	int prevBlk0=curBlk;
	char *prevMemPos0=curMemPos;
	char *prevMemEnd0=curMemEnd;

	int dnum = 0;
	int anum = 0;

	int SZ_fromAr = sizeof(int) * K;
	int SZ_toAr = sizeof(int) * K;
	int SZ_edgeAr = sizeof(int) * K;

	if((curMemEnd - curMemPos) < SZ_fromAr+SZ_toAr+SZ_edgeAr){
		//free mem in cur block is not enough
		if(curBlk < endBlk){
			//we have already allocated free blocks
			++curBlk;
			curMemPos=memBlkAr[curBlk];
			curMemEnd=curMemPos+BLK_SZ;
		}
		else{
			//allocate a new block
			++curBlk;
			++endBlk;
			curMemPos=(char*)malloc(BLK_SZ);
			memBlkAr[curBlk]=curMemPos;
			curMemEnd=curMemPos+BLK_SZ;
		}
	}
	int *fromAr = (int*)curMemPos;
	curMemPos += SZ_fromAr;
	int *toAr = (int*)curMemPos;
	curMemPos += SZ_toAr;
	int *edgeAr = (int*)curMemPos;
	curMemPos += SZ_edgeAr;
	memset(edgeAr,-1,SZ_edgeAr);

	for( int i = 0 ; i < K ; i ++ )
	{
		fromAr[i] = Partition->nodeAr[fromPos][i];
		toAr[i] = Partition->nodeAr[toPos][i];
	}
	for( int i = 0; i < K ; i ++ )
	{
		int curNode;
		int findNode;
		if( fromAr[i] > toAr[i])
		{
			curNode = fromAr[i];
			findNode = toAr[i];
		}
		else
		{
			curNode = toAr[i];
			findNode = fromAr[i];
		}
		int j;
		for( j = 0 ; j < OriginalGraph->degreeAr[curNode] ; j ++ )
		{
			int edge = OriginalGraph->edgePosAar[curNode][j];
			int tmpNode;
			if( curNode == OriginalGraph->edgeAr[edge*EDGESZ])
				tmpNode = OriginalGraph->edgeAr[edge*EDGESZ+1];
			else
				tmpNode = OriginalGraph->edgeAr[edge*EDGESZ];
			if( tmpNode == findNode )
			{
				edgeAr[i] = edge;
				EdgeMapping[edge] = i;
				dnum ++;
				break;
			}
		}
		if( j == OriginalGraph->degreeAr[curNode] )
		{
			anum ++;
		}
	}

	//if( dnum < anum )
	if( Delete_edges < Add_edges)
	{
		for( int i = 0 ; i < K ; i ++ )
		{
			if( edgeAr[i] != -1 )
			{
				EdgeMapping[edgeAr[i]] = -2;
				Delete_edges ++;
			}
		}
	}
	else
	{
		for( int i = 0 ; i < K ; i ++ )
		{
			if( edgeAr[i] == -1 )
			{
				AddEdge *newaddedge;

				if((curMemEnd2 - curMemPos2) < (SZ_AddEdge)){
					//free mem in cur block is not enough
					//allocate a new block
					++curBlk2;
					curMemPos2=(char*)malloc(BLK_SZ2);
					memBlkAr2[curBlk2]=curMemPos2;
					curMemEnd2=curMemPos2+BLK_SZ2;
				}
				newaddedge = (AddEdge*)curMemPos2;
				curMemPos2 += SZ_AddEdge;
				newaddedge->fromnode = fromAr[i];
				newaddedge->tonode = toAr[i];
				newaddedge->next = NULL;
				if( Add_edges == 0 )
				{
					Addedge = newaddedge;
					endAddedge = newaddedge;
				}
				else
				{
					endAddedge->next = newaddedge;
					endAddedge = endAddedge->next;
				}
				Add_edges ++;
			}
		}
	}

	//Release the memory 
	curBlk=prevBlk0;
	curMemPos=prevMemPos0;
	curMemEnd=prevMemEnd0;

	return;
}
void addordeleteEdge()
{
	Add_edges = 0;
	Delete_edges = 0;
	Addedge = NULL;
	endAddedge = NULL;

	

	for( int i = 0; i < OriginalGraph->edgeNum ; i ++ )
	{
		if( EdgeMapping[i] != -1 )
			continue;
		int fromnode = OriginalGraph->edgeAr[i*EDGESZ];
		int tonode = OriginalGraph->edgeAr[i*EDGESZ+1];
		
		if( NodeMapping[fromnode] == NodeMapping[tonode] )
		{
			//test opimize operation from deletion or addition
			int p = NodeMapping[fromnode];
			int fromPos;
			int toPos;
			for( int j = 0 ; j < Partition->nodeNum ; j ++ )
			{
				if( Partition->nodeAr[j][p] == fromnode )
					fromPos = j;
				else if ( Partition->nodeAr[j][p] == tonode )
					toPos = j;
				else;
			}
			addordelete(fromPos,toPos);

		}
		else
		{
			//delete the edge
			Delete_edges ++;
			EdgeMapping[i] = -2;
		}
	}
}
void print_edges(char* result_edgeFN)
{
	FILE *result_edge = fopen(result_edgeFN,"w");
	for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	{
		fprintf(result_edge,"%d %d\n",OriginalGraph->edgeAr[i*EDGESZ],OriginalGraph->edgeAr[i*EDGESZ+1]);
	}
	fprintf(result_edge,"**********\n");
	for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	{
		if( EdgeMapping[i] != -2 )
		{
			fprintf(result_edge,"%d %d\n", OriginalGraph->edgeAr[i*EDGESZ], OriginalGraph->edgeAr[i*EDGESZ+1]);
		}
	}
	AddEdge *curAddedge;
	curAddedge = Addedge;
	while( curAddedge != NULL)
	{
		fprintf(result_edge,"%d %d\n",curAddedge->fromnode,curAddedge->tonode);
		curAddedge = curAddedge->next;
	}
	fclose(result_edge);
	return;
}
int main(int argc, char* argv[])
{
	K = 3;
	//printf("please input the value of K:");
	//scanf("%d",&K);
	K = (int)strtoul(argv[1], 0, 0);
	int curNNum = 50000;
	//printf("please input the value of curNNum:");
	//scanf("%d",&curNNum);
	curNNum = (int)strtoul(argv[2], 0, 0);
    char* newEdge = argv[3];
    char* result = argv[4];
    char* print_edgesFN = argv[5];
	printf( "K = %d, curNNum = %d\n",K, curNNum);
	//int curNNum = 5618;
	//int curNNum = 4811;
	//int curNNum = 6;
	FILE *infile;
	//FILE *outfile;
	//outfile = fopen("./output.txt","w");
	
#ifndef WIN32
	float  userTime, sysTime;
	struct rusage myTime1, myTime2;
#endif


#ifndef WIN32
	float  userTime_findProjected, sysTime_findProjected;
	float  userTime_findGlobal, sysTime_findGlobal;
	//        float  userTime_remainD, sysTime_remainD;
	//        float  userTime_generateST, sysTime_generateST;
	//        float  userTime_compressST, sysTime_compressST;
	//        float  userTime_normalQuery, sysTime_normalQuery;
	//        float  userTime_partialQuery, sysTime_partialQuery;
	//        float  userTime_context, sysTime_context;
	//        float  userTime_totalGenerate, sysTime_totalGenerate;
	
	struct rusage myTime_start;
	struct rusage myTime_findProjected_end;
	struct rusage myTime_findGlobal_end;
	//        struct rusage myTime_remainD_end;
	//        struct rusage myTime_generateST_end;
	//        struct rusage myTime_compressST_end;
	//        struct rusage myTime_normalQuery_end;
	//        struct rusage myTime_partialQuery_end;
	//        struct rusage myTime_context_end;
#endif
	
	
#ifndef WIN32
	getrusage(RUSAGE_SELF,&myTime1);        
#endif 
	
	int SZ_degreeAr = curNNum * sizeof(int);
	if((curMemEnd2 - curMemPos2) < (SZ_degreeAr)){
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}
	
	int *degreeAr = (int*)curMemPos2;
	curMemPos2 += SZ_degreeAr;
	
	
	calculate_degree(degreeAr,curNNum,newEdge);
	
	int sum_degree = 0;
	for( int j = 0 ; j < curNNum; j ++ ) 
	{
		sum_degree += degreeAr[j];
	}
	int curENum = sum_degree/2;
	//printf("curENum is %d, sum_degree is %d\n",curENum, sum_degree);
	
	
	create_graph(curNNum,curENum,degreeAr,newEdge);

	int NodeNum = OriginalGraph->nodeNum;

	//test
	//testtestSubG();
	//return 0;
	//end of test

	//initialize Partition
	int MaxPSZ = OriginalGraph->nodeNum / K + 1;
	int SZ_nodeAr = sizeof(int*)*MaxPSZ;
	int SZ_nodeAr_i = sizeof(int) * K;
	
	if((curMemEnd2 - curMemPos2) < (SZ_Partition + SZ_nodeAr + SZ_nodeAr_i*K))
	{
		//free mem in cur block is not enough
		//allocate a new block
		++curBlk2;
		curMemPos2=(char*)malloc(BLK_SZ2);
		memBlkAr2[curBlk2]=curMemPos2;
		curMemEnd2=curMemPos2+BLK_SZ2;
	}

	Partition = (KPartition*)curMemPos2;
	curMemPos2 += SZ_Partition;

	Partition->nodeNum = 0;
	Partition->nodeAr = (int**)curMemPos2;
	curMemPos2 += SZ_nodeAr;

	for( int i = 0 ; i < MaxPSZ ; i ++ )
	{
		Partition->nodeAr[i] = (int*)curMemPos2;
		curMemPos2 += SZ_nodeAr_i;
		memset(Partition->nodeAr[i],-1,SZ_nodeAr_i);
	}
	//end of initialization of partition

	for( MaxPAGSize=5; MaxPAGSize > 2 ; MaxPAGSize --)
	{
		NodeNum = OriginalGraph->nodeNum;
		
		findMPAGs();
		distribution_notes();
		//test
		for( int i = 0 ; i < OriginalGraph->nodeNum ; i ++ )
		{
			if( NodeMapping[i] != -1 ) NodeNum --;
		}
		//printf("main(): NodeNum is %d\n",NodeNum);

		//printf("end of PAGSZ %d",MaxPAGSize);
		//char c;
		//scanf("%c\n",&c);

	}

	distribution_MPAGSZ2();
	addordeleteEdge();

	//test
	printf("Add_edges is %d, Delete_edges is %d\n OriginalGraph->edgeNum is %d",Add_edges,Delete_edges,OriginalGraph->edgeNum);

	/*int wrong = 0;
	for( int i = 0 ; i < OriginalGraph->edgeNum ; i ++ )
	{
		if( EdgeMapping[i] == -1 )
			wrong ++;
	}
	printf("there %d edges still in the OriginalGraph\n",wrong);*/
	//end of test
	
	
	FILE *E_result_fp;
	E_result_fp = fopen(result,"w");
	
       
	
	
	
#ifndef WIN32                                
	getrusage(RUSAGE_SELF,&myTime2);
	
	userTime =
	((float) (myTime2.ru_utime.tv_sec  - myTime1.ru_utime.tv_sec)) +
	((float) (myTime2.ru_utime.tv_usec - myTime1.ru_utime.tv_usec)) * 1e-6;
    sysTime =
    ((float) (myTime2.ru_stime.tv_sec  - myTime1.ru_stime.tv_sec)) +
    ((float) (myTime2.ru_stime.tv_usec - myTime1.ru_stime.tv_usec)) * 1e-6;
#endif

#ifndef WIN32
		fprintf(E_result_fp, "User time : %f seconds\n",userTime);
        fprintf(E_result_fp, "System time : %f seconds\n",sysTime);
        fprintf(E_result_fp, "Total time : %f seconds\n",userTime+sysTime);
#endif

	fprintf(E_result_fp, "add_edges is %d\n",Add_edges);
	fprintf(E_result_fp, "delete_edges is %d\n",Delete_edges);
	fprintf(E_result_fp,"mem is %d\n", curBlk2*BLK_SZ);

	fclose(E_result_fp);

	print_edges(print_edgesFN);

	return 1;
}
