/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import gov.lanl.sequescan.constants.PropertyConstants;
import gov.lanl.sequtils.util.BaseProperties;

/**
 *
 * @author jcohn
 */
public class SignatureProperties extends BaseProperties 
    implements PropertyConstants {
    
    private static final long serialVersionUID = 1L;
    
    protected String configFilePath = null;
    
    public SignatureProperties() {
        super();
    }
    
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    public void setConfigFilePath(String fname) {
        configFilePath = fname;
    }
    
    public String getAction() {
        return getProperty(ACTION);
    }
    
    public Object setAction(String value) {
        return setProperty(ACTION, value);
    }
    
//    public Integer getTopNode() {
//        return getInteger(ANALYSIS_TOP_NODE);
//    }
//    
//    public Object setTopNode(int topNode) {
//        Integer topNodeInt = new Integer(topNode);
//        return setProperty(ANALYSIS_TOP_NODE, topNodeInt.toString());               
//    }
    
    
    public int getThreadNum() {
        Integer num = getInteger(THREAD_NUM);
        if (num == null)
            return 1;
        else
            return num;
    }
        
    public Object setThreadNum(int threadNum) {
        return setProperty(THREAD_NUM,Integer.toString(threadNum));
    } 
    
    public Integer getFunctionFillNum() {
        Integer num = getInteger(FUNCTION_FILL_NUM);
        if (num == null)
            return 4;
        else
            return num;
    }
        
    public Object setFunctionFillNum(int functFillNum) {
        return setProperty(FUNCTION_FILL_NUM,Integer.toString(functFillNum));
    } 
    
    public String getFunctionPrefix() {
        return getProperty(FUNCTION_PREFIX);
    }
        
    public Object setFunctionPrefix(String prefix) {
        return setProperty(FUNCTION_PREFIX,prefix);
    } 
    
    public Object setMaxHeapMB(long maxHeapVal) {
        return setProperty(MAX_HEAP_MB, Long.toString(maxHeapVal));
    }
    
    public long getMaxHeapMB() {
        return getLong(MAX_HEAP_MB);
    }
    
    public Object setWDWFlag(int flag) {
        return setProperty(WDW_FLAG, Integer.toString(flag));
    }
    
    public Integer getWDWFlag() {
        return getInteger(WDW_FLAG);
    }
    
    public Object setTranslateFlag(boolean flag) {
        return setFlag(TRANSLATE_FLAG, flag);
    }
    
    public Boolean getTranslateFlag() {
        return getFlag(TRANSLATE_FLAG);
    }
    
    
    @Override
    public String getDefaultValue(String key) {
        switch (key) {
            case CUTOFF:
                return "15";
            case THREAD_NUM:
                return "1";
            case ACTION:
                return COMBINED_REPORT;
            case FUNCTION_FILL_NUM:
                return "4";
            case HEX_FLAG:
                return "TRUE";
            case WDW_FLAG:
                return "0";
            default:
                return super.getDefaultValue(key);
        }
    }
    
    public String getJarModule() {
        return getProperty(JAR_MODULE);
    }
    
    public void setJarModule(String fileName) {
        setProperty(JAR_MODULE,fileName);
    }
    
    public String getFunctionSetName() {
        return getProperty(FUNCTIONSET_NAME);
    }
    
    public void setFunctionSetName(String name) {
        setProperty(FUNCTIONSET_NAME, name);
    }
    
    public String getSignatureFile() {
        return getProperty(SIGNATURE_FILE);
    }
    
    public void setSignatureFile(String fileName) {
        setProperty(SIGNATURE_FILE,fileName);
    }
    
    public String getFunctionMapFile() {
        return getProperty(FUNCTION_MAP_FILE);
    }
    
    public void setFunctionMapFile(String fileName) {
        setProperty(FUNCTION_MAP_FILE,fileName);
    }
    
    public String getTreeFile() {
        return getProperty(TREE_FILE);
    }
   
    public Object setTreeFile(String fileName) {
        return setProperty(TREE_FILE,fileName);
    }
    
    public String getNodeDetail() {
        return getProperty(NODE_DETAIL);
    }
   
    public Object setNodeDetail(String fileName) {
        return setProperty(NODE_DETAIL,fileName);
    }
    
    public String getFunctionDetail() {
        return getProperty(FUNCTION_DETAIL);
    }
   
    public Object setFunctionDetail(String fileName) {
        return setProperty(FUNCTION_DETAIL,fileName);
    }
    
    public Integer getCutoff() {       
        return getInteger(CUTOFF);
    }
  
    public Object setCutoff(int cutoff) {
        return setProperty(CUTOFF,Integer.toString(cutoff));
    }
    
    public Boolean getHexFlag() {       
        return getFlag(HEX_FLAG);
    }
  
    public Object setHexFlag(boolean flag) {
        return setFlag(HEX_FLAG, flag);
    }
    
    public Integer getKmerSize() {       
        return getInteger(KMER_SIZE);
    }
  
    public Object setKmerSize(int kmerSize) {
        return setProperty(KMER_SIZE,Integer.toString(kmerSize));
    }
    
    public Integer getFunctionCount() {
        return getInteger(FUNCTION_COUNT);
    }
        
    public Object setFunctionCount(int num) {
        return setProperty(FUNCTION_COUNT,Integer.toString(num));
    } 
    
    public Object setModuleName(String name) {
        return setProperty(MODULE_NAME, name);
    }
    
    public String getModuleName() {
        return getProperty(MODULE_NAME);
    }
    
    public Integer getSignatureCount() {       
        return getInteger(SIG_COUNT);
    }
  
    public Object setSignatureCount(int sigCount) {
        return setProperty(SIG_COUNT,Integer.toString(sigCount));
    }
    
    public void setMaxHeap(String maxHeap) {
        setProperty(MAX_HEAP, maxHeap);
    }
    
    public String getMaxHeap() {
        return getProperty(MAX_HEAP);
    }
    
    
    
}
