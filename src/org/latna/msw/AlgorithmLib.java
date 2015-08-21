package org.latna.msw;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Collection of static methods that are used by all algorithms
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class AlgorithmLib {
    
    private static Random random = new Random();
    
    public static SearchResult aggregateSearch3(AbstractMetricStructure db, MetricElement query, int attempts, int exactNumberOfAnswers, MaskValidator maskValidator) {
        int k = 3;
        int foundAnswers = 0;
        int cyclesWithoutResults = 0;
        boolean goodElementFound = false;
        HashSet <MetricElement> globalViewedSet = new HashSet <MetricElement> ();
        
        while (cyclesWithoutResults < 8) {
            SearchResult res =  AlgorithmLib.kSearchElements(query, db.provider.getRandomEnterPoint(), k, globalViewedSet);
            goodElementFound = false;
            
            
            Set visitedSet = new HashSet <MetricElement>();
            TreeSet <EvaluatedElement> candidateSet = new TreeSet();
            for (EvaluatedElement ee: res.getViewedList()) {
                //globalViewedSet.add(ee.getMetricElement());
                if (maskValidator.validate(ee.getMetricElement(), query)) {
                    candidateSet.add(ee);              
                    foundAnswers++;
                    goodElementFound = true;
                }           
            }
            System.out.println("-----");

            while (!candidateSet.isEmpty()) {
                EvaluatedElement currEv = candidateSet.first();
                candidateSet.remove(currEv);

                for (MetricElement e: currEv.getMetricElement().getAllFriends()) {
                    if (!globalViewedSet.contains(e)) {
                        globalViewedSet.add(e);

                        if ( maskValidator.validate(e,query) ) {
                            foundAnswers++;
                            candidateSet.add(new EvaluatedElement(e.calcDistance(query), e));
                            goodElementFound = true;
                        }
                    }   
                }   
                
                System.out.println(foundAnswers + "/"+ exactNumberOfAnswers + " Scaned% " + (double)globalViewedSet.size() / (double) db.getElements().size() + 
                        " found: " + String.valueOf( (double) foundAnswers / (double) exactNumberOfAnswers )  );
            }
            
            if (!goodElementFound) 
                cyclesWithoutResults++;
            else 
                cyclesWithoutResults = 0;
        }
        
        
   
 
        //TODO
        return new SearchResult(null, null, 0);
    }
    public static SearchResult aggregateSearch2(AbstractMetricStructure db, MetricElement query, int attempts, int exactNumberOfAnswers, MaskValidator maskValidator) {
        int foundAnswers = 0;
        SearchResult res = db.knnSearch(query, 10, attempts);
        HashSet <MetricElement> viewedSet = new HashSet <MetricElement> ();
        Set visitedSet = new HashSet <MetricElement>();
        TreeSet <EvaluatedElement> candidateSet = new TreeSet();
        for (EvaluatedElement ee: res.getViewedList()) {
            viewedSet.add(ee.getMetricElement());
            if (maskValidator.validate(ee.getMetricElement(), query)) {
                candidateSet.add(ee);              
                foundAnswers++;
            }           
        }
        System.out.println("-----");
        
        while (!candidateSet.isEmpty()) {
            EvaluatedElement currEv = candidateSet.first();
            candidateSet.remove(currEv);
            
            for (MetricElement e: currEv.getMetricElement().getAllFriends()) {
                if (!viewedSet.contains(e)) {
                    viewedSet.add(e);
                    
                    if ( maskValidator.validate(e,query) ) {
                        foundAnswers++;
                        candidateSet.add(new EvaluatedElement(e.calcDistance(query), e));
                    }
                }   
            }      
            System.out.println(foundAnswers + "/"+ exactNumberOfAnswers + " Scaned% " + (double)viewedSet.size() / (double) db.getElements().size() + 
                    " found: " + String.valueOf( (double) foundAnswers / (double) exactNumberOfAnswers )  );
        }
        return res;
    }
    
    
    public static SearchResult aggregateSearch(AbstractMetricStructure db, MetricElement query, int attempts, int exactNumberOfAnswers, MaskValidator maskValidator) {
        int foundAnswers = 0;
        SearchResult res = db.knnSearch(query, 10, attempts);
        
        EvaluatedElement start = res.getViewedList().first();
        
        HashSet <MetricElement> viewedSet = new HashSet <MetricElement> ();
        Set visitedSet = new HashSet <MetricElement>();
        TreeSet <EvaluatedElement> candidateSet = new TreeSet();
        
        List <MetricElement> newList;
        
        candidateSet.add(start);
        viewedSet.add(start.getMetricElement());
        
        boolean stillContatinsCorrespoundAnswer = false;
        
        System.out.println("-----");
        
        while (!candidateSet.isEmpty()) {
            EvaluatedElement currEv = candidateSet.first();
            candidateSet.remove(currEv);
            
            stillContatinsCorrespoundAnswer = false;
            
            for (MetricElement e: currEv.getMetricElement().getAllFriends()) {
                if (!viewedSet.contains(e)) {
                    viewedSet.add(e);
                    
                    if ( maskValidator.validate(e,query) ) {
                        foundAnswers++;
                        candidateSet.add(new EvaluatedElement(e.calcDistance(query), e));
                    }
                }   
            }      
            System.out.println(foundAnswers + "/"+ exactNumberOfAnswers + " Scaned% " + (double)viewedSet.size() / (double) db.getElements().size() + 
                    " found: " + String.valueOf( (double) foundAnswers / (double) exactNumberOfAnswers )  );
        }
        return res;
    }
    
    
    
    /**
     * The simple implementation of the greedy search algorithm
     * Search the closest element to toFound in base from newEnterPoint
     */
    public static MetricElement searchElement(MetricElement tofound, MetricElement newEnterPoint) {
        MetricElement curElement=newEnterPoint,newElement;
        while(true){
            newElement=curElement.getClosestElemet(tofound);
            if(newElement==curElement)
                break;
            curElement=newElement;
        }
        return curElement;
    }

    
    /**
     * Performs greedy search of toFound element as query from newEnterPoint and 
     * returns all elements which have been scanned by algorithm
     * @param toFound query element 
     * @param newEnterPoint element from which the search starts
     * @return metric  and  scanned after greedy search
     */
    
    public static SearchResult searchElementSearchResult(MetricElement toFound, MetricElement newEnterPoint) {
        MetricElement curElement=newEnterPoint,newElement;
        int step = 0;
        TreeSet <EvaluatedElement> viewed = new TreeSet();
        Set <MetricElement> visitedSet = new HashSet();
        viewed.add(new EvaluatedElement(toFound.calcDistance(newEnterPoint),newEnterPoint));

        while(true){
            step++;
            viewed.addAll(curElement.getClosestElemetSearchResult(toFound).getViewedList());
            visitedSet.add(curElement);
            
            newElement=viewed.first().getMetricElement();

            if(newElement==curElement)
                break;
            curElement=newElement;
        }
        return new SearchResult(viewed, visitedSet, 0);
    }
    
    /**
     * Search the most k-closest elements to the query. 
     * This algorithm has not published yet. Please, don't steal it :-) 
     * Or you can publish it, but add me as co-author. (c) Alexander Ponomarenko 27.08.2012
     * @param query
     * @param newEnterPoint
     * @param k
     * @return all elements and metric values which was calculated during searching of k-closest elements
     */
    public static SearchResult kSearchElements(MetricElement query, MetricElement newEnterPoint, int k, Set <MetricElement> globalViewedSet  ) {
        double lowerBound = Double.MAX_VALUE;

        Set visitedSet = new HashSet <MetricElement>(); //the set of elements which has used to extend our view represented by viewedSet

        TreeSet <EvaluatedElement> viewedSet = new TreeSet(); //the set of all elements which distance was calculated
      //  Map <MetricElement, Double> viewedMap = new HashMap ();
        
        TreeSet <EvaluatedElement> candidateSet = new TreeSet(); //the set of elememts which we can use to e
        EvaluatedElement ev = new EvaluatedElement(query.calcDistance(newEnterPoint), newEnterPoint);

        candidateSet.add(ev);
        viewedSet.add(ev);
        globalViewedSet.add(newEnterPoint);
        //viewedMap.put(ev.getMetricElement(), ev.getDistance());

        while (!candidateSet.isEmpty()) {
            EvaluatedElement currEv = candidateSet.first();
            candidateSet.remove(currEv);
            lowerBound = getKDistance(viewedSet, k);
            //check condition for lower bound
            if (currEv.getDistance() >  lowerBound ) {
                break;
            }

            visitedSet.add(currEv.getMetricElement());

            List <MetricElement> neighbor = currEv.getMetricElement().getAllFriends();
            synchronized (neighbor){
                //calculate distance to each element from the neighbour

                for (MetricElement el: neighbor) {
                    if (!globalViewedSet.contains(el) ) {
                        EvaluatedElement evEl = new EvaluatedElement(query.calcDistance(el), el);
                        globalViewedSet.add(el);
                        viewedSet.add(evEl);
                        candidateSet.add(evEl);

                    }
                }
            }
        }
        return new SearchResult(viewedSet, visitedSet,0);
    }

    /**
     * Search of most k-closest elements to the query with several attempts
     * @param query
     * @param provider
     * @param k number of closest elements which we should to find
     * @param attempts number of search attempts
     * @return all elements with metric which values have been calculated at the searching time 
     */

    public static SearchResult kSearchElementsWithAttempts(MetricElement query, EnterPointProvider provider, int k, int attempts) {
        Set <MetricElement> globalViewedHashSet = new HashSet <MetricElement> ();
        TreeSet <EvaluatedElement> globalViewedSet = new TreeSet();
        Set <MetricElement> visitedSet = new HashSet();
           
    //    int steps = 0;
        for (int i = 0; i < attempts; i++)  {
            SearchResult sr = kSearchElements(query, provider.getRandomEnterPoint(), k, globalViewedHashSet);

            globalViewedSet.addAll(sr.getViewedList());
            visitedSet.addAll(sr.getVisitedSet());

           // steps = steps + sr.getSteps();
         //    steps = steps + sr.getVisitedSet().size();
        }

       // System.out.println("Global viewed set size = " + globalViewedSet.size());
        //SearchResult sr 
        return new SearchResult(globalViewedSet, visitedSet, globalViewedHashSet.size());
    }
    
    /**
     * deprecated due of useless 
     * @param query
     * @param provider
     * @param k
     * @param attempts
     * @return set of scanned elements. 
     */
    public static SearchResult kSearchElementsWithAttemptsMultiThread(MetricElement query, EnterPointProvider provider, int k, int attempts) {
        ConcurrentSkipListSet <EvaluatedElement> globalViewedSet = new ConcurrentSkipListSet<EvaluatedElement> ();
        int steps = 0;
        
        List <Thread> threads = new ArrayList <Thread>();
        
        for (int i=0; i < attempts; i++)  {
            KSearchRunnable ksr = new KSearchRunnable(query, provider.getRandomEnterPoint(), k, globalViewedSet);
            threads.add(ksr);
            ksr.run();
         //   SearchResult sr = kSearchElements(query, provider.getRandomEnterPoint(), k);
         //   System.out.println("one attempt viewedSet size = " + sr.getViewedList().size() );

        //    globalViewedSet.addAll(sr.getViewedList());
            //check for correct
           // todo steps = steps + sr.getSteps(); 
            
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new Error(ex);
            }
        }
        
        
        
        return new SearchResult(new TreeSet(globalViewedSet), null, 0);
    }
    
     /**
     * Perform m independent k-nn searches in the different threads
     * @param query
     * @param provider
     * @param k
     * @param attempts
     * @return set of scanned elements. 
     */
    public static SearchResult kSearchElementsWithAttemptsFuters(MetricElement query, EnterPointProvider provider, int k, int attempts) {
        ConcurrentSkipListSet<EvaluatedElement> globalViewedSet = new ConcurrentSkipListSet<EvaluatedElement>();
        ConcurrentSkipListSet <MetricElement> visitedSet = new  ConcurrentSkipListSet <MetricElement>();
        int steps = 0;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List< Future<SearchResult>> futureList = new ArrayList<Future<SearchResult>>();

        for (int i = 0; i < attempts; i++) {
            Future<SearchResult> future = executor.submit(new KSearchCallable(query, provider.getRandomEnterPoint(), k));
            futureList.add(future);
        }

        for (Future<SearchResult> future : futureList) {
            try {
                SearchResult sr = future.get();

                globalViewedSet.addAll(sr.getViewedList());
                visitedSet.addAll(sr.getVisitedSet());
                //check for correct
                steps = steps + sr.getSteps();
                
            } catch (InterruptedException ex) {
                throw new Error(ex);
            } catch (ExecutionException ex) {
                throw new Error(ex);
            }
        }
        
        executor.shutdown();

        return new SearchResult(new TreeSet(globalViewedSet), visitedSet, 0);
    }

    private static double getKDistance(SortedSet <EvaluatedElement> treeSet, int k) {
        if (k >= treeSet.size()) return treeSet.last().getDistance();
        int i = 0;
        for (EvaluatedElement e: treeSet) {
            if (i >=k) return e.getDistance();
            i++;
        }

        throw new Error("Can not get K Distance. ");
    }

    /**
     * Scan list of element and returns the closest element for element @param toFound
     */

    public static MetricElement getTheBestElement(MetricElement toFound, List <MetricElement> elements) {
        MetricElement bestResult = null;
        double bestDistance = Double.MAX_VALUE;

        //пока сброс точки входа происходит случайным образом.
        for (MetricElement currElement: elements ) {
            if (currElement.calcDistance(toFound) < bestDistance) {
                bestDistance = currElement.calcDistance(toFound);
                bestResult = currElement;
            }
        }
        return bestResult;
    }

     /**
     *
     * @return all locals minimus after running several attempts
     */
    public static List <MetricElement> getAllSearchedElements(MetricElement toFound, int ac, EnterPointProvider provider) {
        List <MetricElement> result = new ArrayList();
        for (int i = 0; i < ac; i++) {
            MetricElement currElement = AlgorithmLib.searchElement(toFound,provider.getRandomEnterPoint());

            if (!result.contains(currElement)) {
                result.add(currElement);
            }
        }
        return result;
    }

    /**
     *
     * @return all locals minimus after they know after several attempts
     */
    public static SearchResult getAllSearchedElementsSearchResult(MetricElement toFound, int ac, EnterPointProvider provider) {
        TreeSet <EvaluatedElement> set = new TreeSet();
        Set <MetricElement> visitedSet = new HashSet();
        int step =0;
        for (int i = 0; i < ac; i++) {
            SearchResult sr = AlgorithmLib.searchElementSearchResult(toFound,provider.getRandomEnterPoint());

            set.addAll(sr.getViewedList());
            visitedSet.addAll(sr.getVisitedSet());
        }
        return new SearchResult(set, visitedSet, 0);
    }

    public static MetricElement getLocalMin(MetricElement start, MetricElement query) {
        MetricElement current = start;

        MetricElement bestNext = start;
        double bestDistance = bestNext.calcDistance(query);
        do {
            current = bestNext;
            for (MetricElement x: current.getAllFriends()) {
                Double xDistance = x.calcDistance(query);
                if (xDistance < bestDistance) {
                    bestNext = x;
                    bestDistance = xDistance;
                }
            }
        } while (bestNext != current);
        return current;
    }

    public static class ElementComparator implements Comparator{
        MetricElement e;

        public ElementComparator(MetricElement e) {
            this.e = e;
        }


        public int compare(Object o1, Object o2) {
            MetricElement cur1=(MetricElement)o1;
            MetricElement cur2=(MetricElement)o2;
            double d1=e.calcDistance(cur1);
            double d2=e.calcDistance(cur2);
            if(d1>d2)
                return 1;
            if(d1<d2)
                return -1;
            return 0;
        }

    }

    public static MetricElement searchWithAttempts(MetricElement query, EnterPointProvider provider, int attemptsCount) {
        List <MetricElement> localMins = AlgorithmLib.getAllSearchedElements(query, attemptsCount, provider);
        return AlgorithmLib.getTheBestElement(query, localMins);
    }
    
    /**
     * 
     * Useless because it do the same as method kSearchElemts with m starting points in the candidate list
     */
    public static class KSearchRunnable extends Thread  {
        private MetricElement newEnterPoint;
        private MetricElement query; 
        private int k;
        private ConcurrentSkipListSet <EvaluatedElement> globalViewedSet;
        public KSearchRunnable(MetricElement query, MetricElement newEnterPoint, int k, ConcurrentSkipListSet <EvaluatedElement> globalViewedSet) {
            this.newEnterPoint = newEnterPoint;
            this.query = query;
            this.k = k;
            this.globalViewedSet = globalViewedSet;
        }
        public void run() {
            double lowerBound = Double.MAX_VALUE;

         //   Set visitedSet = new HashSet <MetricElement>(); //the set of elements which has used to extend our view represented by viewedSet

       
            Map <MetricElement, Double> viewedMap = new HashMap ();

            TreeSet <EvaluatedElement> candidateSet = new TreeSet(); //the set of elememts which we can use to e
            EvaluatedElement ev = new EvaluatedElement(query.calcDistance(newEnterPoint), newEnterPoint);

            candidateSet.add(ev);
            globalViewedSet.add(ev);
            viewedMap.put(ev.getMetricElement(), ev.getDistance());


            while (!candidateSet.isEmpty()) {
                EvaluatedElement currEv = candidateSet.first();
                candidateSet.remove(currEv);
                lowerBound = getKDistance(globalViewedSet, k);
                //check condition for lower bound
                if (currEv.getDistance() >  lowerBound ) {
                    break;
                }

                //
              //  visitedSet.add(currEv.getMetricElement());

                List <MetricElement> neighbor = currEv.getMetricElement().getAllFriends();
                //calculate distance for each element from enighbor
                for (MetricElement el: neighbor) {
                    if (!viewedMap.containsKey(el) ) {
                        EvaluatedElement evEl = new EvaluatedElement(query.calcDistance(el), el);
                        viewedMap.put(el, evEl.getDistance());
                        globalViewedSet.add(evEl);
                        candidateSet.add(evEl);

                    }
                }

            }
        }
            
    } 
    
    public static class CalculateDistance implements Callable <EvaluatedElement> {
        private MetricElement q,x;
        
        public CalculateDistance(MetricElement q, MetricElement x) {
            this.q = q;
            this.x = x;
        }
        
        public EvaluatedElement call() throws Exception {
            System.out.println("Calc!");
            return new EvaluatedElement(q.calcDistance(x),x);
        }
    }
    
    public static class KSearchCallable implements Callable <SearchResult> { 
        private MetricElement q, enterPoint;
        
        private int k;
               

        public KSearchCallable(MetricElement query, MetricElement newEnterPoint, int k) {
            this.q = query;
            this.k = k;
            this.enterPoint = newEnterPoint;
        }
        
        public SearchResult call() throws Exception {
            double lowerBound = Double.MAX_VALUE;

            Set visitedSet = new HashSet<MetricElement>(); //the set of elements which has used to extend our view represented by viewedSet

            TreeSet<EvaluatedElement> viewedSet = new TreeSet(); //the set of all elements which distance was calculated
            Map<MetricElement, Double> viewedMap = new HashMap();

            TreeSet<EvaluatedElement> candidateSet = new TreeSet(); //the set of elememts which we can use to e
            EvaluatedElement ev = new EvaluatedElement(q.calcDistance(enterPoint), enterPoint);

            candidateSet.add(ev);
            viewedSet.add(ev);
            viewedMap.put(ev.getMetricElement(), ev.getDistance());


            while (!candidateSet.isEmpty()) {
                EvaluatedElement currEv = candidateSet.first();
                candidateSet.remove(currEv);
                lowerBound = getKDistance(viewedSet, k);
                //check condition for lower bound
                if (currEv.getDistance() > lowerBound) {
                    break;
                }

                //
                visitedSet.add(currEv.getMetricElement());

                List<MetricElement> neighbor = currEv.getMetricElement().getAllFriends();
                //calculate distance for each element from enighbor

                ExecutorService executor = Executors.newCachedThreadPool();
                List<Future<EvaluatedElement>> futureList = new ArrayList<Future<EvaluatedElement>>();

                for (MetricElement el : neighbor) {
                    if (!viewedMap.containsKey(el)) {

                        /*       Future <EvaluatedElement> future = executor.submit(new CalculateDistance(query, el));
                         futureList.add(future);
                         */

                        EvaluatedElement evEl = new EvaluatedElement(q.calcDistance(el), el);
                        viewedMap.put(el, evEl.getDistance());
                        viewedSet.add(evEl);
                        candidateSet.add(evEl);

                    }
                }

                //MetricElement currElement = candidateSet.
            }
            return new SearchResult(viewedSet, visitedSet,0);
        }
        
    }
    
    public static MetricElement getRandomWalkElement(MetricElement enterPoint, int numberOfSteps) {
        MetricElement curr = enterPoint;
        
        for (int i = 0;  i <  numberOfSteps; i++ ) {
            List <MetricElement> friends = curr.getAllFriends();
            
            synchronized (friends) {
                int r  = random.nextInt(friends.size());
                
                if (r >= friends.size() ) {
                    System.out.println("Zhopaaaa");
                }
                
                curr = friends.get(r);
                
            }
        }
        
        return curr;
    }
}
