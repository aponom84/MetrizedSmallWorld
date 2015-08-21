package org.latna.msw;

import java.util.Random;

/**
 * One of the simplest implementation of the Metrized Small World Data Structure
 * This implementation uses nn closest elements as an approximation of Voronoi 
 * neighborhood. 
 * @author Alexander Ponomarenko aponom84@gmail.com 
 */
public class MetrizedSmallWorld_PowerLaw extends AbstractMetricStructure{
    private int initAttempts;
    private int nn;
    private Random rnd;
    private int randomWalksNumber;
    private int randomWalkLenght;
    
    /**
     * Number of closest element of approximation of Voronoi neighborhood
     * @param nn 
     */
    public MetrizedSmallWorld_PowerLaw(int nn, int initAttempts, int  randomWalksNumber, int randomWalkLenght) {
        super();
        rnd = new Random();
        this.nn = nn;
        this.initAttempts = initAttempts;
        this.randomWalksNumber = randomWalksNumber;
        this.randomWalkLenght = randomWalkLenght;
    }
    
    /**
     * Number of attempts that will be used for retrieval nn closest elements 
     * in the adding algorithm
     * @param initAttempts 
     */
    public void setInitAttempts(int initAttempts) {
        this.initAttempts = initAttempts;
    }
    public void add(MetricElement newElement) {
        try {
            int linkCount = 0;
            MetricElement enterPoint = this.getProvider().getRandomEnterPoint();

            //check if newElement is the first element, if true then return
            if ((enterPoint == null)) {
                elements.add(newElement);
                incSize();
                return;
            }

            newElement.removeAllFriends();

            SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(newElement, this.getProvider(), nn, initAttempts);

            int i = 0;
            for (EvaluatedElement ee : sr.getViewedList()) {
                if (i >= nn) {
                    break;
                }
                i++;

                if (!newElement.getAllFriends().contains(ee.getMetricElement())) {
                    newElement.addFriend(ee.getMetricElement());
                    ee.getMetricElement().addFriend(newElement);
                    linkCount++;
                } else {
                    //because programm doesn't contain of any bugs, you will never see this error
                    throw new Error("Algorithm bug! Ha-ha-ha. Kill your self!");
                }
            }
            
            //do random walks

            
            
            for (int j=0; j < randomWalksNumber; j++) {
                MetricElement randomElement  = AlgorithmLib.getRandomWalkElement(provider.getRandomEnterPoint(), randomWalkLenght);
                newElement.addFriend(randomElement);
                randomElement.addFriend(newElement);
            }
            
            
            elements.add(newElement); incSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

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
        return "Power_Law_Metrized_Small_World\t "
                + "nn: " + String.valueOf(nn)  + " initAttempts: " + initAttempts  
                + " randomWalksNumber: " + randomWalksNumber + " RandomWalksLenght: " + randomWalkLenght;
    }
}
