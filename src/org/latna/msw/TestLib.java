
package org.latna.msw;
    
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.latna.msw.euclidian.EuclidianFactory;

/**
 * Methods for data structure testing and analyzing  
 * @author aponom aponom84@gmail.com
 */
public class TestLib {
    public static boolean IfGraphFallToTwoComponents(List<MetricElement> allElements)
    {
        return getNumberOfConnectedComponents(allElements) > 1 ;
    }
    
    public static int getNumberOfConnectedComponents(List<MetricElement> allElements) {
        int componentNumber = 0;
        
        Set <MetricElement> notUsed  = new HashSet<>(allElements);
        
        ArrayDeque<MetricElement> aQueue = new ArrayDeque<MetricElement>();

        while (!notUsed.isEmpty()) {
            componentNumber++;
            MetricElement e = notUsed.iterator().next();
            aQueue.add(e);
            while(!aQueue.isEmpty())
            {
                MetricElement currentV = aQueue.remove();
                notUsed.remove(currentV);
                if(!currentV.getAllFriends().isEmpty())
                    for(MetricElement element : currentV.getAllFriends())
                        if(notUsed.contains(element))
                        {
                            aQueue.add(element);
                            notUsed.remove(element);
                        }
            }
        }

        return componentNumber;    
    }
    
     public static int getNumeberOfCloserElements(List <MetricElement> allelements, MetricElement tofound,double distance){
        int num=0;
        for(int i=0;i<allelements.size();i++){
            double temp=allelements.get(i).calcDistance(tofound);
            if(temp<distance){
                num++;
            }
        }
        return num;
    }

    public static int getLinkCount(List <MetricElement> allElements) {
        int linkCount = 0;
        for (MetricElement e: allElements)
            linkCount = linkCount + e.getAllFriends().size();
        return linkCount;
    }

    public static MetricElement getRandomElement(List <MetricElement> allelements) {
        Random rand = new Random();
        return allelements.get( rand.nextInt(allelements.size()));
    }

    public static int [] getDegreeDistribution(List <MetricElement> allelements){
        int totalElements = allelements.size();
        int  degreedistributoin[]=new int[getMaxDegree(allelements)+1];
        for(int i=0; i < totalElements; i++){
            int num = allelements.get(i).getAllFriends().size();
            if(num > totalElements-1)
                num = totalElements-1;
            degreedistributoin[num]++;
        }
        return degreedistributoin;
    }

   public static MetricElement getCorrectElement(List <MetricElement> allelements, MetricElement tofound){
        double min=Double.MAX_VALUE;
        int nummin=-1;
        for(int i=0;i<allelements.size();i++){
            double temp=allelements.get(i).calcDistance(tofound);
            if(temp<min){
                nummin=i;
                min=temp;
            }

        }
        return allelements.get(nummin);
    }

    public static TreeSet <EvaluatedElement> getKCorrectElements(List <MetricElement> allElements, MetricElement query, int k) {
        TreeSet <EvaluatedElement> res = new TreeSet();
        double kDistance = Double.MAX_VALUE;
        for (MetricElement me : allElements) {
            if (me != query) {
                double currDistance = query.calcDistance(me);
                if (kDistance > currDistance) {
                    res.add(new EvaluatedElement(currDistance, me));
                    if (res.size() > k) {
                        res.remove(res.last());
                        kDistance = res.last().getDistance();
                    }
                }
            }
        }
        //if(res.size() < k)
          //  System.out.println("lolwtf");
        return res;
    }
    
    public static double getAvgClusteringCoeff(List <MetricElement> allElements)  {
        double sum;
        /*for (MetricElement e: allElements) {
        sum = sum + getLocalClusteringCoeff(e);
        }*/
        //allElements.stream().filter(e->!e.getAllFriends().isEmpty()).mapToDouble(e->getLocalClusteringCoeff(e)).sum();
        sum = allElements.stream().mapToDouble(e->getLocalClusteringCoeff(e)).sum();
        /*    sum = sum + getLocalClusteringCoeff(e);
                    });*/
            
        return sum / ((double) allElements.size());
    } 
    
    public static double getLocalClusteringCoeff(MetricElement e) {
        if (e.getAllFriends().isEmpty()) 
            return 0.0;
        if (  e.getAllFriends().size() == 1)  
            return 1.0;
        
        double localClustering = 0;
        for (MetricElement f : e.getAllFriends()) {
            for (MetricElement p : f.getAllFriends()) {
                if (e.getAllFriends().contains(p)) {
                    localClustering = localClustering + 1;
                }
            }
        }
       /* 
        if (localClustering < 0.01 ) {
            System.out.println(" Kaka");
        }
        */
        localClustering = localClustering / (2.0 * (double) combination(e.getAllFriends().size(), 2));
        return localClustering;
    };
    
    public static double getAvgSecondDegree(List <MetricElement> allElements) { 
        double sum  = 0;
        for (MetricElement e: allElements )  {
            sum = sum + e.getAllFriendsOfAllFriends().size() - 1;
        }
        return sum / (double) allElements.size();
    }
    
    public static double getGlobalClustering(List <MetricElement> allElements) { 
        long numberOfTriangles = countNumberOfTriangles(allElements);
        long numberOfTriples = countNumberOfTriples(allElements);
        double clusteringCoeff = 3 * numberOfTriangles /(double) numberOfTriples;
        return clusteringCoeff;
    }
    
    /**
     * C from n to k
     *
     */
    private static long combination(long n, long k) {
        if (n < k) {
            throw new IllegalArgumentException("n: " + String.valueOf(n) +  " k: " + String.valueOf(k));
        }
        long numerator = 1;
        for (long i = n - k +1; i <= n; i++) {
            numerator = numerator * i;
        }
        long denomerator = 1;
        for (long i = 1; i <= k; i++) {
            denomerator = denomerator * i;
        }
        return numerator / denomerator;
    }

    private static long countNumberOfTriangles(List <MetricElement> allElements){
        long toReturn = 0; //number of triangles
        Set<MetricElement> globalVisited = new HashSet<MetricElement>();
        for (MetricElement me : allElements) {
            globalVisited.add(me);
            Set<MetricElement> visited = new HashSet<MetricElement>();

            for (MetricElement firstLevelFriend : me.getAllFriends()) {

                if (visited.contains(firstLevelFriend) || globalVisited.contains(firstLevelFriend)) {
                    continue;
                }
                for (MetricElement secondLevelFriend : firstLevelFriend.getAllFriends()) {

                    if (secondLevelFriend == me || visited.contains(secondLevelFriend) || globalVisited.contains(secondLevelFriend)) {
                        continue;
                    }
                    if (secondLevelFriend.getAllFriends().contains(me)) {
                        toReturn++;
                    }
                    visited.add(firstLevelFriend);
                }
            }
        }
        return toReturn;
    }

    private static long countNumberOfTriples(List <MetricElement> allElements) {
        long toReturn = 0; //number of triples
        for (MetricElement me : allElements) {
            if (me.getAllFriends().size() >= 2) {
                toReturn = toReturn + combination(me.getAllFriends().size(), 2);
            }
        }
        System.out.println(toReturn);
        return toReturn;
    }

    public static int getMaxDegree(List <MetricElement> allElements) {
        int maxDegree = 0;
        for (MetricElement e: allElements) 
            if ( e.getAllFriends().size() > maxDegree )
                maxDegree = e.getAllFriends().size();
        return maxDegree;
    }
    
    /**
     * Calculates the degree distribution of vertices and saves it to the file with name {@code fileName}
     * @param fileName the name of the output file
     * @param db an abstract graph structure
     * @throws IOException 
     */
    public static void saveStatisticToFile(String fileName, AbstractMetricStructure db) throws IOException { 
        FileWriter fw = new FileWriter(fileName, true);
        saveStatisticToFile(fw, db);
    }
    
    /**
     * Calculates the degree distribution of vertices and saves it using {@code FileWriter fw}
     * @param fw the output FileWriter
     * @param db an abstract graph structure
     * @throws IOException 
     */
    public static void saveStatisticToFile(FileWriter fw, AbstractMetricStructure db) throws IOException { 
        fw.append("Algorithm:\t" + db.toString() + "\n");
        fw.append("Degree;Fraction;\n");
        int linkNumber = 0;
        int degreeDistribution[] = getDegreeDistribution(db.getElements());
        for (int degree = 1; degree < degreeDistribution.length; degree++) {
            if (degreeDistribution[degree] > 0) 
                fw.append(String.valueOf(degree) + ";" + String.valueOf(degreeDistribution[degree]) + "\n");
            linkNumber = linkNumber + degree*degreeDistribution[degree];
        }
        fw.append("Total link number;" + String.valueOf(linkNumber/2)+ "\n");
        
        
        fw.append("AvgClusteringCoeff;" + TestLib.getAvgClusteringCoeff(db.getElements()) + "\n");
        fw.append("Number of Connected Components;" + TestLib.getNumberOfConnectedComponents(db.getElements()) + "\n");
        
        //TestLib.
        
        fw.flush();
    }
    
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
       // System.out.print("shutdownAndAwaitTermination");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            while (!pool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                // System.out.println("Waiting for executor termination");
            }
            //pool.shutdown();
            pool.shutdownNow();
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
    
    public static void out(String msg, FileWriter fw) throws IOException {
        System.out.println(msg);
        
        fw.append(msg);
        fw.flush();
    }
    
    public static Vector <Integer> getShortestPathDistribution(List<MetricElement> allElements) {

        Vector <Integer> spDistribution = new Vector <Integer> ();
        //allElements.parallelStream().forEach(s->{
        allElements.stream().forEach(s->{
            Map <MetricElement, Integer> sp  = getShortestPathValueMap(allElements, s);
            //synchronized (spDistribution) {
                for (Integer v: sp.values()) {
                    spDistribution.add(v, spDistribution.get(v) + 1);
              //  }
            }
        });
        
        return spDistribution;
    };
 /*
    public static Object getRandomElement(List allElements) {
        if (allElements.size() == 0) return null;
        return allElements.get(random.nextInt(allElements.size()));
    }
   */
    
    /**
     * returns <query, List of search results> 
    */
    public static Map <MetricElement, List<EvaluationResult>> evaluateSearchAlgorithm(List<MetricElement> allElements, EuclidianFactory testQueryFactory, int numberOfQueries, int numberOfTries) {
        ConcurrentHashMap <MetricElement, List <EvaluationResult>> result = new ConcurrentHashMap (numberOfTries);
        
        EnterPointProvider enterPointProvider = new EnterPointProvider(allElements);
        //Stream <MetricElement> stream = Stream.generate(testQueryFactory);
        //stream.parallel().forEach(q->{
        //stream.limit(numberOfQueries).forEach(q->{
        //testQueryFactory.getElements().parallelStream().forEach(q->{
        testQueryFactory.getElements().stream().filter(Objects::nonNull).forEach(q-> {
            List <EvaluationResult> results = new ArrayList();
            TreeSet<EvaluatedElement> treeSet = TestLib.getKCorrectElements(allElements, q, 1);
            EvaluatedElement globalOptima = treeSet.first();
            for (int i = 0; i < numberOfTries; i++) {
                MetricElement source = enterPointProvider.getRandomEnterPoint();
                SearchResult sr = AlgorithmLib.searchElementSearchResult(q, source);
                EvaluatedElement localMin = sr.getViewedList().first();
                
                Map <MetricElement, Integer> sp  = 
                    getShortestPathValueMap(allElements, globalOptima.getMetricElement());
                
                results.add(new EvaluationResult(
                        sp.getOrDefault(localMin.getMetricElement(), Integer.MAX_VALUE),
                        localMin.getDistance() - globalOptima.getDistance() , 
                        sr.getVisitedSet().size(), 
                        sr.getViewedList().size()));
            }
            result.put(q, results);
            
        }); //TODO*/
        
       return result; 
    }
    
    public static class EvaluationResult {
        /**
         * topological graph distance from local minimum to global minima 
         */
        public int graphDistance; 
        /**
         * the distance from local minimum to global minima with respect to the given distance function
         */
        public double metricDistance; 
        
        public int greedyWalkPathLenght;
        
        public int numberOfScanned;

        public EvaluationResult(int graphDistance, double metricDistance, int greedyWalkPathLenght, int numberOfScanned) {
            this.graphDistance = graphDistance;
            this.metricDistance = metricDistance;
            this.greedyWalkPathLenght = greedyWalkPathLenght;
            this.numberOfScanned = numberOfScanned;
        }
    }
    
    /**
     * @param allElements
     * @param source - starting point
     * @return the map of values of the shortest path between source and every element of allElements
     */
    public static Map <MetricElement, Integer> getShortestPathValueMap(List <MetricElement> allElements, MetricElement source) {
        ArrayDeque<MetricElement> aQueue = new ArrayDeque<MetricElement>();
        Map <MetricElement, Integer> spMap = new HashMap <MetricElement, Integer>(allElements.size());
        aQueue.add(source);
        spMap.put(source, 0);
        
        while(!aQueue.isEmpty())
        {
            MetricElement currentV = aQueue.remove();
            int step = spMap.get(currentV);
            if(!currentV.getAllFriends().isEmpty())
                for(MetricElement element : currentV.getAllFriends())
                    if(!spMap.containsKey(element))
                    {
                        aQueue.add(element);
                        spMap.put(element, step+1); 
                    }
        }
        return spMap;
    }
    
    
    /*
    public static Fout getFout(FileWriter fw) {
        return new Fout(fw);
    }
    */
    public static class Fout {
        private FileWriter fw;
        public Fout(FileWriter writer) {
            fw = writer;
        }
        
        public void out(String msg) {
            try {
                fw.append(msg);
                System.out.print(msg);
            } catch (IOException ex) {
                System.out.println(msg);
            }
        }
    }

}
