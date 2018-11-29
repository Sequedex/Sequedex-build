/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.constants;

/**
 *
 * @author jcohn
 */
public interface ModeConstants {
    
    // modes
    public static final String CREATE_MODULE = "create_module";
    public static final String MODULE_INFO = "module_info";
    public static final String ADD_FUNCTION = "add_function";
    public static final String ADD_DETAIL = "add_detail";
    public static final String ADD_FUNCTION_DETAIL = "add_function_detail";
    public static final String KMER_MAP = "kmer_map";
    public static final String REMOVE_FUNCTION = "remove_function";  // not yet implemented 
    public static final String RUN = "run";
    
    // query types for DataModuleMode with modeStr = INFO
    public static final String FUNCTIONSET_LIST = "functionset_list";
    public static final String MAX_HEAP = "max_heap";
    public static final String SIGNATURE_COUNT = "signature_count";
    public static final String NODE_COUNT = "node_count";
    public static final String MODULE_VERSION = "module_version";
    public static final String ALL = "all";
    
    // other
    public static final String VALUE_DELIM = "|";
    
}
