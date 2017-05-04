package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;
import org.latna.msw.TestLib;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.TestResult;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.latna.msw.singleAttribute.SingleAttributeElementFactory;
import org.latna.msw.singleAttribute.SingleAttributeMetrizedSmallWorld;

/**
 *
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class SingleAttributeDistributionTest {

    public static final int NUMBER_OF_THREADS = 12;
    public static final String resultsFolderPath = "evaluationResults\\SingleAttribute\\";

    public static enum DISTRIBUTION_TYPE {

        Uniform, Norm, LogNorm, PowerLaw, Centres
    };

    public static void doTest(String outFileName, int querySetSize, int testSeqSize, int numberOfTests, int nn, int k, int[] checkPoints, DISTRIBUTION_TYPE dType) throws IOException {
      
        System.out.println("Working on: " + outFileName);
       // System.out.println("Algorithms version: " + db.toString());
        double[] avgPathLenght = new double[checkPoints.length];
        double[] avgScanned = new double[checkPoints.length];
        double[] avgScannedPercent = new double[checkPoints.length];
        double[] avgRecall = new double[checkPoints.length];
        double[] avgMaxDegree = new double[checkPoints.length];
        
        for (int j = 0; j < numberOfTests; j++) {
            SingleAttributeMetrizedSmallWorld db = new SingleAttributeMetrizedSmallWorld();
            db.setNN(nn);
            for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
                    

                SingleAttributeElementFactory ef = new SingleAttributeElementFactory(checkPoints[stageNumber] - db.getSize());
                SingleAttributeElementFactory testQueryFactory = new SingleAttributeElementFactory(querySetSize);

                //It is not the best implementaion. Better solution is to pass the object which specifies the paricular behavior of the factory
                switch (dType) {
                    case Uniform: {
                        ef.setUniformalyDistribution();
                        testQueryFactory.setUniformalyDistribution();
                        break;
                    }
                    case Norm: {
                        ef.setGaussianDistribution();
                        testQueryFactory.setGaussianDistribution();
                        break;
                    }

                    case LogNorm: {
                        ef.setLogNormDistribution(4, 0.5);
                        testQueryFactory.setLogNormDistribution(4, 0.5);
                        break;
                    }

                    case PowerLaw: {
                        ef.setPowerLawDistribution(1, 0.5);
                        //testQueryFactory.setLogNormDistribution(1, 0.5);
                        testQueryFactory.setPowerLawDistribution(1, 0.5);
                        break;
                    }

                    case Centres: {
                        double[] centers = {-10, -7, -5, 0, 1, 2, 10};
                        ef.setCentersDistribution(centers);
                        testQueryFactory.setCentersDistribution(centers);
                        break;
                    }
                }

                ArrayList<MetricElement> testQueries = new ArrayList();

                for (MetricElement me : ef.getElements()) {
                    db.add(me);
                }

                Map<MetricElement, TreeSet<EvaluatedElement>> rightResultMap = new HashMap<MetricElement, TreeSet<EvaluatedElement>>();

                //System.out.println("The second stage");
                for (MetricElement newQuery : testQueryFactory.getElements()) {
                    testQueries.add(newQuery);
                    rightResultMap.put(newQuery, TestLib.getKCorrectElements(db.getElements(), newQuery, k));
                }

                Random random = new Random();

                ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
                List<Future<TestResult>> searchResultList = new ArrayList<Future<TestResult>>();

                int good = 0;
                long scanned = 0;
                int steps = 0;
                
                
                for (int i = 0; i < testSeqSize; i++) {
                    MetricElement testQ = testQueries.get(random.nextInt(testQueries.size()));

                    Callable testSearcher = new MyCallable(db, testQ, rightResultMap.get(testQ), 1, k);

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

                avgRecall[stageNumber] += ((double) good) / ((double) testSeqSize * k);
                avgScannedPercent[stageNumber] += ((double) (scanned)) / ((double) db.getElements().size() * (double) testSeqSize);
                avgScanned[stageNumber] += scanned / (double) testSeqSize;
                avgPathLenght[stageNumber] += ((double) steps / (double) testSeqSize);
                
                avgMaxDegree[stageNumber] += TestLib.getMaxDegree(db.getElements());
            }
        }
        
        FileWriter fw = new FileWriter(new File(outFileName), true);
        
        for (int i = 0; i < checkPoints.length; i++) {
            String s = "NN=" + nn + " dataBaseSize=" + checkPoints[i]
                        + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize
                        + " K = " + k + "\trecall = " + avgRecall[i] /  (double) numberOfTests + "\tScanedPercent = "
                        + avgScannedPercent[i] /(double) numberOfTests + "\tAvg Scanned\t" + ( avgScanned[i] / (double) numberOfTests)
                        + "\tAvg Path\t" + avgPathLenght[i] / (double) numberOfTests + "\tMaxDegre:\t" + avgMaxDegree[i] / (double) numberOfTests +"\n";
                System.out.print(s);
                fw.append(s);
        }
        
        fw.close();

    }

    /**
     * Scan input string and run the test
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //int[] checkPoints = {1000, 5000};
        int[] checkPoints = {1000, 5000, 10000, 50000, 100000, 500000, 1000000};
        //  int[] checkPoints = {1000,5000, 10000, 50000,100000,500000, 1000000,2000000,5000000,10000000,20000000, 50000000}; 
        int nn = 10; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int k = 1; //number of k-closest elements for the knn search
        //int numberOfTests = 20;
        int numberOfTests = 4;

        //int querySetSize = 200; //the restriction on the number of quleries. To set no restriction set value = 0
        //int testSeqSize = 100; //number elements in the random selected subset used to verify accuracy of the search. 

        int querySetSize = 0; //the restriction on the number of quleries. To set no restriction set value = 0
        int testSeqSize = 0;
        //doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,nn, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
        doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,1, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
        doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,2, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
        doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,5, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
        doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,20, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
        doTest(resultsFolderPath + "maxDegree.txt", querySetSize, testSeqSize, numberOfTests,40, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
       // doTest(resultsFolderPath + "uniform.txt", querySetSize, testSeqSize, numberOfTests,nn, k, checkPoints, DISTRIBUTION_TYPE.Uniform);
     //   doTest(resultsFolderPath + "norm.txt", querySetSize, testSeqSize, numberOfTests, nn, k, checkPoints, DISTRIBUTION_TYPE.Norm);
     //   doTest(resultsFolderPath + "logNorm.txt", querySetSize, testSeqSize, numberOfTests, nn, k, checkPoints, DISTRIBUTION_TYPE.LogNorm);
     //  doTest(resultsFolderPath + "powerLaw.txt", querySetSize, testSeqSize, numberOfTests, nn, k, checkPoints, DISTRIBUTION_TYPE.PowerLaw);
     //   doTest(resultsFolderPath + "centers.txt", querySetSize, testSeqSize, numberOfTests, nn, k, checkPoints, DISTRIBUTION_TYPE.Centres);

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
            //result.
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
