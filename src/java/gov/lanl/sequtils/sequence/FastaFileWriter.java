/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import gov.lanl.sequescan.constants.ReportConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jcohn
 */
public class FastaFileWriter extends SequencingWriter implements ReportConstants {
   
    public FastaFileWriter(String fileName) {
        super(fileName);
    }
    
    public FastaFileWriter (File file) {
        super(file);
    }
    
    @Override
    public boolean open() {
        
        if (writer != null)
            return true;
        
        try {
            FileWriter fwriter = new FileWriter(seqFile);
            writer = new BufferedWriter(fwriter);
        } catch (Exception ex) {
            logger.error("Problem opening writer for "+seqFile.getAbsolutePath());
            return false;            
        }
        
        return true;
            
    }
    
    @Override
    public boolean close() {
        
        if (writer == null) {
            logger.warn("FastaFileWriter is null");
            return false;
        }
        else {
            try {
                writer.close();
                writer = null;
            } catch (Exception ex) {
                logger.error("Problem closing writer: "+ex.getMessage());
                writer = null;
                return false;
            }
            return true;
        }
    }
    
    @Override
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
    
    @Override
    public boolean writeRecord(SequencingRecord record) {       
        return writeRecord(record.getHeader(), record.getSequence());       
    }
    
    public boolean writeRecord(SequencingRecord record, int lineSize) {
        return writeRecord(record.getHeader(), record.getSequence(), lineSize);
    }
    
    public boolean writeRecord(String header, String sequence) {
        return writeRecord(header, sequence, 0);
    }
    
    public boolean writeRecord(String header, String sequence, int lineSize) {
 
        if (writer == null) 
            return open();
        
        try {
            writer.append(FASTA_BEGIN+header);
            writer.newLine();
            Collection<String> lines = chopSequence(sequence, lineSize);
            Iterator<String> lineIter = lines.iterator();
            while (lineIter.hasNext()) {
                writer.append(lineIter.next());
                writer.newLine();
            }
        
        } catch (Exception ex) {
            logger.error("Problem writing record: "+
                ex.getMessage());
            return false;  
        }
    
        return true;
    }
    
        /**
     *
     * @param lineSize 
     * @param sequence sequence to be chopped - if lineSize == 0, single line put in lines
     * @return Collection<String> 
     */
    public static Collection<String> chopSequence(String sequence, int lineSize) {
        if (sequence == null || sequence.trim().length() == 0)
            return null;
        
        int arrSize;
        if (lineSize == 0) {
            arrSize = 1;
        }
        else if (lineSize < 0) {
            lineSize = 0;
            arrSize = 1;
        }
        else {
            arrSize = 1+(sequence.length()/lineSize);
        }
        
        Collection<String> lines = new ArrayList<String>(arrSize);
        
        if (lineSize == 0) {
            lines.add(sequence.trim());
        }
        else {
            int len = sequence.length();
            int indx = 0;
            char[] letters = sequence.toCharArray();
            while (indx < len) {
                char[] fastaLine = new char[lineSize];
                for (int i=0;i<lineSize;i++) {
                    fastaLine[i] = letters[indx++];
                    if (indx >= len)
                        break;
                }
                String line = new String(fastaLine);
                lines.add(line.trim());
            }
        }
        
        return lines;
    }

    @Override
    public String getFileType() {
        return FASTA;
    }
    
    
}
