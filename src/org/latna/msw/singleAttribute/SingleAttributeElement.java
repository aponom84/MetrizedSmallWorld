package org.latna.msw.singleAttribute;

import org.latna.msw.euclidian.*;
import org.latna.msw.MetricElement;

/**
 * 
 * @author aponom
 */
public class SingleAttributeElement extends MetricElement {
    
    private double x;
    
    private SingleAttributeElement next = null;
    private SingleAttributeElement pre = null;
    
    public SingleAttributeElement(double x) {
        this.x = x;
    }
        
    @Override
    public double calcDistance(MetricElement gme) {
        SingleAttributeElement other = (SingleAttributeElement) gme;
        
        return Math.abs(this.x -other.x );
    }
    
    @Override
    public String toString() {
        return "(" + String.valueOf(x) + ")" ;
    }

    public void setNext(SingleAttributeElement next) {
        this.addFriend(next);
        this.next = next;
    }
    
    public SingleAttributeElement getNext() {
        return next;
    }
    
    public void setPre(SingleAttributeElement pre) {
        this.addFriend(pre);
        this.pre = pre;
    }
    
    public SingleAttributeElement getPre() {
        return pre;
    }
    
    public double getX() {
        return x;
    }
}
