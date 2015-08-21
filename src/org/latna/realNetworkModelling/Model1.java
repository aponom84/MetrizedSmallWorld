package org.latna.realNetworkModelling;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.latna.msw.*;
import static org.latna.msw.TestLib.shutdownAndAwaitTermination;
import static org.latna.msw.TestLib.out;
import org.latna.msw.euclidian.EuclidianFactory;
import org.latna.msw.evaluation.DimensionalityTest;

/**
 *
 * @author Aponom
 */
public class Model1 {

    public static final int NUMBER_OF_THREADS = 4;
    public static final String outFileName = "RealWorldModel01.txt";
    public static final String degreeDistributionFileName = "RealWorldModel01_degrees.txt";
    public static final int initAttempts = 5;
    public static final int nn = 5;
 //   public static final int   size = 3766521;
    //public static final int size = 1000000;
    public static final int size =  404733; //Flickr 
    //public static final int size = 1000;
    
    public static final int dim = 70;

    public static void main(String args[]) throws IOException {
        MetrizedSmallWorld db = new MetrizedSmallWorld();
        db.setNN(nn);
        db.setInitAttempts(initAttempts);
        FileWriter fw = new FileWriter(outFileName, true);
        
        out("Algorithms version: " + db.toString() + "\n", fw);
        

        ExecutorService executorAdder = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        //EuclidianFactory ef = new EuclidianFactory(dim, size);
        System.out.println("Size: " + size);
        

        for (int elementNumber = 1; elementNumber <= size; elementNumber++) {
            //------
            executorAdder.submit(new DimensionalityTest.Adding(db, EuclidianFactory.getRandomElement(dim), elementNumber));
        }
        shutdownAndAwaitTermination(executorAdder);
        
        out("AvgClusteringCoeff: " + String.valueOf(TestLib.getAvgClusteringCoeff(db.getElements())) + "\n", fw);
        out("AvgSecondOrderDegree: " + String.valueOf(TestLib.getAvgSecondDegree(db.getElements()))  + "\n", fw);
        //TestLib.
        
        fw.close();
        
        TestLib.saveStatisticToFile(degreeDistributionFileName, db);
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
            
//            synchronized (db) {
                db.add(newElement);
                System.out.println(elementNumber);
  //          }
        }
    }

}
