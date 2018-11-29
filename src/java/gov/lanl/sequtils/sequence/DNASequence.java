/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Currently DNASequence is restricted to a maximum length dependent upon int size
 * since char[] is used in some methods
 */
package gov.lanl.sequtils.sequence;

import ch.qos.logback.classic.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import org.slf4j.MDC;


/**
 *
 * @author jcohn
 */
abstract public class DNASequence extends Sequence {
    
    // constant variables
    public static final char[] EXTENDED_DNA_CHARS =
        "ACGTMRWSYKVHDBN-".toCharArray();
    public static final String EXTENDED_DNA_ALPHABET = " Extended DNA Alphabet";
    
    public static final String STOP_CHAR = "*";
    public static final String START_CHAR = "~";
    
    // instance variables
    protected Map<String,Character> codonMap;
    protected Set<String> startCodons;
   
    public DNASequence(String name, String sequence) {
        super(name,sequence);
        init();
    }
    
    public DNASequence(SequencingRecord record) {
        this(record.getShortHeader(), record.getSequence());
    }
    
    public final void init() {
        createAlphabet();
        initCodonMap();
        initStartCodons();
 //       MDC.put("seqName",name);
    }
    
    
    @Override
    protected void createAlphabet() {
        alphabet = new Alphabet(EXTENDED_DNA_ALPHABET,EXTENDED_DNA_CHARS);
    } 
    
    public String reverse() {
        int seqLen = letters.length();
        char[] reverseArr = new char[seqLen];
        int end = seqLen-1;
        for (int i=0;i<seqLen;i++) {
            reverseArr[end-i] = reverseChar(letters.charAt(i));  
        }
        return new String(reverseArr);
    }

    public int reverseIndex(int index) {
        int end = letters.length()-1;
        return end - index;
    }
       
    public String readingFrame(int frameID) {
        if (frameID < 1 || frameID > 6) {
            publish("Invalid reading frame: "+frameID,Level.ERROR);
            return null;
        }
        
        int len = length();
        if (len < 1)
            return null;
        boolean exitFlag = false;
        if (len < 2) {
            if (frameID == 1 || frameID == 4)
                exitFlag = false;
            else
                exitFlag = true;
        }
        else if (len < 3) {
            if (frameID == 3 || frameID == 6)
                exitFlag = true;
            else
                exitFlag = false;
        }
        if (exitFlag)
            return null;
        
        int offset;
        if (frameID >=1 && frameID <=3) {
            offset = frameID-1;
            return letters.substring(offset);
        }
        else {
            offset = frameID - 4;
            String reverseLetters = reverse();
            return reverseLetters.substring(offset);
        }
        
    }
    
    public static char reverseChar(char rawBase) {

        char base = Character.toUpperCase(rawBase);
        switch(base) {
            case 'A':
                return 'T';
            case 'C':
                return 'G';
            case 'G':
                return 'C';
            case 'T':
                return 'A';
            case 'M':
                return 'K';
            case 'R':
                return 'Y';
            case 'W':
                return 'W';
            case 'S':
                return 'S';
            case 'Y':
                return 'R';
            case 'K':
                return 'M';
            case 'V':
                return 'B';
            case 'H':
                return 'D';
            case 'D':
                return 'H';
            case 'B':
                return 'V';
            case 'N':
                return 'N';
            case '-':
                return '-';
            default:
                return 'N';
        }

    }
    
    public Character translateCodon(String codon) {
        return codonMap.get(codon);
    }
    
    public List<FragmentRecord> translate2Fragments(int frameID) {
        return translate2Fragments(frameID,false);
    }
    
   
    public List<FragmentRecord> translate2Fragments(int frameID, boolean ambiguousFlag) {
        List<FragmentRecord> fragments = new ArrayList<>();
        
        String frameSeq = readingFrame(frameID);
        if (frameSeq == null)
            return null;
        StringBuilder buf = new StringBuilder((frameSeq.length()/3)+1);
        int bufOffset = 0;
        for (int i=0; i<frameSeq.length()-2; i+=3) {
            String codon = frameSeq.substring(i,i+3);
            Character chr = translateCodon(codon);
            if (chr == null) {
                if (ambiguousFlag)
                    chr = '*';
                else
                    chr = 'X';
            }
            if (chr.equals('*')) {
                fragments.add( new FragmentRecord( buf.toString(), getFrameOffset( bufOffset, frameID), frameID));
                buf.setLength(0);  //buf = new StringBuilder((frameSeq.length()/3)+1);
                bufOffset = i;
            }
            else
                buf.append(chr);
        }
        
        if (buf.toString().length() > 0) {
        	fragments.add( new FragmentRecord( buf.toString(), getFrameOffset( bufOffset, frameID), frameID));
        }
            
        return fragments;
    }
    
       public String translate2Str(int frameID, boolean ambiguousFlag) {
        
        StringBuilder builder = new StringBuilder();
        
        String frameSeq = readingFrame(frameID);
        if (frameSeq == null)
            return null;
        StringBuilder buf = new StringBuilder((frameSeq.length()/3)+1);
        int bufOffset = 0;
        for (int i=0; i<frameSeq.length()-2; i+=3) {
            String codon = frameSeq.substring(i,i+3);
            Character chr = translateCodon(codon);
            if (chr == null) {
                if (ambiguousFlag)
                    chr = '*';
                else
                    chr = 'X';
            }
            builder.append(chr);
        }
         
        return builder.toString();
    }
    
    private int getFrameOffset( int i, int frameID){
    	int frameOffset = 0;
    	if( frameID <= 3){
    		frameOffset = i+frameID-1;
    	}
    	else{
    		frameOffset = (letters.length()-i-frameID+1); 
    	}
    	return frameOffset;
    }
    
    public Set<String> getStartCodons() {
        return startCodons;
    }
    
    protected abstract void initCodonMap();
    protected abstract void initStartCodons();

}
