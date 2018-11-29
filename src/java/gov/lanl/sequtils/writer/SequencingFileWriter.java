/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.data.AnalysisEventData;
import gov.lanl.sequtils.data.NodeAssignment;
import gov.lanl.sequtils.event.AnalysisEvent;
import gov.lanl.sequtils.sequence.*;
import gov.lanl.sequtils.util.StringOps;
import java.io.File;
import java.util.*;

/**
 *
 * @author jcohn
 */
public class SequencingFileWriter<W> extends AnalysisFileWriter<W> {
    
    // instance variables
    protected SequencingWriter sequencingWriter;
    protected String fileExt;
    protected int recordType;  // RF, READ, FRAG from ReportConstants  
                               // currently only RF is supported - so ignore
    
    public SequencingFileWriter () {
        super();
        sequencingWriter = null;
        recordType = RF;
        eventQueue = new LinkedList<>();
    }
   
    @Override
    public void setOutput(String db, File dir, String inputFilenm, 
        String inputFType) {
        super.setOutput(db, dir, inputFilenm, inputFType); 
        if (outputDir == null) {
            logger.error("Missing output directory for SequencingFileWriter");
            return;
        }
        if (inputFileName == null) {
            logger.error("Missing input file name for SequencingFileWriter");
            return;
        }
        if (inputFileType == null) {
            logger.error("Missing input file type for SequencingFileWriter");
        }
        String coreStr = dbName;
        if (coreStr == null) {
            publish("Name for db file is null; setting to default", this,Level.WARN);
            coreStr = "default";
        }
        
        String outputType;
        if (analysisOutputType == null || analysisOutputType.equals(SAME_AS_INPUT)) 
 //           outputType = SequencingReader.getInputFileType(inputFileName);
            outputType = inputFileType;
        else
            outputType = analysisOutputType;
        
        switch (outputType) {
            case FASTQ:
                fileExt = FASTQ_EXT;
                break;
            case FASTA_FAA:
                fileExt = FASTA_FAA_EXT;
                break;
            default:
                fileExt = FASTA_EXT;
                break;
        }
        File seqFile = new File(outputDir,coreStr+fileExt); 
        if (seqFile.exists())
            seqFile.delete();
 
        sequencingWriter = SequencingWriterFactory.getWriter(seqFile,outputType);   
 //       sequencingWriter.open();
    }     
    
    public boolean closeWriter() {
        if (sequencingWriter != null) {
            return sequencingWriter.close();
        }
        else
            return true;
    }
    
    @Override
    public void observeAnalysisEvent(AnalysisEvent<W> event) {
        // add event and immediately process it
//        eventsObserved++;
        eventQueue.add(event);
        processQueue();
    }

    @Override
    protected void processAnalysisEvent(AnalysisEvent<W> event) {
        
        int action = event.getAction();
        
        switch (action) {
            case AnalysisEvent.OPEN:
                openFile();
                return;
            case AnalysisEvent.CLOSE:
                closeFile();
                return;
            default:
                break;
        }
            
        // AnalysisEvent is PROCESS_DATA
        if (action != AnalysisEvent.PROCESS_DATA) {
            publish("Unknown action for SequencingFileWriter: "+action,this,Level.ERROR);
            return;
        }
        
        Short nomatch = (short) functionCount;
        AnalysisEventData<W> data = event.getData();
        SequencingRecord sequencingRecord = data.getSequencingRecord();
        String shortName = sequencingRecord.getShortHeader();
        int seqType = sequencingRecord.getSequenceType();
        boolean translateFlag = data.getTranslateFlag();
        NodeAssignment nodeAssign = data.getNodeAssignment();
        int assignFlag;
        if (nodeAssign.hasSingleKmer())
            assignFlag = SINGLE_KMER;
        else if (nodeAssign.hasSingleNode())
            assignFlag = SINGLE_NODE;
        else if (nodeAssign.isNotMonophyl())
            assignFlag = NON_MONOPHYL;        
        else
            assignFlag = MONOPHYL;
        short nodeNum = nodeAssign.getNodeNum();
        String nodeName = getNodeName(nodeNum);
               
        List<KmerSet<W>> kmerSetList = data.getKmerSetList(); 
        Iterator<KmerSet<W>> kmerSetIter = kmerSetList.iterator();
        Map<Short,Set<Object>> unqKmerSets = new TreeMap<>();
        Map<Short,Set<Short>> frameFunctionSets = new HashMap<>();
        
            // key is frameID, value is function set for the frame
        while (kmerSetIter.hasNext()) {
            KmerSet<W> fragKmerSet = kmerSetIter.next();
            Short frameID = (short) fragKmerSet.getFrameID();
            Set<Object> frameUnqKmerSet = unqKmerSets.get(frameID);
            if (frameUnqKmerSet == null) {
                frameUnqKmerSet = new TreeSet<>();
                unqKmerSets.put(frameID,frameUnqKmerSet);
            }
            Set frameUnqKmers = getMatchSet(fragKmerSet);
            @SuppressWarnings("unchecked")
            boolean setChanged = frameUnqKmerSet.addAll(frameUnqKmers);           
            Set<Short> fragFunctionSet = fragKmerSet.getFunctionSet();
            if (functionCount > 0 && fragFunctionSet != null && fragFunctionSet.size() > 0) {
                Set<Short> frameFunctionSet = frameFunctionSets.get(frameID);
                if (frameFunctionSet == null) {
                    frameFunctionSet = new TreeSet<>();
                    frameFunctionSets.put(frameID, frameFunctionSet);
                }
                frameFunctionSet.addAll(fragFunctionSet);  
            }
        }
      
        String filePath = sequencingWriter.getFile().getAbsolutePath();
        Iterator<Short> frameIter = unqKmerSets.keySet().iterator();
        // kludge:  for now protein sequence has only one "frame"
        // in future should restructure so that this is built in not an assumption
        while (frameIter.hasNext()) {
            
            Short frameID = frameIter.next();
            String seqName = shortName + "_" + frameID.toString();
            Set unqKmerSet = unqKmerSets.get(frameID);            
            int unqKmerCnt = unqKmerSet.size();
            String frameSeq;
            if (seqType == SequencingRecord.DNA) {
                frameSeq = getFrameSequence(sequencingRecord, 
                    frameID.intValue(),translateFlag);
            }
            else
                frameSeq = sequencingRecord.getSequence();
           
            if (frameSeq == null) {
                logger.error("Problem getting frameSeq for: "+seqName,
                    this, Level.ERROR);
                return;
            }
            Set<Short> frameFunctionSet;
            if (functionCount > 0) 
                frameFunctionSet = frameFunctionSets.get(frameID);
            else
                frameFunctionSet = null;
            if (frameFunctionSet != null && frameFunctionSet.size() > 1) {
                boolean wasRemoved = frameFunctionSet.remove(nomatch);
            }
            Set<String> functionStrSet = getFunctionStrSet(frameFunctionSet);
//            String seqName = shortName + "_" + frameID.toString();
            String header = generateHeader(seqName, nodeName, assignFlag,
                unqKmerCnt, functionStrSet, unqKmerSet);//, frameKmerList) ; 
            String frameQualStr = getFrameQualStr(sequencingRecord, frameID.intValue());
            if (fileExt.equals(FASTQ_EXT) && frameSeq.length() != frameQualStr.length()) {
               String msg = seqName + ": sequence and qualStr are not the same length";
               publish(msg,this,Level.ERROR);
            }
            
            SequencingRecord outputRecord = new SequencingRecord(header,
                frameSeq,sequencingRecord.getQualHeader(),frameQualStr,
                seqType);
            boolean okay = sequencingWriter.writeRecord(outputRecord);
            if (!okay) {
                logger.error("Problem writing to "+filePath);
                return;
            }
        }
        
        boolean okay = sequencingWriter.flush();
        if (!okay)
            publish("Problem flushing buffer to "+filePath,this,Level.ERROR);
           
    }
    

    protected String generateHeader(String seqName, String nodeName, 
        int assignFlag, int uniqueKmerCount, Set<String> functionList, Set unqKmerSet)  { 
            
        StringBuilder builder = new StringBuilder(100);
        builder.append(seqName);
        builder.append(TAB);
        builder.append("node=");
        builder.append(nodeName);
        builder.append(TAB);
        builder.append("assign=");
        builder.append(Integer.toString(assignFlag));
        builder.append(TAB);
        builder.append("unq_k=");
        builder.append(Integer.toString(uniqueKmerCount));
        if (unqKmerSet != null && !unqKmerSet.isEmpty() && kmerFlag) {
            addKmersToHeaderStr(unqKmerSet, builder);
        }

        if (functionList != null && !functionList.isEmpty()) {
            builder.append(TAB);
            builder.append("func=");
            Iterator<String> functionIter = functionList.iterator();
            boolean beginFlag = true;
            while (functionIter.hasNext()) {
                if (!beginFlag)
                    builder.append("|");
                else
                    beginFlag = false;
                builder.append(functionIter.next());                
            }
        }


        return builder.toString();
    }
    
    protected void addKmersToHeaderStr(Set kmers, StringBuilder builder) {
        
        builder.append(TAB);
        builder.append("kmer=");
        Iterator kIter = kmers.iterator();
        boolean beginFlag = true;
        while (kIter.hasNext()) {
            if (!beginFlag)
                builder.append("|");
            else
                beginFlag = false;
            builder.append(kIter.next());                
        }     
    }
    
    protected String getFrameSequence(SequencingRecord record, int frameID, 
        boolean translateFlag) {
        
        if (record.getSequenceType() != SequencingRecord.DNA)
            return null;
        
        boolean doTranslation;
        doTranslation = !fileExt.equals(FASTQ_EXT) && 
           record.getSequenceType() == SequencingRecord.DNA && translateFlag;
        
 
        String seq, finalSeq;
        DNASequence dnaSeq = new StandardDNASequence(record);
        
        if (doTranslation) {
            String protSeq = dnaSeq.translate2Str(frameID,false);
                //dnaSeq.translate2Str(frameID,true);
            finalSeq = protSeq.toUpperCase();
        }
        else {
            if (frameID > 3)
                seq = dnaSeq.reverse().toUpperCase();
            else
                seq = dnaSeq.sequence().toUpperCase();
            int remainder = frameID%3;

            // add extra X or XX as necessary to create sequence starting
            // in appropriate reading frame
            String paddedSeq;
            switch (remainder) {
                case 1:
                    paddedSeq = seq;
                    break;
                case 2:
                    paddedSeq = "NN"+seq;
                    break;
                default:
                    // remainder == 0
                    paddedSeq = "N"+seq;
                    break;
            }
            finalSeq = paddedSeq;
        }  
        
       return finalSeq; 
       
    }
    
    protected String getFrameQualStr(SequencingRecord record, int frameID) {
        String qualScores = record.getQualScores();
        if (qualScores == null)
            return null;
        String qualStr;
        if (frameID <= 3) 
            qualStr = qualScores;
        else 
            qualStr = record.getReverseQualScores();
        int remainder = frameID%3;
        String frameQualStr;
        char firstQualChar = qualStr.charAt(0);
        switch (remainder) {
            case 1:
                frameQualStr = qualStr;
                break;
            case 2:
                // putting empty string before char in concat tells the compiler
                // that one is concatenating strings;
                // otherwise, get weird output from concat of chars if they precede string
                frameQualStr = "" + firstQualChar + firstQualChar + qualStr;
                break;
            default:
                // remainder == 0
                // putting empty string before char in concat tells the compiler
                // that one is concatenating strings;
                // otherwise, get weird output from concat of chars if they precede string
                frameQualStr = "" + firstQualChar + qualStr;
                break;
        }
        
        return frameQualStr;
    }
    
    protected Set<String> getFunctionStrSet(Set<Short> functionSet) {
        if (functionSet == null)
            return null;
        Set<String> funcStrSet = new TreeSet<>();
        Iterator<Short> funcIter = functionSet.iterator();
        while (funcIter.hasNext()) {
            Short functionIndex = funcIter.next();
            if (functionDetail != null) {
                String[] detailArr = functionDetail.get(functionIndex);
                String functionShortName = detailArr[0];
                funcStrSet.add(functionShortName);
            }
            else {
                funcStrSet.add("fn"+StringOps.leftFill(functionIndex.toString(),4,'0'));
            }
        }
        return funcStrSet;
    }
    
    protected String getNodeName(Short nodeNum) {
        if (nodeDetail != null) {
            String[] detailArr = nodeDetail.get(nodeNum);
            return detailArr[0];
        }
        else {
            return "nd"+StringOps.leftFill(nodeNum.toString(),4,'0');
        }
    }
    
    protected Set getMatchSet(KmerSet<W> kmerSet) {
        return kmerSet;
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (sequencingWriter != null) {
            sequencingWriter.flush();
            sequencingWriter.close();
        }
        super.finalize();
    }
    
    public static void main(String[] args) {
        
        String str = "1>??=123";
        char firstChar = str.charAt(0);
        System.out.println(firstChar+firstChar+str);
        System.out.println(""+firstChar+firstChar+str);
               
    }

    @Override
    protected boolean openFile() {
        sequencingWriter.open();
        return true;
    }

    @Override
    protected boolean closeFile() {
        return closeWriter();
    }
  
}
