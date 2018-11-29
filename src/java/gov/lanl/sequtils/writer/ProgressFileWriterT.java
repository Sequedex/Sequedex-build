/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;
import gov.lanl.sequtils.util.ConfigFile;
import gov.lanl.sequtils.util.ReportUtilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author jcohn
 */
public final class ProgressFileWriterT extends ProgressFileWriter {
    
    public ProgressFileWriterT() {
        super();
    }
    
    @Override
    protected void writeStats() {
        
 //   	logger.debug("\t==========Writing Stats==========\n");
        
        // rewrite this method to use writeProgressFile for .tsv file?

        if (statsFile == null) {
            String statsStr = config.getProperty(STATS);
            if (statsStr == null) {
                logger.error("stats parameter is missing from config file");
                return;
            }

            String statsTsvStr = statsStr+".tsv";
      
            statsFile = new File( outputDir, statsTsvStr);
            logger.debug("Stats tsv file: " + statsFile.getAbsolutePath());
        }

        
        if (statsFile.exists())
        	statsFile.delete();
     
    	//new values added by Mira (precision - 1 digit after .)
        //reads_assigned_percent_float (reads_assigned*100./reads_in_int)
        long[] readsByAssignType = progressData.getReadsByAssignType();
        long[] fileCnts = progressData.getFileCnts();
        long[] cutoffCnts = progressData.getCutoffCnts();
        int cutoff = progressData.getCutoff(); 
        float readsAssignedPercent = (float) readsByAssignType[TOTAL]*(float)100/(float) fileCnts[READ];   
        Float readsAssignedPercentObj = readsAssignedPercent;
        Float completedPercentObj;
        if (progressData.getFileSize() > 0) {
            float completedPercent = (float) progressData.getCharProcessed()* (float)100/(float) progressData.getFileSize();
            completedPercentObj = completedPercent;
        }
        else
            completedPercentObj = null;
        boolean dnaFlag = progressData.getDnaFlag();
     
        DecimalFormat form1 = new DecimalFormat("0.0"); 
        DecimalFormat form3 = new DecimalFormat("0.000");
        Boolean processingDone;
        if (progressData.processingDone())
            processingDone = Boolean.TRUE;
        else
            processingDone = Boolean.FALSE;  
        
        long timeSum = progressData.getMapReadTime() + progressData.getFastaReadTime() + 
        progressData.getTranslateChopTime() +
        progressData.getMatchTime() + progressData.getAssignTime();
        long totalTime = progressData.getEndRunTime() - progressData.getBeginRunTime();
        long other = totalTime - timeSum;       
        Float gbpPerHr = (float) progressData.getBpProcessed()/((float)1.E9*(float)totalTime/(float)1000/(float)3600);
        
        StringBuilder builder = new StringBuilder();
        
        // File information    
        builder.append("Sequescan Version"); builder.append(TAB);
//        builder.append(VERSION_NUMBER); builder.append(EOL);
        String buildStr = ConfigFile.getBuildString();
        if (buildStr == null)
            buildStr = "Unknown";
        builder.append("Sequedex ");
        String sqdxVersion = ConfigFile.getSequedexVersion();
        if (sqdxVersion == null)
            sqdxVersion = "Unknown";
        builder.append(sqdxVersion);
        builder.append(": Sequescan build ");
        builder.append(buildStr); 
        builder.append(EOL);
        
        String logFileName = config.getProperty(LOG_FILE_NAME);  //(String) GlobalConfig.get(LOG_FILE_NAME);
        builder.append("Log File");  builder.append(TAB);
        builder.append(logFileName);  builder.append(EOL);

        builder.append("Data Input File"); builder.append(TAB); 
        builder.append(progressData.getFnaFileName());  builder.append(EOL);
        
        builder.append("Sequence Type");  builder.append(TAB);
        if (dnaFlag)
            builder.append("DNA");
        else
            builder.append("Protein");
        builder.append(EOL);
        
        builder.append("Data Output Directory"); builder.append(TAB);
        builder.append(outputDir.toString());  builder.append(EOL); 
        
        builder.append("Local Time");  builder.append(TAB);
        builder.append( getLocalTime());  builder.append(EOL);
        
        builder.append("Percent of File Processed");  builder.append(TAB);
        if (completedPercentObj != null)
            builder.append(form1.format(completedPercentObj)); 
        else if (!processingDone)
            builder.append("Unknown");
        else
            builder.append("100.0");
        builder.append(EOL);                     
        builder.append("Processing Complete"); builder.append(TAB); 
        builder.append(Boolean.toString(processingDone)); builder.append(EOL); 
        builder.append("Thread Pool Size"); builder.append(TAB);
        builder.append(Integer.toString(getThreadPoolSize())); builder.append(EOL);
        builder.append("Processing Rate (Gbp/hr)"); builder.append(TAB); 
        builder.append(form3.format(gbpPerHr)); builder.append(EOL); 
                     
        // Read stats     
        LinkedHashMap<String,Object> readStats = new LinkedHashMap<>();
     
        readStats.put("Reads In", fileCnts[READ]);
        builder.append("Reads In"); builder.append(TAB); 
        builder.append(Long.toString(fileCnts[READ])); builder.append(EOL);
        
        readStats.put("Bases In", progressData.getBpProcessed());  
        builder.append("Bases In"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getBpProcessed())); builder.append(EOL);
        
        readStats.put("Reads With Fragments >= "+cutoff+" AA", cutoffCnts[READ]);
        builder.append("Reads With Fragments >= ").append(cutoff).append(" AA"); builder.append(TAB); 
        builder.append(Long.toString(cutoffCnts[READ])); builder.append(EOL);
             
        readStats.put("Frames Processed", fileCnts[RF]);
        builder.append("Frames Processed"); builder.append(TAB); 
        builder.append(Long.toString(fileCnts[RF])); builder.append(EOL);
        
        readStats.put("Frames with Fragments >= "+cutoff+" AA", cutoffCnts[RF]);
        builder.append("Frames with Fragments >= ").append(cutoff).append(" AA"); 
        builder.append(TAB); 
        builder.append(Long.toString(cutoffCnts[RF])); builder.append(EOL);
        
        
        readStats.put("Fragments In", fileCnts[FRAG]);
        builder.append("Fragments In"); builder.append(TAB); 
        builder.append(Long.toString(fileCnts[FRAG])); builder.append(EOL);     
        
        
        readStats.put("Fragments >= "+cutoff+" AA", cutoffCnts[FRAG]);
        builder.append("Fragments >= ").append(cutoff).append("AA"); builder.append(TAB); 
        builder.append(Long.toString(cutoffCnts[FRAG])); builder.append(EOL);          

        // Phylogeny stats 
        LinkedHashMap<String,Object> phyloStats = new LinkedHashMap<>();
        
        phyloStats.put("Reads Assigned", readsByAssignType[TOTAL]);
        builder.append("Reads Assigned"); builder.append(TAB); 
        builder.append(Long.toString(readsByAssignType[TOTAL])); builder.append(EOL);      
              
        phyloStats.put("Single-Signature Reads", readsByAssignType[SINGLE_KMER]);
        builder.append("Single-Signature Reads"); builder.append(TAB); 
        builder.append(Long.toString(readsByAssignType[SINGLE_KMER])); builder.append(EOL);               
               
        phyloStats.put("Single-Node Reads", readsByAssignType[SINGLE_NODE]);
        builder.append("Single-Node Reads"); builder.append(TAB); 
        builder.append(Long.toString(readsByAssignType[SINGLE_NODE])); builder.append(EOL);              
        
        phyloStats.put("Monophyletic Reads", readsByAssignType[MONOPHYL]);
        builder.append("Monophyletic Reads"); builder.append(TAB); 
        builder.append(Long.toString(readsByAssignType[MONOPHYL])); builder.append(EOL);  
        
        phyloStats.put("Non-Monophyletic Reads", readsByAssignType[NON_MONOPHYL]);
        builder.append("Non-Monophyletic Reads"); builder.append(TAB); 
        builder.append(Long.toString(readsByAssignType[NON_MONOPHYL])); builder.append(EOL);       
              
        phyloStats.put("Fragments Assigned", cutoffCnts[PHYL_FRAG]) ;
        builder.append("Fragments Assigned"); builder.append(TAB); 
        builder.append(Long.toString(cutoffCnts[PHYL_FRAG])); builder.append(EOL);  
              
        phyloStats.put("Percent of Reads Assigned", Float.valueOf(form1.format(readsAssignedPercentObj)));
        builder.append("Percent of Reads Assigned"); builder.append(TAB); 
        builder.append(Float.valueOf(form1.format(readsAssignedPercentObj))); builder.append(EOL);  
               
        phyloStats.put("Total Size of Matched Fragments (bp)", progressData.getTotFragSizeBP());
        builder.append("Total Size of Matched Fragments (bp)"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getTotFragSizeBP())); builder.append(EOL);            
        
        // Function stats
        if (functionFlag > 0) {
            
            LinkedHashMap<String,Object> functionStats = new LinkedHashMap<>();
            
            functionStats.put("Fragments Assigned", cutoffCnts[FUNC_FRAG]);
            builder.append("Fragments Assigned Function"); builder.append(TAB); 
            builder.append(Long.toString(cutoffCnts[FUNC_FRAG])); builder.append(EOL); 
            functionStats.put("Total Size (bp) of Fragments Assigned", progressData.getTotFragSizeInSchemeBP());
            builder.append("Total Size of Fragments Assigned Function (bp)"); builder.append(TAB); 
            builder.append(Long.toString(progressData.getTotFragSizeInSchemeBP())); builder.append(EOL); 
            
        }
       
        // Timing
        
        LinkedHashMap<String,Object> timingMap = new LinkedHashMap<>();
        
        if (progressData.getMapReadTime() > 0) {
            timingMap.put("Read Signature Maps (ms)", progressData.getMapReadTime()); 
            builder.append("Read Signature Maps (ms)"); builder.append(TAB); 
            builder.append(Long.toString(progressData.getMapReadTime())); builder.append(EOL); 
        }
        
        timingMap.put("Input Time (ms)", progressData.getFastaReadTime()); 
        builder.append("Input Time(ms)"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getFastaReadTime())); builder.append(EOL);       
              
        timingMap.put("Translate Time (ms)", progressData.getTranslateChopTime()); 
        builder.append("Translate Time (ms)"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getTranslateChopTime())); builder.append(EOL); 
        
        timingMap.put("Match Time (ms)", progressData.getMatchTime()); 
        builder.append("Match Time (ms)"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getMatchTime())); builder.append(EOL); 
        
        timingMap.put("Assignment Time (ms)", progressData.getAssignTime()); 
        builder.append("Assignment Time (ms)"); builder.append(TAB); 
        builder.append(Long.toString(progressData.getAssignTime())); builder.append(EOL); 
        
        timingMap.put("Other Time (ms)", other); 
        builder.append("Other Time (ms)"); builder.append(TAB); 
        builder.append(Long.toString(other)); builder.append(EOL); 
        
        timingMap.put("Total Time (ms)", totalTime); 
        builder.append("Total Time (ms)"); builder.append(TAB); 
        builder.append(Long.toString(totalTime)); builder.append(EOL); 
      
        
        try {
            try (FileWriter statsTsvWriter = new FileWriter(statsFile)) {
                statsTsvWriter.write(builder.toString());
                // flush/close BufferedWriter
                statsTsvWriter.flush();
            }
            
        } catch (IOException ex) {
        	logger.error("Problem writing to stats file "+
        			statsFile.getAbsolutePath()+": " + ex.getMessage());
        }     
    }
    
    @Override
    protected void writeNodeCounts() {
        
 //   	logger.debug("\t==========Writing Node Counts==========\n");
        
        // rewrite this method to use writeProgressFile method?
        if (nodeFile == null) {
            String nodeStr = config.getProperty(WHO);
            if (nodeStr == null) {
                logger.error("who parameter is missing from config file");
                return;
            }
            
            nodeStr = nodeStr+".tsv";
      
            nodeFile = new File( outputDir, nodeStr);
            logger.debug("Node tsv file: " + nodeFile.getAbsolutePath());  
        }
    
        
        if (nodeFile.exists())
        	nodeFile.delete();
        
        long[][] readsByNode = progressData.getReadsByNode();
        int internalNodeCount =  readsByNode.length;         
        boolean rowFlag;
        if (nodeIDs == null) {
            nodeIDs = new String[internalNodeCount];
            rowFlag = true;
        }
        else rowFlag = false;
        
        int rowNum, colNum;
        try {
            FileWriter nodeCountTsvWriter = new FileWriter(nodeFile);
            try (BufferedWriter nodeCountTsvOut = new BufferedWriter(nodeCountTsvWriter)) {
                StringBuilder buf = new StringBuilder(200);
                //added by Mira - count nodes to construct #tsv%dx%d
                // changed by jcohn to use internalNodeCount and size of nodeLookupColumns (List)
                rowNum = internalNodeCount;  // 0;
                colNum = nodeDataColNames[VAR_NAMES].length;
                if (nodeDetail != null)
                    //colNum++;
                    colNum += nodeDetailColumns[VAR_NAMES].length;
                else
                    colNum += 1;
                
                // output names for first two columns of details or indx only if no details
                // nodeDetails must have at least 2 columns - or this will fail    (jc: 6/14/13)

                if (nodeDetailColumns != null && nodeDetailColumns[VAR_NAMES].length >= 2) {
                    
                    for (int i=0; i<2;i++) {
                        String colName = nodeDetailColumns[VAR_NAMES][i];
                        buf.append(colName); buf.append(TAB);
                    }
                }
                else {
                    buf.append("indx");
                    buf.append(TAB); 
                }
                
                // names for output columns
                buf.append(nodeDataColNames[VAR_NAMES][0]);  buf.append(TAB);
                buf.append(nodeDataColNames[VAR_NAMES][1]);  buf.append(TAB);
                buf.append(nodeDataColNames[VAR_NAMES][2]);  buf.append(TAB);
                buf.append(nodeDataColNames[VAR_NAMES][3]);  buf.append(TAB);
                buf.append(nodeDataColNames[VAR_NAMES][4]);
                
                // rest of detail column names (if any)
                if (nodeDetailColumns != null && nodeDetailColumns[VAR_NAMES].length > 2) {
                    for (int i=2; i<nodeDetailColumns[VAR_NAMES].length;i++) {
                        String colName = nodeDetailColumns[VAR_NAMES][i];
                        buf.append(TAB); buf.append(colName);
                    }
                }
                
                buf.append(EOL);
                nodeCountTsvOut.write(buf.toString());  // write column var_names
                
                for (int i=0; i<internalNodeCount; i++) {
                    buf = new StringBuilder(40);
                    buf.append(Integer.toString(i));
                    // if nodeDetails, then add short name (col 2?)
                    
                    Short nodeIndx = (short)i;
                    String[] detailArr = null;
                    if (nodeDetail != null)
                        detailArr = nodeDetail.get(nodeIndx);
                    
                    if (detailArr != null && detailArr.length > 0) {
                        buf.append(TAB);
                        buf.append(detailArr[0]);
                        if (rowFlag)
                            nodeIDs[i] = detailArr[0];
                    }
                    else if (rowFlag)
                        nodeIDs[i] = Integer.toString(i);  //lpadIndxStr;
                    // add output numbers
                    buf.append(TAB);
                    buf.append(readsByNode[i][TOTAL]); buf.append(TAB);
                    buf.append(readsByNode[i][SINGLE_KMER]); buf.append(TAB);
                    buf.append(readsByNode[i][SINGLE_NODE]); buf.append(TAB);
                    buf.append(readsByNode[i][MONOPHYL]); buf.append(TAB);
                    buf.append(readsByNode[i][NON_MONOPHYL]);
                    // add rest of details
                    if (detailArr != null &&  detailArr.length > 1)
                        for (int j=1; j<detailArr.length; j++) {
                            buf.append(TAB);
                            buf.append(detailArr[j]);
                        }
                    
                    buf.append(EOL);
                    nodeCountTsvOut.write(buf.toString());
                }
                nodeCountTsvOut.flush();
            }
      
        } catch (IOException ex) {
            logger.error("Problem writing to node files in directory "+
                outputDir.getAbsolutePath()+": "+ex.getMessage());
        }
        
    }
    
    @Override
    protected void writeFunctionAndWDWCounts()  {
            
//    	logger.debug("\t==========Writing Function Files==========\n");
        
        if (functionFile == null) {
            String functionStr = config.getProperty(WHAT);
            if (functionStr == null) {
                logger.error("what parameter is missing from config file");
                return;
            }
 
            String functionTsvStr = functionStr+".tsv";
      
            functionFile = new File( outputDir, functionTsvStr);
            logger.debug("Function tsv file: " + nodeFile.getAbsolutePath());
            
            String wdwStr = config.getProperty(WDW);
            if (wdwStr == null) {
                logger.error("whoDoesWhat parameter is missing from config file");
                return;
            }
            
            String wdwTsvStr = wdwStr+".tsv";
            wdwFile = new File(outputDir, wdwTsvStr);
        }  
   
        if (functionFile.exists())
        	functionFile.delete();
        
        if (wdwFile.exists()) 
        	wdwFile.delete();

        boolean functStatus;
        double[][] fragsWDW = progressData.getFragsWDW();
        if (fragsWDW == null) {
            logger.error("fragsWDW is null;  cannot write wdw file");
            return;
        }
        double[] rowSum = ReportUtilities.getMatrixRowSumVec(fragsWDW);
            
        DecimalFormat dformat = new DecimalFormat();
        dformat.setRoundingMode(RoundingMode.HALF_UP);
        dformat.setMaximumFractionDigits(1);
        dformat.setGroupingUsed(false);
      
        int functionCount = rowSum.length;
        
        boolean colFlag;
        if (functionIDs == null) {
            functionIDs = new String[functionCount];
            colFlag = true;
        }
        else
            colFlag = false;
        
        int rowNum, colNum=1;      
        rowNum =  rowSum.length;
        if (functionDetail != null) {
            colNum += functionDetailColumns[VAR_NAMES].length;
        }
        else colNum++;
        
        StringBuilder buf = new StringBuilder(200);
        List<String> functionLines = new ArrayList<>(rowNum+1);
            // element 0 is header line which is used only in .tsv file

        
        String col0Name = "indx"; 
        buf.append(col0Name);  buf.append(TAB);
        // assumes functionDetail has at least 2 columns
        if (functionDetail != null) {
            if (functionDetailColumns[VAR_NAMES].length >= 2) {
                buf.append(functionDetailColumns[VAR_NAMES][1]);              
            }             
            
        }
        // assumes only one data column  
        buf.append(TAB);
        buf.append(functionDataColNames[VAR_NAMES][0]); 
        
        if (functionDetail != null) {
            for (int i=2; i< functionDetailColumns[VAR_NAMES].length; i++) {
                buf.append(TAB);
                buf.append(functionDetailColumns[VAR_NAMES][i]);
            }
        }
        functionLines.add(buf.toString());

        for (int i=0; i<rowSum.length; i++) {
            buf.setLength(0);            
            String[] functionCategories = null;
            Short indx = (short)i;
            if (functionDetail != null)
                functionCategories = functionDetail.get(indx);
            buf.append(indx.toString()); 
            buf.append(TAB);
            if (functionCategories != null) {
                buf.append(functionCategories[0]);
                if (colFlag)
                    functionIDs[i] = functionCategories[0];
                buf.append(TAB);
            }
            else if (colFlag)
                functionIDs[i] = indx.toString();
                
            buf.append(dformat.format(rowSum[i]));

            if (functionCategories != null && functionCategories.length > 1) {
                for (int j=1; j<functionCategories.length; j++) {
                    buf.append(TAB);
                    buf.append(functionCategories[j]);
                }

            }
 
            functionLines.add(buf.toString());
        }


        //  again, element 0 is header line with col names used for .tsv file
        String idColName;
        if (functionDetail == null)
            idColName= "indx";
        else
            idColName = "id";
        List<String> wdwLines = ReportUtilities.getMatrixLines(
            fragsWDW, functionIDs, nodeIDs,1,idColName);
        
        // for now, do nothing with boolean returned by writeProgressFile method
        writeProgressFile(functionFile, functionLines, false, null);
        writeProgressFile(wdwFile, wdwLines, false, null);   
    }

    @Override
    protected void writeFunctionCounts() {
        
        if (functionFile == null) {
            String functionStr = config.getProperty(WHAT);
            if (functionStr == null) {
                logger.error("what parameter is missing from config file");
                return;
            }
 
            String functionTsvStr = functionStr+".tsv";
      
            functionFile = new File( outputDir, functionTsvStr);
            logger.debug("Function tsv file: " + nodeFile.getAbsolutePath());
        }  
   
        if (functionFile.exists())
        	functionFile.delete();

        boolean functStatus;
        double[] fragsFunc = progressData.getFragsFunc();
        
            
        DecimalFormat dformat = new DecimalFormat();
        dformat.setRoundingMode(RoundingMode.HALF_UP);
        dformat.setMaximumFractionDigits(1);
        dformat.setGroupingUsed(false);
      
        int rowNum = fragsFunc.length;
        int colNum=1;
        
        boolean colFlag;
        if (functionIDs == null) {
            functionIDs = new String[rowNum];
            colFlag = true;
        }
        else
            colFlag = false;
        
        if (functionDetail != null) {
            colNum += functionDetailColumns[VAR_NAMES].length;
        }
        else colNum++;
        
        StringBuilder buf = new StringBuilder(200);
        List<String> functionLines = new ArrayList<>(rowNum+1);
            // element 0 is header line which is used only in .tsv file

        
        String col0Name = "indx"; 
        buf.append(col0Name);  buf.append(TAB);
        // assumes functionDetail has at least 2 columns
        if (functionDetail != null) {
            if (functionDetailColumns[VAR_NAMES].length >= 2) {
                buf.append(functionDetailColumns[VAR_NAMES][1]);              
            }             
            
        }
        // assumes only one data column  
        buf.append(TAB);
        buf.append(functionDataColNames[VAR_NAMES][0]); 
        
        if (functionDetail != null) {
            for (int i=2; i< functionDetailColumns[VAR_NAMES].length; i++) {
                buf.append(TAB);
                buf.append(functionDetailColumns[VAR_NAMES][i]);
            }
        }
        functionLines.add(buf.toString());

        for (int i=0; i<fragsFunc.length; i++) {
            buf.setLength(0);            
            String[] functionCategories = null;
            Short indx = (short)i;
            if (functionDetail != null)
                functionCategories = functionDetail.get(indx);
            buf.append(indx.toString()); 
            buf.append(TAB);
            if (functionCategories != null) {
                buf.append(functionCategories[0]);
                if (colFlag)
                    functionIDs[i] = functionCategories[0];
                buf.append(TAB);
            }
            else if (colFlag)
                functionIDs[i] = indx.toString();
                
            buf.append(dformat.format(fragsFunc[i]));

            if (functionCategories != null && functionCategories.length > 1) {
                for (int j=1; j<functionCategories.length; j++) {
                    buf.append(TAB);
                    buf.append(functionCategories[j]);
                }

            }
 
            functionLines.add(buf.toString());
        }
     
        // for now, do nothing with boolean returned by writeProgressFile method
        writeProgressFile(functionFile, functionLines, false, null);
        
    }
    
}
