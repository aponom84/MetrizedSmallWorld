package org.latna.msw.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetricElement;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.SearchResult;
import org.latna.msw.SelfAdapted.Repair02;
import org.latna.msw.SelfAdapted.SelfAdaptedMetrizedSmallWorld;
import org.latna.msw.TestLib;
import org.latna.msw.TestResult;
import org.latna.msw.euclidian.EuclidianFactory;

/**
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class ReparingByQueriesTest {
    public static final int NUMBER_OF_THREADS = 12;
    public static final String outFileName = "SelfAdapted_DimText.txt";
    public static final String degreeDistributionFileName = "SelfAdapted_DegreeDistribution.txt";
    
    static int dimensionality = 30;
    static int k = 5; //number of k-closest elements for the knn search
    //static int dataBaseSize = 1000;
    //static  int dataBaseSize = 0; the restriction on the number of elements in the data structure. To set no restriction set value = 0
    static int querySetSize = 500; //the restriction on the number of quleries. To set no restriction set value = 0
    static int testSeqSize = 300; //number elements in the random selected subset used to verify accuracy of the search. 
    static int elementNumber = 0;
    static int minAttempts = 1;
    static int maxAttempts = 20;
    static int succeedThreshould = 9;
    static int numberOfQueriesForReparing = 10000;
    static int reparingTimes = 100;
    /**
     * Scans input string and runs the test
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
         /*int nn = Integer.valueOf(args[0]);
        int k = Integer.valueOf(args[1]);
        int initAttempts = Integer.valueOf(args[2]);
        int minAttempts = Integer.valueOf(args[3]);
        int maxAttempts = Integer.valueOf(args[4]);
        int dataBaseSize = Integer.valueOf(args[5]);
        int querySetSize = Integer.valueOf(args[6]);
        int testSeqSize = Integer.valueOf(args[7]);
        String dataPath = args[8];
        String queryPath = args[9];
      */  
        //int[] checkPoints = {5000,50000,500000,5000000};
       // int[] checkPoints = {1000,5000, 10000, 50000,100000,500000, 1000000,2000000,5000000,10000000,20000000, 50000000}; 
       // int[] checkPoints = {1,2,3,4,5,6,7,8,9,10,20, 100, 1000, 5000, 10000, 50000,100000,500000, 1000000};
        //int[] checkPoints = {1,2,3,4,5,6,7,8,9};
        int[] checkPoints = {100000}; 

         
        //SelfAdaptedMetrizedSmallWorld db = new SelfAdaptedMetrizedSmallWorld(succeedThreshould);
        //AbstractMetricStructure db = new Ranger(succeedThreshould);
       // Repair02 db = new Repair02(succeedThreshould);
        MetrizedSmallWorld db = new MetrizedSmallWorld();
        db.setInitAttempts(10);
        db.setNN(9);
        
        Map<MetricElement, TreeSet<EvaluatedElement>> rightResultMap = new HashMap<MetricElement, TreeSet<EvaluatedElement>>();;
        EuclidianFactory testQueryFactory;
        ArrayList<MetricElement> testQueries = new ArrayList();

        
        System.out.println("Algorithms version: " + db.toString());
            
        for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
            FileWriter fw = new FileWriter(new File(outFileName), true);

            System.out.println("The first stage\n");
            System.out.println(" k=" + k + " + " + "dataBaseSize=" + checkPoints[stageNumber] + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize);

            EuclidianFactory ef = new EuclidianFactory(dimensionality, checkPoints[stageNumber]-db.getSize());
            testQueryFactory = new EuclidianFactory(dimensionality, testSeqSize);
             testQueries = new ArrayList();

            //ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            
            System.out.println("Factory Size: " + ef.getElements().size());
           
            
            for (MetricElement me : ef.getElements()) {
              //------
                //executorAdder.submit(new Adding(db, me, elementNumber));
                //elementNumber++;
                db.add(me);
                //System.out.println("Number of links added: " + me.getAllFriends().size());
            }
             
            //shutdownAndAwaitTermination(executorAdder);
            
            rightResultMap = new HashMap<MetricElement, TreeSet<EvaluatedElement>>();

            System.out.println("The second stage");
            for (MetricElement newQuery : testQueryFactory.getElements()) {
                testQueries.add(newQuery);
                rightResultMap.put(newQuery, TestLib.getKCorrectElements(db.getElements(), newQuery, k));
            }

            System.out.println("The third stage");
            System.out.println("Structure size: " + db.getElements().size());
            
            String s = " k=" + k + " minAttempts=" + minAttempts + 
                    " maxAttempts=" + String.valueOf(maxAttempts) + 
                    " dataBaseSize=" + String.valueOf(db.getSize()) + 
                    " querySetSize=" + String.valueOf(querySetSize) + 
                    " testSeqSize=" + String.valueOf(testSeqSize);
            System.out.println(s);
            fw.append(s+'\n');

            Random random = new Random();

            for (int a = minAttempts; a <= maxAttempts; a++) {
                ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
                List<Future<TestResult>> searchResultList = new ArrayList<Future<TestResult>>();

                int good = 0;
                long scanned = 0;
                int steps = 0;
                for (int i = 0; i < testSeqSize; i++) {
                    MetricElement testQ = testQueries.get(random.nextInt(testQueries.size()));

                    Callable testSearcher = new MyCallable(db, testQ, rightResultMap.get(testQ), a, k);

                    Future<TestResult> submit = executor.submit(testSearcher);
                    searchResultList.add(submit);
                }

                for (Future<TestResult> future : searchResultList) {
                    try {
                        TestResult tr = future.get();
                        good += tr.getRightResutls();
                        scanned += tr.getScannedNumber();
                        steps += tr.getSteps();

                    } catch (InterruptedException e) {
                        throw new Error(e);
                    } catch (ExecutionException e) {
                        throw new Error(e);
                    }
                }
                executor.shutdown();

                double recall = ((double) good) / ((double) testSeqSize * k);
                double scannedPercent = ((double) (scanned)) / ((double) db.getElements().size() * (double) testSeqSize);
                s = "K = " + k + " Attepts = " + a + "\trecall = " + recall + 
                        "\tScanedPercent = " + scannedPercent + "\tAvg Scanned\t" + ((double) scanned / (double) testSeqSize) + 
                        "\tAvg Steps\t" + ((double) steps / (double) testSeqSize) + 
                        "\n";
                System.out.print(s); 
                fw.append(s);
            }
            
            TestLib.saveStatisticToFile(degreeDistributionFileName, db);
            
            fw.close();
        }   
        
        
        
        //--------------Reparing!!!!!!----------------------------------------------------
            FileWriter fw = new FileWriter(new File(outFileName), true);
            System.out.println("Starting reparing!!!\n ReparingQueries:\t"  + numberOfQueriesForReparing);
            fw.append("Starting reparing!!!\n ReparingQueries:\t"  + numberOfQueriesForReparing + "\n");
            
            fw.close();
            
            String s; 
            Random random = new Random();
            
            
            for (int time = 0; time < reparingTimes; time++) {
                fw = new FileWriter(new File(outFileName), true);
                
                EuclidianFactory reparingTestQueryFactory = new EuclidianFactory(dimensionality, numberOfQueriesForReparing);
                int numberOfAddedLink = 0;    
                for (MetricElement query: reparingTestQueryFactory.getElements()) {
                    numberOfAddedLink = numberOfAddedLink + db.repair(query, succeedThreshould);
                }
                                
                System.out.print("Time: " + time + " Links Added: " + numberOfAddedLink + "\t");
                fw.append("Time:\t" + time + " Links Added:\t" + numberOfAddedLink  + "\t");
                
                
                
                //-----------------------------test-------------------------

                for (int a = minAttempts; a <= maxAttempts; a++) {
                    ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
                    List<Future<TestResult>> searchResultList = new ArrayList<Future<TestResult>>();

                    int good = 0;
                    long scanned = 0;
                    int steps = 0;
                    for (int i = 0; i < testSeqSize; i++) {
                        MetricElement testQ = testQueries.get(random.nextInt(testQueries.size()));

                        Callable testSearcher = new MyCallable(db, testQ, rightResultMap.get(testQ), a, k);

                        Future<TestResult> submit = executor.submit(testSearcher);
                        searchResultList.add(submit);
                    }

                    for (Future<TestResult> future : searchResultList) {
                        try {
                            TestResult tr = future.get();
                            good += tr.getRightResutls();
                            scanned += tr.getScannedNumber();
                            steps += tr.getSteps();

                        } catch (InterruptedException e) {
                            throw new Error(e);
                        } catch (ExecutionException e) {
                            throw new Error(e);
                        }
                    }
                    executor.shutdown();

                    double recall = ((double) good) / ((double) testSeqSize * k);
                    double scannedPercent = ((double) (scanned)) / ((double) db.getElements().size() * (double) testSeqSize);
                    s = "recall\t" + recall + 
                            "\tScanedPerc\t" + scannedPercent + "\tAvgScanned\t" + ((double) scanned / (double) testSeqSize) + 
                            "\tAvgSteps\t" + ((double) steps / (double) testSeqSize + "\t");
                    System.out.print(s); 
                    fw.append(s);
                
                
                //-----------------------------test--------------------------
                
                
               
            }
            System.out.println();
            fw.append("\n");
            fw.close();
            
        } //reparing time
        
    }
    
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
       // System.out.print("shutdownAndAwaitTermination");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            while (!pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                // System.out.prin?tln("Waiting for executor termination");
            }
            //pool.shutdown();
            pool.shutdownNow();
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static class Adding extends Thread {
        private SelfAdaptedMetrizedSmallWorld db;
        private MetricElement newElement; 
        private int  elementNumber;
        
        public Adding(SelfAdaptedMetrizedSmallWorld db, MetricElement newElement, int elementNumber) {
            this.db = db;
            this.newElement = newElement;
            this.elementNumber = elementNumber;
        }

        public void run() {
            /*
              synchronized (db){
                System.out.println(">"+elementNumber+"<");
              }
              */
            db.add(newElement);
            synchronized (db) {
                //System.out.println("["+ elementNumber + "]");
                System.out.println("Number of links added: " + newElement.getAllFriends().size());
            }
               
        }
        
    }
    
    public static class MyCallable implements Callable<TestResult> {
        private AbstractMetricStructure db;
        private MetricElement testQ;
        TreeSet <EvaluatedElement> answer;
        private int attempts;
        int k;
        public MyCallable(AbstractMetricStructure db, MetricElement testQ, TreeSet <EvaluatedElement> answer,int attempts, int k) {
            this.db = db;
            this.testQ = testQ;
            this.answer = answer;
            this.attempts = attempts;
            this.k = k;
        }
        @Override
        public TestResult call() throws Exception {
            SearchResult result = db.knnSearch(testQ, k, attempts);
            int mySteps = result.getVisitedSet().size();
            int good=0;
            for (EvaluatedElement ee: result.getViewedList()) {
                if (answer.contains(ee)) good++;
            }

            return new TestResult(good, result.getViewedList().size(), result.getVisitedSet().size(),  result.getVisitedSet());

        }
    }

}
