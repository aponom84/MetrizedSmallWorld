package org.latna.msw.SelfAdapted;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.AlgorithmLib;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;

/**
 * One of the simplest implementation of the Metrized Small World Data Structure
 * This implementation uses nn closest elements as an approximation of Voronoi 
 * neighborhood. 
 * @author Alexander Ponomarenko aponom84@gmail.com 
 */
public class Repair02 extends AbstractMetricStructure{
    //private int succeedThreshould = 5;
    final private int succeedThreshould;
    
    public Repair02(int succeedThreshould) {
        super();
        this.succeedThreshould = succeedThreshould;
    }
    
    
    private void makeSearchable(MetricElement newElement) {
        TreeSet <EvaluatedElement> neighborhod = new TreeSet();
        TreeSet <EvaluatedElement> localMins = new TreeSet <EvaluatedElement> ();
        int succeedNumber  = 0;
        
        while (succeedNumber < succeedThreshould) {
            SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(newElement, this.getProvider(), 1, 1);
            if (!localMins.contains(sr.getViewedList().first())) {
                neighborhod.addAll(sr.getViewedList());
                localMins.add(sr.getViewedList().first());
                succeedNumber  = 0;
            } else {
                succeedNumber++;
            }
            
        } 
        for (EvaluatedElement localMin: localMins) {
            double bestDistance = localMin.getDistance();
            MetricElement bestConnector = newElement; 
            
            for (EvaluatedElement i: neighborhod.headSet(localMin)) {
                if ( localMin.getMetricElement().calcDistance( i.getMetricElement() ) < bestDistance ) {
                    bestDistance = localMin.getMetricElement().calcDistance( i.getMetricElement() ) ;
                    bestConnector = i.getMetricElement();
                }
            }
            localMin.getMetricElement().addFriend(bestConnector);
            bestConnector.addFriend(localMin.getMetricElement());
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
        return "Self Adapted Metrized Small World [Repair 02]\t" + 
                "succeedThreshould: " + String.valueOf(succeedThreshould)  + "\t" +
                "size: "+ elements.size();
    }
}
