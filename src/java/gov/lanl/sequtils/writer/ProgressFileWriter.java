/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.writer;

import gov.lanl.sequtils.event.ProgressEvent;
import java.time.LocalDateTime;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import gov.lanl.sequescan.constants.AppConstants;


/**
 *
 * @author jcohn
 */
public abstract class ProgressFileWriter extends ProgressWriter implements AppConstants {
    
    // instance variables
    protected File statsFile=null, nodeFile=null, functionFile=null, wdwFile=null;
    protected File outputDir;
    protected String[] nodeIDs = null; 
    protected String[] functionIDs = null; 
    protected String[][] nodeDataColNames;
    protected String[][] functionDataColNames;
    
    public ProgressFileWriter() {
        init();
    }
    
    protected final void init() {
        setDataColNames();
    }
    
    protected void setDataColNames() {
        nodeDataColNames = new String[2][5];
        nodeDataColNames[VAR_NAMES][0] = "total";
        nodeDataColNames[VAR_NAMES][1] = "single_sig";
        nodeDataColNames[VAR_NAMES][2] = "single_node";
        nodeDataColNames[VAR_NAMES][3] = "monophyletic";
        nodeDataColNames[VAR_NAMES][4] = "non_monophyl";
        nodeDataColNames[PRETTY_NAMES][0] = "Total";
        nodeDataColNames[PRETTY_NAMES][1] = "Single Sig.";
        nodeDataColNames[PRETTY_NAMES][2] = "Single Node";
        nodeDataColNames[PRETTY_NAMES][3] = "Monophyletic";
        nodeDataColNames[PRETTY_NAMES][4] = "Non-monophyl.";
        functionDataColNames = new String[2][1];
        functionDataColNames[VAR_NAMES][0] = "fragments";
        functionDataColNames[PRETTY_NAMES][0] = "Fragments";
    }
    
    public void setOutputDir(File outputDirFile) {
        outputDir = outputDirFile;
//        if (outputDir.exists())
//            FileAndDirHandler.deleteDirectory(outputDir);
        if (!outputDir.exists())
            outputDir.mkdirs();
    }
        
    @Override
    public void observeProgressEvent(ProgressEvent event) {
        super.observeProgressEvent(event);
    }
    
    protected String getFnaFileStr() {

            String fnaFileName = progressData.getFnaFileName();
            File fnaFile = new File(fnaFileName);
            return fnaFile.getName();
    }
    
    protected boolean writeProgressFile(File file, List<String> lines,
        boolean substituteHeaderFlag, String substituteHeaderStr) {
        
        if (lines == null || lines.size() < 1) {
            logger.error("There are no lines for progress file "+file.getAbsolutePath());
            return false;
        }
        else
            logger.debug("Writing progress file: "+file.getAbsolutePath());
        try {
            FileWriter fileWriter = new FileWriter(file);
            try (BufferedWriter writer = new BufferedWriter(fileWriter)) {
                Iterator<String> lineIter = lines.iterator();
                String headerLine = lineIter.next();
                if (substituteHeaderFlag) {
                    writer.write(substituteHeaderStr);
                }
                else
                    writer.write(headerLine);
                writer.newLine();
                while (lineIter.hasNext()) {
                    writer.write(lineIter.next());
                    writer.newLine();
                }
                writer.flush();
            }
        
        } catch (IOException ex) {
            logger.error("Problem writing progress file "+file.getAbsolutePath()+": "+ex.getMessage());
            return false;          
        }
    
        return true;
    }

    /*
     * Generates and returns a column's name, it's data type and number of decimal places.
     * (e.g. data structure that looks like this:  {"name":"ID","dtype":"S4"})
     * @param prettyName
     * @param dataType
     * @param decimalPlaces
     * @return
     */
    protected Map<String,Object> getColMap( String varName, String prettyName,
        String dataType, Integer decimalPlaces) {
            Map<String,Object> colMap = new LinkedHashMap<>(3);  //was 4
            //ProgressFileWriterJ passes in a null placeholder - so skip if that's the case
            if( varName != null)
            	colMap.put("varname", varName);
            colMap.put("name", prettyName);
            colMap.put("dtype", dataType);
            if (decimalPlaces != null)
                colMap.put("dplaces",decimalPlaces);   
            return colMap;      
    }

    public static int[] getDetailMaxWidth(Map<Short,String[]> detail) {
        
        if (detail == null)
            return null;
        
        // assumes String[] has same size for each map value
        Collection<String[]> values = detail.values();
        Iterator<String[]> valueIter = values.iterator();
        String[] firstValue = valueIter.next();
        int[] maxWidth = new int[firstValue.length];
        for (int i=0; i<maxWidth.length; i++) {
            maxWidth[i] = firstValue[i].length();
        }
        while (valueIter.hasNext()) {
            String[] value = valueIter.next();
            for (int i=0; i<maxWidth.length; i++) {
                if (value[i].length() > maxWidth[i]) 
                    maxWidth[i] = value[i].length();
            }
        }
        
        return maxWidth;
    }
    
    public String getLocalTime(){
        return LocalDateTime.now().toString();
    }
    

}
