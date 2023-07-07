#!/bin/python
import sys
from pygraph.classes.graph import graph
from pygraph.algorithms.minmax import cut_tree
from pygraph.algorithms.generators import generate
from pygraph.readwrite.markup import write

graphFile=open(sys.argv[1],'r');
outputFile=open(sys.argv[2],'w');
gr = graph();
nodeSet = set();
for line in graphFile:
	nodeList = map(int, line.strip().split(' '));
	nodeSet.add(nodeList[0]);
	nodeSet.add(nodeList[1]);
for node in nodeSet:
	gr.add_node(node);
graphFile=open(sys.argv[1],'r');
for line in graphFile:
	nodeList = map(int, line.strip().split(' '));
	if not gr.has_edge((nodeList[0],nodeList[1])):
		gr.add_edge((nodeList[0],nodeList[1]));
print >> outputFile, cut_tree(gr)