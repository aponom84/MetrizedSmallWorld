package org.latna.msw.documents;

import org.latna.msw.MetricElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
public class Document  extends MetricElement {
    private Map <String, Double> termVector;
    String id;

    public Document(String id) {
        super();
        termVector = new HashMap();
        this.id = id;
    }

    public void addTerm(String termId, double value) {
         termVector.put(termId, value);
    }

    @Override
    public double calcDistance(MetricElement gme) {
        double res = 0;
        if (!(gme instanceof Document)) {
            throw new Error("You trie calculate distance for non Document type object");
        }
        Document otherDoc = (Document) gme;
        Document doc1;
        Document doc2;

        if (otherDoc.termVector.size() < this.termVector.size()) {
            doc1 = otherDoc;    
            doc2 = this;
        } else {
            doc1 = this;
            doc2 = otherDoc;
        }

        for ( Entry <String, Double>  i :  doc1.termVector.entrySet()) {
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

    public boolean equals(Object o) {
        Document otherDocument = (Document) o ;
        return otherDocument.termVector.equals(this.termVector);
    }

}
