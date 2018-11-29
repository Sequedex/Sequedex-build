/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.signaturemap;

import gov.lanl.sequtils.util.BaseProperties;
import java.io.File;

/**
 *
 * @author jcohn
 */
public class GeneratorProperties extends BaseProperties implements GeneratorConstants {
    
    private static final long serialVersionUID = 1L;
  
    
    public GeneratorProperties() {
        super();
    }
    
    public File getInputDir() {
        return getExistingDir(INPUT_DIR);
    }
    
    public File getOutputDir() {
        return getExistingDir(OUTPUT_DIR);
    }
    
    public File getPhyXml() {
        return getExistingFile(SIG_PHYXML_FILE);
    }
    
    public String getSigDirMapFileName() {
        return getProperty(SIG_DIR_MAP_FILE);
    }
    
    public String getSigNodeMapFileName() {
        return getProperty(SIG_NODE_MAP_FILE);
    }
    
    public boolean getRecursiveFlag() {
        Boolean flag = getFlag(RECURSIVE_FLAG);
        if (flag == null)
            flag = false;
        return flag;
    }
    
    public String getAction() {
        return getProperty(ACTION);
    }
  
    
}
