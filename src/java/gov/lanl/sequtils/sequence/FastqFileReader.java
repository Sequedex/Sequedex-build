/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcohn
 */
public class FastqFileReader extends SequencingReader {
    
    public FastqFileReader(String fileName) {
        super(fileName);
    }
    
    public FastqFileReader (File file) {
        super(file);
    }

    @Override
    public SequencingRecord nextRecord() {
          
        if (reader == null) {
            boolean okay = open();
            if (!okay)
                return null;
        }
        List<String> lines = new ArrayList<>(4); 
    
        try {
            for (int i=0; i<4; i++) {
                String line = reader.readLine();
                if (line == null || line.length() == 0) {
                    if (i==0)
                        return null;
                    else if (i>0) {
                        logger.error("fastq file read error: less than 4 lines in record; header="+
                            lines.get(0));
                        return null;
                    }                       
                }
                else {
                    lines.add(line);
//                    int extraBytes;
//                    if (i==0 || i==2) 
//                        extraBytes = 2;
//                    else
//                        extraBytes = 1;
                    bytesProcessed += (long) (line.length() + 1);
                    
                }
            }
            
        } catch (IOException ex) {
            logger.error("Problem reading next record: "+
                ex.getMessage());
            return null;   
        }
        
        if (!lines.get(0).startsWith(FASTQ_BEGIN)) {
            logger.error(seqFile.getAbsolutePath()+":  first line of record does not begin with "+
                FASTQ_BEGIN+": "+lines.get(0));
            return null;
        }
       
        if (!lines.get(2).startsWith(FASTQ_QBEGIN)) {
            logger.error(seqFile.getAbsolutePath()+": third line of record does not begin with "+
                FASTQ_QBEGIN+": "+lines.get(2));
            return null;
        }  
        
        String header = lines.get(0).substring(1);
        String seq = lines.get(1);
        String qheader = lines.get(2).substring(1);
        String qualStr = lines.get(3);
        if (seq.length() != qualStr.length()) {
            logger.error(seqFile.getAbsolutePath()+"sequence length != qualStr length in record: "+header);
            return null;
        }
        return new SequencingRecord(header,seq,qheader, qualStr, SequencingRecord.DNA); 
    }   
    
    @Override
    public String getFileType() {
        return FASTQ;
    }
    
}
