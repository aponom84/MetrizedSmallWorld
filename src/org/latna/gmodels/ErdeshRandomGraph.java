/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.latna.gmodels;

import java.util.Random;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;

/**
 * A dynamic modification of the Erdős-Rényi model (1959). A term "dynamic" means 
 * that graph is builded by adding new vertices into the graph one by one. 
 * At the i-th step a new vertex can produce i-1 new edges. So for this each 
 * possible edge we build it with the probability <i>p<p>

 * @author Erdesh Ponomarenko 
 */
public class ErdeshRandomGraph extends AbstractMetricStructure {
    /**
     * Probability
     */
    private double p; 
    private Random rnd;
        
    public ErdeshRandomGraph(double p) {
        this.p = p;
        rnd  = new Random();
    }
    @Override
    synchronized public void  add(MetricElement newElement) {
        //int newEdgesNumber = (int) (p * (float) elements.size());
        
        elements.stream().filter(e->rnd.nextDouble() <= p).forEach(e->{
            e.addFriend(newElement);
            newElement.addFriend(e);
        });
        
        /*for (int i = 0; i < newEdgesNumber; i++) {
            
            MetricElement randomElement = elements.get(rnd.nextInt(elements.size()));
            
            randomElement.addFriend(newElement);
            newElement.addFriend(randomElement);
        } */
        elements.add(newElement);
        incSize();
    }

    @Override
    public SearchResult nnSearch(MetricElement query, int attempts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SearchResult knnSearch(MetricElement query, int k, int attempts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String toString() {
        return "ErdeshRandomGraph p: " + p ;
    }
    
}
