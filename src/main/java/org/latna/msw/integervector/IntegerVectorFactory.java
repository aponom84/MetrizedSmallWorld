package org.latna.msw.integervector;

import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author aponom
 */
public class IntegerVectorFactory implements MetricElementFactory {
    
    private int dimension; 
    private int n; //number of elements
    private List <MetricElement> allElements;
    private int [] randomBonds;
    private int i;
    private Random random = new Random();
    

    public List<MetricElement> getElements() {
        return allElements;
    }
    
    public MetricElement getElement() {
        if ( i >= n )
            return null;
        
        int[]  vector = new int [dimension];
        for (int j = 0; j < dimension; j++) 
            vector[j] = random.nextInt(randomBonds[j]);
        
        i++;
        
        return new IntegerVector(vector);
        //IntegerVector iv = new IntegerVector
    } 

    public void setParameterString(String param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /*
     * Create factory and generate list of n elements
     * @param dimension
     * @param n - number of elements 
     * @param seed - random seed
     */    
    public IntegerVectorFactory(int n, int dim, int [] randomBonds) {
        this.dimension = dim;
        this.n = n;
        this.i = 0;
       
        
        this.randomBonds = randomBonds;
        
    }
    
    
    public int getDimension() {
        return dimension;
    }
}
