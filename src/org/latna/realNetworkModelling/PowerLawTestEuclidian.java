
package org.latna.realNetworkModelling;

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
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import org.latna.msw.MetrizedSmallWorld_PowerLaw;
import org.latna.msw.SearchResult;
import org.latna.msw.TestLib;
import static org.latna.msw.TestLib.out;
import static org.latna.msw.TestLib.shutdownAndAwaitTermination;
import org.latna.msw.TestResult;
import org.latna.msw.euclidian.EuclidianFactory;
import org.latna.msw.evaluation.DimensionalityTest;
/**
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class PowerLawTestEuclidian {
    /**
     * Scans input string and runs the test
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        int NUMBER_OF_THREADS  = 1;
        int size = 100000;
        int nn = 5; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 5; //number of search attempts used during the contruction of the structure
        int randomWalksNumber = 5;
        int randomWalksLenght = 10;
        int dim = 2;
        
        
        int minAttempts = 1 ; //minimum number of attempts used during the test search 
        int maxAttempts = 20; //maximum number of attempts 
        int querySetSize = 50; //the restriction on the number of quleries. To set no restriction set value = 0
        int testSeqSize = 30; //number elements in the random selected subset used to verify accuracy of the search. 
        String dataPath = "C:/msw/MetricSpaceLibrary/dbs/documents/short"; //Path to directory documents from Trec-3 collection. See Metric Space Library 
        String queryPath = "C:/msw/MetricSpaceLibrary/dbs/documents/shortQueries"; //Path to queries 
        String degreeDistributionFileName = "PowerLaw_MSW_Degrees.txt";
        String outputFileName = "PowerLaw_MSW_output.txt";
        
        FileWriter fw = new FileWriter(outputFileName, true);
       
        
        out("nn=" + nn + " k=" + k + " initAttemps="+initAttempts + " minAttempts=" + minAttempts + " maxAttempts=" +
                maxAttempts + " dataBaseSize=" + size + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize , fw);

        MetricElementFactory testQueryFactory = new EuclidianFactory(dim, 1000);
        ArrayList <MetricElement> testQueries = new ArrayList();

        MetrizedSmallWorld_PowerLaw db = new MetrizedSmallWorld_PowerLaw(nn, initAttempts, randomWalksNumber, randomWalksLenght);

        
        
        
        
        
        
        System.out.println("The first stage. Data structure building. \n");
        ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        //EuclidianFactory ef = new EuclidianFactory(dim, size);
        System.out.println("Size: " + size);
        

        for (int elementNumber = 1; elementNumber <= size; elementNumber++) {
            //------
            executorAdder.submit(new DimensionalityTest.Adding(db, EuclidianFactory.getRandomElement(dim), elementNumber));
        }
        shutdownAndAwaitTermination(executorAdder);
        
        Map <MetricElement, TreeSet <EvaluatedElement>> rightResultMap = new HashMap<MetricElement,  TreeSet <EvaluatedElement>> ();

        System.out.println("The second stage");
        for (MetricElement newQuery: testQueryFactory.getElements()) {
            testQueries.add(newQuery);
            rightResultMap.put(newQuery,  TestLib.getKCorrectElements(db.getElements(), newQuery, k));
        }

        System.out.println("The third stage");

        Random random = new Random(108);
        
        for (int a = minAttempts; a <= maxAttempts; a++) {
             ExecutorService executor = Executors.newFixedThreadPool(10);
             List<Future<TestResult>> searchResultList = new ArrayList<Future<TestResult>>();

            int good = 0;
            long scanned = 0;
            for (int i = 0; i < testSeqSize; i++) {
                MetricElement testQ =  testQueries.get(random.nextInt(testQueries.size()));

                Callable testSearcher = new MyCallable(db, testQ, rightResultMap.get(testQ), a, k);

                Future<TestResult> submit = executor.submit(testSearcher);
                searchResultList.add(submit);
            }

            for (Future<TestResult> future : searchResultList) {
                try {
                        TestResult tr = future.get();
                        good += tr.getRightResutls();
                        scanned+=tr.getScannedNumber();

                } catch (InterruptedException e) {
                    throw new Error(e);
                } catch (ExecutionException e) {
                    throw new Error(e);
                }
            }
            executor.shutdown();

            double recall = ((double)good)/((double)testSeqSize *k);
            double scannedPercent =  ((double)(scanned)) /((double)db.getElements().size() * (double)testSeqSize);
            System.out.print("K = "+k+" Attepts = " + a+ "\trecall = "+recall + "\tScaned% = " + scannedPercent + "\n");
            
        } //different attempts
        
        out("AvgClusteringCoeff: " + String.valueOf(TestLib.getAvgClusteringCoeff(db.getElements())) + "\n", fw);
        out("AvgSecondOrderDegree: " + String.valueOf(TestLib.getAvgSecondDegree(db.getElements()))  + "\n", fw);
        //TestLib.
        
        fw.close();
        
        TestLib.saveStatisticToFile(degreeDistributionFileName, db);

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

            return new TestResult(good, result.getViewedList().size(), result.getSteps(), result.getVisitedSet());

        }
    }

}
