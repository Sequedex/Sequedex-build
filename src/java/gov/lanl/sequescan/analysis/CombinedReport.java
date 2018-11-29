/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.analysis;

import gov.lanl.sequescan.constants.SignatureConstants;
import gov.lanl.sequescan.constants.TreeConstants;
import gov.lanl.sequescan.signature.ModuleFactory;
import gov.lanl.sequescan.signature.SignatureMap;
import gov.lanl.sequescan.signature.SignatureModule;
import gov.lanl.sequescan.signature.SignatureProperties;
import gov.lanl.sequescan.tree.TreeManager;
import gov.lanl.sequescan.constants.ReportConstants;
import gov.lanl.sequtils.data.AnalysisEventData;
import gov.lanl.sequtils.data.NodeAssignment;
import gov.lanl.sequtils.data.ProgressEventData;
import gov.lanl.sequtils.event.AnalysisEvent;
import gov.lanl.sequtils.event.AnalysisObserver;
import gov.lanl.sequtils.event.ProgressEvent;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.sequence.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.signature.SignatureMapMLongArr;
import gov.lanl.sequescan.signature.SignatureMapMLongList;
import gov.lanl.sequescan.signature.SignatureMapMStrArr;
import gov.lanl.sequescan.signature.SignatureMapMStrList;
/**
 *
 * @author Judith Cohn <jcohn@lanl.gov>
 */
abstract public class CombinedReport<K,W> extends MessageManager implements Runnable, TreeConstants, ReportConstants,
    SignatureConstants  {
     
    // instance variables
    protected SequencingReader reader;
    protected SignatureMap sigMap;
    protected SignatureModule dataModule;
    protected SignatureProperties sigProps;
    protected long[] fileCnts;   // READ, RF, FRAG
    protected long[] cutoffCnts;  // READ, RF, FRAG, PHYL_FRAG, FUNC_FRAG
    protected long totFragSizeBP;
    protected long totFragSizeInSchemeBP;
    protected long[] readsByAssignType;   
        // [5]: TOTAL, SINGLE_KMER, SINGLE_NODE, MONOPHYL, NON_MONOPHYL
    protected long[][] readsByNode;  // [internalNodeCnt][5];
    protected double[][] fragsWDW; 
        // [functionCnt+1][internalNodeCnt] - note:  I have added a row for fragments 
        // which don't have a function match
    protected double[] fragsFunc;
 //   protected Set<String> matchingKmerSet;  // unused for now
    protected TreeManager treeMgr;
    protected int kmerSize;
    protected int functionCnt;
    protected boolean hexFlag;
    protected Integer cutoff;  // minimum length of protein fragment for analysis
    protected String job;   // fnaFile.getName();
    protected long fastaReadTime;
    protected long translateChopTime;
    protected long matchTime;
    protected long assignTime;
    protected long mapReadTime;
    protected long beginRunTime;
    protected long endRunTime;
    protected long readsProcessed;
    protected long bpProcessed;
    protected long bpFragAboveCutoff;
    protected long bpFragWithMatch;  // before this can be calculated, need to
    // somehow keep track of length of matching fragments...
    protected List<AnalysisObserver<W>> analysisObservers;
    protected List<ProgressObserver> progressObservers;
//    protected List<ThreadRunnerObserver> threadRunnerObservers;
    protected long progressInterval = Long.MAX_VALUE;   // increment in reads used for writing output files
                                                   // Long.MAX_VALUE means results will be written only when run is completed
    protected long nextProgressInterval;
    protected int functionFlag = 0;
    protected boolean translateFlag = false;
    protected boolean dnaFlag;
    
    
        
    public CombinedReport(SignatureProperties props, File seqFile, 
        SignatureModule module, Long pInterval) {
        
        sigProps = props;
        reader = SequencingReaderFactory.getReader(seqFile);
        job = seqFile.getName();
 //       String seqFileType = SequencingReader.getInputFileType(job);
  //      seqFileType = SequencingReader.getInputFileType(seqFile);
  //      String seqFType = reader.getFileType();
        dnaFlag = getDnaFlagFromReader();
        dataModule = module;  
        if (dataModule == null)
            dataModule = ModuleFactory.getSignatureModule(sigProps);
        if (pInterval != null)
            progressInterval = pInterval;
        nextProgressInterval = progressInterval;
        analysisObservers = new ArrayList<>();
        progressObservers = new ArrayList<>();
        translateFlag = sigProps.getTranslateFlag();
    }
    
    
    
    public String getReaderType() {
        if (reader == null)
            return null;
        else
            return reader.getFileType();
    }
    
    protected boolean getDnaFlagFromReader() {
        String seqType = getReaderType();
        if (seqType == null)
            return true;
        else
            switch (seqType) {
                case FASTA_FAA:
                    return false;
                default:
                    return true;                 
            }
    }
    
    public void addProgressObserver(ProgressObserver observer) {
        progressObservers.add(observer);
    }
    
    public void addAnalysisObserver(AnalysisObserver<W> observer) {
        analysisObservers.add(observer);
    }
    
//    public void addThreadRunnerObserver(ThreadRunnerObserver observer) {
//        threadRunnerObservers.add(observer);
//    }
    
    public void removeProgressObserver(ProgressObserver observer) {
        progressObservers.remove(observer);
    }
    
    public void removeAnalysisObserver(AnalysisObserver observer) {
        analysisObservers.remove(observer);
    }
    
//    public void removeThreadRunnerObserver(ThreadRunnerObserver observer) {
//        threadRunnerObservers.remove(observer);
//    }   
    
    public void notifyProgressObservers(boolean processComplete) {
        // send ProgressEvent to ProgressObservers
        // for example ProgressObservers may be ProgressWriters, 
        // who will write current stats to various files
        
        if (progressObservers == null || progressObservers.size() < 1)
            return;
             
        ProgressEventData data = new ProgressEventData(reader.getFileName(),
            reader.getFileSize(), reader.getBytesProcessed(), cutoff,
            functionFlag,processComplete);
        data.setAssignTime(assignTime);
        data.setModuleName(sigProps.getModuleName());
//        data.setNodeCount(treeMgr.getNumberInternalNodes());
        if (sigMap.hasFunctions()) {
            data.setFunctionSetName(sigProps.getFunctionSetName());
        } 
        // single stats
        data.setReadsProcessed(readsProcessed);    
        data.setBpProcessed(bpProcessed);
        data.setBpFragAboveCutoff(bpFragAboveCutoff);
        data.setBpFragWithMatch(bpFragWithMatch);
        data.setDnaFlag(dnaFlag);

        // stats arrays
        data.setFileCnts(fileCnts);   // READ, RF, FRAG
        data.setCutoffCnts(cutoffCnts);  // READ, RF, FRAG, PHYL_FRAG, FUNC_FRAG
        data.setTotFragSizeBP(totFragSizeBP);
        data.setTotFragSizeInSchemeBP(totFragSizeInSchemeBP);  // jc:  what is this?    
        data.setReadsByAssignType(readsByAssignType);   
        // [5]: TOTAL, SINGLE_KMER, SINGLE_NODE, MONOPHYL, NON_MONOPHYL
        data.setReadsByNode(readsByNode);  // [internalNodeCnt][5];
        if (functionFlag == ProgressEventData.FUNCTIONS_AND_WDW)
            data.setFragsWDW(fragsWDW); 
        else if (functionFlag == ProgressEventData.FUNCTIONS_ONLY)
            data.setFragsFunc(fragsFunc);

        // set times
        data.setFastaReadTime(fastaReadTime);
        data.setTranslateChopTime(translateChopTime);
        data.setMatchTime(matchTime);
        data.setAssignTime(assignTime);
        data.setMapReadTime(mapReadTime);
        data.setBeginRunTime(beginRunTime);
        // endRunTime is not set until end of run, thus need to set current time
        // rather than use endRunTime variable - otherwise some of the intermediate timings will
        // have problems (e.g. total time and other)
  //      data.setEndRunTime(endRunTime);
        Date now = new Date();
        data.setEndRunTime(now.getTime());
        
        ProgressEvent event = new ProgressEvent(data);
        
        Iterator<ProgressObserver> observerIter = progressObservers.iterator();
        while (observerIter.hasNext()) {
            observerIter.next().observeProgressEvent(event);
        }
    }
    
  public void notifyAnalysisObservers(AnalysisEventData<W> data) {
      
        // notify analysis observers of analysis event for a single read
        // analysis observers (e.g. FastaAnalysisWriter) will determine
        // now frequently this data will be processed (FastaAnalysisWriter
        // currently processes AnalysisEventData immediately;  a future
        // database writer might only write to the database after
        // the queue of AnalysisEventData objects reaches a size of n
      
        if (analysisObservers == null || analysisObservers.size() < 1)
            return;
        
        AnalysisEvent<W> event = new AnalysisEvent<>(data);
        Iterator<AnalysisObserver<W>> observerIter = analysisObservers.iterator();
        while (observerIter.hasNext()) {
            observerIter.next().observeAnalysisEvent(event);
        }
    }
  
    public void notifyAnalysisObservers(int action) {
      
        // notify analysis observers of analysis event for a single read
        // analysis observers (e.g. FastaAnalysisWriter) will determine
        // now frequently this data will be processed (FastaAnalysisWriter
        // currently processes AnalysisEventData immediately;  a future
        // database writer might only write to the database after
        // the queue of AnalysisEventData objects reaches a size of n
      
        if (analysisObservers == null || analysisObservers.size() < 1)
            return;
        
        AnalysisEvent<W> event = new AnalysisEvent<>(action);
        Iterator<AnalysisObserver<W>> observerIter = analysisObservers.iterator();
        while (observerIter.hasNext()) {
            observerIter.next().observeAnalysisEvent(event);
        }
    }
  
//    protected void notifyThreadRunnerObservers(String msg) {
//        
//        ThreadRunnerEvent event = new ThreadRunnerEvent(this, msg, 2);
//        notifyThreadRunnerObservers(event);
//    }
        
//    protected void notifyThreadRunnerObservers(ThreadRunnerEvent event) {
//     
//        Iterator<ThreadRunnerObserver> observerIter = threadRunnerObservers.iterator();
//        while (observerIter.hasNext())
//            observerIter.next().observeThreadRunnerEvent(event);
//    }  
     
    public void setSigMap(SignatureMap sfmap) {
        sigMap = sfmap;
    }
    
    public SignatureMap getSigMap() {
        return sigMap;
    }
    
    public SignatureModule getDataModule() {
        return dataModule;
    }
    
    public String getJob() {
        return job;
    }
    
    public void setTreeManager(TreeManager mgr) {
        treeMgr = mgr;
    }
    
    public TreeManager getTreeManager() {
        return treeMgr;
    }
   
      
    @Override
    public void run() {
         
    	logger.debug("\t==========Running Combined Report==========\n");
        boolean status = initRun();
        if (status == false) {
            String initmsg = "Problem initializing run";
            publish(initmsg,this, Level.ERROR);
            return;
        }
        
        if (reader == null) {
            logger.error("No reader available for "+job);
            return;           
        }
        
        String msg = "Begin matching of reads in file " + reader.getFileName();
        publish(msg,this, Level.INFO);
        
        @SuppressWarnings("unchecked")
        Set<K> signatures = sigMap.getSignatureSet(); 
        String rootNodeName = treeMgr.getRootNodeName();
        // assumes parent map is stored with info id = "0"
        Map<Short, short[]> pathMap = treeMgr.getPathMap();
  
        Date begin = new Date();
        SequencingRecord record = reader.nextRecord();
        Date end = new Date();
        fastaReadTime += end.getTime() - begin.getTime();

        while (record != null) {
            status = processRecord(record, signatures, rootNodeName, pathMap);
            if (status == false) {
                publish("processRecord returned false",this, Level.INFO);
                return;
            }
            else
                readsProcessed++;
            begin = new Date();
            record = reader.nextRecord();
            end = new Date();
            fastaReadTime += end.getTime() - begin.getTime();
            if (progressInterval != Long.MAX_VALUE && readsProcessed >= nextProgressInterval) {    
                nextProgressInterval += progressInterval;
                notifyProgressObservers(false);               
            }
            
        }
      
        publish("Matching is done for file "+reader.getFileName());
        notifyAnalysisObservers(AnalysisEvent.CLOSE);
        end = new Date();
        endRunTime = end.getTime();
        
        // processing complete, write final results
        notifyProgressObservers(true);  // it is possible that last interval gets written twice ??
//        List<String> returnList = new ArrayList<>();
//        returnList.add(job);
    //    return returnList;
        
    }
    

    protected boolean initRun() {  
        
 //   	logger.debug("\t==========Initializing==========\n");
        if (dataModule.getHexFlag() == null) {
            logger.error("Hex Flag is missing from data module "+
                dataModule.getModulePath());
            return false;
        }
        else {
            hexFlag = dataModule.getHexFlag();
        }
             
        Date begin = new Date();
        
        // initialize timing variables       
        beginRunTime = begin.getTime();
        mapReadTime = 0;
        fastaReadTime = 0;
        translateChopTime = 0;
        matchTime = 0;
        assignTime = 0;
        endRunTime = 0;
        
        // check if fna file is missing or does not exist
        File fnaFile = reader.getFile();
        if (fnaFile == null) {
            System.err.println("fnaFile is null");
            return false;
        }
        if (!fnaFile.exists()) {         
            System.err.println("fna file "+fnaFile.getAbsolutePath()+
                " does not exist ");
            return false;
        }
              
  
        
        String fnaName = fnaFile.getName();
        
        String initMsg = "Initializing run for file "+reader.getFileName()+" (read as "+reader.getFileType()+")";
        logger.info(initMsg);
        
        
        // get kmerSize, cutoff
        Integer kmerSizeInt = dataModule.getKmerSize();
        Integer cutoffInt = sigProps.getCutoff();
        
        if (kmerSizeInt == null || cutoffInt == null) {
            logger.error(
            "kmer size (module) and/or cutoff (param file)is missing");
            return false;
        }
        else {
            kmerSize = kmerSizeInt;
  //          cutoff = cutoffInt.intValue();
        }
        
        if (cutoffInt < kmerSize)
            cutoff = kmerSize;
        else
            cutoff = cutoffInt;
       
        // open Fasta File for reading
        boolean okay = reader.open();
        if (!okay) 
            return false;
        
        // read signature map from file
        if (sigMap == null) {
            Date sigBegin = new Date();
            sigMap = readMapFromModule(dataModule);
            Date sigEnd = new Date();
            mapReadTime = sigEnd.getTime() - sigBegin.getTime();
        }
        else
            mapReadTime = 0;
    
        if (sigMap == null) {
            return false;
        }
       
        if (sigMap.hasFunctions()) { 
            Integer functionCntInt = dataModule.getFunctionCount();
            if (functionCntInt == null) {
                logger.error("Function Count is missing from module");
                return false;
            }
            else      
                functionCnt = functionCntInt;
            if (sigProps.getWDWFlag() == 1)
                functionFlag = ProgressEventData.FUNCTIONS_AND_WDW;
            else
                functionFlag = ProgressEventData.FUNCTIONS_ONLY;
        }
        else {
            functionCnt = -1;  
            functionFlag = ProgressEventData.NO_FUNCTIONS;
        }
       
        
        // read tree
        if (treeMgr == null)
            treeMgr = readTreeFromModule(dataModule);
        if (treeMgr == null)
            return false;
        
        int internalNodeCount = treeMgr.getNumberInternalNodes();
       
        // reset matching kmerSet - currently unused
        // eventually could hold set of all matching kmers
//        matchingKmerSet = new TreeSet<String>();
       
        // initialize single counts
        readsProcessed = 0;
        bpProcessed = 0;
        bpFragAboveCutoff = 0;
        bpFragWithMatch = 0;
        
        // initialize count arrays            
        fileCnts = new long[3];   
        cutoffCnts = new long[5];
        totFragSizeBP = 0;
        totFragSizeInSchemeBP = 0;
        readsByAssignType = new long[5];   
        readsByNode = new long[internalNodeCount][5];
        switch (functionFlag) {
            case ProgressEventData.FUNCTIONS_AND_WDW:
                fragsWDW = new double[functionCnt+1][internalNodeCount];
                fragsFunc = null;
                break;
            case ProgressEventData.FUNCTIONS_ONLY:
                fragsWDW = null;
                fragsFunc = new double[functionCnt+1];
                break;
            default:
                fragsWDW = null;
                fragsFunc = null;
                break;
        }
        
        notifyAnalysisObservers(AnalysisEvent.OPEN);
        
        return true;
    }

    
    public static SignatureMap readMapFromModule(SignatureModule module) {
        return readMapFromModule(module, true, true);     
    }
   
    public static SignatureMap readMapFromModule(SignatureModule module, boolean arrayFlag,
        boolean functionFlag) {
        
        boolean hexFlag = module.getHexFlag();
        SignatureMap sigMap;
        
        if (hexFlag){
            if (arrayFlag)
                    sigMap = new SignatureMapMLongArr(module);
            else 
                sigMap = new SignatureMapMLongList(module);  
        }
        else {
            if (arrayFlag) 
                sigMap = new SignatureMapMStrArr(module);
            else 
                sigMap = new SignatureMapMStrList(module);
        }
        
        boolean okay = sigMap.readPhyloMapFromModule();
        if (!okay)
            return null;
        if (functionFlag) {
            okay = sigMap.addFunctionsFromModule();
            if (!okay) {
                logger.info("No functions added to map:  output will include phylogeny only");
            }
        }
         
        return sigMap;
   
    }
    
    public static TreeManager readTreeFromModule(SignatureModule module) {
        
        InputStream treeReader = module.getTreeInputStream();
        String treeSrc = module.getModulePath();

        TreeManager treeMgr = new TreeManager(treeSrc);
        boolean okay = treeMgr.readTreeFromStream(treeReader);
        if (!okay)
            return null;
        else {
            int internalNodeCount = treeMgr.getNumberInternalNodes();
            logger.info(internalNodeCount+" internal nodes in the tree");
            return treeMgr;
        }
        
    }
    
    protected boolean processRecord(SequencingRecord record,
        Set<K> signatures, String rootNodeName, 
        Map<Short, short[]> pathMap) {
        
        Date begin = new Date();
 
        List<KmerSet<W>> kmerSets;
        fileCnts[READ]++;
        
        if (dnaFlag) {
            DNASequence dnaSeq = new StandardDNASequence(record);
            bpProcessed += dnaSeq.length();
            fileCnts[RF] += 6;
            // translate dna sequence into 6 RF, generate fragments,
            // generate set of overlapping kmers from each fragments >= cutoff length
            // if dataModule hexFlag is true, kmers are converted to hex string
  //          kContainer = getAllKmers(dnaSeq);
            kmerSets = getAllKmers(dnaSeq);
        }
        else {
            ProteinSequence protSeq = new ProteinSequence(record);
            bpProcessed += 3*protSeq.length();
            fileCnts[RF]+= 1; 
            kmerSets = getAllKmersFromProtein(protSeq);
        }
    
          
        Date end = new Date();
        translateChopTime += end.getTime() - begin.getTime();
        
        if (kmerSets == null || kmerSets.isEmpty()) { 
            // i.e. no "good" fragments
            return true;
        }
           
        // matching step  
        begin = new Date();
        Short noMatch = (short) functionCnt;
        Set<K> readMatchingSet = new HashSet<>(); 
        List<Set<Short>> functionSets = new ArrayList<>();      
        Iterator<KmerSet<W>> kmerSetIter = kmerSets.iterator();
        List<KmerSet<W>> kmerSetList = new ArrayList<>();  
        
   
        // iterate through kmerSets (one KmerSet per fragment)
        while (kmerSetIter.hasNext()) {
            KmerSet<W> kmerSet = kmerSetIter.next();
            if (kmerSet.isEmpty())
                continue;
            Set<K> kmerMatchSet = getNodeAssignSet(kmerSet);
            kmerMatchSet.retainAll(signatures);
            if (kmerMatchSet.isEmpty()) {
                continue;
            }else{
                cutoffCnts[PHYL_FRAG]++;
                totFragSizeBP+=kmerSet.getLength();
            }
       
            HashSet<Short> functionSet = new HashSet<>();
            if (sigMap.hasFunctions()) {
                Iterator<K> kmerIter =  kmerMatchSet.iterator();
                while (kmerIter.hasNext()) {
                    K kmer = kmerIter.next();
                    @SuppressWarnings("unchecked")
                    Set<Short> kmerFunctions = sigMap.getFunctions(kmer);
                    if (kmerFunctions != null) 
                        functionSet.addAll(kmerFunctions);
                }
                if (functionSet.isEmpty()){ 
                    functionSet.add(noMatch);
                }else{
                    cutoffCnts[FUNC_FRAG]++;
                    totFragSizeInSchemeBP+=kmerSet.getLength();
                }
                functionSets.add(functionSet);
                kmerSet.setFunctionSet(functionSet); // note:  kmerSet now has optional function set variable
   
            }
            KmerSet<W> matchedKmerSet = getMatchedKmerSet(kmerSet, kmerMatchSet);
            kmerSetList.add(matchedKmerSet);
            readMatchingSet.addAll(kmerMatchSet); 
        }
        // note:  status always appears to be true?
        end = new Date();
        matchTime += end.getTime() - begin.getTime();
    
        if (readMatchingSet.size() > 0) {
            begin = new Date();
           // nodeAssignSet = getNodeAssignSet(readMatchingSet);
            readsByAssignType[TOTAL]++;
            NodeAssignment phyloAssign = assignNodeToKmerList(
                readMatchingSet,sigMap, pathMap);
            if (phyloAssign == null) {
                String errorMsg = "Not able to assign node to "+record.getShortHeader();
                publish(errorMsg, this, Level.ERROR);
                return false;
            }
            int selectedNode = (int) phyloAssign.getNodeNum();
            boolean hasSingleKmer = phyloAssign.hasSingleKmer();
            boolean hasSingleNode = phyloAssign.hasSingleNode();
            boolean isNotMonophyl = phyloAssign.isNotMonophyl();
            if (hasSingleKmer) {
                readsByAssignType[SINGLE_KMER]++;
                readsByNode[selectedNode][SINGLE_KMER]++;
            }
            else if (hasSingleNode) {
                readsByAssignType[SINGLE_NODE]++;
//                System.out.println("process read selected node: "+selectedNode);
//                System.out.println(readsByNode);
                readsByNode[selectedNode][SINGLE_NODE]++;
            }
            else if (isNotMonophyl) {
                readsByAssignType[NON_MONOPHYL]++;
                readsByNode[selectedNode][NON_MONOPHYL]++;
            }
            else {
                readsByAssignType[MONOPHYL]++;
                readsByNode[selectedNode][MONOPHYL]++;
            }
            readsByNode[selectedNode][TOTAL]++;
            Iterator<Set<Short>> functionSetIter = functionSets.iterator();
            while (functionSetIter.hasNext()) {
                Set<Short> functionSet = functionSetIter.next();
                int functionNum = functionSet.size();
                if (functionNum > 0) {
                    double scoreFraction = (double) 1 / (double) functionNum;
                    Iterator<Short> functionIter = functionSet.iterator();
                    while (functionIter.hasNext()) {
                        int functIndx = functionIter.next().intValue();
                        if (functionFlag == ProgressEventData.FUNCTIONS_AND_WDW)
                            fragsWDW[functIndx][selectedNode] += scoreFraction;
                        else if (functionFlag == ProgressEventData.FUNCTIONS_ONLY)
                            fragsFunc[functIndx] += scoreFraction;
                    }             
                }
            }
            end = new Date();
            assignTime += end.getTime() - begin.getTime();
            if (analysisObservers.size() > 0) {
                // AnalysisEventData comprises:  1) original sequencing record for the read
                // being processed, 2) list of KmerSet objects (one for each fragment
                // with a match), 3) node assignment object, and 4) list of function sets
                // (one for each fragment with a match) in the same order as the 
                // list of KmerSets.  If sigMap has no functions, list of functionSets will be empty 

                AnalysisEventData<W> eventData = new AnalysisEventData<>(record, kmerSetList,
                phyloAssign, translateFlag); 
                notifyAnalysisObservers(eventData);
            }

        }
        return true;
    }
      
    protected List<KmerSet<W>> getAllKmers(DNASequence sequence) {
        
        List<FragmentRecord> goodFragments = new ArrayList<>();
        int goodRfCnt;
        boolean goodFlag;
   
        for (int i=1; i<=6; i++) {
            List<FragmentRecord> rfFragments = sequence.translate2Fragments(i,true);
            if (rfFragments == null)
                continue;
            goodRfCnt = 0;
            int fragNum = rfFragments.size();
            fileCnts[FRAG] += fragNum;
            Iterator<FragmentRecord> fragIter = rfFragments.iterator();
         
            for (int f=0; f<fragNum; f++) {
                FragmentRecord fragment = fragIter.next();              
                if (fragment.getSeq().length() >= cutoff) {
                    goodFragments.add(fragment);
                    bpFragAboveCutoff += 3*fragment.getSeq().length();
                    goodRfCnt++;
                }
            }
            if (goodRfCnt > 0)
                cutoffCnts[RF]++;
        }
        if (goodFragments.size() > 0) {
            cutoffCnts[READ]++;
            cutoffCnts[FRAG] += goodFragments.size();
  //          return generateKmerContainer(goodFragments);
            return generateKmerSetList(goodFragments);
        }
        else {
   //         System.out.println("good fragments size < 1");
            return null;
        }
        
    }
    
    protected List<KmerSet<W>> getAllKmersFromProtein(ProteinSequence seq) {       
        // note 6/16/18 jc:  kludge for now is to treat protein sequence as fragment
        // with offset 0 and reading frame of 1 (thus "fwd")
        // in future, code will be revamped to change variable names and 
        // written output to reflect dna vs protein sequences
        // ques:  will protein seq ever have equiv of start and stop codons?
       
        
        List<FragmentRecord> fragList = new ArrayList<>();
   
        fileCnts[FRAG]++;
        
        if (seq.length() < cutoff)
            return null;
   
        bpFragAboveCutoff += 3*seq.length();
        cutoffCnts[RF]++;
        cutoffCnts[READ]++;
        cutoffCnts[FRAG]++;
        
        FragmentRecord fragRecord = new FragmentRecord(seq.sequence(),0,1);
        fragList.add(fragRecord);
       
        return generateKmerSetList(fragList);
        
    }
      
    

    protected List<KmerSet<W>> generateKmerSetList(List<FragmentRecord> goodFragments) {
        // when using CombinedReportM, this is the last thing which happens?
   //     publish("generateKmerSetList",this,Level.INFO);
        
        List<KmerSet<W>> kmerSets = new ArrayList<>(); 
   
        Iterator<FragmentRecord> fragIter = goodFragments.iterator();
        while (fragIter.hasNext()) {
            FragmentRecord fragment = fragIter.next(); 
            Integer frameID = fragment.getFrameID();
            KmerSet<W> kmerSet = new KmerSet<>( fragment.getOffset(), frameID, fragment.getSeq().length()*3);
            for (int i=0; i<=fragment.getSeq().length()-kmerSize; i++) {
                W matchObj;
                String kmer = fragment.getSeq().substring(i,i+kmerSize);
                matchObj = getMatchObj(kmer);
                kmerSet.add(matchObj);
            }
            kmerSets.add(kmerSet);
        }   
        return kmerSets;
    }
        
    public static NodeAssignment assignNodeToKmerList(Set kmerList,
        SignatureMap signatureMap, Map<Short, short[]> pathMap) {
        
        
        Short selectedNode;
        boolean notMonophylFlag;
        boolean singleKmer; 
        boolean singleNode; 
        
        singleKmer = (kmerList.size() == 1);
        
        // generate set of nodes associated with kmer list
        TreeSet<Short> nodeList = new TreeSet<>();
        Iterator kmerIter = kmerList.iterator();
        while (kmerIter.hasNext()) {
            Object kmer = kmerIter.next();
            @SuppressWarnings("unchecked")
            Short node = signatureMap.getNode(kmer);
            if (node == null) {
                publish("kmer not found in list: "+kmer,kmer,Level.ERROR);
                // kludge to use kmer object as source for publishing msg
            }
            nodeList.add(node);
        }
        
        if (nodeList.size() == 1) {
            selectedNode = nodeList.first();
            notMonophylFlag = false;
            singleNode = true;
        }        
        
        else {
            
            notMonophylFlag = false;
            singleNode = false;
            selectedNode = null;
            
            // create list of node paths
            ArrayList<short[]> pathList = new ArrayList<>();
            Iterator<Short> nodeIter = nodeList.iterator();
            while (nodeIter.hasNext()) {
                short[] path = pathMap.get(nodeIter.next());
                pathList.add(path);
            }
                
            // cycle through positions in path arrays
            int indx = 0;
            Short lastSingleNode = null;
            Set<Short> currentNodeSet = new HashSet<>();
            boolean done = false;
            while (!done) {
                currentNodeSet.clear();
                Iterator<short[]> pathIter = pathList.iterator();
                while (pathIter.hasNext()) {
                    short[] path = pathIter.next();
                    if (path.length >= indx+1) {
                        currentNodeSet.add(path[indx]);
                    }                       
                }
  
                if (currentNodeSet.size() > 1) {
                    notMonophylFlag = true;
                    selectedNode = lastSingleNode;
                    done = true;
                }
                else if (currentNodeSet.isEmpty()) {
                    notMonophylFlag = false;
                    selectedNode = lastSingleNode;
                    done = true;
                }
                else {  
                    Iterator<Short> currentNodeSetIter = currentNodeSet.iterator();
                    lastSingleNode = currentNodeSetIter.next(); 
                    indx++;
                }
                
            }
        }

        if (selectedNode == null)
            return null;
        else
            return new NodeAssignment(selectedNode,notMonophylFlag,
                singleKmer,singleNode);
        
    }
    
    // abstract methods
    abstract protected W getMatchObj(String kmer);
    abstract protected Set<K> getNodeAssignSet(Set<W> set);
    abstract protected K getKmerForMatch(W kmerObj);
    abstract protected KmerSet<W> getMatchedKmerSet(KmerSet<W> inputKmerSet,
        Set<K> matchedKmers);
     
       
}