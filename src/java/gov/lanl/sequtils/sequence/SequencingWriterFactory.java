/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;

import gov.lanl.sequescan.constants.ReportConstants;
import java.io.File;

/**
 *
 * @author jcohn
 */
public class SequencingWriterFactory implements ReportConstants {
    
    public static SequencingWriter getWriter(File sequencingFile) {
        
        if (sequencingFile == null)
            return null;
        
//        String fileName = sequencingFile.getName();
        String fileType = SequencingReader.genSequenceFileType(sequencingFile, true);
        if (null == fileType)
            return null;
        else switch (fileType) {
            case FASTQ:
                return new FastqFileWriter(sequencingFile);
            default:
                return new FastaFileWriter(sequencingFile);
        }
    }
    
    public static SequencingWriter getWriter(File sequencingFile, String outputType) {
        
        if (outputType == null)
            return null;
        else if (outputType.equals(FASTQ))
            return new FastqFileWriter(sequencingFile);
        else
            return new FastaFileWriter(sequencingFile);
    }
    
}
