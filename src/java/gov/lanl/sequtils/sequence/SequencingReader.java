/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.ReportConstants;
import static gov.lanl.sequescan.constants.ReportConstants.FASTA_BEGIN;
import gov.lanl.sequtils.log.MessageManager;
import static gov.lanl.sequtils.log.MessageManager.publish;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author jcohn
 */
abstract public class SequencingReader extends MessageManager implements ReportConstants {
    
    // instance variables
    protected File seqFile;
    protected long bytesProcessed = 0;
    protected BufferedReader reader = null;
    protected boolean gzipFlag;
    protected Boolean dnaFlag;
    
    public SequencingReader(String fileName) {
        this(new File(fileName));
    }
    
    public SequencingReader (File file) {
        seqFile = file;
        gzipFlag = seqFile.getName().endsWith(".gz");
    }
    
    public File getFile() {
        return seqFile;
    }
    
    public long getFileSize() {
        if (gzipFlag)
            return -1;
        else
            return seqFile.length();
    }
    
    public long getBytesProcessed() {
        return bytesProcessed;
    }
    
    public String getFileName() {
        return seqFile.getAbsolutePath();
    }
    
    // used when first starting to analyze file and don't yet know type
    // otherwise use instance getFileType function
    public static String genSequenceFileType(File seqFile) {       
        return genSequenceFileType(seqFile,false);
    }
    
       public static String genSequenceFileType(File seqFile, boolean aqFlag) {
        String unzipName;
        String fileName = seqFile.getName();
        if (fileName.endsWith(".zip"))
            return null;
        else if (fileName.endsWith(".gz")) 
            unzipName = getProcessFileName(fileName);
        else 
            unzipName = fileName;
        if (unzipName == null)
            return null;
        else if (unzipName.endsWith(".fq") || unzipName.endsWith(".fastq"))
            return FASTQ;
        else if (unzipName.endsWith(".faa"))
            return FASTA_FAA;
        else if (unzipName.endsWith(".fna") || unzipName.endsWith(".ffn"))
            return FASTA;
        else {
            if (aqFlag)
                return FASTA;
            else
                return checkFastaSequenceType(seqFile);
        }
    }
    
    
    public static String checkFastaSequenceType(File seqFile) {
        // assume FASTA file since .fq or .fastq automatically assumed to be FASTQ
        // thus trying to determine if FASTA or FASTA_FAA
        
        String fType = null;
        boolean gzipFlag = seqFile.getName().endsWith(".gz");
        BufferedReader reader = null;
        
        // open file
        try {
            if (gzipFlag) {
               String fileName = seqFile.getAbsolutePath();
                FileInputStream fIn = new FileInputStream(fileName);
                GZIPInputStream gzIn = new GZIPInputStream(fIn);
                InputStreamReader inRdr = new InputStreamReader(gzIn);
                reader = new BufferedReader(inRdr);       
            }
            else {
                FileReader freader = new FileReader(seqFile);
                reader = new BufferedReader(freader);
            }
        } catch (IOException ex) {
            logger.error("Problem opening FastaFile "+seqFile.getAbsolutePath());
            return null;           
        }
        
        // look at first 3 records (is that enough??)
        String line;
        String currentHeader = null;
        SequencingRecord record;
        ArrayList<String> recordLines = new ArrayList<>();
        int recordMax = 3;
        int recordNum = 0;
        Boolean dnaFlag = null;
        float percentDNA = 0;
        float percentNonDNA = 0;
        
        try {
            line = reader.readLine();
            if (line == null)
                return null;
            while (line != null && recordNum <= recordMax) {
                if (currentHeader == null) {
                    if (line.startsWith(FASTA_BEGIN)) 
                        currentHeader = line.substring(1);
                }
                else {
                    if (line.startsWith(FASTA_BEGIN)) {
                        record = new SequencingRecord(currentHeader,recordLines,
                            null,null,SequencingRecord.DNA); 
                        recordNum++;
                        currentHeader = line.substring(1);
                        Sequence seq = new StandardDNASequence(record);
                        Set<Character> dnaMatchSet = new TreeSet<>();
                        dnaMatchSet.add('A');
                        dnaMatchSet.add('C');
                        dnaMatchSet.add('G');
                        dnaMatchSet.add('T');
                        Set<Character> nonAlphabetLetters = Sequence.getNonAlphabetSet(seq);                        
                        float percentDNALetters = Sequence.getPercentSeqLetters(
                            seq, dnaMatchSet);
//                        System.out.println("percentDNALetters: "+percentDNALetters);
                        percentDNA += percentDNALetters;
                        float percentNonAlphabetLetters = Sequence.getPercentSeqLetters(
                            seq, nonAlphabetLetters);
//                        System.out.println("percentNonAlphabetMatch: "+percentNonAlphabetLetters);
                        percentNonDNA += percentNonAlphabetLetters;                       
                    }
                    else
                        recordLines.add(line);
                        
                }
                line = reader.readLine();
            }
            
//            System.out.println("percentDNA "+percentDNA);
//            System.out.println("percentNonDNA "+percentNonDNA);
//            if (percentNonDNA != 0)
//                System.out.println("ratio "+percentDNA/percentNonDNA);
//            
            if (percentNonDNA == 0)
                fType = FASTA;
            else if (percentDNA/percentNonDNA > 10)
                fType = FASTA;
            else
                fType = FASTA_FAA;            
        
        } catch (IOException ex) {
            publish("Problem reading next record: "+
                ex.getMessage(), null, Level.ERROR);
            return null;   
        }
  
        try {
              reader.close();
            } catch (IOException ex) {
                logger.error("Problem closing reader: "+ex.getMessage());                   
            }
    
        return fType;
 
    }
    
    public static String getProcessFileName(String rawFileName) {
        if (rawFileName.endsWith(".gz")) 
            return rawFileName.substring(0,rawFileName.length()-3);
        else
            return rawFileName;
    }
    
    public static boolean validInputFile(String fileName, List<String> validExtList) {
      
        if (validExtList == null || validExtList.isEmpty())
            return false;
        
        Iterator<String> iter = validExtList.iterator();
        while (iter.hasNext()) {
            if (fileName.endsWith("."+iter.next()))
                return true;
        }
        
        return false;
        
    }
    
    public boolean open() {
            
        try {
            if (gzipFlag) {
               String fileName = getFileName();
                FileInputStream fIn = new FileInputStream(fileName);
                GZIPInputStream gzIn = new GZIPInputStream(fIn);
                InputStreamReader inRdr = new InputStreamReader(gzIn);
                reader = new BufferedReader(inRdr);
        
            }
            else {
                FileReader freader = new FileReader(seqFile);
                reader = new BufferedReader(freader);
            }
        } catch (IOException ex) {
            logger.error("Problem opening FastaFile "+seqFile.getAbsolutePath());
            return false;            
        }
        
        return true;
            
    }
    
   
    public boolean close() {
        if (reader == null) {
            logger.warn("Reader is null");
            return false;
        }
        else {
            try {
                reader.close();
                reader = null;
            } catch (IOException ex) {
                logger.error("Problem closing reader: "+ex.getMessage());                   
                reader = null;
                return false;
            }
            return true;
        }
    } 
    
    public final Boolean getDnaFlag() {
        if (dnaFlag == null) {
            String fType = getFileType();
            if (fType == null)
                return null;
            else 
                switch(fType) {
                    case FASTA_FAA:
                        return false;
                    default:
                        return true;
                }
        }
        else
            return dnaFlag;
    }
    
    // abstract methods  
    abstract public SequencingRecord nextRecord();
    abstract public String getFileType();
    
 
    
    
}
