package org.latna.msw.wikisparse;

import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import org.latna.msw.documents.Document;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class WikiSparseFactory {
    public String inputFilePath="G:\\WikiData\\wikipedia.txt";
    private int maxDoc; 
    private Scanner globalScanner = null;

    public WikiSparseFactory(String inputFilePath) {
        if (!inputFilePath.isEmpty())
            this.inputFilePath = inputFilePath;
        
        try {
            globalScanner = new Scanner(new File(this.inputFilePath));
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
    }
    
    public MetricElement getElement() {
        if ( !globalScanner.hasNextLine() ) 
            return null;
        Scanner lineScanner = new Scanner(globalScanner.nextLine());
                 
        WikiSparse newDoc = new WikiSparse();

        while (lineScanner.hasNext()) {
            int termId = lineScanner.nextInt();
            String d = lineScanner.next();
            double value = new Double(d);

            newDoc.addTerm(termId, value);
        }
        
        return newDoc;
    }
    
    public List<MetricElement> getElements(int n) {
        List <MetricElement> docList = new ArrayList(n);
        int i=0;
        while (globalScanner.hasNextLine()) {
            if (i >= n)  break;           
            Scanner lineScanner = new Scanner(globalScanner.nextLine());
                 
            WikiSparse newDoc = new WikiSparse();

            while (lineScanner.hasNext()) {
                int termId = lineScanner.nextInt();
                String d = lineScanner.next();
                double value = new Double(d);

                newDoc.addTerm(termId, value);
            }
            docList.add(newDoc);
                        
            i++;
        }
        System.out.println(i+ " documents readed");
        return docList;
    }
    
    public List <MetricElement> getShortQueries(int n, int wordCount, int maxWordNumber) {
        List <MetricElement> docList = new ArrayList(n);
        Random random = new Random(100);
        for (int i = 0; i < n; i++) {
            WikiSparse newDoc = new WikiSparse();
            for (int j = 0; j < wordCount; j++) 
                newDoc.addTerm(random.nextInt(maxWordNumber), 1);
            docList.add(newDoc);
        }
        return docList;
    }
    
    public List<MetricElement> getShortQueriesReal(int n, int wordCount) {
        List<MetricElement> docList = new ArrayList(n);
        Random random = new Random(100);
        for (int i = 0; i < n; i++) {
            Scanner lineScanner = new Scanner(globalScanner.nextLine());

            WikiSparse newDoc = new WikiSparse();

            while (lineScanner.hasNext()) {
                int termId = lineScanner.nextInt();
                String d = lineScanner.next();
                double value = new Double(d);

                newDoc.addTerm(termId, value);


            }
            
            
            
            
            
            docList.add(newDoc.getRandomSubDoc(random, wordCount));
        }
        return docList;
    }
}
