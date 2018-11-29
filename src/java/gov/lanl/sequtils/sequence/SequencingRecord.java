/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * New parent class to support both Fasta and FastQ files
 * 
 * @author jcohn
 */
public class SequencingRecord {
    
    // constants
    public static int DNA = 0;
    public static int PROTEIN = 1;
    public static int RNA = 2;
    
    
    
    protected String header;
    protected String shortHeader;
    protected String sequence;  
    protected String qualScores;
    protected String qualHeader;
    protected int sequenceType;
    
    public SequencingRecord(String hdr, String seq, String qualHdr, 
            String qualScoreStr, int seqType) {
        header = hdr;
        sequence = seq;
        if (header != null) {
            StringTokenizer stok = new StringTokenizer(header);
            shortHeader = stok.nextToken(); 
        }
        else {
            header = "empty";
            shortHeader = "empty";
        }
        qualHeader = qualHdr;
        qualScores = qualScoreStr;
        sequenceType = seqType;
    }
    
    public SequencingRecord(String hdr, String seq, int seqType) {
        this(hdr, seq, null,null, seqType);
    }
    
    public SequencingRecord(String hdr, String seq) {
        this(hdr, seq, null,null, DNA);
    }
   
    public SequencingRecord(String hdr, String seq, String qualHdr, 
            String qualScoreStr) {
        this(hdr, seq, qualHdr, qualScoreStr, DNA);
    }
    
    public SequencingRecord(String hdr, ArrayList<String> seqLines,
        String qualHdr, String qualScoreStr, int seqType) {
        
        StringBuilder buf = new StringBuilder(1000);
        Iterator<String> lineIter = seqLines.iterator();
        while (lineIter.hasNext())
            buf.append(lineIter.next());
        header = hdr;
        if (header != null) {
            StringTokenizer stok = new StringTokenizer(header);
            shortHeader = stok.nextToken(); 
        }
        else {
            header = "empty";
            shortHeader = "empty";
        }
        sequence = buf.toString();
        qualHeader = qualHdr;
        qualScores = qualScoreStr;
        sequenceType = seqType;
    }
    
    public SequencingRecord(String hdr, ArrayList<String> seqLines) {
        this(hdr, seqLines, null, null, DNA);
    }
   
    
    public String getHeader() {
        return header;
    }
    
    public String getShortHeader() {
        return shortHeader;
    }
    
    public String getQualHeader() {
        if (qualHeader == null)
            return header;
        else
            return qualHeader;
    }
    
    public String getSequence() {
        return sequence;
    }
    
    public String getQualScores() {
        // should we fake this if qualScores == null ???
        return qualScores;
    }
    
    public String getReverseQualScores() { 
        if (qualScores == null || qualScores.length() == 0)
            return null;
        
        int seqLen = qualScores.length();
        char[] reverseArr = new char[seqLen];
        int end = seqLen-1;
        for (int i=0;i<seqLen;i++) {
            reverseArr[end-i] = qualScores.charAt(i);  
        }
        return new String(reverseArr);
    }
    
    public int getSequenceType() {
        return sequenceType;
    }

    
    @Override
    public String toString() {
        return header+"|"+sequence;
    }
    
}
