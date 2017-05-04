package org.latna.msw.singleAttribute;

import cern.jet.random.Normal;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;
import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Factory of elements which has only one attribute.
 * @author aponom
 */
public class SingleAttributeElementFactory implements MetricElementFactory {
    
    public enum DistributionType {UNIFORM, GAUSSIAN, EXPONENTIAL, LOGNORM, POWER_LAW};
    
    private int n; //number of elements
    private List <MetricElement> allElements;
    private Random random;

    public List<MetricElement> getElements() {
        return allElements;
    }

    public void setParameterString(String param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /*
     * Create factory and generate list with n uniformaly distributed elements
     * @param dimension
     * @param n - number of elements 
     * @param seed - random seed
     */    
    public SingleAttributeElementFactory(int n) {
        this.n = n;
        random = new Random();
        
    }
    
    public void setUniformalyDistribution() {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement(random.nextDouble()));
        }
    }
    
    public void setGaussianDistribution() {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement(random.nextGaussian()));
        }
    }
    
    public void setExponentialDistribution(double lambda) {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement( -1/lambda*Math.log(random.nextDouble()))  );
        }
    }
    
    public void setPowerLawDistribution(double xMin, double alfa) {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement(xMin* Math.pow(1 - random.nextDouble(),-1/(alfa-1))));
        }
    }
    
    public void setLogNormDistribution(double mean, double standartDeviation) {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement(                    
                Math.exp(mean + standartDeviation*random.nextGaussian() )
            ));
        }
    }
    
    public void setCentersDistribution(double[] centers) {
        allElements = new ArrayList(n);
        for (int i=0; i < n; i++) {
            allElements.add(new SingleAttributeElement(
                    random.nextGaussian() + centers[random.nextInt(centers.length)] ));
        }
    }
    
    public SingleAttributeElementFactory(int n, double standardDeviation) {
        this.n = n;
        
        RandomEngine engine = new DRand();
        Normal normal = new Normal(0, standardDeviation, engine);
        
        allElements = new ArrayList(n);
        
        for (int i=0; i < n; i++) {
            
            allElements.add(new SingleAttributeElement(normal.nextDouble()));
        }
    }
    
    
}
