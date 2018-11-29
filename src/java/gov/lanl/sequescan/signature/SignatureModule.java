/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import gov.lanl.sequtils.log.MessageManager;
import java.io.BufferedReader;
import java.io.InputStream;


/**
 *
 * @author jcohn
 */
abstract public class SignatureModule extends MessageManager {
   
    // instance variables
    protected String modulePath;
    protected boolean goodModule;
    
  
 
    public SignatureModule(String modulePathStr) {
        modulePath = modulePathStr;
    }
    
    public SignatureModule() {
        super();
    }
    
    public String getModulePath() {
        return modulePath;
    }
    
    public boolean isGoodModule() {
        return goodModule;
    }
    
    public void setModulePath(String mpath) {
        modulePath = mpath;
    }
    
    abstract public String getSelectedFunctionSet();
    abstract protected BufferedReader getReader(String name);
    abstract protected InputStream getInputStream(String name);
    abstract public InputStream getTreeInputStream();
    abstract public BufferedReader getSignatureReader(); 
    abstract public BufferedReader getNodeDetailReader(); 
    abstract public String[][] getNodeColumns();
    abstract public BufferedReader getFunctionReader();
    abstract public BufferedReader getFunctionDetailReader(); 
    abstract public String[][] getFunctionColumns();
    abstract public BufferedReader getTreeReader(); 
    abstract public Integer getKmerSize(); 
    abstract public Integer getFunctionCount();
    abstract public Integer getNodeCount(); 
    abstract public Integer getSignatureCount();
    abstract public String getMaxHeap();
    abstract public Boolean getHexFlag();
    abstract public String getVersion();
    abstract protected void init();
  
  
}
