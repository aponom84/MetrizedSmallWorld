package org.latna.msw;

import java.util.Set;

/**
 * The simple helper class
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class TestResult {
    private int rightResutls; //number of true found elements
    private long scannedNumber; //number of all scanned elemets
    private int steps;
    private Set <MetricElement> visitedSet;

    public int getRightResutls() {
        return rightResutls;
    }

    public long getScannedNumber() {
        return scannedNumber;
    }
    
    public int getSteps() {
        return steps;
    }
    
    public Set <MetricElement> getVisitedSet() {
        return visitedSet;
    }

    public TestResult(int rightResutls, int scannedNumber, int steps, Set <MetricElement> visitedSet) {
        this.rightResutls = rightResutls;
        this.scannedNumber = scannedNumber;
        this.steps = steps;
        this.visitedSet = visitedSet;
    }


}
