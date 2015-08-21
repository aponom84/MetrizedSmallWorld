package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.MaskValidator;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.AlgorithmLib;
import org.latna.msw.euclidian.EuclidianFactory;
import org.latna.msw.wikisparse.WikiSparse;
import org.latna.msw.wikisparse.WikiSparseFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * V02.05.2014
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class WikiSparseAggregationTest {
    public static final int NUMBER_OF_THREADS = 4;
    public static final String outFileName = "WikiSparceAggregate.txt";
    
    
   /* 
    public static SearchResult aggregateSearch(AbstractMetricStructure db, WikiSparse query, int attempts, int exactNumberOfAnswers) {
        int foundAnswers = 0;
        SearchResult res = db.knnSearch(query, 1, attempts);
        
        EvaluatedElement start = res.getViewedList().first();
        
        HashSet <MetricElement> viewedSet = new HashSet <MetricElement> ();
        Set visitedSet = new HashSet <MetricElement>();
        TreeSet <EvaluatedElement> candidateSet = new TreeSet();
        
        List <MetricElement> newList;
        
        candidateSet.add(start);
        viewedSet.add(start.getMetricElement());
        
        boolean stillContatinsCorrespoundAnswer = false;
        
        while (!candidateSet.isEmpty()) {
            EvaluatedElement currEv = candidateSet.first();
            candidateSet.remove(currEv);
            
            stillContatinsCorrespoundAnswer = false;
            
            for (MetricElement e: currEv.getMetricElement().getAllFriends()) {
                if (!viewedSet.contains(e)) {
                    viewedSet.add(e);
                    
                    WikiSparse ws = (WikiSparse) e;
                    
                    if ( ws.correspoundToMask(query) ) {
                        foundAnswers++;
                        candidateSet.add(new EvaluatedElement(e.calcDistance(query), e));
                    }
                    
                    
                }   
            }      
            System.out.println("Scaned: " + viewedSet.size() + " found: " + String.valueOf( (double) foundAnswers / (double) exactNumberOfAnswers )  );
            
        }
        
        
        return res;
    }
    */
    
    
    /**
     * Scan input string and run the test
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
      //  int[] checkPoints = {1000, 5000, 10000, 50000, 100000, 500000};
          int[] checkPoints = {1000, 2000, 3000, 4000, 5000, 10000, 20000, 30000, 40000, 50000, 100000, 500000};
        
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
        int querySetSize = 10; //the restriction on the number of quleries. To set no restriction set value = 0
        //number elements in the random selected subset used to verify accuracy of the search. 
        int elementNumber = 0;
        int lastSize = 0; 
         
     //   WikiSparse    Factory factory = new WikiSparseFactory("C:\\Users\\Aponom\\wikipedia.txt");
        WikiSparseFactory factory = new WikiSparseFactory("E:\\WikiData\\wikipedia.txt");
        
        WikiSparseFactory queryFactory = new WikiSparseFactory("");
        
        MetrizedSmallWorld db = new MetrizedSmallWorld();
        db.setNN(nn);
        db.setInitAttempts(initAttempts);
 
        List <MetricElement> testQueries = queryFactory.getShortQueries(querySetSize, 2, 10000);
        Integer [] exactNumbers = new Integer[testQueries.size()];
        
        for (int i=0; i < exactNumbers.length; i++) 
            exactNumbers[i] = new Integer(0);
            
        
        MaskValidator maskValidator = new MaskValidator() {

            public boolean validate(MetricElement element, MetricElement query) {
                WikiSparse wikiElement, wikiQuery;
                wikiElement = (WikiSparse) element;
                wikiQuery = (WikiSparse) query;
                return wikiElement.correspoundToMask(wikiQuery);
            }
        };
        
        System.out.println("Algorithms version: " + db.toString());
            
        for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
            FileWriter fw = new FileWriter(new File(outFileName), true);

            System.out.println("The first stage\n");
            System.out.println("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + checkPoints[stageNumber] + " querySetSize=" + querySetSize );
            
            ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            
            int numberOfNewElements = checkPoints[stageNumber]-db.getSize();
            
            for (int i = 0; i < numberOfNewElements; i++ ) 
            {
                MetricElement me = factory.getElement();
                if (me == null) break;
                executorAdder.submit(new Adding(db, me, elementNumber, maskValidator, testQueries, exactNumbers));
                elementNumber++;
            }
            
            shutdownAndAwaitTermination(executorAdder);
            
            System.out.println("The second stage");
            System.out.println("Elements Array Size: " + db.getElements().size());

            System.out.println("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + db.getSize() + " querySetSize=" + querySetSize);

            fw.append("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + db.getSize() + " querySetSize=" + querySetSize + '\n');

            int good = 0;
            long scanned = 0;
            long steps = 0;

            int i = 0;
            for (MetricElement query: testQueries) {
                if (exactNumbers[i] > 0) {
                    AlgorithmLib.aggregateSearch3(db, query, maxAttempts, exactNumbers[i], maskValidator);
                }
                i++;
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
        private MaskValidator maskValidator;
        private List <MetricElement> queries;
        private Integer [] exactNumbers;
        
        public Adding(MetrizedSmallWorld db, MetricElement newElement, int elementNumber, MaskValidator maskValidators, List <MetricElement> queries , Integer[] exactNumber) {
            this.db = db;
            this.newElement = newElement;
            this.elementNumber = elementNumber;
            this.maskValidator = maskValidators;
            this.queries = queries;
            this.exactNumbers = exactNumber;
        }

        @Override
        public void run() {
            db.add(newElement);
            int i=0;  
            for (MetricElement query: queries) {
                if (maskValidator.validate(newElement, query)) {
                    synchronized (exactNumbers[i]) {
                        exactNumbers[i]++;
                    }
                }
                i++;    
            }
            /*  synchronized (db){
                System.out.println("["+ elementNumber + "]");
              }
              */ 
        }
        
    }

}
