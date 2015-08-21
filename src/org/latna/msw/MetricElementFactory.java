package org.latna.msw;

import java.util.List;

/**
 * Abstract Factory for Metric Elements
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public interface MetricElementFactory {
    /**
     * 
     * @return List of Metric elements
     */
    public List <MetricElement> getElements();
    /**
     * Distinct implementation can have many configuration which may be incompatible 
     * among themselve, therefore we use String to pass configuration to the factory
     * @param param configuration string for the factory
     */
    public void setParameterString(String param);
}
