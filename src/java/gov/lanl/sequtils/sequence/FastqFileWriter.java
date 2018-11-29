/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import java.io.File;

/**
 *
 * @author jcohn
 */
public class FastqFileWriter extends SequencingWriter {
    
    public FastqFileWriter(String seqFilePath) {
        super(seqFilePath);
    }
    
    public FastqFileWriter(File seqFile) {
        super(seqFile);
    }

    @Override
     public boolean writeRecord(SequencingRecord record) {
 
        if (writer == null) 
            return open();
        
        try {
            writer.append(FASTQ_BEGIN);
            writer.append(record.getHeader());
            writer.newLine();
            writer.append(record.getSequence());
            writer.newLine();
            String qheader = record.getQualHeader();
            if (qheader == null)
                qheader = "";
            writer.append(FASTQ_QBEGIN);
            writer.append(qheader);
            writer.newLine();
            String qualStr = record.getQualScores();
            if (qualStr == null)
                qualStr = "";
            writer.append(qualStr);
            writer.newLine();
        
        } catch (Exception ex) {
            logger.error("Problem writing record: "+
                ex.getMessage());
            return false;  
        }
    
        return true;
    }
    

    @Override
    public String getFileType() {
        return FASTQ;
    }
    
    
    
}
