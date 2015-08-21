# MetrizedSmallWorld
An open java implementation of the MetrizedSmallWorld data structure
The Metrized Small World is a highly scalable data structure for the approximate nearest neighbor search in terms of both data size and data dimensionality dedicated to the approximate nearest neighbor search. 
The Metrized Small World data structure was proposed by Alexander Ponomarenko, Yuri Malkov, Andrey Logvinov and Vladimir Krylov in the article “Approximate Nearest Neighbor Search Small World Approach” published in ICTA 2011 conference proceedings. A more  detailed description of the data structure is given in the article article “Scalable Distributed Algorithm for Approximate Nearest Neighbor Search Problem in High Dimensional General Metric Spaces” which was presented at SISAP 2012 conference.  

WHAT IS IT FOR?

The Metrized Small World data structure is dedicated to the approximate nearest neighbor search problem (ann) in arbitrary metric space. The approximation is defined in terms of probability. This means that the search algorithm may return the true nearest neighbor element to the query with some given probability (for example 95%), if not, most likely it will be the second closest and so on with sharply decreasing probability. 
The algorithm requires a linear(n)*poly(d) space and the expected algorithms complexity is as follows:

•	insertion of one element into the structure ~log(n)* d^1.7 (  log(n)*poly(d)  );
•	approximate nearest neighbor search ~log(n)* d^1.7;
•	approximate k-nearest neighbor search ~k*log(n) * d^1.7;
•	deletion – not described yet, but supposedly will be ~log(n) and poly(d).

WHY METRIZED SMALL WORLD IS BETTER THAN OTHER APPROXIMATE NEAREST NEIGHBOR DATA STRUCTURES?

The first most unique property of this data structure consists in (assumed) complete resistance to the curse of dimensionality for arbitrary metric space. 
The second major property is decentralization: all the algorithms use only local information on each step and can be initiated from any vertex. Also, all the data elements in the structure are of the same rank - there is no central or root element.
So, high scalability both with size and data dimensionality and the distributed nature of the algorithm are a good base for building many real-world extreme dataset size high dimensionality similarity search applications.

WHAT SHOULD I DO TO RUN THE EXAMPLE?

COMPILE SOURCE CODE
You can use apache ant http://ant.apache.org/ and type “ant” from the common line in the folder where “build.xml” file is located. 
Or you can open and compile project with NetBeans 7.0 http://netbeans.org

RUN EXAMPLE
Run testTrec3.bat at the Windows or testTrec3.sh at the Linux machine.
The list of parameters used in the example class “SearchAttemptsTestTrec3”:
1.	 (NN) The number of nearest neighbors used in construction algorithm to approximate Voronoi neighbor
2.	(K) number of k-closest elements for the k-nn search
3.	(initAttempts) - number of search attempts used during the construction of the structure
4.	(minAttempts) - minimum number of attempts which will be uses during the test search
5.	(maxAttempts) - maximum number of attempts
6.	(dataBaseSize) - the restriction of number elements in the data structure. To remove the restriction, set the value to 0.
7.	(querySetSize) - the restriction on the number of all possible queries. Set to 0 for unlimited number of queries.
8.	(testSeqSize) - the number elements in a randomly selected subset used to verify accuracy of the search. 
9.	(dataPath) – the path to the directory containing the set frequency term vectors extracted from the set of documents.
10.	(queryPath) – the path to the set of frequency term vectors used as a set of all possible queries.

Note that the example uses the Trec-3 data set as the part of Metric Space Library Note that our example uses the Trec-3 collection which is a part of Metric Space Library: http://sisap.org/Metric_Space_Library.html

Thise code is under GNU LESSER GENERAL PUBLIC LICENSE v3
Alexander Ponomarenko aponom84@gmail.com
