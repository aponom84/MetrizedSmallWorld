package org.latna.msw.SelfAdapted;

import java.util.ArrayList;
import java.util.List;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.AlgorithmLib;
import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;

/**
 * 
 * 
 * neighborhood. 
 * @author Alexander Ponomarenko aponom84@gmail.com 
 */
public class RecursiveUpdateNeighbors extends AbstractMetricStructure{
    //private int succeedThreshould = 5;
    private int succeedThreshould;
    
    public RecursiveUpdateNeighbors(int succeedThreshould) {
        super();
        this.succeedThreshould = succeedThreshould;
    }
    
    
    private void makeSearchable(MetricElement newElement) {
        List <MetricElement> neighbors = new ArrayList();
    
        int succeedNumber  = 0;
        do {
            SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(newElement, this.getProvider(), 1, 1);
            
            if (sr.getViewedList().first().getMetricElement() != newElement) {
                succeedNumber = 0;
                newElement.addFriend(sr.getViewedList().first().getMetricElement());
                sr.getViewedList().first().getMetricElement().addFriend(newElement);
                neighbors.add(sr.getViewedList().first().getMetricElement());
            } else {
                succeedNumber++;
            } 
            
        } while (succeedNumber < succeedThreshould);
        
        for (MetricElement e: neighbors) {
            makeSearchable(e);
        }
        /*
        for (MetricElement i: neighbors) 
            for (MetricElement j: neighbors )
                if (i != j ) {
                    i.addFriend(j);
                    j.addFriend(i);
                }
                */
    }
    public void add(MetricElement newElement) {
        try {
        MetricElement enterPoint = this.getProvider().getRandomEnterPoint();

        //check if newElement is the first element, if true then return
        if (  (enterPoint == null)  ) {
            elements.add(newElement);
            incSize();
            return;
        }

        newElement.removeAllFriends();
        
        makeSearchable(newElement);
  

        elements.add(newElement);
        incSize();
        } catch (Exception ex){
           ex.printStackTrace();
        } catch (Throwable ex) {
           ex.printStackTrace(); 
        }
        
    }

    @Override
    public SearchResult nnSearch(MetricElement query, int attempts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SearchResult knnSearch(MetricElement query, int k, int attempts) {
        return AlgorithmLib.kSearchElementsWithAttempts(query, getProvider(), k, attempts);
    }

    public String toString() {
        return "Self Adapted Metrized Small World [Recurcive Update Neighbors]\t" + 
                "succeedThreshould: " + String.valueOf(succeedThreshould)  + "\t" +
                "size: "+ elements.size();
    }
}
