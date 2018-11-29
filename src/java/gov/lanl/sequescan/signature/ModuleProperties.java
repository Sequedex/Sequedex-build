/*
 *   At some point it might be a good idea to merge SignatureProperties and ModuleProperties
 *   or have one inherit from the other.  They are separate for historical reasons.
 * 
 *   For the moment, this class has convenience methods for using, creating, or
 *   modifying jar modules
 */
package gov.lanl.sequescan.signature;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.PropertyConstants;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.BaseProperties;
import java.io.Reader;
import java.util.List;


/**
 *
 * @author jcohn
 */
public class ModuleProperties extends BaseProperties 
    implements PropertyConstants {
    
    private static final long serialVersionUID = 1L;
      
    // instance variables
    protected String moduleEntry;

    public ModuleProperties() {
        super();
        moduleEntry = null;
    }
    
    public ModuleProperties (String entryName) {
        super();
        moduleEntry = entryName;  
    }
    
    public String getModuleEntry() {
        return moduleEntry;
    }
    
    public String getVersion() {
        return getProperty(MODULE_VERSION);
    }
    
    public Object setVersion(String version) {
        return setProperty(MODULE_VERSION, version);
    }
    
    public List<String> getVarNames() {
        return getStringList(VAR_NAMES_KEY);
    }
    
    public List<String> getPrettyNames() {
        return getStringList(PRETTY_NAMES_KEY);
    }
    
    public void setVarNames(List<String> names) {
        setStringList(VAR_NAMES_KEY, names);      
    }
    
    public void setPrettyNames(List<String> names) {
        setStringList(PRETTY_NAMES_KEY, names);
    }
    
    public Integer getCount() {
        return getInteger(COUNT);
    }
    
    public void setCount(int cnt) {
        setInteger(COUNT, cnt);
    }
    
    public String getSource() {
        return getProperty(SOURCE);
    }
    
    public void setSource(String src) {
        setProperty(SOURCE, src);
    }
    
    public String getTreeSource() {
        return getProperty(TREE_SOURCE);
    }
    
    public void setTreeSource(String tsrc) {
        setProperty(TREE_SOURCE, tsrc);
    }
    
    public String getDetailSource() {
        return getProperty(DETAIL_SOURCE);
    }
    
    public void setMaxHeap(String maxHeap) {
        setProperty(MAX_HEAP, maxHeap);
    }
    
    public String getMaxHeap() {
        return getProperty(MAX_HEAP);
    }
    
    public void setDetailSource(String src) {
        setProperty(DETAIL_SOURCE, src);
    } 
    
    public Integer getKmerSize() {
        return getInteger(KMER_SIZE);
    }
    
    public void setKmerSize(int ksz) {
        setInteger(KMER_SIZE, ksz);
    }
    
    public Boolean getHexFlag() {
        return getFlag(HEX_FLAG);
    }
    
    public void setHexFlag(boolean flag) {
        setFlag(HEX_FLAG, flag);
    }
    
    public Integer getSignatureCount() {
        return getInteger(SIG_COUNT);
    }
    
    public void setSignatureCount(int cnt) {
        setInteger(SIG_COUNT, cnt);
    }
    
       public void setScratchDir(String scrDir) {
        setProperty(SCRATCH_DIR, scrDir);
    }
    
    public String getScratchDir() {
        return getProperty(SCRATCH_DIR);
    }
    
    public void setModuleName(String name) {
        setProperty(MODULE_NAME, name);
    }
    
    public String getModuleName() {
        return getProperty(MODULE_NAME);
    }
    
    public void setPrefix(String pfx) {
        setProperty(PREFIX, pfx);
    }
    
    public String getPrefix() {
        return getProperty(PREFIX);
    }
    
    public void setFillNum(int num) {
        setInteger(FILL_NUM,num);
    }
    
    public Integer getFillNum() {
        return getInteger(FILL_NUM);
    }
    
    public String getModulePath() {
        return getProperty(MODULE_PATH);
    }
    
    public void setModulePath(String pathStr) {
        setProperty(MODULE_PATH, pathStr);
    }
    
    public String getFastaPath() {
        return getProperty(FASTA_PATH);
    }
    
    public void setFastaPath(String pathStr) {
        setProperty(FASTA_PATH, pathStr);
    }
    
    public String getFunctionsetName() {
        return getProperty(FUNCTIONSET_NAME);
    }
    
    public void setFunctionSetName(String name) {
        setProperty(FUNCTIONSET_NAME, name);
    }
    
    @Override
    public void load(Reader reader) {
        try {
            super.load(reader);
            reader.close();
        } catch (Exception ex) {
            if (moduleEntry != null)
                MessageManager.publish("Problem loading properties from "+moduleEntry,this,Level.ERROR);
        }
    }
    
}
