package org.latna.msw.wikisparse;

import org.latna.msw.MetricElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * The typical document similarity used under the vector space
 * model (R. Baeza-Yates and B. Ribeiro-Neto. Modern Information Retrieval.
 * Addison-Wesley, 1999) can be adapted to define a metric space. In this model each text
 * document is seen as a vector in a high-dimensional space (one coordinate per
 * vocabulary word), and the so-called cosine similarity is used, which measures
 * the number and relevance of shared terms between
 * 
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class WikiSparse  extends MetricElement {
    private Map <Integer, Double> termVector;

    public WikiSparse() {
        super();
        termVector = new HashMap();
    }

    public void addTerm(int termId, double value) {
         termVector.put(termId, value);
    }

    @Override
    public double calcDistance(MetricElement gme) {
        double res = 0;
        if (!(gme instanceof WikiSparse)) {
            throw new Error("You trie calculate distance for non Document type object");
        }
        WikiSparse otherDoc = (WikiSparse) gme;
        WikiSparse doc1;
        WikiSparse doc2;

        if (otherDoc.termVector.size() < this.termVector.size()) {
            doc1 = otherDoc;    
            doc2 = this;
        } else {
            doc1 = this;
            doc2 = otherDoc;
        }

        for ( Entry <Integer, Double>  i :  doc1.termVector.entrySet()) {
            if (doc2.termVector.containsKey(i.getKey()) ) {
                res = res + i.getValue()*doc2.termVector.get(i.getKey());
            }
        }

        res  = res / (  doc1.getVectorLenght() * doc2.getVectorLenght()  );
        res = java.lang.Math.acos(res);
        return res;
    }

    private double getVectorLenght() {
        double res = 0;

        for (Double d: termVector.values())
            res = res + d*d;

        return Math.sqrt(res);
    }
    /**
     * 
     * @param mask
     * @return true if the term vector of current element contains all words from the mask
     */
    
    public boolean correspoundToMask(WikiSparse mask) {  
        for (Integer t: mask.termVector.keySet()) {
            if (this.termVector.get(t) == null)
                return false;
        }      
        return true;
    }

    public boolean equals(Object o) {
        WikiSparse otherDocument = (WikiSparse) o ;
        return otherDocument.termVector.equals(this.termVector);
    }
    
    
    public WikiSparse getRandomSubDoc(Random random, int len) {
        int size = termVector.size();
        WikiSparse newDoc = new WikiSparse();
        Integer [] keys = new Integer [size];
        keys = (Integer[]) termVector.keySet().toArray(keys);
        
        for (int i = 0; i < len; i++) {
            newDoc.addTerm(keys[random.nextInt(size)], 1.0/(double) len);
        }
        return newDoc;
    } 

}
