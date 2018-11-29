/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;

import gov.lanl.sequescan.constants.ReportConstants;
import gov.lanl.sequtils.event.AnalysisEvent;
import gov.lanl.sequtils.event.AnalysisObserver;
import gov.lanl.sequtils.log.MessageManager;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import gov.lanl.sequescan.constants.AppConstants;

/**
 *
 * @author jcohn
 */
public abstract class AnalysisFileWriter<W> extends MessageManager
    implements AnalysisObserver<W>, ReportConstants, AppConstants {
    
    protected Map<Short,String[]> functionDetail = null;
    protected Map<Short,String[]> nodeDetail = null;     
    protected Queue<AnalysisEvent<W>> eventQueue;  
    protected File outputDir = null;
    protected String inputFileName = null;
    protected String inputFileType = null;
    protected int functionCount;
    protected String dbName = null;
    protected String analysisOutputType = null;
    protected boolean kmerFlag = false;
    
    
    public AnalysisFileWriter() {
        super();
    }
    
    // sets whether or not to put kmer list in header
    public void setKmerFlag(boolean flag) {
        kmerFlag = flag;
    }
   
    public void setAnalysisOutputType(String aType) {
        analysisOutputType = aType;
    }
    
    public void setOutput(String db, File dir, String inputFileNm, String inputFType) {
        dbName = db;
        outputDir = dir;
        inputFileName = inputFileNm;
 //       inputFileType = SequencingReader.getInputFileType(inputFileNm);
        inputFileType = inputFType;
    }
    
    public File getOutputDir() {
        return outputDir;
    }
    
    public String getInputFileName() {
        return inputFileName;
    }
    
    public String getInputFileType() {
        return inputFileType;
    }
    
    public void setFunctionDetail(Map<Short,String[]> detail) {
        functionDetail = detail;
    }
    
    public Map<Short,String[]> getFunctionDetail() {
        return functionDetail;
    }
    
    public void setFunctionCount(int cnt) {
        functionCount = cnt;
    }
    
    public int getFunctionCount() {
        return functionCount;
    }
    
    @SuppressWarnings("unchecked")  // for one statement
    public void setNodeDetail(Map<Short,String[]> detail) {
        nodeDetail = detail;
    }
    
    public Map<Short,String[]> getNodeDetail() {
        return nodeDetail;
    }
 
    @Override
    public void observeAnalysisEvents(Collection<AnalysisEvent<W>> events) {
        eventQueue.addAll(events);
    }
    
    @Override
    public void observeAnalysisEvent(AnalysisEvent<W> event) {
        eventQueue.add(event);
    }
    
   
    public void processQueue() {
        AnalysisEvent<W> event = eventQueue.poll();
        while (event != null) {
            processAnalysisEvent(event);
            event = eventQueue.poll();
        }
    } 
    
    abstract protected void processAnalysisEvent(AnalysisEvent<W> event); 
    abstract protected boolean openFile();
    abstract protected boolean closeFile();
    
}
