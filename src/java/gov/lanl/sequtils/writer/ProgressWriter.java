/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;

import gov.lanl.sequescan.constants.ReportConstants;
import gov.lanl.sequtils.event.ProgressEvent;
import gov.lanl.sequtils.data.ProgressEventData;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.ConfigFile;
import gov.lanl.sequtils.util.FormatHandler;

import java.util.Map;

/**
 *
 * @author jcohn
 */
abstract public class ProgressWriter extends MessageManager implements ProgressObserver, ReportConstants {
    
    // constants used for data and detail column names
    public static final int VAR_NAMES = 0;
    public static final int PRETTY_NAMES = 1;
    
    // instance variables
    protected Map<Short,String[]> functionDetail;
    protected Map<Short,String[]> nodeDetail;
    protected String[][] nodeDetailColumns;
    protected String[][] functionDetailColumns;
    protected ProgressEventData progressData;
    protected ConfigFile config;
    protected int functionFlag = 0;
    protected int threadPoolSize = 1;
    protected FormatHandler formatHandler = new FormatHandler();
    
    public ProgressWriter() {
        super();
    }
    
    public void setConfig(ConfigFile cfile) {
        config = cfile;
    }
    
    @Override
    public void setThreadPoolSize(int threadPoolSz) {
        threadPoolSize = threadPoolSz;
    }
    
    @Override
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
   
    public void setNodeDetail(Map<Short,String[]> detail, String[][] detailColumns) {
        nodeDetail = detail;
        nodeDetailColumns = detailColumns;
    }
    
    public void setFunctionDetail(Map<Short,String[]> detail, String[][] detailColumns) {
        functionDetail = detail;     
        functionDetailColumns = detailColumns; 
    }
    
    @Override
    public void observeProgressEvent(ProgressEvent event) {
   
        int action = event.getAction();
        if (action != ProgressEvent.PROGRESS_DATA)
            return;
        progressData = event.getData();
        functionFlag = progressData.getFunctionFlag();
        writeStats();
        writeNodeCounts();
        if (functionFlag == ProgressEventData.FUNCTIONS_AND_WDW) 
            writeFunctionAndWDWCounts();
        else if (functionFlag == ProgressEventData.FUNCTIONS_ONLY)
            writeFunctionCounts();
        
    }
    
    // abstract methods
    abstract protected void writeStats();
    abstract protected void writeNodeCounts();
    abstract protected void writeFunctionCounts();
    abstract protected void writeFunctionAndWDWCounts();
}
