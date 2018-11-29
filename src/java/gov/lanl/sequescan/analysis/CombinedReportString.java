/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.analysis;

import gov.lanl.sequescan.signature.SignatureModule;
import gov.lanl.sequescan.signature.SignatureProperties;
import gov.lanl.sequtils.sequence.KmerSet;
import java.io.File;
import java.util.Set;

/**
 *
 * @author jcohn
 */
public class CombinedReportString extends CombinedReport<String,String> {
    
    public CombinedReportString(SignatureProperties props, File fnaFile, 
        SignatureModule module, Long pInterval) {       
        super(props,fnaFile,module,pInterval); 
    }
  
    @Override
    protected String getMatchObj(String kmer) {
        return kmer;
    }

    @Override
    protected Set<String> getNodeAssignSet(Set<String> set) {
        return set;
    }

    @Override
    protected String getKmerForMatch(String kmerObj) {
        return kmerObj;
    }
    
    @Override
    protected KmerSet<String> getMatchedKmerSet(KmerSet<String> inputKmerSet, Set<String> matchedKmers) {
         return inputKmerSet;
    }
    
    
}
