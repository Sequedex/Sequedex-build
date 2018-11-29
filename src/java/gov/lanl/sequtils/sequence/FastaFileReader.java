/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import ch.qos.logback.classic.Level;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author jcohn
 */
public class FastaFileReader extends SequencingReader {
    
    // instance variables
    protected String currentHeader = null; 
    protected String fileType = null;
  
     
    public FastaFileReader(String fileName) {
        super(fileName);
    }
    
    public FastaFileReader (File file) {
        super(file);
    }
    
    public FastaFileReader(String fileName, String fType) {
        super(fileName);
        fileType = fType;
    }
    
    public FastaFileReader(File file, String fType) {
        super(file);
        fileType = fType;
    }
   
    @Override
    public String getFileType() {
        return fileType;
    }
    
//    public void setFileType(String fType) {
//        fileType = fType;
//    }
    
    @Override
    public SequencingRecord nextRecord() {
        
          
        if (reader == null) {
            boolean okay = open();
            if (!okay)
                return null;
        }
        
        if (fileType == null)
            fileType = SequencingReader.genSequenceFileType(seqFile);
        boolean dna = getDnaFlag();
        int seqType;
        if (dna)
            seqType = SequencingRecord.DNA;
        else
            seqType = SequencingRecord.PROTEIN;
        String line; 
        SequencingRecord record; 
        ArrayList<String> recordLines = new ArrayList<>();
        try {
            line = reader.readLine();
            if (line == null)
                return null;
            while (line != null) {
                if (currentHeader == null) {
                    if (line.startsWith(FASTA_BEGIN)) 
                        currentHeader = line.substring(1);
                }
                else {
                    if (line.startsWith(FASTA_BEGIN)) {
                        record = new SequencingRecord(currentHeader,recordLines,null,null,seqType); 
                        currentHeader = line.substring(1);
                        bytesProcessed += (long) (currentHeader.length() + 2);
                        Iterator<String> lineIter = recordLines.iterator();
                        while (lineIter.hasNext()) 
                            bytesProcessed += (long) (lineIter.next().length() + 1);
                        return record;
                    }
                    else
                        recordLines.add(line);
                        
                }
                line = reader.readLine();
            }
              
            
        } catch (IOException ex) {
            publish("Problem reading next record: "+
                ex.getMessage(), null, Level.ERROR);
            return null;   
        }
  
        if (currentHeader != null) {
            bytesProcessed += (long) (currentHeader.length() + 2);
            Iterator<String> lineIter = recordLines.iterator();
            while (lineIter.hasNext()) 
                bytesProcessed += (long) (lineIter.next().length() + 1);
            return new SequencingRecord(currentHeader, recordLines, null,null,seqType);
        }
        else
            return null;
    }
    
//    @Override
//    public String getFileType() {
//        return FASTA;
//    }
    
    public static void main(String[] argv) {
        
        // code copied from www.java2s.com
        
        String fileName1 = "/Users/jcohn/testData/Bacillus_mojavensis.150bp.fna.gz";
        String fileName2 = "/Users/jcohn/sqdxPrj/spike/input/CC3_S3_L001_R1_001.fastq.gz";
        String fileName3 = "/Users/jcohn/sequedexDev/testData/si_0681.faa";
        String fileName4 = "/Users/jcohn/sequedexDev/testData/si_0681.fas";
        String fileName5 = "/Users/jcohn/sequedexDev/testData/test.fa";
        String fileName6 = "/Users/jcohn/sequedexDev/testData/test2.fa";
        File seqFile1 = new File(fileName1);
        File seqFile2 = new File(fileName2);
        File seqFile3 = new File(fileName3);
        File seqFile4 = new File(fileName4);
        File seqFile5 = new File(fileName5);
        File seqFile6 = new File(fileName6);        
        String fileType1 = SequencingReader.genSequenceFileType(seqFile1);
        System.out.println(fileName1+"  "+fileType1);
        String fileType2 = SequencingReader.genSequenceFileType(seqFile2);
        System.out.println(fileName2+"  "+fileType2);  
        String fileType3 = SequencingReader.genSequenceFileType(seqFile3);
        System.out.println(fileName3+"  "+fileType3); 
        String fileType4 = SequencingReader.genSequenceFileType(seqFile4);
        System.out.println(fileName4+"  "+fileType4); 
        String fileType5 = SequencingReader.genSequenceFileType(seqFile5);
        System.out.println(fileName5+"  "+fileType5); 
        String fileType6 = SequencingReader.genSequenceFileType(seqFile6);
        System.out.println(fileName6+"  "+fileType6); 
        System.exit(0);
        
        
        
//        try {
//
//            // Since there are 4 constructor calls here, I wrote them out in full.
//            // In real life you would probably nest these constructor calls.
//            FileInputStream fin = new FileInputStream(FILENAME);
//            GZIPInputStream gzis = new GZIPInputStream(fin);
//            InputStreamReader xover = new InputStreamReader(gzis);
//            BufferedReader is = new BufferedReader(xover);
//
//            String line;
//            // Now read lines of text: the BufferedReader puts them in lines,
//            // the InputStreamReader does Unicode conversion, and the
//            // GZipInputStream "gunzip"s the data from the FileInputStream.
//            while ((line = is.readLine()) != null)
//                System.out.println("Read: " + line);
//        } catch (IOException ex) {
//            System.out.println(ex.getMessage());
//        }
        
        System.exit(0);
    }


  
}
