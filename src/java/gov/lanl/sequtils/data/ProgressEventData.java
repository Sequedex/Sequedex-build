/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.data;

/**
 * 
 * @author jcohn
 */
public class ProgressEventData {
    
    // Constants for functionFlag
    public static final int NO_FUNCTIONS = 0;
    public static final int FUNCTIONS_ONLY = 1;
    public static final int FUNCTIONS_AND_WDW = 2;
    
    // times
    protected long fastaReadTime;
    protected long translateChopTime;
    protected long matchTime;
    protected long assignTime;
    protected long mapReadTime;
    protected long beginRunTime;
    protected long endRunTime;
    
    // single stats
    protected long readsProcessed;
    protected long bpProcessed;
    protected long bpFragAboveCutoff;
    protected long bpFragWithMatch;
    
    // stats arrays
    protected long[] fileCnts;   // READ, RF, FRAG
    protected long[] cutoffCnts;  // READ, RF, FRAG, PHYL_FRAG, FUNC_FRAG
    protected long totFragSizeBP;
    protected long totFragSizeInSchemeBP;
    protected long[] readsByAssignType;   
    // [5]: TOTAL, SINGLE_KMER, SINGLE_NODE, MONOPHYL, NON_MONOPHYL
    protected long[][] readsByNode;  // [internalNodeCnt][5];
    // note:  only one or the other of fragsWDW or fragsFunc should be set
    protected double[][] fragsWDW; // [functionCnt+1][internalNodeCnt]
    protected double[] fragsFunc; // [functionCnt+1]
    
    protected int functionFlag;
    protected boolean completionFlag;
    protected Boolean dnaFlag = null;
    
    // file info
    protected long fileSize;
    protected long charProcessed;
    protected String fnaFileName;
    
    // module info
    protected String moduleName;
    protected String functionSetName;
    protected int cutoff;
 
    
    
    // constructor
    public ProgressEventData(String fileName, long fileSz, long chProcessed, 
        int cutoffInt, int funcFlag, boolean processingDone) {
        fnaFileName = fileName;
        fileSize = fileSz;
        charProcessed = chProcessed; 
        functionFlag = funcFlag;
        completionFlag = processingDone;
        cutoff = cutoffInt;
        moduleName = "";
        functionSetName = "";
    }

    /**
     * @return the fastaReadTime
     */
    public long getFastaReadTime() {
        return fastaReadTime;
    }
    
    public void setDnaFlag(boolean flag) {
        dnaFlag = flag;
    }
    
    public boolean getDnaFlag() {
        return dnaFlag;
    }

    /**
     * @param fastaReadTime the fastaReadTime to set
     */
    public void setFastaReadTime(long fastaReadTime) {
        this.fastaReadTime = fastaReadTime;
    }
    
       /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }
    
    public String getFnaFileName() {
        return fnaFileName;
    }

    /**
     * @return charProcessed
     */
    public long getCharProcessed() {
        return charProcessed;
    }
    
    public float getFractionProcessed() {
        if (getFileSize() < 0)
            return -1;
        else
            return (float) getCharProcessed() / (float) getFileSize();
    }
    
    /**
     * 
     * @return the functionFlag
     */
    public int getFunctionFlag() {
        return functionFlag;
    }

    /**
     * @return the translateChopTime
     */
    public long getTranslateChopTime() {
        return translateChopTime;
    }

    /**
     * @param translateChopTime the translateChopTime to set
     */
    public void setTranslateChopTime(long translateChopTime) {
        this.translateChopTime = translateChopTime;
    }

    /**
     * @return the matchTime
     */
    public long getMatchTime() {
        return matchTime;
    }

    /**
     * @param matchTime the matchTime to set
     */
    public void setMatchTime(long matchTime) {
        this.matchTime = matchTime;
    }

    /**
     * @return the assignTime
     */
    public long getAssignTime() {
        return assignTime;
    }

    /**
     * @param assignTime the assignTime to set
     */
    public void setAssignTime(long assignTime) {
        this.assignTime = assignTime;
    }

    /**
     * @return the mapReadTime
     */
    public long getMapReadTime() {
        return mapReadTime;
    }

    /**
     * @param mapReadTime the mapReadTime to set
     */
    public void setMapReadTime(long mapReadTime) {
        this.mapReadTime = mapReadTime;
    }

    /**
     * @return the beginRunTime
     */
    public long getBeginRunTime() {
        return beginRunTime;
    }

    /**
     * @param beginRunTime the beginRunTime to set
     */
    public void setBeginRunTime(long beginRunTime) {
        this.beginRunTime = beginRunTime;
    }

    /**
     * @return the endRunTime
     */
    public long getEndRunTime() {
        return endRunTime;
    }

    /**
     * @param endRunTime the endRunTime to set
     */
    public void setEndRunTime(long endRunTime) {
        this.endRunTime = endRunTime;
    }

    /**
     * @return the readsProcessed
     */
    public long getReadsProcessed() {
        return readsProcessed;
    }

    /**
     * @param readsProcessed the readsProcessed to set
     */
    public void setReadsProcessed(long readsProcessed) {
        this.readsProcessed = readsProcessed;
    }

    /**
     * @return the bpProcessed
     */
    public long getBpProcessed() {
        return bpProcessed;
    }

    /**
     * @param bpProcessed the bpProcessed to set
     */
    public void setBpProcessed(long bpProcessed) {
        this.bpProcessed = bpProcessed;
    }

    /**
     * @return the bpFragAboveCutoff
     */
    public long getBpFragAboveCutoff() {
        return bpFragAboveCutoff;
    }

    /**
     * @param bpFragAboveCutoff the bpFragAboveCutoff to set
     */
    public void setBpFragAboveCutoff(long bpFragAboveCutoff) {
        this.bpFragAboveCutoff = bpFragAboveCutoff;
    }

    /**
     * @return the bpFragWithMatch
     */
    public long getBpFragWithMatch() {
        return bpFragWithMatch;
    }

    /**
     * @param bpFragWithMatch the bpFragWithMatch to set
     */
    public void setBpFragWithMatch(long bpFragWithMatch) {
        this.bpFragWithMatch = bpFragWithMatch;
    }

    /**
     * @return the fileCnts
     */
    public long[] getFileCnts() {
        return fileCnts;
    }

    /**
     * @param fileCnts the fileCnts to set
     */
    public void setFileCnts(long[] fileCnts) {
        this.fileCnts = fileCnts;
    }

    /**
     * @return the cutoffCnts
     */
    public long[] getCutoffCnts() {
        return cutoffCnts;
    }

    /**
     * @param cutoffCnts the cutoffCnts to set
     */
    public void setCutoffCnts(long[] cutoffCnts) {
        this.cutoffCnts = cutoffCnts;
    }

    /**
     * @return the totFragSizeBP
     */
    public long getTotFragSizeBP() {
        return totFragSizeBP;
    }

    /**
     * @param totFragSizeBP the totFragSizeBP to set
     */
    public void setTotFragSizeBP(long totFragSizeBP) {
        this.totFragSizeBP = totFragSizeBP;
    }

    /**
     * @return the totFragSizeInSchemeBP
     */
    public long getTotFragSizeInSchemeBP() {
        return totFragSizeInSchemeBP;
    }

    /**
     * @param totFragSizeInSchemeBP the totFragSizeInSchemeBP to set
     */
    public void setTotFragSizeInSchemeBP(long totFragSizeInSchemeBP) {
        this.totFragSizeInSchemeBP = totFragSizeInSchemeBP;
    }

    /**
     * @return the readsByAssignType
     */
    public long[] getReadsByAssignType() {
        return readsByAssignType;
    }

    /**
     * @param readsByAssignType the readsByAssignType to set
     */
    public void setReadsByAssignType(long[] readsByAssignType) {
        this.readsByAssignType = readsByAssignType;
    }

    /**
     * @return the readsByNode
     */
    public long[][] getReadsByNode() {
        return readsByNode;
    }

    /**
     * @param readsByNode the readsByNode to set
     */
    public void setReadsByNode(long[][] readsByNode) {
        this.readsByNode = readsByNode;
    }

    /**
     * @return the fragsWDW
     */
    public double[][] getFragsWDW() {
        return fragsWDW;
    }

    /**
     * @param fragsWDW the fragsWDW to set
     */
    public void setFragsWDW(double[][] fragsWDW) {
        this.fragsWDW = fragsWDW;
    }
    
       /**
     * @return the fragsWDW
     */
    public double[] getFragsFunc() {
        return fragsFunc;
    }

    /**
     * @param fragsWDW the fragsWDW to set
     */
    public void setFragsFunc(double[] fragsFunc) {
        this.fragsFunc = fragsFunc;
    }

    /**
     * @return the moduleName
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * @param moduleName the moduleName to set
     */
    public void setModuleName(String moduleNm) {
        if (moduleNm == null)
            moduleNm = "";
        this.moduleName = moduleNm;
    }

    /**
     * @return the functionSetName
     */
    public String getFunctionSetName() {
        return functionSetName;
    }

    /**
     * @param functionSetName the functionSetName to set
     */
    public void setFunctionSetName(String functionSetNm) {
        if (functionSetNm == null)
            functionSetNm = "";
        this.functionSetName = functionSetNm;
    }
    
    public int getCutoff() {
        return cutoff;
    }
      
    public boolean processingDone() {
        return completionFlag;
    }

 
    
}
