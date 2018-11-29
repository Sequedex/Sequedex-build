/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.constants;

/**
 *
 * @author jcohn
 */
public interface PropertyConstants {
    
    // Constants for AnalysisParamFile only
    
    // general key values 
    public static final String THREAD_NUM = "THREAD_NUM";
    public static final String ACTION = "ACTION";
    public static final String CUTOFF = "CUTOFF";
    public static final String WDW_FLAG = "WDW_FLAG";
    public static final String ANALYSIS_TOP_NODE = "ANALYSIS_TOP_NODE";
    public static final String MAX_HEAP_MB = "MAX_HEAP_MB";  // is both this and MAX_HEAP needed (see below)?
    public static final String TRANSLATE_FLAG = "TRANSLATE_FLAG";
    // keys for JarModule only
    public static final String JAR_MODULE = "JAR_MODULE";
        
    // values for action key 
    public static final String COMBINED_REPORT = "combined_report";
    
    // keys for PropertiesModule only
    public static final String SIGNATURE_FILE = "SIGNATURE_FILE";
    public static final String FUNCTION_MAP_FILE = "FUNCTION_MAP_FILE";
    public static final String TREE_FILE = "TREE_FILE";
    public static final String NODE_DETAIL = "NODE_DETAIL";
    public static final String FUNCTION_COUNT = "FUNCTION_COUNT";
    public static final String FUNCTION_DETAIL = "FUNCTION_DETAIL";
    public static final String FUNCTION_FILL_NUM = "FUNCTION_FILL_NUM";
    public static final String FUNCTION_PREFIX = "FUNCTION_PREFIX";
    public static final String NODE_DETAIL_COLUMNS = "NODE_DETAIL_COLUMNS";
    public static final String FUNCTION_DETAIL_COLUMNS = "FUNCTION_DETAIL_COLUMNS";
  
    
    // keys for Jar and Param Modules, also used by ModuleProperties
    public static final String FUNCTIONSET_NAME = "FUNCTIONSET_NAME";
    
    // Constants for ModuleProperties (and child classes) only
    
    // keys in nodes.prop or various function .prop files
    public static final String COUNT = "COUNT";
    public static final String SOURCE = "SOURCE";
    public static final String TREE_SOURCE = "TREE_SOURCE";
    public static final String DETAIL_SOURCE = "DETAIL_SOURCE";
    // need _KEY in next two constants to distinguish from VAR_NAMES, PRETTY_NAMES int constants
    public static final String VAR_NAMES_KEY = "VAR_NAMES";
    public static final String PRETTY_NAMES_KEY = "PRETTY_NAMES";
    public static final String MODULE_VERSION = "VERSION";
    public static final String SIG_COUNT = "SIG_COUNT";
    public static final String PREFIX = "PREFIX";
    public static final String FILL_NUM = "FILL_NUM";
    
    // Constants for ParamModule and data.prop file    
    public static final String HEX_FLAG = "HEX_FLAG";
    public static final String KMER_SIZE = "KMER_SIZE";
    public static final String MAX_HEAP = "MAX_HEAP";
    
    // Additional Constants for ModuleModeProperties
    public static final String SCRATCH_DIR = "SCRATCH_DIR";
   
    // Constants for ModuleModeProperties and PropertiesModule
    public static final String MODULE_NAME = "MODULE_NAME";
    public static final String MODULE_PATH = "MODULE_PATH";
    public static final String FASTA_PATH = "FASTA_PATH";
    
}
