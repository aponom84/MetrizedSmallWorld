package org.latna.msw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
/**
 * An abstraction of element (a point) of metric space. 
 * 
 * We assume that every element knows how to calculate distance to another element.
 * Particular implementation of distance calculation corresponds to the distinct data domain (metric spaces)
 * 
 * Because we use graph as the data structure therefore our metric element stores information 
 * about its neighborhood represented by the set of links to another elements.
 * 
 * Note that at this point we don't store any information about distance value between element
 * however this information can be used to decrease search complexity of the search algorithm.
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public abstract class MetricElement {
    private List<MetricElement> friends;

    public MetricElement() {
        friends = Collections.synchronizedList(new ArrayList());
    }
    
    /**
     * Calculate metric between current object and another.
     * @param gme any element for whose metric can be calculated. Any element from domain set. 
     * @return distance value
     */
    public abstract double calcDistance(MetricElement gme);

    public synchronized List<MetricElement> getAllFriends() {
        return friends;
    }

    public List<MetricElement> getAllFriendsOfAllFriends() {
        HashSet <MetricElement> hs=new HashSet();
        hs.add(this);
        for(int i=0;i<friends.size();i++){
            hs.add(friends.get(i));
            for(int j=0;j<friends.get(i).getAllFriends().size();j++)
                hs.add(friends.get(i).getAllFriends().get(j));
        }
        return new ArrayList(hs);
    }

    public MetricElement getClosestElemet(MetricElement de) {
        double min=this.calcDistance(de);
        int nummin=-1;
        for(int i=0;i<friends.size();i++){
            double temp=friends.get(i).calcDistance(de);
            if(temp<min){
                nummin=i;
                min=temp;
            }

        }
        if(nummin==-1)
            return this;
        else
            return friends.get(nummin);
    }

    public SearchResult getClosestElemetSearchResult(MetricElement de) {
        SearchResult s;
        TreeSet <EvaluatedElement> set = new TreeSet();
        for(int i=0;i<friends.size();i++){
            double temp=friends.get(i).calcDistance(de);

            set.add(new EvaluatedElement(temp, friends.get(i)));

        }
        return new SearchResult(set, null, 0);
    }

    public synchronized void addFriend(MetricElement de) {
        if(!friends.contains(de))
            friends.add(de);
    }

    public synchronized void removeFriend(MetricElement de) {
        if(friends.contains(de))
            friends.remove(de);
    }

    public synchronized void removeAllFriends() {
        friends.clear();
    }

}
