/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class StandardDNASequence extends DNASequence {
    
    public StandardDNASequence(String name, String sequence) {
        super(name,sequence);
    }
    
    public StandardDNASequence(SequencingRecord record) {
        super(record);
    }
    
    @Override
    protected void initCodonMap() {
        
        codonMap = new TreeMap<>();
        
        codonMap.put("TTT", 'F');
        codonMap.put("TTC", 'F');
        codonMap.put("TTA", 'L');
        codonMap.put("TTG", 'L');
        
        codonMap.put("TCT", 'S');
        codonMap.put("TCC", 'S');
        codonMap.put("TCA", 'S');
        codonMap.put("TCG", 'S');
        
        codonMap.put("TAT", 'Y');
        codonMap.put("TAC", 'Y'); // TAC no code ?
        codonMap.put("TAA", '*');
        codonMap.put("TAG", '*');
        
        codonMap.put("TGT", 'C');
        codonMap.put("TGC", 'C'); // TGC no code ?
        codonMap.put("TGA", '*');
        codonMap.put("TGG", 'W');
              
        codonMap.put("CTT", 'L');
        codonMap.put("CTC", 'L');
        codonMap.put("CTA", 'L');
        codonMap.put("CTG", 'L');
        
        codonMap.put("CCT", 'P');
        codonMap.put("CCC", 'P');
        codonMap.put("CCA", 'P');
        codonMap.put("CCG", 'P');
        
        codonMap.put("CAT", 'H');
        codonMap.put("CAC", 'H');
        codonMap.put("CAA", 'Q');
        codonMap.put("CAG", 'Q');
        
        codonMap.put("CGT", 'R');
        codonMap.put("CGC", 'R');
        codonMap.put("CGA", 'R');
        codonMap.put("CGG", 'R');
        
        codonMap.put("ATT", 'I');
        codonMap.put("ATC", 'I');
        codonMap.put("ATA", 'I');
        codonMap.put("ATG", 'M');
        
        codonMap.put("ACT", 'T');
        codonMap.put("ACC", 'T');
        codonMap.put("ACA", 'T');
        codonMap.put("ACG", 'T');
        
        codonMap.put("AAT", 'N');
        codonMap.put("AAC", 'N');
        codonMap.put("AAA", 'K');
        codonMap.put("AAG", 'K');
        
        codonMap.put("AGT", 'S');
        codonMap.put("AGC", 'S');
        codonMap.put("AGA", 'R');
        codonMap.put("AGG", 'R');
        
        codonMap.put("GTT", 'V');
        codonMap.put("GTC", 'V');
        codonMap.put("GTA", 'V');
        codonMap.put("GTG", 'V');
        
        codonMap.put("GCT", 'A');
        codonMap.put("GCC", 'A');
        codonMap.put("GCA", 'A');
        codonMap.put("GCG", 'A');
        
        codonMap.put("GAT", 'D');
        codonMap.put("GAC", 'D');
        codonMap.put("GAA", 'E');
        codonMap.put("GAG", 'E');
        
        codonMap.put("GGT", 'G');
        codonMap.put("GGC", 'G');
        codonMap.put("GGA", 'G');
        codonMap.put("GGG", 'G');   
       
    }
    
    @Override
    protected void initStartCodons() {
        
        startCodons = new TreeSet<>();
        startCodons.add("ATG");
        startCodons.add("GTG");
        startCodons.add("TTG");
                
    } 
    
    public static void main(String[] args) {
 
        String seq = "ANAGGNGGGGAGCCGGGAACTAAGGCGCGGTAGTG";
        System.out.println("DNA: "+seq);
        StandardDNASequence dnaSeq = new StandardDNASequence("test1",seq);
        String revSeq = dnaSeq.reverse();
        System.out.println("reverse: "+revSeq);
        List<FragmentRecord> fragments = dnaSeq.translate2Fragments(1);
        Iterator<FragmentRecord> iter = fragments.iterator();
        System.out.println("Fragments: count="+fragments.size());
        while (iter.hasNext()) {
            FragmentRecord fragRec = iter.next();
            System.out.println("length: "+fragRec.getSeq().length()+" sequence: "+fragRec.getSeq());
        }
        System.exit(0);
    }
      
}
