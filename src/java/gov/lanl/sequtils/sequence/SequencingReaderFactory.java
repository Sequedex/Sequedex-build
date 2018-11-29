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
public class SequencingReaderFactory implements ReportConstants {
    
    public static SequencingReader getReader(File sequencingFile) {
        
        if (sequencingFile == null)
            return null;
        
        String fileType = SequencingReader.genSequenceFileType(sequencingFile);
        if (null == fileType)
            return null;
        else switch (fileType) {
            case FASTQ:
                return new FastqFileReader(sequencingFile);
            default:
                return new FastaFileReader(sequencingFile, fileType);
        }
    }
    
}
