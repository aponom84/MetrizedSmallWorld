package org.latna.msw.euclidian;

import cern.jet.random.Normal;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;
import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 *
 * @author aponom
 */
public class EuclidianFactory implements MetricElementFactory, Supplier<MetricElement>  {
    
    private int dimension; 
    private int n; //number of elements
    private List <MetricElement> allElements;
    private static Random random =  new Random();

    public List<MetricElement> getElements() {
        allElements = new ArrayList(n);
        
        for (int i=0; i < n; i++) {
            double x[] = new double[dimension];
            for (int j=0; j < dimension; j++) {
                x[j] = random.nextDouble();
            }
            
            allElements.add(new Euclidean(x));
        }
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
    public EuclidianFactory(int dimension, int n) {
        this.dimension = dimension;
        this.n = n;
    }
    
    public static MetricElement getRandomElement(int dimension) {
        double x[] = new double[dimension];
            for (int j=0; j < dimension; j++) {
                x[j] = random.nextDouble();
            }
        return new Euclidean(x);
    }
    
    public EuclidianFactory(int dimension, int n, double standardDeviation) {
        this.dimension = dimension;
        this.n = n;
        
        RandomEngine engine = new DRand();
        Normal normal = new Normal(0, standardDeviation, engine);
        
        allElements = new ArrayList(n);
        
        for (int i=0; i < n; i++) {
            double x[] = new double[dimension];
            for (int j=0; j < dimension; j++) {
                x[j] = normal.nextDouble();
            }
            
            allElements.add(new Euclidean(x)); 
        }
    }
    
    
    public int getDimension() {
        return dimension;
    }

    @Override
    public MetricElement get() {
        double x[] = new double[dimension];
        for (int j=0; j < dimension; j++) {
            x[j] = random.nextDouble();
        }
        return new Euclidean(x);
    }
}
