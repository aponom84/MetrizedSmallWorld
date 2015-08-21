package org.latna.msw.singleAttribute;

import org.latna.msw.AbstractMetricStructure;
import org.latna.msw.AlgorithmLib;
import org.latna.msw.EvaluatedElement;
import org.latna.msw.MetricElement;
import org.latna.msw.SearchResult;

/**
 * The special case of Metrized Small World data structure
 * @author Alexander Ponomarenko aponom84@gmail.com 
 */
public class SingleAttributeMetrizedSmallWorld extends AbstractMetricStructure{
    private int nn;
    
    public SingleAttributeMetrizedSmallWorld() {
        super();
    }

    /**
     * Number of closest element of approximation of Voronoi neighborhood
     * @param nn 
     */
    public void setNN(int nn) {
        this.nn = nn;
    }
    
    public void add(MetricElement metricElement) {
        SingleAttributeElement newElement = (SingleAttributeElement) metricElement;
        newElement.removeAllFriends();
        MetricElement enterPoint = this.getProvider().getRandomEnterPoint();

        //check if newElement is the first element, if true then return
        if (  (enterPoint == null)  ) {
            elements.add(newElement); incSize(); return; 
        }
        
        SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(newElement, this.getProvider(), 1, 1);
        SingleAttributeElement closestElement = ((SingleAttributeElement) sr.getViewedList().first().getMetricElement());
        
        if ( closestElement.getX() >  newElement.getX()  ) {
            newElement.setNext(closestElement);
            if (closestElement.getPre() != null) {
                closestElement.getPre().setNext(newElement);
                newElement.setPre(closestElement.getPre());
            }
            closestElement.setPre(newElement);
        } else {
            newElement.setPre(closestElement);
            if (closestElement.getNext() != null) {
                closestElement.getNext().setPre(newElement);
                newElement.setNext(closestElement.getNext());
            }
            closestElement.setNext(newElement);
        }
        
        SingleAttributeElement pre = newElement.getPre();
        SingleAttributeElement next = newElement.getNext();
        
        for (int i = 0; i < nn; i++) {
            if ((pre == null)&&(next == null)) 
                break;
            
            if (pre == null) {
                newElement.addFriend(next);
                next.addFriend(newElement);
                next = next.getNext();
                continue;
            }
            if (next == null) {
                newElement.addFriend(pre);
                pre.addFriend(newElement);
                pre = pre.getPre();
                continue;
            }
            
            if ( newElement.calcDistance(pre) < newElement.calcDistance(next) ) {
                newElement.addFriend(pre);
                pre.addFriend(newElement);
                pre = pre.getPre();
            } else {
                newElement.addFriend(next);
                next.addFriend(newElement);
                next = next.getNext();
            }
        }
        
        elements.add(newElement);
        incSize();
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
        return "Single Attribute Metrized Small World";
    }
}
