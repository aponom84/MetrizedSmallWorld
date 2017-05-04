package org.latna.msw;

/**
 *
 * @author Aponom
 */
public interface MaskValidator {
    /**
     * @param element which need to verify
     * @param query
     * @return true if element corresponds to query
     */
    public boolean validate(MetricElement element, MetricElement query);
}
