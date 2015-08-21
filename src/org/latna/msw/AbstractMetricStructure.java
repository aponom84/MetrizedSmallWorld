package org.latna.msw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * An abstraction of the data structure which supports search in metric space and sequentially data addition
 * We assume that structure provides search with the multiple attempts which can be performed 
 * from random element (enter point) of the structure
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public abstract class AbstractMetricStructure {
    protected List <MetricElement> elements; //list of all elemets in the structure in the explicit form. 

    protected EnterPointProvider provider; //object to obtain random "enter point"
    protected int size = 0; //number of elements
    
    public AbstractMetricStructure() {
        elements = Collections.synchronizedList(new ArrayList());
        provider = new EnterPointProvider(elements);
    }
    /**
     * 
     * @return list of all elements in the structure
     */
    public synchronized List<MetricElement> getElements() {
        return elements;
    }
    /**
     * If you need to get an enter point element from the structure, you can use EnterPointPointProvider
     * 
     * All algorithms can be initiated from arbitrary point  
     * @return 
     */
    public EnterPointProvider getProvider() {
        return provider;
    }

    /**
     * Adds new metric element to the structure
     * @param  newElement  new metric element
     */    
    public abstract void add(MetricElement newElement);
    /**
     * Nearest neighbor search with multiple attempts
     * @param  query  metric element
     * @param attempts number of search attempts
     * @return set of elements which have been scanned by the search algorithm
     */
    public abstract SearchResult nnSearch(MetricElement query, int attempts);

    
    /**
     * K-Nearest neighbor search with  multiple attempts
     * @param query metric element
     * @param k number of closest elements
     * @param attempts number of search attempts
     * @return set of elements which have been scanned by the search algorithm
     */
    public abstract SearchResult knnSearch(MetricElement query, int k, int attempts);
    
        /**
     * Force remove element from the structure. Halls and gaps are possible to be appeared. 
     * @param element - removed element
     */
    public void removeElement(MetricElement element) {
        for(MetricElement friend : element.getAllFriends())
        {
            friend.removeFriend(element);
        }
        elements.remove(element);
        decSize();
        
    }
    
    /**
     * Restore element and all of his links. The restorable links should be contains in friendList of restoringElements. 
     * @param element - restoring element. 
     */
    public void restoreElement(MetricElement element){
        elements.add(element);
        for(MetricElement aElement : element.getAllFriends())
        {
            aElement.addFriend(element);
        }
        incSize();
    }
    
    public synchronized int getSize() {
        return elements.size();
    }
    
    protected synchronized void incSize() {
        size = size + 1;
    }
    
    protected synchronized void decSize() {
        size = size - 1;
    }
    
     /**
     * 
     * @param query
     * @return number of added links
     */
    public int repair(MetricElement query, int succeedThreshould) {
        
        TreeSet <EvaluatedElement> neighborhod = new TreeSet();
        TreeSet <EvaluatedElement> localMins = new TreeSet <EvaluatedElement> ();
        int succeedNumber  = 0;
        int numberOfAddedLinks = 0;
        
        while (succeedNumber < succeedThreshould) {
            SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(query, this.getProvider(), 1, 1);
            if (!localMins.contains(sr.getViewedList().first())) {
                neighborhod.addAll(sr.getViewedList());
                localMins.add(sr.getViewedList().first());
                succeedNumber  = 0;
            } else {
                succeedNumber++;
            }
            
        } 
        EvaluatedElement bestResult = localMins.first();
        localMins.remove(bestResult);
        
        for (EvaluatedElement localMin: localMins) {
            double bestDistance = localMin.getMetricElement().calcDistance(bestResult.getMetricElement());
            MetricElement bestConnector = bestResult.getMetricElement(); 
            
            for (EvaluatedElement i: neighborhod.headSet(localMin)) {
                if ( localMin.getMetricElement().calcDistance( i.getMetricElement() ) < bestDistance ) {
                    bestDistance = localMin.getMetricElement().calcDistance( i.getMetricElement() ) ;
                    bestConnector = i.getMetricElement();
                }
            }
            
            if ( !bestConnector.getAllFriends().contains(localMin.getMetricElement()) ) {
                localMin.getMetricElement().addFriend(bestConnector);
                bestConnector.addFriend(localMin.getMetricElement());
                numberOfAddedLinks++;
            }
        }
        
        return numberOfAddedLinks;
    }
}
