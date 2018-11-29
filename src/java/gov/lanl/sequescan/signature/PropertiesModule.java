/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import java.io.*;

/**
 *
 * @author jcohn
 */
public class PropertiesModule extends SignatureModule  {
    
    // instance variables
    protected SignatureProperties  props;
    protected Integer nodeFillNum = new Integer(4);
    protected Integer nodeCount = null;
    protected String nodePrefix = "n";
    
    
    public PropertiesModule(SignatureProperties sprops) {
        super();
        props = sprops;  // assumes that values have been read from file or set using set functions
        if (props.getFilePath() != null)
            setModulePath(props.getFilePath());
        else
            setModulePath(props.getConfigFilePath());
        init();
    }
    
    @Override
    protected final void init() {
        if (getHexFlag() == null) {
            logger.error( "HEX_FLAG is missing from "+getModulePath());
            goodModule = false;
        }
        else if (getKmerSize() == null) {
        	logger.error( "KMER_SIZE is missing from "+getModulePath());
            goodModule = false;
        }
        else
            goodModule = true;
    }

    @Override
    protected BufferedReader getReader(String name) {
        File file = new File(name);
        if (name == null) {
            return null;
        }
        else if (!file.exists()) {
        	logger.error( name+" does not exist");
            return null;
        }
        else {
            try {
                FileReader freader = new FileReader(name);
                return new BufferedReader(freader);
            } catch (Exception ex) {
            	logger.error( "Problem creating BufferedReader for "+name+": "+
                    ex.getMessage());
                return null;
            }
        }
    }

    @Override
    public BufferedReader getSignatureReader() {
        return getReader(props.getSignatureFile());
    }

    @Override
    public BufferedReader getNodeDetailReader() {
        return getReader(props.getNodeDetail());
    }

    @Override
    public BufferedReader getFunctionReader() {
        return getReader(props.getFunctionMapFile());
    }

    @Override
    public BufferedReader getFunctionDetailReader() {
        return getReader(props.getFunctionDetail());
    }

    @Override
    public BufferedReader getTreeReader() {
        return getReader(props.getTreeFile());
    }

    @Override
    public Integer getKmerSize() {
        if (props == null)
            return null;
        else
            return props.getKmerSize();
    }
    
    public void setNodeCount(Integer cnt) {
        nodeCount = cnt;
    }
    
    @Override
    public Integer getNodeCount() {
        return nodeCount;
    }
    
    public void setNodePrefix(String str) {
        nodePrefix = str;
    }
    

    @Override
    public Integer getFunctionCount() {
        if (props == null)
            return null;
        else 
            return props.getFunctionCount();
    }


    @Override                                                                                                                                                         
    public String getSelectedFunctionSet() {
        return props.getFunctionSetName();
    }
    
    public String getFunctionMapFile() {
        return props.getFunctionMapFile();
    }

    @Override
    public Boolean getHexFlag() {
        return props.getHexFlag();                       
    }

    @Override
    protected InputStream getInputStream(String name) {
        File file = new File(name);
        if (name == null) {
            return null;
        }
        else if (!file.exists()) {
        	logger.error( name+" does not exist");
            return null;
        }
        else {
            try {
                return new FileInputStream(name);
            } catch (Exception ex) {
            	logger.error( "Problem creating FileInputStream for "+name+": "+
                    ex.getMessage());
                return null;
            }
        }
    }

    @Override
    public InputStream getTreeInputStream() {
        return getInputStream(props.getTreeFile());
    }

    @Override
    public String[][] getNodeColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[][] getFunctionColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Integer getSignatureCount() {
        if (props == null)
            return null;
        else
            return props.getSignatureCount();
    }

    @Override
    public String getMaxHeap() {
        return props.getMaxHeap();
    }
    
    
    
  

    
}
