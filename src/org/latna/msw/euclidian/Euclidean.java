package org.latna.msw.euclidian;

import org.latna.msw.MetricElement;

/**
 *
 * @author aponom
 */
public class Euclidean extends MetricElement {
    
    private double x[];
    
    public Euclidean(double x[]) {
        this.x = x;
    }
        
    @Override
    public double calcDistance(MetricElement gme) {
        double d = 0;
        Euclidean other = (Euclidean) gme;
        if (other.x.length != this.x.length) {
            throw new Error("Can't calculate distance between " + this.toString() + "and" + other.toString() + 
                ". Because the dimensionlity of two elements is differnt");
        }
        
        for (int i=0; i<x.length; i++) {
            d = d+(this.x[i]-other.x[i])*(this.x[i]-other.x[i]);
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
    
    public int getDim() {
        return x.length;
    }
    
    
    public boolean correspoundToMask(int component) {
        return true;
        
        
    }
    
}
