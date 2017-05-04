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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.latna.gmodels.ErdeshRandomGraph;
import org.latna.msw.TestLib.Fout;

/**
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class CommonFeatures {
    public static final int NUMBER_OF_THREADS = 12;
    public static final String fld = "c:/Users/aponomarenko/MSW/CommonFeatures/dim=1/nn=2/";
    //public static final String fld = "";
    public static final String outFileName = fld + "commonFeatures.csv";
    public static final String outFileName_sp = fld + "commonFeatures_sp.csv";
    public static final String outFileName_dg = fld + "commonFeatures_dg.csv";
    /**
     * Scans input string and runs the test
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //int[] checkPoints = {5000,50000,500000,5000000};
        
        //int[] checkPoints = {1000, 5000, 10000, 50000, 100000, 500000, 1000000, 2000000,5000000,10000000,20000000, 50000000}; 
        //int[] checkPoints = {1000, 5000, 10000, 50000, 100000}; 
        //int[] checkPoints = {100000};
        int[] checkPoints = {100000};
        int dimensionality = 1;
        int nn = 2; //number of nearest neighbors used in construction algorithm to approximate voronoi neighbor
        int k = 5; //number of k-closest elements for the knn search
        int initAttempts = 20; //number of search attempts used during the contruction of the structure
        int minAttempts = 1; //minimum number of attempts used during the test search 
        int maxAttempts = 20; //maximum number of attempts 
        //int dataBaseSize = 1000;
        // int dataBaseSize = 0; the restriction on the number of elements in the data structure. To set no restriction set value = 0
        int querySetSize = 100; //the restriction on the number of quleries. To set no restriction set value = 0
        int testSeqSize = 100; //number elements in the random selected subset used to verify accuracy of the search. 
         
        MetrizedSmallWorld db = new MetrizedSmallWorld();
        db.setNN(nn);
        db.setInitAttempts(initAttempts);
        
       
        //AbstractMetricStructure db = new ErdeshRandomGraph( (double) nn * 2.0 / (double) (checkPoints[0]-1));
                
        System.out.println("Algorithms version: " + db.toString());
        //FileWriter fw = new FileWriter(new File(outFileName), true);
        FileWriter fw = new FileWriter(new File(outFileName));
        FileWriter fw_sp = new FileWriter(new File(outFileName_sp));
        FileWriter fw_dg = new FileWriter(new File(outFileName_dg));
        
        Fout fout = new Fout(fw);
        Fout fout_sp = new Fout(fw_sp);
        
        //<len, count>
        NavigableMap <Integer, Integer> gwMap = new TreeMap<>();
        NavigableMap <Integer, Integer> succeedMap  = new TreeMap <> ();
        NavigableMap <Integer, Integer> spMap = new TreeMap <> ();
        //<degree, numberOfVertex>
        NavigableMap <Integer, Integer> degreeMap = new TreeMap<>();
        
        for (int stageNumber = 0; stageNumber < checkPoints.length; stageNumber++) {
            System.out.println("The first stage\n");
            System.out.println("nn=" + nn + " k=" + k + " initAttemps=" + initAttempts + " minAttempts=" + minAttempts + " maxAttempts="
                    + maxAttempts + " dataBaseSize=" + checkPoints[stageNumber] + " querySetSize=" + querySetSize + " testSeqSize=" + testSeqSize);

            EuclidianFactory ef = new EuclidianFactory(dimensionality, checkPoints[stageNumber]-db.getSize());
            EuclidianFactory testQueryFactory = new EuclidianFactory(dimensionality, querySetSize);

            ef.getElements().stream().forEach((me) -> {
                db.add(me);
            });
             
            System.out.println("The second stage");
            fout.out("size" + ";" + "graphDistance" + ";" + "greedyWalkPathLenght" + ";"  + "metricDistance" + ";" + "\n");
            fout_sp.out("sp;\n");
            Map <MetricElement, List<TestLib.EvaluationResult>> res = TestLib.evaluateSearchAlgorithm(db.getElements(), testQueryFactory, querySetSize, testSeqSize);
            
            for (List <TestLib.EvaluationResult> l: res.values()) {
                for (TestLib.EvaluationResult r: l) { 
                   //s = db.getSize() + ";" + r.graphDistance + "; " + r.greedyWalkPathLenght + ";"  + r.metricDistance + ";" + "\n";
                   //fout.out(db.getSize() + ";" + r.graphDistance + "; " + r.greedyWalkPathLenght + ";"  + String.format("%,f", r.metricDistance) + ";" + "\n");
                   //System.out.print(s);
                   //fw.append(s);
                   gwMap.put(r.greedyWalkPathLenght-1, gwMap.getOrDefault(r.greedyWalkPathLenght-1, 0) + 1);
                   if (r.graphDistance == 0 ) 
                       succeedMap.put(r.greedyWalkPathLenght-1, succeedMap.getOrDefault(r.greedyWalkPathLenght-1, 0) + 1);
                }
            } 
            
            testQueryFactory.getElements().stream().limit(10).forEach((MetricElement q) -> {
                TestLib.getShortestPathValueMap(db.getElements(), 
                        TestLib.getKCorrectElements(db.getElements(), q, 1).first().getMetricElement())
                    .values().stream().forEach((v) -> {
                        //fout_sp.out(v + ";\n");
                        spMap.put(v, gwMap.getOrDefault(v, 0) + 1);
                });
            });
            
            
            TestLib.saveStatisticToFile(fw_dg, db);
                

            fw.flush();
            fw_sp.flush();
        }
        
    }

}
