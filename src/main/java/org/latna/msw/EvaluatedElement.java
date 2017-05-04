package org.latna.msw;

/**
 * Element for which the distance has been calculated
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class EvaluatedElement implements Comparable<EvaluatedElement>{
    private double distance;
    private MetricElement metricElement;


    public EvaluatedElement(double distance, MetricElement metricElement)  {
        this.distance = distance;
        this.metricElement = metricElement;
    }

    public double getDistance() {
        return distance;
    }

    public MetricElement getMetricElement() {
        return metricElement;
    }

    public int compareTo(EvaluatedElement o) {
        int res = 0;
        if (this.metricElement == o.metricElement) return 0; 
        if (this.distance == o.distance) return 0;
        if (this.distance < o.distance) res = -1;
        if (this.distance > o.distance) res = 1;
        
        return res;
    }
}
