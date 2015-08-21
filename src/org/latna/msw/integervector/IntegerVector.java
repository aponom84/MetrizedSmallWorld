package org.latna.msw.integervector;

import org.latna.msw.MetricElement;

/**
 *
 * @author aponom
 */
public class IntegerVector extends MetricElement {
    
    private int x[];
    
    public IntegerVector(int x[]) {
        this.x = x;
    }
        
    @Override
    public double calcDistance(MetricElement gme) {
        double d = 0;
        IntegerVector other = (IntegerVector) gme;
        if (other.x.length != this.x.length) {
            throw new Error("Can't calculate distance between " + this.toString() + "and" + other.toString() + 
                ". Because the dimensionlity of two elements is differnt");
        }
        
        for (int i=0; i<x.length; i++) {
            d = d+(double)(this.x[i]-other.x[i])*(double)(this.x[i]-other.x[i]);
        }
        
        return Math.sqrt(d);
    }
    
    @Override
    public String toString() {
        String s = "(";
        for (int i = 0; i < x.length-1; i++)  {
            s = s + String.valueOf(x[i]) + ",";
        }
        s = s +String.valueOf(x[x.length-1]) +")";
        return s;
    }
}
