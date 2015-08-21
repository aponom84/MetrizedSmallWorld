package org.latna.msw.documents;

import org.latna.msw.MetricElement;
import org.latna.msw.MetricElementFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Reads the file directory and creates list of Documents
 * @author Alexander Ponomarenko aponom84@gmail.com
 */
public class DocumentFileFactory implements MetricElementFactory {
    public String testDataDirPath="C:/msw/MetricSpaceLibrary/dbs/documents/short";
    private int maxDoc; 

    @Override
    public List<MetricElement> getElements() {
        List <MetricElement> docList = new ArrayList();
        File dir = new File(testDataDirPath);
        int i = 0;
        for (File file: dir.listFiles() ) {
            if (i >  maxDoc)  break;
            
            Scanner sc = null;
            try {
                 sc = new Scanner(file);
                 Document newDoc = new Document(file.getName());

                 while (sc.hasNext()) {
                     String termId = sc.next();
                     String d = sc.next();
                     double value = new Double(d);

                     newDoc.addTerm(termId, value);
                 }
                 docList.add(newDoc);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            } finally {
                sc.close();
            }            
            i++;
        }
        System.out.println(i+ " documents readed");
        return docList;
    }
    /**
     * DocumentFileFactory get two parameters: [dir="filePath"] - path to directory 
     * which contains set of documents presented by frequnce term vector and 
     * [maxDoc=d+] - the restriction on the number returned documents. 
     * if you want do not restrict the number of documents set maxDoc=0
     * @param param 
     */
    @Override
    public void setParameterString(String param) {
         Scanner s = new Scanner(new StringReader(param));
            s.findInLine("dir=(.+); maxDoc=(\\d+)");
            MatchResult matchResult = s.match();

            for (int i=1; i<=matchResult.groupCount(); i++)
                System.out.println(matchResult.group(i));

            s.close();

          testDataDirPath = matchResult.group(1);
          maxDoc = Integer.valueOf(matchResult.group(2));
          
          if (maxDoc == 0) maxDoc =  Integer.MAX_VALUE;
        
    }

}
