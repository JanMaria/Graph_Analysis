# Graph_Analysis
F-Core and Clustering on a graph comprising roughly 1.5 million vertices

If you want to view my code please check out the "cleen" branch which was made for peer review purposes. 

This is a capstone project for the UCSD Java specialization on Coursera.org. In short, this project’s goal was to use data about some real life social network, to ask two questions about that data and to implement algorithms to answer those questions. In particular this project uses DBLP data (available at http://projects.csail.mit.edu/dnd/DBLP/ ) containing information about collaboration of 1.5 million Computer Scientists on about 4 million CS papers (9 million connections between authors in total). It parses the .json file to create adjacency list type of weighted and undirected graph (MyParser class). Then it searches for something called f-core (or fractional k-core) which can be thought of as a subgraph consisting of authors that have most connections with each other (FCore class). And finally it searches for communities inside that f-core, building a dendrogram in the process (Forest class). 

RandMap class is a random graph generator created for debugging purposes. MyRunner is just a meta-level class that runs other classes in proper order. 

