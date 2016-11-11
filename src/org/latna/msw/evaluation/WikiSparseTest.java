package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;
import org.latna.msw.TestLib;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.TestResult;
import org.latna.msw.euclidian.EuclidianFactory;
import org.latna.msw.wikisparse.WikiSparseFactory;
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

/**
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class WikiSparseTest {
    public static final int NUMBER_OF_THREADS = 4;
    public static final String outFileName = "WikiSparce.txt";
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
        int[] checkPoints = {1000,5000,10000,50000,100000, 500000};
        
       // int[] checkPoints = {1000,5000, 10000, 50000,100000,500000, 1000000,2000000,5000000,10000000,20000000, 50000000}; 
      //  int[] checkPoints = {1000}; 
      //  int dimensionality = 30;
        int nn = 20; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 4; //number of search attempts used during the contruction of the structure
        int minAttempts = 1; //minimum number of attempts used during the test search 
        int maxAttempts = 10; //maximum number of attempts 
        //int dataBaseSize = 1000;
        // int dataBaseSize = 0; the restriction on the number of elements in the data structure. To set no restriction set value = 0
        int querySetSize = 100; //the restriction on the number of quleries. To set no restriction set value = 0
        //number elements in the random selected subset used to verify accuracy of the search. 
        int elementNumber = 0;
        int lastSize = 0; 
         
        WikiSparseFactory factory = new WikiSparseFactory("E:\\WikiData\\wikipedia.txt");
        
        
        MetrizedSmallWorld db = new MetrizedSmallWorld();
        db.setNN(nn);
        db.setInitAttempts(initAttempts);
 
        List <MetricElement> testQueries = new ArrayList();

        testQueries = factory.getShortQueriesReal(querySetSize, 4);
        //testQueries = factory.getElements(querySetSize);
      /*  for (MetricElement newQuery : factory.getElements(querySetSize)) {
                testQueries.add(newQuery);
                
        }
        
        */

        System.out.println("Algorithms version: " + db.toString());
            
        for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
            FileWriter fw = new FileWriter(new File(outFileName), true);

            System.out.println("The first stage\n");
            System.out.println("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + checkPoints[stageNumber] + " querySetSize=" + querySetSize );

            
            

            ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            
            //System.out.println("Factory Size: " + ef.getElements().size());
           
            /*
            for (MetricElement me : factory.getElements(checkPoints[stageNumber]-db.getSize())) {
                executorAdder.submit(new Adding(db, me, elementNumber));
                elementNumber++;
            }*/
            
            int numberOfElementsToAdd  = checkPoints[stageNumber]-db.getSize();
            
            for (int i = 0; i < numberOfElementsToAdd; i++ ) 
            {
                MetricElement me = factory.getElement();
                if (me == null) break;
                executorAdder.submit(new Adding(db, me, elementNumber));
                elementNumber++;
            }
            
             
            shutdownAndAwaitTermination(executorAdder);
            
            System.out.println("The second stage");
                        
            Map<MetricElement, TreeSet<EvaluatedElement>> rightResultMap = new HashMap<MetricElement, TreeSet<EvaluatedElement>>();
            for (MetricElement query : testQueries) {     
                rightResultMap.put(query, TestLib.getKCorrectElements(db.getElements(), query, k));
            }

            System.out.println("The third stage");
            
            System.out.println("Elements Array Size: " + db.getElements().size());

            System.out.println("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + db.getSize() + " querySetSize=" + querySetSize);

            fw.append("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + db.getSize() + " querySetSize=" + querySetSize + '\n');

            for (int a = minAttempts; a <= maxAttempts; a++) {
                ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
                //ExecutorService executor = Executors.newFixedThreadPool(48);
                List<Future<TestResult>> searchResultList = new ArrayList<Future<TestResult>>();

                int good = 0;
                long scanned = 0;
                long steps = 0;
                for (MetricElement testQ : testQueries) {
                    
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
                        
                      //  System.out.println("Scanned = " + tr.getScannedNumber());
                      //  System.out.println("Scanned total= " + scanned );

                    } catch (InterruptedException e) {
                        throw new Error(e);
                    } catch (ExecutionException e) {
                        throw new Error(e);
                    }
                }
                executor.shutdown();

                double recall = ((double) good) / ((double) querySetSize * k);
                double scannedPercent = ((double) (scanned)) / ((double) db.getElements().size() * (double) querySetSize);
                System.out.print("NN=" + nn + "K = " + k + " Attepts = " + a + "\trecall = " + recall + "\tScanedPercent = " + scannedPercent + "\tAvg Scanned\t" + ((double) scanned / (double) querySetSize) + "\n");
                fw.append("NN " + nn + " K " + k + " Attepts " + a + " recall " + recall + " ScanedPercent " + scannedPercent + " AvgScanned " + ((double) scanned / (double) querySetSize) + " AvgSteps " + ((double) steps / (double) querySetSize) + "\n");
            }
            fw.close();
        }
        
    }
    
    private static void shutdownAndAwaitTermination(ExecutorService pool) {
       // System.out.print("shutdownAndAwaitTermination");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            while (!pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                // System.out.println("Waiting for executor termination");
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
        private MetrizedSmallWorld db;
        private MetricElement newElement; 
        private int  elementNumber;
        
        public Adding(MetrizedSmallWorld db, MetricElement newElement, int elementNumber) {
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
            /*  synchronized (db){
                System.out.println("["+ elementNumber + "]");
              }
              */ 
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
            int good=0;
            for (EvaluatedElement ee: result.getViewedList()) {
                if (answer.contains(ee)) good++;
            }

            return new TestResult(good, result.getViewedList().size(), result.getVisitedSet().size(), result.getVisitedSet());

        }
    }

}
