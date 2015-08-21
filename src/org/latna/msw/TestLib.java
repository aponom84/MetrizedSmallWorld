
package org.latna.msw;
    
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Methods for data structure testing and analyzing  
 * @author aponom aponom84@gmail.com
 */
public class TestLib {
    public static boolean IfGraphFallToTwoComponents(List<MetricElement> allElements)
    {
        ArrayDeque<MetricElement> aQueue = new ArrayDeque<MetricElement>();
        Set <MetricElement> markedElements = new HashSet <MetricElement>(allElements.size());
        aQueue.add(allElements.get(0));
        markedElements.add(allElements.get(0));

        while(!aQueue.isEmpty())
        {
            MetricElement currentV = aQueue.remove();

            if(!currentV.getAllFriends().isEmpty())
                for(MetricElement element : currentV.getAllFriends())
                    if(!markedElements.contains(element))
                    {
                        aQueue.add(element);
                        markedElements.add(element);
                    }
        }

        return allElements.size() > markedElements.size();
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
        double sum = 0;
        for (MetricElement e: allElements) {
            sum = sum + getLocalClusteringCoeff(e);
        }
        
        return sum / ((double) allElements.size());
    } 
    
    public static double getLocalClusteringCoeff(MetricElement e) {
        if (  e.getAllFriends().isEmpty()  || ( e.getAllFriends().size() == 1)  ) 
            return 1.0;
        
        double localClustering = 0;
        for (MetricElement f : e.getAllFriends()) {
            for (MetricElement p : f.getAllFriends()) {
                if (e.getAllFriends().contains(p)) {
                    localClustering = localClustering + 1;
                }
            }
        }
        
        if (localClustering < 0.01 ) {
            System.out.println(" Kaka");
        }
        
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
    
    public static void saveStatisticToFile(String fileName, AbstractMetricStructure db) throws IOException { 
        FileWriter fw = new FileWriter(fileName, true);
        
        fw.append("Algorithm:\t" + db.toString() + "\n");
        int linkNumber = 0;
        int degreeDistribution[] = getDegreeDistribution(db.getElements());
        for (int degree = 1; degree < degreeDistribution.length; degree++) {
            if (degreeDistribution[degree] > 0) 
                fw.append(String.valueOf(degree) + "\t" + String.valueOf(degreeDistribution[degree]) + "\n");
            linkNumber = linkNumber + degree*degreeDistribution[degree];
        }
        fw.append("Total link number:\t" + String.valueOf(linkNumber/2)+ "\n");
        
        fw.close();
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
    
    

}
