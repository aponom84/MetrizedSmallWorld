package org.latna.msw;

import java.util.Set;
import java.util.TreeSet;

/**
 *  
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class SearchResult {
    private TreeSet <EvaluatedElement> viewedList;
    private Set <MetricElement> visitedSet;
    

    public SearchResult(TreeSet <EvaluatedElement> viewedList, Set <MetricElement> visetedSet) {
        this.viewedList = viewedList;
        this.visitedSet = visetedSet;
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