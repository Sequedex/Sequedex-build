/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequescan.constants;

/**
 *
 * @author jcohn
 */
public interface TreeConstants extends ReportConstants {
    
    // actions for TreeUtilities
    public static final String PROCESS_TREE = "process_tree";
    public static final String NODE_INFO = "node_info";
    
    // property units - optional
    public static final String UNIT_SET = "u:set";
    public static final String UNIT_ID = "u:id";
    public static final String UNIT_INFO = "u:info";
    public static final String UNIT_DATE = "u:date";   
    public static final String UNIT_CNT = "u:cnt";

    // property data types - required 
    public static final String TYPE_STR = "xsd:string";
    public static final String TYPE_INT = "xsd:integer";
    public static final String TYPE_DATE = "xsd:string";

    // node property ref names
    public static final String INPUT_NAME = "inName";  // note:  used to be inName: - removed : but older files will have it
    public static final String CREATION_NODE_ID = "nodeID";
    public static final String PARENT_NAME_SET = "parents";
     
    // current actions for TreeExtractor
    public static final String WRITE_CHILDREN = "write_children";
    public static final String WRITE_CHILDREN_ALL = "write_children_all";
    public static final String WRITE_TAXONOMY = "write_taxonomy";
    public static final String WRITE_NODE_NAMES = "write_node_names";
    public static final String WRITE_LEAVES = "write_leaves";
    public static final String WRITE_DESC_COUNTS = "write_descendent_counts";

    // tree file extensions
    public static final String PHYLOXML = ".phyloxml";
    public static final String NODE_EXT = ".node.phyloxml";
    public static final String PSET_EXT = ".pset.phyloxml";
    public static final String CNTS_EXT = ".cnts.phyloxml";

    // info key name roots
    public static final String TARGET = "target";
    public static final String JOB = "jobID";
    public static final String SRC_TREE = "srcTree";
    public static final String GRP_DATE = "runDate";
    public static final String GRP_ACTION = "action";
   
    
    // iterator types
    public static final int PREORDER = 0;
    public static final int POSTORDER = 1;
    public static final int LEVELORDER = 2;
    public static final int EXTERNALFWD = 3;
   

    // other constants
    public static final String DELIM = "|";
    public static final String EMPTY_TREE = "empty tree";
    public static final String NODE_REF_DELIM = ":";
    


}
