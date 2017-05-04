package org.latna.msw;
/**
 * This is the old version of Metrized Small World. The main difference from 
 * new version of Metrized Small World is that old algorithm use k attempts of 
 * simple greedy search algorithm to obtain to obtain k-closest elements to the query.
 * The current version of Metrized Small World uses more smart k-nn search.
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class MetrizedSmallWord_Old extends AbstractMetricStructure{
    private int initAttempts;
    private int nn;
    
    public MetrizedSmallWord_Old() {
        super();
    }

    public void setNN(int nn) {
        this.nn = nn;
    }

    public void setInitAttempts(int initAttempts) {
        this.initAttempts = initAttempts;
    }
    public void add(MetricElement newElement) {
        int linkCount = 0;
        MetricElement enterPoint = provider.getRandomEnterPoint();

        //check if newElement is the first element, if true then return
        if (  (enterPoint == null)  ) {
            elements.add(newElement);
            return;
        }

        newElement.removeAllFriends();
      //  List <MetricElement> localMins = AlgorithmLib.getAllSearchedElementsSearchResult(newElement, initAttempts, provider);
        SearchResult sr = AlgorithmLib.getAllSearchedElementsSearchResult(newElement, initAttempts, provider);
        

        int i = 0;
        for(EvaluatedElement ee: sr.getViewedList()){
            if (i >= nn) break;
            i++;

            if (!newElement.getAllFriends().contains(ee.getMetricElement()) ) {
                newElement.addFriend(ee.getMetricElement());
                ee.getMetricElement().addFriend(newElement);
                linkCount++;
            } else {
                System.out.println("Legacy Bug!!!");
            }
        }
        elements.add(newElement);
    }

    @Override
    public SearchResult nnSearch(MetricElement query, int attempts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SearchResult knnSearch(MetricElement query, int k, int attempts) {
       return AlgorithmLib.getAllSearchedElementsSearchResult(query, attempts, provider);
    }

    public String toString() {
        return "Old MSW Algorithms";
    }
}
