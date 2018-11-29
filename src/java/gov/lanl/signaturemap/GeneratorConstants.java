/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.signaturemap;

/**
 *
 * @author jcohn
 */
public interface GeneratorConstants {
    
    
    /* property names for GeneratorProperties */
    
    public static String INPUT_DIR = "INPUT_DIR";
    public static String OUTPUT_DIR = "OUTPUT_DIR";
    public static String SIG_PHYXML_FILE = "SIG_PHYXML_FILE";
    public static String SIG_DIR_MAP_FILE = "SIG_DIR_MAP_FILE";
    public static String SIG_NODE_MAP_FILE = "SIG_NODE_MAP_FILE";
    public static String ACTION = "ACTION";
    public static String NODE_PREFIX = "NODE_PREFIX";
    public static String RECURSIVE_FLAG = "RECURSIVE_FLAG";
  
    /* actions */
    public static String KMER_DIR_MAP = "kmer_dir_map";
    public static String KMER_NODE_MAP = "kmer_node_map";
    public static String BOTH = "both";

    /* aakbar signature directories and files */
    
    public static String SIGNATURES_DIR = "signatures";
    public static String LOCAL_SIG_LIST = "all-signatures_siglist.tsv";
    public static String COMBINED_SIG_LIST = "signatures_terms.tsv";
    
}
