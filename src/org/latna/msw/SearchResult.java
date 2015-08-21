package org.latna.msw;

import java.util.Set;
import java.util.TreeSet;

/**
 *  
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class SearchResult {
    private TreeSet <EvaluatedElement> viewedList;
    private int steps;
    private int scanned;
    private Set <MetricElement> visitedSet;
    

    public SearchResult(TreeSet <EvaluatedElement> viewedList, Set <MetricElement> visetedSet, int scanned) {
        this.viewedList = viewedList;
        this.steps = steps;
        this.scanned = scanned;
        this.visitedSet = visetedSet;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
    
    public int getScanned() {
        return scanned;
    }

    public TreeSet<EvaluatedElement> getViewedList() {
        return viewedList;
    }
    public Set <MetricElement> getVisitedSet() {
        return visitedSet;
    }
/*
    public void setViewedList(TreeSet<EvaluatedElement> viewedList) {
        this.viewedList = viewedList;
    }
*/
}