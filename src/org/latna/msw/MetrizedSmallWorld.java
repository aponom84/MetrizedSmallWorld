package org.latna.msw;

/**
 * One of the simplest implementation of the Metrized Small World Data Structure
 * This implementation uses nn closest elements as an approximation of Voronoi 
 * neighborhood. 
 * @author Alexander Ponomarenko aponom84@gmail.com 
 */
public class MetrizedSmallWorld extends AbstractMetricStructure{
    private int initAttempts;
    private int nn;
    private int size = 0; //number of elements
    
    public MetrizedSmallWorld() {
        super();
    }
    
    /**
     * Number of closest element of approximation of Voronoi neighborhood
     * @param nn 
     */
    public void setNN(int nn) {
        this.nn = nn;
    }
    
    /**
     * The number of attempts that will be used for retrieval of nn closest elements 
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
        if (  (enterPoint == null)  ) {
            elements.add(newElement);
            incSize();
            return;
        }

        newElement.removeAllFriends();

        SearchResult sr = AlgorithmLib.kSearchElementsWithAttempts(newElement, this.getProvider(), nn, initAttempts);
     //    SearchResult sr = AlgorithmLib.kSearchElementsWithAttemptsMultiThread(newElement, this.getProvider(), nn, initAttempts);
    //   SearchResult sr = AlgorithmLib.kSearchElementsWithAttemptsFuters(newElement, this.getProvider(), nn, initAttempts);
      // System.out.println("viewedList size = " + sr.getViewedList().size());

        int i = 0; 
        for(EvaluatedElement ee: sr.getViewedList()){
            if (i >= nn) break;
            i++;

            if (!newElement.getAllFriends().contains(ee.getMetricElement()) ) {
                newElement.addFriend(ee.getMetricElement());
                ee.getMetricElement().addFriend(newElement);
                linkCount++;
            } else {
                //because programm doesn't contain of any bugs, you will never see this error
                throw new Error("Algorithm bug! Ha-ha-ha. Kill your self!");
            }
        }
      //  System.out.println("link Added: " + linkCount);
        elements.add(newElement);
        incSize();
        } catch (Exception ex){
           ex.printStackTrace();
        } catch (Throwable ex) {
           ex.printStackTrace(); 
        }
        
    }
    
    /**
     * Force remove element from the structure. Halls and gaps are possible to be appeared. 
     * @param element - removed element
     */
    public void removeElement(MetricElement element) {
        for(MetricElement friend : element.getAllFriends())
        {
            friend.removeFriend(element);
        }
        elements.remove(element);
        decSize();
        
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
        return "Modified Algorithm nn=" + String.valueOf(nn) + " initAttems=" + String.valueOf(initAttempts);
    }
}
