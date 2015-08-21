package org.latna.msw;

import java.util.List;
import java.util.Random;

/**
 * The class represents an abstraction of random point selection algorithm
 * Note that current model doesn't describe the decentralized algorithm of random point selection
 * however it can be implemented as random blind walk through the graph(If graph 
 * has small world property, you should to do few number of steps to reach any vertex of this graph).
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class EnterPointProvider {
    private static int seed = 108;
    private Random random;
    private List <MetricElement> allElements;
    /**
     * @param  elements  reference list of all elements in the structure 
     */
    public EnterPointProvider (List <MetricElement> elements) {
        allElements = elements;
        random = new Random(seed);
    }
    /**
     * @return random selected element from the structure
     */
    public MetricElement getRandomEnterPoint() {
        if (allElements.size() == 0) return null;
        return allElements.get(random.nextInt(allElements.size()));
    }
}
