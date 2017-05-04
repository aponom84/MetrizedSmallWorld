package org.latna.msw.evaluation;

import org.latna.msw.MetricElement;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import org.latna.msw.singleAttribute.SingleAttributeElement;
import org.latna.msw.singleAttribute.SingleAttributeElementFactory;

/**
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class RandomGeneratorTest {
   
    public static final String outFileName = "randomGeneratorTest.txt";
    public static final int n = 1000;
    /**
     * Scan input string and run the test
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        FileWriter fw = new FileWriter(outFileName);
        
        SingleAttributeElementFactory factory = new SingleAttributeElementFactory(n);
        //factory.setGaussianDistribution();
        //factory.setLogNormDistribution(1, 0.25);
        //factory.setExponentialDistribution(2.0);
        //factory.setPowerLawDistribution(1, 0.5);
        
        double[] centers = {-10,-7,-5,0,1,2,10};
        factory.setCentersDistribution(centers);
        
        for ( MetricElement e: factory.getElements() ) {
            SingleAttributeElement se = (SingleAttributeElement) e;
            se.getX();
            fw.write(new BigDecimal(se.getX()).toPlainString() + "\n");
        }
        
        fw.close();
    }
}
