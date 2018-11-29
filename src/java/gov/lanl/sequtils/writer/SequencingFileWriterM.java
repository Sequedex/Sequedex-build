/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;

import gov.lanl.sequtils.sequence.KmerSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jcohn
 */
public class SequencingFileWriterM extends SequencingFileWriter<Object[]> {
    
    public SequencingFileWriterM () {
        super();
    }
    
    @Override
   protected Set getMatchSet(KmerSet<Object[]> kmerSet) {
       
       Iterator<Object[]> iter = kmerSet.iterator();
       Set<Object> set = new HashSet<>();
       while (iter.hasNext()) {
           Object[] elem = iter.next();
           set.add(elem[1]);
       }
       return set;   
       
    }
  
    
}
