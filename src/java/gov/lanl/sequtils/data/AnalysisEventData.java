/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.data;

import gov.lanl.sequtils.sequence.KmerSet;
import gov.lanl.sequtils.sequence.SequencingRecord;
import java.util.Iterator;
import java.util.List;

/**
 * @author jcohn
 */
public class AnalysisEventData<W> {

    protected List<KmerSet<W>> kmerSetList;
    protected SequencingRecord sequencingRecord;
    protected NodeAssignment nodeAssignment;
    protected String assignmentType;
    protected boolean translateFlag;

    public AnalysisEventData(SequencingRecord seqRecord, List<KmerSet<W>> kmerSetLst,
        NodeAssignment nodeAssign, boolean tFlag) {
        kmerSetList = kmerSetLst;
        sequencingRecord = seqRecord;
        nodeAssignment = nodeAssign;
        translateFlag = tFlag;
    }
    
    public AnalysisEventData(SequencingRecord seqRecord, List<KmerSet<W>> kmerSetLst,
        NodeAssignment nodeAssign) {
        this(seqRecord, kmerSetLst, nodeAssign, false);
    }
    
    public boolean getTranslateFlag() {
        return translateFlag;
    }
  
    public SequencingRecord getSequencingRecord() {
        return sequencingRecord;
    }

    public List<KmerSet<W>> getKmerSetList() {
        return kmerSetList;
    }
    
    public Iterator<KmerSet<W>> getKmerSetIterator() {
        return kmerSetList.iterator();
    }
       
    public NodeAssignment getNodeAssignment() {
        return nodeAssignment;
    }

 
}
