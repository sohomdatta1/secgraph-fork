/* Graph transformation using random walks */
/* A central server is performing the random walks */
/* Written by Prateek Mittal*/ 

#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <iostream>
#include <fstream>
#include <set>
#include <map>
#include <vector>
#include <assert.h>
#include <string.h>
#include <tr1/unordered_map>
#include <algorithm>
#include <math.h>
#include <sstream>

#define min(a,b) (((a) < (b)) ? (a) : (b))
#define max(a,b) (((a) < (b)) ? (b) : (a))
//#define debug
#define adjacency_format
//#define link_format

using namespace std;
using namespace std::tr1;

int randomInt(int range); // random integer between [0, range]

class graph{
	public:
		typedef unsigned int vertex;
		typedef pair<vertex, vertex> edge; 
		typedef unsigned int edge_label;
		int transform_wlen;

		set<vertex> vertices;	// maintain vertices
		int max_vertex_val; 	// maximum vertex value (useful for random vertex sampling)

		//set<pair<vertex,vertex> > edges;	// maintain edges 
		unordered_map<vertex,set<vertex> > adjacency; 	// friend list per vertex	
		unordered_map<vertex, unordered_map<vertex, unsigned int> > edge_map;
		unsigned int edge_counter;

		graph(){
			max_vertex_val=0;
		}
		void add_vertex(vertex node1){
			vertices.insert(node1);
		}
		void add_edge(vertex node1,vertex node2){
			// add edge node1, node2
			
			// ensure node1, node2 in the vertex set
			// can simply insert in vertex set
			// if they already exist, no harm
			vertices.insert(node1);	
			vertices.insert(node2);
			if(max_vertex_val < node1){
				max_vertex_val = node1;
			}
			if(max_vertex_val <  node2){
				max_vertex_val = node2;
			}

			// no self loops
			if(node1==node2){
				return;
			}

			// what if this already exists? 
			//edge e1=make_pair(node1,node2);
			//edge e2=make_pair(node2,node1); 
			//if(edges.find(e2)==edges.end()){
			//	edges.insert(e1);
			//}		
						
			// add this in the adjacency list for both nodes 
			// graph is undirected
			if(adjacency[node1].find(node2)==adjacency[node1].end()){
				adjacency[node1].insert(node2);
				adjacency[node2].insert(node1);
				edge_map[node1][node2]=edge_counter;
				edge_counter++;
				edge_map[node2][node1]=edge_counter;
				edge_counter++;
			}
		}
		vertex randVertex(){
			// Commenting since we are not running Sybillimit
			//srand(seed);
			int random = randomInt(max_vertex_val);
			set<vertex>::iterator iter=vertices.lower_bound(random);
			if(iter==vertices.end()){
				iter=vertices.begin();
			}
			return *iter;
		}
		vertex randNeighbor(vertex current){
			//srand(seed);
			//int random = randomInt(max_vertex_val);
			//set<vertex>::iterator iter=vertices.lower_bound(random);
			set<vertex>::iterator iter;
			double sum=0;
			double rand_value = (double) ((float)1.0*rand()/(RAND_MAX+1.0));
			//cout << "Rand Value: " << rand_value << endl;
			for(iter=adjacency[current].begin();iter!=adjacency[current].end();iter++){
				sum=sum+1.0/(double)adjacency[current].size();
				//cout << "Current sum: " << sum << endl;
				if(sum >= rand_value){
					break;
				}
			}
			if(iter==adjacency[current].end()){
				iter=adjacency[current].begin();
			}
			vertex randN = *iter;
			//cout << *iter << endl;
			return randN;
		}

		/*void delete_vertex(vertex node1){
			// remove all edges involving vertex
			set<vertex>::iterator iter;
			for(iter=adjacency[node1].begin();iter!=adjacency[node1].end();iter++){
				delete_edge(node1,*iter);
			}
			
			// remove adjacency for vertex
			adjacency[node1].clear();
			
			// remove from vertex set
			vertices.erase(node1);
		}
		void delete_edge(vertex node1,vertex node2){
			// delete edge node1-node2
			edges.erase(make_pair(node1,node2));
			edges.erase(make_pair(node2,node1));
			// only one of the above will succeed
			
			// remove edge from adjacency list
			adjacency[node1].erase(node2);
			adjacency[node2].erase(node1);

			// in case node 1 or node 2 have no edges left
			// then we could remove those vertices from the graph?
		}*/
		void print_graph(){
			set<vertex>::iterator iter;
		//	cout << "Vertices:" << endl;
		//	for(iter=vertices.begin();iter!=vertices.end();iter++){
		//		cout << *iter << endl;
		//	}
			
		//	set<pair<vertex,vertex> >::iterator edge_iter;
		//	cout << "Edges:" << endl;
		//	for(edge_iter=edges.begin();edge_iter!=edges.end();edge_iter++){
		//		cout << edge_iter->first << "---" << edge_iter->second << endl;
		//	}

			set<vertex>::iterator iter2;
		//	cout << "Adjacency" << endl;
			for(iter=vertices.begin();iter!=vertices.end();iter++){
				cout << *iter;
				for(iter2=adjacency[*iter].begin();iter2!=adjacency[*iter].end();iter2++){
					cout << " " << *iter2;
				}
				cout << endl;	
			}
		}
		/*edge_string get_string(edge e1){
			//string line;
			//stringstream ss_line(line);
			//ss_line << e1.first;
			//ss_line << " ";
			//ss_line << e1.second;
			//return ss_line.str();
			return edge_to_string_map[e1];
		}
		edge get_edge(edge_string line){
			stringstream ss_line(line);
			edge e1;
			ss_line >> e1.first;
			ss_line >> e1.second;
			return e1;
		}*/

		/*void compute_transient_dist(vertex initiator){

			// If we want to run this code for very large graphs, it would be better to have node vertices be consecutive, and use vertex label as index into an array. Currently, I am using unordered_map, which should be goodenough for small to moderate scale topologies 

			unordered_map<vertex,long double> prob_vector;
			unordered_map<vertex,long double> new_prob_vector;
			
			set<vertex>::iterator iter,iter2;
	
 		       	for(iter=vertices.begin();iter!=vertices.end();iter++){
       		        	prob_vector[*iter]=0;
    		        	new_prob_vector[*iter]=0;
         		}
           		prob_vector[initiator]=1;
		
			for(int l=0;l<max_walk_length;l++){
     		        	// now, we need to multiply the prob vector with the transition probability$
       		                // row into column multiplication -- for each column, we have a value. 
      		        	for(iter=vertices.begin();iter!=vertices.end();iter++){
        		        	// need to multiple the prob vector with the i'th column. 
                       		        // the i'th column can be computed using p[i]
              		                new_prob_vector[*iter]=0;
					for(iter2=adjacency[*iter].begin();iter2!=adjacency[*iter].end();iter2++){
                               		         // here, we will compute the result for new_prob_vector[i]
                   		                 // lets first consider normal random walks
                                       		 new_prob_vector[*iter] = new_prob_vector[*iter] + prob_vector[*iter2]*(1.0/((double)adjacency[*iter2].size()));
                                	}
                        	}
                        	for(iter=vertices.begin();iter!=vertices.end();iter++){
                               		prob_vector[*iter]=new_prob_vector[*iter];
                        	}
			}
		}*/
	
		
};

class transform: public graph{
	public:
		void transform_graph(class transform *g_new){		
			// perform random walks (along each edge) for each node
			// replace corresponding neighbor with terminus point of random walks
			set<vertex>::iterator iter,iter2;
			vertex prev_hop,cur_hop,next_hop;
			edge tail;
			int random,count_neighbors;
			unordered_map<vertex,vertex> map;		
			// should vertex selection be randomized? 
			int old_edges=0, new_edges=0;
			for(iter=vertices.begin();iter!=vertices.end();iter++){
				if(adjacency[*iter].size()==0){
					// isolated vertices remain isolated
					g_new -> vertices.insert(*iter);
					continue;
				}
				for(iter2=adjacency[*iter].begin();iter2!=adjacency[*iter].end();iter2++){
					// for each neighbor *iter2
					next_hop=*iter2;
					int num_loop=0;
					old_edges++;
					do{
						next_hop=*iter2;
						for(int j=1;j<transform_wlen;j++){
							prev_hop=cur_hop;
							cur_hop=next_hop;
							// perform a random walk from the current hop, and choose the next hop	
							next_hop=randNeighbor(cur_hop);
							//next_hop=map[prev_hop];	
					
						}
						num_loop++;
					}while((next_hop==*iter || g_new->adjacency[*iter].find(next_hop)!=g_new->adjacency[*iter].end()) && num_loop < 5);
					// store the result in a data structure
					// are we storing just the node or the edge?
					//tail = make_pair(cur_hop,next_hop);
					//tail_string = get_string(tail);
					//vertex_tails[*iter].push_back(edge_map[cur_hop][next_hop]);
					// lets not needlessly allocate memory for everyone
					
					// lets add this edge with probability 0.5 (otherwise the degree is going to be doubled)
					// special case for the first edge (want to make sure that atleast one edge in the graph)
					// if(g_new->adjacency[*iter].size()==0){
					// Bug: the above condition may not map to the first edge here
					// since adjacency size may be non zero is someone else setup an edge with me. 

					if(iter2==adjacency[*iter].begin()){
						
						g_new->add_edge(*iter,next_hop);
						g_new->add_edge(next_hop,*iter);
						new_edges++;	
					}
					else{
						double num_degree= (double)adjacency[*iter].size();
					 	double rand_value = (double) ((float)1.0*rand()/(RAND_MAX+1.0));
						if(rand_value <= (0.5*num_degree-1)/(num_degree-1)){
							g_new->add_edge(*iter,next_hop);
							g_new->add_edge(next_hop,*iter);
							new_edges++;
						}
		
					}
				}
			}
			g_new-> max_vertex_val = max_vertex_val;
			//cout << "Graph transformed" << endl; 
			//cout << "# Old edges: " <<  old_edges/2.0 << endl; 
			//cout << "# New edges: " <<  new_edges << endl;
			//exit(1); 

		}
};

void readSocialTopology(char *graph_file);	// read in the social network topology

typedef unsigned int vertex;
typedef pair<vertex, vertex> edge;
class transform g_original;
set<int> id_set;

int succ(int key){
	set<int>::iterator int_iter;
	//int_iter=id_set.find(key);
	//if(int_iter!=id_set.end()){
	//	return *int_iter;
	//}
	int_iter=id_set.lower_bound(key);
	if(int_iter==id_set.end()){
		int_iter=id_set.begin();
	}
	return *int_iter;
}
int m=20;
int max_id=pow(2,m);


unsigned int simCanon_NodeId_IncreasingDistance(unsigned int idsrc, unsigned int iddest) {
    // finds the distance to a point right before the iddest
    if (idsrc<=iddest) {
        return iddest-idsrc;
    } else {
        // find distance across zero
        return (iddest-0)+(max_id-idsrc+1);
    }
}

unsigned int simCanon_NodeId_Closer(unsigned int idsrc1, unsigned int idsrc2, unsigned int iddest) {

  if (simCanon_NodeId_IncreasingDistance(idsrc1,iddest)<simCanon_NodeId_IncreasingDistance(idsrc2,iddest))
    return idsrc1;
  else
    return idsrc2;
}


int main(int argc, char *argv[]){
	int seed; // random seed for simulations
	char graph_file[200];  // file name for social graph 
	
	int c;
        while ((c = getopt (argc, argv, "g:t:r:")) != -1){
		switch(c){
			case 'g': strncpy(graph_file,optarg,200);
				break;
			case 't': g_original.transform_wlen=atoi(optarg);
				break;
			case 'r':
				seed=atoi(optarg);
				break;
			default:
				cout << "Usage: ./a.out -g <graph-file> -t <transform_walk_length> -r <rseed> " << endl;
				exit(EXIT_FAILURE);
		}
	}
	#ifdef debug
		cout << "Graph file: " << graph_file << endl;
		cout << "Transform_walk_length:" << g_original.transform_wlen << endl;
		cout << "Random seed for simulation: " << seed << endl; 
	#endif
	srand(seed);

	readSocialTopology(graph_file);	// read in the social network topology

	vertex rand_vertex;
	set<vertex>::iterator iter,iter2;

	// lets assign numeric identifiers to nodes
	unordered_map<vertex,int> vertex_ids;
	unordered_map<int,vertex> ids_vertex;
		for(iter=g_original.vertices.begin();iter!=g_original.vertices.end();iter++){
		int rand_id; 
		do{
			rand_id =  (int) ((float)max_id*rand()/(RAND_MAX+1.0));
		}while(id_set.find(rand_id)!=id_set.end());
		vertex_ids[*iter]=rand_id;
		ids_vertex[rand_id]=*iter;
		id_set.insert(rand_id);
	}
	//cout << "id generation done" << endl;
	// lets setup the fingertables
	unordered_map<vertex,set<int> > fingertable;
	set<int>::iterator int_iter;
	for(iter=g_original.vertices.begin();iter!=g_original.vertices.end();iter++){
		for(int i=0;i<=m;i++){
			// i'th fingertable entry is succ(myid + 2^i)
			int key=(int)(vertex_ids[*iter]+pow(2,i))% max_id;
			fingertable[*iter].insert(succ(key));
		}
	}
	//cout << "fingertable generation done" << endl;

	double pathlength=0;
	double reliability=0;
	int max_nodes=1000;
	int max_lookups=1000;
	for(int i=1; i<= max_nodes; i++){
		vertex honest = g_original.randVertex();
		unordered_map<vertex,double> trust;
		trust.clear();
		trust[honest]=1;
		set<vertex> cur_neighborhood, next_neighborhood;
		cur_neighborhood.insert(honest);
		for(int j=1; j<=8; j++){
			next_neighborhood.clear();
			for(iter=cur_neighborhood.begin();iter!=cur_neighborhood.end();iter++){
				next_neighborhood.insert(*iter);
				for(iter2=g_original.adjacency[*iter].begin();iter2!=g_original.adjacency[*iter].end();iter2++){
					next_neighborhood.insert(*iter2);
					if(trust[*iter2]==0){
						trust[*iter]=1-j*0.05;
					}
				}
			}
			cur_neighborhood.clear();
			cur_neighborhood=next_neighborhood;
		}
		//cout << "trust values done" << endl;
		// lets do routing now.
		set<int>::iterator int_iter;
		for(int j=1;j<=max_lookups;j++){
			double path_reliability=1;
			vertex cur_hop=honest;
			int key =  (int) ((float)max_id*rand()/(RAND_MAX+1.0));
			//cout << vertex_ids[cur_hop] << " routing towards "  << key << endl;	
			while(1){
				if(succ(key)==vertex_ids[cur_hop] || fingertable[cur_hop].find(succ(key))!=fingertable[cur_hop].end()){
					break;
				}
			//	cout << vertex_ids[cur_hop] << " routing towards "  << key << endl;
				int next_hop=vertex_ids[cur_hop];

				// lets search in the friend table first
				for(iter=g_original.adjacency[cur_hop].begin();iter!=g_original.adjacency[cur_hop].end();iter++){
					if(simCanon_NodeId_Closer(next_hop,vertex_ids[*iter],key)!=next_hop){
						next_hop=vertex_ids[*iter];
					}
				}

				// if we don't find anyone amongst friends, we'll search in fingertable
				if(next_hop==vertex_ids[cur_hop]){
					for(int_iter=fingertable[cur_hop].begin();int_iter!=fingertable[cur_hop].end();int_iter++){
						if(simCanon_NodeId_Closer(next_hop,*int_iter,key)!=next_hop){
							next_hop=*int_iter;
						}
					}
				}		

				cur_hop=ids_vertex[next_hop];
				pathlength++;
				if(trust[cur_hop]==0){
					path_reliability=path_reliability*0.6;
				}
				else{
					path_reliability=path_reliability*trust[cur_hop];
				}	
			};
			reliability=reliability+path_reliability;
		}
	}
	cout << reliability/((double)max_nodes*max_lookups) << " " << pathlength/((double)max_nodes*max_lookups) << endl;
    cerr << reliability/((double)max_nodes*max_lookups) <<endl;
}

/* Read in the social graph */
void readSocialTopology(char *graph_file){
	ifstream in(graph_file,ifstream::in);
	assert(in);

	string line;
	vertex node1,node2;
	while(getline(in,line)!=NULL){
	#ifdef link_format
		node1=(unsigned long int)atol(strtok((char *)line.c_str()," \n\t\r"));
		node2=(unsigned long int)atol(strtok(NULL," \n\t\r"));
		g_original.add_edge(node1,node2);
	#endif
	#ifdef adjacency_format
		stringstream ss(line);
		if(ss){	
			ss >> node1;
			if(node1 >= g_original.max_vertex_val){
				g_original.max_vertex_val=node1;
			}
			while(ss){	
				ss >> node2;
				g_original.add_edge(node1,node2);
			}
		}
	#endif
	}
	in.close();

}

int randomInt(int range){  // random integer between [0,range)
	int random_no = (int) ((float)range*rand()/(RAND_MAX+1.0));
	return random_no;
}

