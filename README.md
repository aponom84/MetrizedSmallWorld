# Metrized Small World
This is java implementation of the Metrized Small World data structure (MSW). 
MSW is a highly scalable data structure for approximate nearest neighbor search (ann) in arbitrary metric space. 
MSW demonstrate ability to scale logarithmically both on the size of data (number of records) and the number dimensions.
The approximation is defined in terms of probability. This means that the search algorithm may return the true nearest neighbor element to the query with some given probability (for example 95%), if not, most likely it will be the second closest and so on with sharply decreasing probability. 
The algorithm requires a linear(n)*poly(d) space and the expected algorithms complexity is as follows:

* insertion of one element into the structure ~log(n)* d^1.7 (  log(n)*poly(d)  );
* approximate nearest neighbor search ~log(n)* d^1.7;
* approximate k-nearest neighbor search ~k*log(n) * d^1.7;
* deletion – not described yet, but supposedly complexity will be ~log(n) and poly(d).

MSW has topology of the network constructed in such way, that starting from any node of the network, one can efficiently find any other node 
without the entire knowledge of network topology.

Also, the present code intended to demonstrate how a huge set of network nodes can efficiently communicate and find each other. 
The search process based on the wide class of the distance functions (in contrast with DHT). 
This makes it possible to apply MSW's algorithms for many real application which needs complex search and require a distributed architecture. 

The list of papers related to the MSW presented below

* Ponomarenko, A., Malkov, Y., Logvinov, A., & Krylov, V. published at ICTA 2011 proceedings. [Approximate Nearest Neighbor Search Small World Approach](http://www.iiis.org/CDs2011/CD2011IDI/ICTA_2011/Abstract.asp?myurl=CT175ON.pdf)
* Malkov, Y., Ponomarenko, A., Logvinov, A., & Krylov, V. presented at SISAP 2012. [Scalable distributed algorithm for approximate nearest neighbor search problem in high dimensional general metric spaces](http://link.springer.com/chapter/10.1007/978-3-642-32153-5_10)
* Ponomarenko, A., Averlin, N., Bilegsaikhan, N., Boytsov, L., 2014. [Comparative Analysis of Data Structures for Approximate Nearest Neighbor Search.](http://boytsov.info/pubs/da2014.pdf) [**[BibTex]**](http://scholar.google.com/scholar.bib?q=info:yOjNiT2Ql4AJ:scholar.google.com/&output=citation&hl=en&ct=citation&cd=0)
* Malkov, Y., Ponomarenko, A., Logvinov, A., & Krylov, V. 2014. Information Systems, 45, 61-68. [Approximate nearest neighbor algorithm based on navigable small world graphs (http://www.sciencedirect.com/science/article/pii/S0306437913001300)
* Malkov, Y. A. 2015. preprint. [Growing homophilic networks are natural optimal navigable small worlds](http://arxiv.org/abs/1507.06529)

WHY METRIZED SMALL WORLD IS BETTER THAN OTHER APPROXIMATE NEAREST NEIGHBOR DATA STRUCTURES?

The first a good property of this data structure is a resistance to the [curse of dimensionality](https://en.wikipedia.org/wiki/Curse_of_dimensionality).  
The second major property is a decentralization which means that all the algorithms use only local information on each step and can be initiated from any vertex. 
Also, all the data elements in the structure are the same rank - there is no central or root element.
So, high scalability both with size and data dimensionality and the distributed nature of the algorithm are a good base for building many real-world distributed 
similarity search applications.
Currently the MSW method is a part of the [Non-Metric Space Library](https://github.com/searchivarius/NonMetricSpaceLib).
This library can be used to compare MSW algorithms with many others.

WHAT SHOULD I DO TO RUN EXAMPLE?

COMPILE SOURCE CODE
You can use apache ant http://ant.apache.org/ and type “ant” from the common line in the folder where “build.xml” file is located. 
Or you can open and compile project with [NetBeans](http://netbeans.org)

RUN EXAMPLE
Run testTrec3.bat at the Windows or testTrec3.sh at the Linux machine.
The list of parameters used in the example class “SearchAttemptsTestTrec3”:
1.	 (NN) The number of nearest neighbors used in construction algorithm to approximate Voronoi neighbors
2.	(K) number of k-closest elements for the k-nn search
3.	(initAttempts) - number of search attempts used during the construction of the structure
4.	(minAttempts) - minimum number of attempts which will be uses during the test search
5.	(maxAttempts) - maximum number of attempts
6.	(dataBaseSize) - the restriction of number elements in the data structure. To remove the restriction, set the value to 0.
7.	(querySetSize) - the restriction on the number of all possible queries. Set to 0 for unlimited number of queries.
8.	(testSeqSize) - the number elements in a randomly selected subset used to verify accuracy of the search. 
9.	(dataPath) – the path to the directory containing the set frequency term vectors extracted from the set of documents.
10.	(queryPath) – the path to the set of frequency term vectors used as a set of all possible queries.

Note that the example uses the Trec-3 data set as the part of [Metric Space Library](http://sisap.org/Metric_Space_Library.html)