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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jcohn
 */
public class CombinedReportM extends CombinedReport<Long,Object[]> {
    
    public CombinedReportM(SignatureProperties props, File fnaFile, 
        SignatureModule module, Long pInterval) {       
        super(props,fnaFile,module,pInterval); 
    }
  
    @Override
    protected Object[] getMatchObj(String kmer) {
        Object[] map = new Object[2];
        map[0] = sigMap.getKmerHash(kmer);
        map[1] = kmer;
        return map;
    }

    @Override
    protected Set<Long> getNodeAssignSet(Set<Object[]> set) {
        Set<Long> nodeAssignSet = new HashSet<>(set.size());
        Iterator<Object[]> iter = set.iterator();
        while (iter.hasNext()) {
            Object[] elem = iter.next();
//            publish("node assign: "+elem[0]+" "+elem[1]);
            Long matchVal = (Long) elem[0];
            nodeAssignSet.add(matchVal);
        }
        return nodeAssignSet;
    }

    @Override
    protected Long getKmerForMatch(Object[] kmerObj) {
        Long matchVal = (Long) kmerObj[0];
        return matchVal;
    }

    @Override
    protected KmerSet<Object[]> getMatchedKmerSet(KmerSet<Object[]> inputKmerSet, Set<Long> matchedKmers) {
        
        KmerSet<Object[]> matchedKmerSet = new KmerSet<>(inputKmerSet.getOffset(),
            inputKmerSet.getFrameID(), inputKmerSet.getLength());
        
        Map<Object,Object[]> kmerSetMap = new HashMap<>(inputKmerSet.size());
        Iterator<Object[]> ksetIter = inputKmerSet.iterator();
        while (ksetIter.hasNext()) {
            Object[] elem = ksetIter.next();
            kmerSetMap.put(elem[0], elem);
        }
        Iterator<Long> iter = matchedKmers.iterator();
        while (iter.hasNext()) {
            Object matchElem = iter.next();
            Object[] inputElem = kmerSetMap.get(matchElem);
            matchedKmerSet.add(inputElem);          
        }
        
        matchedKmerSet.setFunctionSet(inputKmerSet.getFunctionSet());
        return matchedKmerSet;
        
    }
    
}