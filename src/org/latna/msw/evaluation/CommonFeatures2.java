package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.TestLib;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.euclidian.EuclidianFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;

/**
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class CommonFeatures2 {
    //public static final String FLD = "c:/Users/aponomarenko/MSW/CommonFeatures/";
    public static final String FLD = "";
    public static  String outFileName;
    
    //= fld + "commonFeatures.csv";
    public static void main(String[] args) throws IOException {
        //int[] checkPoints = {1000, 5000, 10000, 50000, 100000, 500000, 1000000, 2000000,5000000,10000000,20000000, 50000000}; 
        //int[] checkPoints = {1000, 5000, 10000, 50000, 100000}; 
        //int[] checkPoints = {100000};
        //int dbSize = 100000;
        //int dbSize = 1000;
        
        int dbSize = Integer.parseInt(args[0]);
        
        //int dimensionality = 1;
        int [] allDim = {20};
        //int nn = 2; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int [] allNN = {2,3,4,5,6,7,8,9,10,20,30,40,50,60,70};
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 20; //number of search attempts used during the contruction of the structure
        int querySetSize = 100; //the restriction on the number of quleries. To set no restriction set value = 0
        int testSeqSize = 100; //number elements in the random selected subset used to verify accuracy of the search. 
        int maxExpNumber  = 20;

        for (int dim : allDim) {
            for (int nn : allNN) {
                System.out.println("dim=" + dim + " nn=" + nn + " expNumber=" + maxExpNumber + " k=" + k + " initAttemps=" + initAttempts + " minAttempts="
                    + " dataBaseSize=" + dbSize + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize);
                outFileName = FLD + "dim=" + dim + "size=" + dbSize + "nn=" + nn + "exp=" + maxExpNumber + "initA=" + initAttempts + ".csv";

                FileWriter fw = new FileWriter(new File(outFileName));
                //<len, count>
                ConcurrentNavigableMap<Integer, Integer> gwMap = new ConcurrentSkipListMap<>();
                ConcurrentNavigableMap<Integer, Integer> succeedMap = new ConcurrentSkipListMap<>();
                ConcurrentNavigableMap<Integer, Integer> spMap = new ConcurrentSkipListMap<>();
                //<degree, numberOfVertex>
                ConcurrentNavigableMap<Integer, Integer> degreeMap = new ConcurrentSkipListMap<>();

                DoubleAdder avgClusteringCoeff = new DoubleAdder();
                AtomicLong numberOfSucceed = new AtomicLong(0);
                AtomicLong numberOfAllSearches = new AtomicLong(0);

                IntStream.rangeClosed(1, maxExpNumber).parallel().forEach(expNumber -> {

                    //AbstractMetricStructure db = new ErdeshRandomGraph( (double) nn * 2.0 / (double) (checkPoints[0]-1));
                    MetrizedSmallWorld db = new MetrizedSmallWorld();
                    db.setNN(nn);
                    db.setInitAttempts(initAttempts);

                    System.out.println("Exp number: " + expNumber);
                    EuclidianFactory ef = new EuclidianFactory(dim, dbSize);
                    EuclidianFactory testQueryFactory = new EuclidianFactory(dim, querySetSize);

                    ef.getElements().stream().forEach((me) -> {
                        db.add(me);
                    });

                    Map<MetricElement, List<TestLib.EvaluationResult>> res = TestLib.evaluateSearchAlgorithm(db.getElements(), testQueryFactory, querySetSize, testSeqSize);

                    for (List<TestLib.EvaluationResult> l : res.values()) {
                        for (TestLib.EvaluationResult r : l) {
                            //numberOfAllSearches++;
                            numberOfAllSearches.incrementAndGet();
                            gwMap.put(r.greedyWalkPathLenght - 1, gwMap.getOrDefault(r.greedyWalkPathLenght - 1, 0) + 1);
                            if (r.graphDistance == 0) {
                                succeedMap.put(r.greedyWalkPathLenght - 1, succeedMap.getOrDefault(r.greedyWalkPathLenght - 1, 0) + 1);
                                //numberOfSucceed++;
                                numberOfSucceed.incrementAndGet();
                            };
                        }
                    }

                    testQueryFactory.getElements().stream().limit(10).forEach((MetricElement q) -> {
                        TestLib.getShortestPathValueMap(db.getElements(),
                                TestLib.getKCorrectElements(db.getElements(), q, 1).first().getMetricElement())
                                .values().stream().forEach((v) -> {
                                    //fout_sp.out(v + ";\n");
                                    spMap.put(v, spMap.getOrDefault(v, 0) + 1);
                                });
                    });

                    db.getElements().stream().forEach(
                            e -> degreeMap.put(e.getAllFriends().size(), degreeMap.getOrDefault(e.getAllFriends().size(), 0) + 1)
                    );

                    //avgClusteringCoeff += TestLib.getAvgClusteringCoeff(db.getElements());
                    avgClusteringCoeff.add(TestLib.getAvgClusteringCoeff(db.getElements()));
                });

                fw.write("len;gw;succeed;sp;degree;avgClusteringCoeff;recall;\n");

                int allSpNumber = spMap.values().stream().mapToInt(e -> e).sum();
                int allDegreeNumber = degreeMap.values().stream().mapToInt(e -> e).sum();

                fw.write("0;"
                        + String.format("%,f", (double) gwMap.getOrDefault(0, 0) / numberOfAllSearches.doubleValue()) + ";"
                        + String.format("%,f", (double) succeedMap.getOrDefault(0, 0) / numberOfAllSearches.doubleValue()) + ";"
                        + String.format("%,f", (double) spMap.getOrDefault(0, 0) / +allSpNumber) + ";"
                        + String.format("%,f", (double) degreeMap.getOrDefault(0, 0) / allDegreeNumber) + ";"
                        + String.format("%,f", avgClusteringCoeff.doubleValue() / maxExpNumber) + ";"
                        + String.format("%,f", (double) numberOfSucceed.doubleValue() / numberOfAllSearches.doubleValue()) + ";\n"
                );

                int maxVal = Math.max(gwMap.lastKey(), degreeMap.lastKey());
                for (int i = 1; i <= maxVal; i++) {
                    fw.write(i + ";"
                            + String.format("%,f", (double) gwMap.getOrDefault(i, 0) / numberOfAllSearches.doubleValue()) + ";"
                            + String.format("%,f", (double) succeedMap.getOrDefault(i, 0) / numberOfAllSearches.doubleValue()) + ";"
                            + String.format("%,f", (double) spMap.getOrDefault(i, 0) / +allSpNumber) + ";"
                            + String.format("%,f", (double) degreeMap.getOrDefault(i, 0) / allDegreeNumber) + ";"
                    );

                    if (i == 0) {
                        fw.write(
                                String.format("%,f", avgClusteringCoeff.doubleValue() / maxExpNumber) + ";"
                                + String.format("%,f", (double) numberOfSucceed.doubleValue() / numberOfAllSearches.doubleValue()) + ";\n"
                        );
                    } else {
                        fw.write(
                                ";"
                                + ";\n"
                        );
                    }

                }

                fw.close();
            }
        }
    }
}
