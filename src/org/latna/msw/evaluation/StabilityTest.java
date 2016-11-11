package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;
import org.latna.msw.TestLib;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetrizedSmallWorld;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.TestResult;
import org.latna.msw.euclidian.EuclidianFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class StabilityTest {

    public static final int NUMBER_OF_THREADS = 48;

    public static void main(String[] args) throws IOException {
        int numberOfGlobalTrials = 10; //how many times we build data graph and repeat all tests
        int numberOfTrials  = 20;
        int[] checkPoints = {1000, 10000, 50000, 100000};
        int dimensionality = 1;//!
        int initAttempts = 5;//!
        int elementNumber = 0;
        int nn = 6;//!

        int [][] pointOfDiscontinuity = new int[checkPoints.length][numberOfGlobalTrials];

        String currDir = new File(".").getAbsolutePath();
        Path H = Paths.get(currDir);
        H = H.normalize();
        Path checkPointsDir = Paths.get(H + "\\" + "checkPoints");
        if (!Files.exists(checkPointsDir)) {
            Files.createDirectory(checkPointsDir);
        }

        String aName = "D=" + dimensionality + "NN=" + nn;

        //FileWriter fwOut = new FileWriter(new File("out" + aName), true);
        FileWriter fwArray = new FileWriter(new File(checkPointsDir + "//" + "array" + aName + ".txt"), true);
        FileWriter fwMain = new FileWriter(new File(checkPointsDir + "//" + "main" + aName + ".txt"), true);

        for ( int t=0; t < numberOfGlobalTrials; t++) {
            MetrizedSmallWorld db = new MetrizedSmallWorld();
            db.setNN(nn);
            db.setInitAttempts(initAttempts);
            
            System.out.println("Trial Number = " + t);
            
            for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
                System.out.print("Number of elements in stage = " + checkPoints[stageNumber] + "\n" );
                
                EuclidianFactory ef = new EuclidianFactory(dimensionality, checkPoints[stageNumber] - db.getSize());

                ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

                for (MetricElement me : ef.getElements()) {
                    executorAdder.submit(new Adding(db, me, elementNumber));
                    elementNumber++;
                }
                shutdownAndAwaitTermination(executorAdder);
                
              
               
                //removing part of structure
                
                int sumRemoved = 0;
                for (int trial = 0; trial < numberOfTrials; trial++) {
                    List<MetricElement> removedElements = new ArrayList();
                    int fallToComp = 0;
                    float stepRemove = 0.001f; // part of structure to remove in next iteration
                    int removeInIteration = Math.round(stepRemove * checkPoints[stageNumber]);
                    for (int numberOfRemoved = removeInIteration; numberOfRemoved <= checkPoints[stageNumber]; numberOfRemoved+=removeInIteration ) {
                        
                        for (int i = 0; i < removeInIteration; i++ ) {
                            MetricElement toRemove = TestLib.getRandomElement(db.getElements());
                            removedElements.add(toRemove);
                            db.removeElement(toRemove);
                        }

                        if ((fallToComp == 0) && TestLib.IfGraphFallToTwoComponents(db.getElements())) {
                            fallToComp = 1;
                            sumRemoved = sumRemoved + numberOfRemoved;
                            

                            System.out.print("Broken at " + numberOfRemoved + " = "  );
                            System.out.printf("%.2f",  (float) numberOfRemoved * 100.0/ (float) checkPoints[stageNumber]);
                            System.out.println("%");

                            break;
                        }    
                    }

                    for (MetricElement element : removedElements) 
                        db.restoreElement(element);
                }
                int avgRemoved = sumRemoved/numberOfTrials;
                pointOfDiscontinuity[stageNumber][t] = avgRemoved;
                System.out.print("AVG Broken at " + avgRemoved + " = "  );
                System.out.printf("%.2f",  (float) avgRemoved * 100.0/ (float) checkPoints[stageNumber]);
                System.out.println("%");
                
            }
            
        }

        fwMain.append("structureSize" + '\t' + "removedPercent" + "\r\n");
        for(int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
            Integer aCheckPoints = checkPoints[stageNumber];
            fwMain.append(aCheckPoints.toString() + '\t');

            float percentOut = 0;
            for ( int t=0; t < numberOfGlobalTrials; t++){
                percentOut += (float) pointOfDiscontinuity[stageNumber][t] * 100.0/ (float) checkPoints[stageNumber];
            }
            percentOut /= numberOfGlobalTrials;
            
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter (sb, Locale.GERMANY);
            formatter.format("%.4f", percentOut);            
            fwMain.append(formatter.toString() + "\r\n");
        }
        fwMain.close();

        fwArray.append(Arrays.deepToString(pointOfDiscontinuity));
        fwArray.close();
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
        private int elementNumber;
        
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
        TreeSet<EvaluatedElement> answer;
        private int attempts;
        int k;
        
        public MyCallable(AbstractMetricStructure db, MetricElement testQ, TreeSet<EvaluatedElement> answer, int attempts, int k) {
            this.db = db;
            this.testQ = testQ;
            this.answer = answer;
            this.attempts = attempts;
            this.k = k;
        }
        
        @Override
        public TestResult call() throws Exception {
            SearchResult result = db.knnSearch(testQ, k, attempts);
            int good = 0;
            for (EvaluatedElement ee : result.getViewedList()) {
                if (answer.contains(ee)) {
                    good++;
                }
            }
            
            return new TestResult(good, result.getViewedList().size(), result.getVisitedSet().size(), result.getVisitedSet());
            
        }
    }
}
