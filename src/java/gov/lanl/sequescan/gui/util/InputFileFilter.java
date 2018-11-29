/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.gui.util;

import gov.lanl.sequtils.sequence.SequencingReader;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author jcohn
 */
public class InputFileFilter extends FileFilter {
    
    protected String[] validExtArr;
    
    public InputFileFilter(String[] extArr) {
        super();
        validExtArr = extArr;
    }
    
    @Override
    public boolean accept(File f) { 
        if (f.isDirectory())
            return true;
        String rawName = f.getName();
        String fileName = SequencingReader.getProcessFileName(rawName);
        List<String> validExtList = Arrays.asList(validExtArr);
        return SequencingReader.validInputFile(fileName, validExtList);
    }
    
    @Override
    public String getDescription() {
        return "DNA sequencing Files";
    }
    
}
