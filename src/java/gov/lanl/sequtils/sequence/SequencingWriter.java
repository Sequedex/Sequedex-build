/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import gov.lanl.sequescan.constants.ReportConstants;
import gov.lanl.sequtils.log.MessageManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author jcohn
 */
abstract public class SequencingWriter extends MessageManager implements ReportConstants {
     
    // instance variables
    protected BufferedWriter writer = null;
    protected File seqFile;
    protected boolean translateFlag;
    
    public SequencingWriter(String fileName, boolean tFlag) {
        seqFile = new File(fileName);
        translateFlag = tFlag;
    }
    
    public SequencingWriter(String fileName) {
        this(fileName, false);
    }
    
    public SequencingWriter (File file) {
        seqFile = file;
    }
    
    public File getFile() {
        return seqFile;
    }
    
    public boolean open() {
        
        if (writer != null)
            return true;
        
        try {
            FileWriter fwriter = new FileWriter(seqFile);
            writer = new BufferedWriter(fwriter);
        } catch (IOException ex) {
            logger.error("Problem opening writer for "+seqFile.getAbsolutePath());
            return false;            
        }
        
        return true;
            
    }
    
    public boolean close() {
        
        if (writer == null) {
            logger.warn("FastaFileWriter is null");
            return false;
        }
        else {
            try {
                writer.close();
                writer = null;
            } catch (IOException ex) {
                logger.error("Problem closing writer: "+ex.getMessage());
                writer = null;
                return false;
            }
            return true;
        }
    }
    
    public boolean flush() {
        
        if (writer == null) {
            logger.warn("FastaFileWriter is null");
            return false;
        }
        else {
            try {
                writer.flush();
            } catch (Exception ex) {
                logger.error("Problem flushing FastaFileWriter: "+ex.getMessage());
                return false;
            }
            return true;
        }     
    }   
    
    // abstract methods
    abstract public boolean writeRecord(SequencingRecord record);
    abstract public String getFileType();
 
    
}
