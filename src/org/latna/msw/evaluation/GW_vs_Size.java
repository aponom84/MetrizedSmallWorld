package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.TestLib;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.euclidian.EuclidianFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class GW_vs_Size {

    //public static final String FLD = "./gw_vs_size/";
    public static String FLD = "";
    public static String outFileName;

    public static void main(String[] args) throws IOException {
        int attemptsNumber = 10;
        
        int[] checkPoints = {1000, 5000, 10000, 50000, 100000, 500000, 1000000};
        //int[] checkPoints = {1000, 5000, 10000};
        
        //int dbSize = 100000;
        //int dbSize = 1000;

        //FLD = args[0];
        int dim = Integer.parseInt(args[0]);
        int allNN [] = new int [args.length-1];
        
        for (int i = 1; i < args.length; i++) 
            allNN[i-1] = Integer.parseInt(args[i]);
                
        //int[] allDim = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        //int nn = 2; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        //int[] allNN = {2, 3, 4, 5, 6, 10, 15, 20, 25, 30, 40};
        //int[] allNN = {2};
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 20; //number of search attempts used during the contruction of the structure
        int querySetSize = 1000; //the restriction on the number of queries. To set no restriction set value = 0
        //int testSeqSize = 100; //number elements in the random selected subset used to verify accuracy of the search. 
        int maxExpNumber = 20;

        //AbstractMetricStructure db = new ErdeshRandomGraph( (double) nn * 2.0 / (double) (checkPoints[0]-1));
        //for (int dim : allDim) {
            for (int nn : allNN) {
                MetrizedSmallWorld dbs[] = new MetrizedSmallWorld[maxExpNumber];

                IntStream.range(0, maxExpNumber).forEach(i -> {
                    dbs[i] = new MetrizedSmallWorld();
                    dbs[i].setNN(nn);
                    dbs[i].setInitAttempts(initAttempts);
                });

                outFileName = FLD + "GW_vs_Size_dim=" + dim + "nn=" + nn + "exp=" + maxExpNumber + "initA=" + initAttempts + ".csv";
                FileWriter fw = new FileWriter(new File(outFileName));
                //fw.write("size;avg_scanned;avg_gw_path_len;avg_gw_succeed_path_len;avg_sp_path_len;succeed_ratio;avg_clustering;\n");
                fw.write("size;avg_scanned;avg_gw_path_len;avg_gw_succeed_path_len;avg_sp_path_len;" 
                    + IntStream.range(0, attemptsNumber).mapToObj(i-> String.format("succeed_ratio_%d",i)).collect(Collectors.joining(";")) + ";" 
                    + IntStream.range(0, attemptsNumber).mapToObj(i-> String.format("joint_sc_ratio_%d",i)).collect(Collectors.joining(";")) 
                    + ";avg_succeed_ratio;avg_clustering;\n");
                
                for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
                    int dbSize = checkPoints[stageNumber] - dbs[0].getSize();
                    System.out.println("dim=" + dim + " nn=" + nn + " expNumber=" + maxExpNumber + " k=" + k + " initAttemps=" + initAttempts + " minAttempts="
                            + " dataBaseSize=" + dbSize + " querySetSize=" + querySetSize );

                    DoubleAdder avgClusteringCoeff = new DoubleAdder();
                    AtomicLong numberOfJointSucceed[] = new AtomicLong [attemptsNumber];
                    AtomicLong numberOfSucceed[] = new AtomicLong [attemptsNumber];
                    AtomicLong numberOfSearches[] = new AtomicLong [attemptsNumber];
                    AtomicLong numberOfJointScanned[] = new AtomicLong [attemptsNumber];
                   
                    for (int i=0; i < attemptsNumber; i++ ) {
                        numberOfSucceed[i] = new AtomicLong();
                        numberOfSearches[i] = new AtomicLong();
                        numberOfJointSucceed[i] = new AtomicLong();
                        numberOfJointScanned[i] = new AtomicLong();
                    }
                    
                    AtomicLong numberOfAllSearches = new AtomicLong(0);
                    AtomicLong allSpNumber = new AtomicLong(0);
                    AtomicLong sum_gw_path_len = new AtomicLong(0);
                    AtomicLong sum_gw_succeed_path_len = new AtomicLong(0);
                    AtomicLong sum_sp_path_len = new AtomicLong(0);

                    AtomicLong sum_number_of_scanned = new AtomicLong(0);
                    AtomicLong sum_number_of_scanned_in_good = new AtomicLong(0);
                   

                    IntStream.range(0, maxExpNumber).parallel().forEach(i -> {
                         
                        EuclidianFactory ef = new EuclidianFactory(dim, dbSize);
                        EuclidianFactory testQueryFactory = new EuclidianFactory(dim, querySetSize);

                        Stream.generate(ef).limit(dbSize).forEach((me) -> {
                            dbs[i].add(me);
                        });

                        Map<MetricElement, List<TestLib.EvaluationResult>> res = TestLib.evaluateSearchAlgorithm(dbs[i].getElements(), testQueryFactory, querySetSize, attemptsNumber);

                        for (List<TestLib.EvaluationResult> l : res.values()) {
                            int j = 0;
                            boolean found = false;
                            for (TestLib.EvaluationResult r : l) {
                                if ( (l.indexOf(r) < 0) || (l.indexOf(r) >= attemptsNumber) ) {
                                    System.out.println("Ko-ko-ko!");
                                        continue;
                                }
                                numberOfAllSearches.incrementAndGet();
                                numberOfSearches[j].incrementAndGet();

                                sum_gw_path_len.addAndGet(r.greedyWalkPathLenght - 1);
                                sum_number_of_scanned.addAndGet(r.numberOfScanned);
                                if (r.graphDistance == 0) {
                                    found = true;
                                    numberOfSucceed[j].incrementAndGet();
                                    sum_gw_succeed_path_len.addAndGet(r.greedyWalkPathLenght - 1);
                                    sum_number_of_scanned_in_good.addAndGet(r.numberOfScanned);
                                };
                                if (found) numberOfJointSucceed[j].incrementAndGet();
                                j++;
                            }
                        }

                        testQueryFactory.getElements().stream().limit(10).forEach((MetricElement q) -> {
                            TestLib.getShortestPathValueMap(dbs[i].getElements(),
                                    TestLib.getKCorrectElements(dbs[i].getElements(), q, 1).first().getMetricElement())
                                    .values().stream().forEach((v) -> {
                                        sum_sp_path_len.addAndGet(v);
                                        allSpNumber.incrementAndGet();
                                    });
                        });

                        avgClusteringCoeff.add(TestLib.getAvgClusteringCoeff(dbs[i].getElements()));

                    });
                    
                    double totalSucceed = Arrays.stream(numberOfSucceed).mapToDouble(nmSc->nmSc.doubleValue()).sum();

                    fw.write(dbs[0].getSize() + ";"
                            + String.format("%,f", sum_number_of_scanned.doubleValue() / numberOfAllSearches.doubleValue()) + ";"
                            + String.format("%,f", sum_gw_path_len.doubleValue() / numberOfAllSearches.doubleValue()) + ";"
                            + String.format("%,f", sum_gw_succeed_path_len.doubleValue() / totalSucceed) + ";"
                            + String.format("%,f", sum_sp_path_len.doubleValue() / allSpNumber.doubleValue()) + ";"
                            //numberOfSucceed
                             //+ String.format("%,f", numberOfSucceed.doubleValue() / numberOfAllSearches.doubleValue()) + ";"
                            /*
                            Arrays.stream(numberOfSucceed).map(
                                    nmSc-> String.format("%,f", nmSc.doubleValue() / numberOfAllSearches.doubleValue())
                            ).collect(Collectors.joining(";"))  + ";"  */
                            + IntStream.range(0, attemptsNumber)
                                    .mapToObj(i-> String.format("%,f", numberOfSucceed[i].doubleValue() / numberOfSearches[i].doubleValue()))
                                    .collect(Collectors.joining(";")) + ";"
                            + IntStream.range(0, attemptsNumber)
                                    .mapToObj(i-> String.format("%,f", numberOfJointSucceed[i].doubleValue() / numberOfSearches[i].doubleValue()))
                                    .collect(Collectors.joining(";")) + ";"
                    
                            + String.format("%,f", totalSucceed / numberOfAllSearches.doubleValue()  ) + ";"
                            + String.format("%,f", avgClusteringCoeff.doubleValue() / maxExpNumber) + ";\n"
                    );

                    fw.flush();
                }
                fw.close();
            }
        //}
    }
}
