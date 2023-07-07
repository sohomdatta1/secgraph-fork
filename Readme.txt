The software comes with a GUI as well as a commandline mode. The commandline mode uses apacheCLI library and should support most systems.

The GUI uses the SWT library thus in order to use the GUI on systems other than Linux it must be rebuild with the correct libraries. 
Some of the modules below directed runs cpp executables created via the SNAP library from Stanford thus those modules need to rebuild on
systems other than Linux. 

The whole system is developed on Ubuntu 14.04 and currently is only tested on Ubuntu 14.04 if there are compatibility issues please contact the authors. 
Some of the modules requires running c++/cSharp codes that is provided in the package please build those executables before running those modules.
List of directories that requires building:
./Influmax
./pmittal
./Snap-2.4/examples/secGraphTools/community
./privHRG-master
./Proserpio/VLDB2014 Weighted PINQ
./kISO


de-anonymization usages:

java -jar secGraphCLI -m d -a Walk -gA <Graph A File Name> -addDegFN <addedDegreeFileName> -trgFN <targetFileName> -gO <Output Graph Name> -intEFN <internalEdgeFileName> -recFN <recoverNodesFileName>

java -jar secGraphCLI -m d -a Cut -gA <Graph A File Name> -trgFN <targetFileName> -gO <Output Graph Name>

java -jar secGraphCLI -m d -a NS -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -theta <Theta (double)> -gO <Output Graph Name>

java -jar secGraphCLI -m d -a DV -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -gO <Output Graph Name> -am <algorithm mode (queue or stack)>

java -jar secGraphCLI -m d -a Percolation -gA <Graph A File Name> -gB <Graph B File Name> -nKeep <number of elements to keep from each bipartite matching> -seed <Seed File Name> -gO <Output Graph Name>

java -jar secGraphCLI -m d -a SJISC2014 -gA <Graph A File Name> -gB <Graph B File Name>  -gACen <Graph A Centrality File > -gBCen <Graph B Centrality File > -am <algorithm mode (queue or stack)> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DisSimW <Distance Similarity Weight> -StrSimW <Structual Similarity Weight>  -InhSimW <Inherited Similarity Weight> -gO <Output Graph Name>
					
java -jar secGraphCLI -m d -a SJISCA2014 -gA <Graph A File Name> -gB <Graph B File Name> -gACen <Graph A Centrality File> -gBCen <Graph B Centrality File> -am <algorithm mode (queue or stack)> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DisSimW <Distance Similarity Weight> -StrSimW <Structual Similarity Weight>  -InhSimW <Inherited Similarity Weight>  -l <limit (double)> -gO <Output Graph Name>
					
java -jar secGraphCLI -m d -a Reconciliation -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -nTest <Number of Nodes to Test> -gO <Output Graph Name>
					
					
java -jar secGraphCLI -m d -a SJCCS2014 -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -bSize <Size of Bipartite Matching> -nKeep <number of elements to keep from each bipartite matching> -DegSimW <Degree Similarity Weight> -NeiSimW <Neighborhood Similarity Weight> -DisSimW <Distance Similarity Weight> -nNei <max number of Neighbor to consider> -gO <Output Graph Name> -am <algorithm mode (queue or stack)>

java -jar secGraph -m d -a Passive -gA <Input Graph File Name> -cG <Coalition Graph File> -cG <Coalition Degree File> -gO <Output Graph Name>

java -jar secGraph -m d -a Link -gA <Graph A File Name> -gB <Graph B File Name> -seed <Seed File Name> -theta <Theta (double)> -gO <Output Graph Name>
(We use the same implementation for both Link Prediction and the Narayanan-Shmatikov Attack since Link Prediction is a simplified version of the attack)


java -jar secGraph -m d -a Community -RD <ResultsDirectory> -sN <NumSeeds>  -noise <Noise (double)> -exp <Exp (double)> -gA <Input Graph A> -gB <Input Graph B> -cliqueFN <Clique File Name> -probDeg <ProbDegAnon boolean> Overlap <Overlap (boolean)> avgProb <avgProb Optional use if want to get degree of anonymity>

java -jar secGraph -m d -a Bayesian -gA <Graph A File Name> -gB <Graph B File Name> -gO <Output Graph Name>


anonymization usages:

java -jar secGraphCLI -m a -a id -gA <Graph A File Name> -gO <Output Graph Name>

java -jar secGraphCLI -m a -a addDel -gA <Graph A File Name> -gO <Output Graph Name>  -f <fraction of edges to modify>
					
java -jar secGraphCLI -m a -a sw -gA <Graph A File Name> -gO <Output Graph Name>  -f <fraction of edges to modify>
					
java -jar secGraphCLI -m a -a kDa -gA <Graph A File Name> -gO <Output Graph Name>  -k <k-value>

java -jar secGraphCLI -m a -a tMean -gA <Graph A File Name> -gO <Output Graph Name>  -t <t-value>

java -jar secGraphCLI -m a -a salaDP -gA <Graph A File Name> -gO <Output Graph Name> -e <epsilon-value>

java -jar secGraphCLI -m a -a rWalk -gA <Graph A File Name> -gO <Output Graph Name> -rn <randwalk number of step> -rm <randwalk max retry>	

java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> -eHRG <epsilonHRG> -eE <epsilonE> -eq <eq> -stop <stop>
java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> -eHRG <epsilonHRG> -eE <epsilonE>
java -jar secGraph -m a -a XiaoDP -gA <Graph A File Name> 

java -jar secGraph -m a -a P_DP -gA <Graph A File Name> -parallel <parallel> -gO <Output Graph Name> -mcmcIt <number of MCMC Iterations> -e <epsilon-value>

java -jar secGraph -m a -a union -gA <Graph A File Name> -gO <Output Graph Name> -k <cluster size>

java -jar secGraph -m a -a kIso -gA <Graph A File Name> -gO <Output Graph Name>  -k <k-value> -curNNum <curNNum> -statusResult <statusResult> -edgeResult <edgeResult>

utility usages:

java -jar secGraphCLI -m u -a deg -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a JD -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a ED -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a PL -gA <Graph A File Name> -gB <Graph B File Name> -n <number of nodes used for path length>

java -jar secGraphCLI -m u -a LCC -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a CC -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a CCen -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a BC -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a EV -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a NC -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a NR -gA <Graph A File Name> -gB <Graph B File Name> -l <limit>

java -jar secGraphCLI -m u -a Infec -gA <Graph A File Name> -gB <Graph B File Name>  -nInf <number of repeats for infectiousness>

java -jar secGraphCLI -m u -a Rolx -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a RE -gA <Graph A File Name> -gB <Graph B File Name>  -gASRE <graphA spam node sequence file> -gBSRE <graphB spam node sequence file>  -naRE <number of neighbor to accept for RE> -thRE <use two hop for RE (true/false)>

java -jar secGraphCLI -m u -a Infl -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a MINS -gA <Graph A File Name> -gB <Graph B File Name> -pMINS <p value for MINS (double<=1)> -l <threshold (double<=1)>

java -jar secGraphCLI -m u -a CD  -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a SR -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a SD -gA <Graph A File Name> -gB <Graph B File Name>  -sbn <Sybil Detection numSInstances> -sbs <Sybil Detection rseed> -sbl <Sybil Detection walkLengh>  -sbh <Sybil Detection balanceCondHva>

java -jar secGraphCLI -m u -a AS -gA <Graph A File Name> -gB <Graph B File Name>

java -jar secGraphCLI -m u -a HS -gA <Graph A File Name> -gB <Graph B File Name>   

java -jar secGraphCLI -m u -a PR -gA <Graph A File Name> -gB <Graph B File Name>
