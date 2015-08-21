package org.latna.msw;

import org.latna.msw.documents.DocumentFileFactory;
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

/**
 * This is the simple example of building MetrizedSmallWorld under the trec-3 collection data set.
 * Any document or query is represented by term frequency vector.
 * The test is divided into the three stages.
 * 
 * The first stage - reading data set to the memory (documents and queries), configuring algorithms and building the data structure.   
 * 
 * The second stage - to obtain the true nearest neighbor element for every query by the use sequenteally scan
 * 
 * The third stage - search in the metric data structure with several number of attemps, 
 * validate the search ressults.
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class PowerLawTestTrec3 {
    /**
     * Scans input string and runs the test
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int nn = 7; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 5; //number of search attempts used during the contruction of the structure
        int minAttempts = 1 ; //minimum number of attempts used during the test search 
        int maxAttempts = 10; //maximum number of attempts 
  //      int dataBaseSize = 1000; 
        int dataBaseSize = 0; //the restriction on the number of elements in the data structure. To set no restriction set value = 0
        int querySetSize = 50; //the restriction on the number of quleries. To set no restriction set value = 0
        int testSeqSize = 30; //number elements in the random selected subset used to verify accuracy of the search. 
        String dataPath = "C:/msw/MetricSpaceLibrary/dbs/documents/short"; //Path to directory documents from Trec-3 collection. See Metric Space Library 
        String queryPath = "C:/msw/MetricSpaceLibrary/dbs/documents/shortQueries"; //Path to queries 
       

       /* int nn = Integer.valueOf(args[0]);
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
    
        System.out.println("nn=" + nn + " k=" + k + " initAttemps="+initAttempts + " minAttempts=" + minAttempts + " maxAttempts=" +
                maxAttempts + " dataBaseSize=" + dataBaseSize + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize);

        

        MetricElementFactory elementFactory= new DocumentFileFactory();
        elementFactory.setParameterString("dir="+dataPath+"; maxDoc="+dataBaseSize);
        MetricElementFactory testQueryFactory = new DocumentFileFactory();
        testQueryFactory.setParameterString("dir="+queryPath+"; maxDoc="+querySetSize);
        ArrayList <MetricElement> testQueries = new ArrayList();

        MetrizedSmallWorld db = new MetrizedSmallWorld();
      //  OriginalDataBase db = new OriginalDataBase();

        System.out.println("Algorithms version: "+ db.toString());

        db.setNN(nn);
        db.setInitAttempts(initAttempts);
        
        
        System.out.println("The first stage. Data structure building. \n");
        int j = 0;

        for (MetricElement me: elementFactory.getElements()) {
            db.add(me);
           // System.out.println(j+" ");
            j++;
        }
        
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
            
            int degrees[] = TestLib.getDegreeDistribution(db.getElements()); 
            
            for (int degree  = 0; degree < degrees.length; degree++) {
                System.out.println(String.valueOf(degree) + ";" + degrees[degree] + ";");
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
            int good=0;
            for (EvaluatedElement ee: result.getViewedList()) {
                if (answer.contains(ee)) good++;
            }

            return new TestResult(good, result.getViewedList().size(), result.getSteps(), result.getVisitedSet());

        }
    }

}
