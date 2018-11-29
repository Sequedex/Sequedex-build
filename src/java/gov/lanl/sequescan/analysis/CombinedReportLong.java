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
public class CombinedReportLong extends CombinedReport<Long,Long> {
    
    public CombinedReportLong(SignatureProperties props, File fnaFile, 
        SignatureModule module, Long pInterval) {       
        super(props,fnaFile,module,pInterval); 
    }
    
    @Override
    protected Long getMatchObj(String kmer) {
        return sigMap.getKmerHash(kmer);
    }

    @Override
    protected Set<Long> getNodeAssignSet(Set<Long> set) {
        return set;
    }

    @Override
    protected Long getKmerForMatch(Long kmerObj) {
        return kmerObj;
    }

    @Override
    protected KmerSet<Long> getMatchedKmerSet(KmerSet<Long> inputKmerSet, Set<Long> matchedKmers) {
         return inputKmerSet;
    }
    
}
