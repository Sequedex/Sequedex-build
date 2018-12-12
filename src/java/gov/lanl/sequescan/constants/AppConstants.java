package gov.lanl.sequescan.constants;

/**
 * Java interface to define and access global variables for the package sequescan.
 * Renamed (from SequescanConstants) and moved
 *
 * @since     0.9.0
 */
public interface AppConstants {
	/**
	 * The name of the application.
	 */
	public static final String PROGRAM_NAME = "sequescan";
    
	/** The constant FILE_URL_PREFIX
	 */
//	public static final String FILE_URL_PREFIX = "file://";
	
	/** The Constant SEP.
	 * The character used as file separator depending on the operating system. */
	public static final String SEP 	= java.io.File.separator; 
	
	/**
	 * default name of the log file subdirectory (in output directory)
	 */
	public static final String LOG_DIR = "log";
	
	/**
	 * default name of the data file subdirectory in distrib directory
	 */
	public static final String DATA_DIR = "data";
        
        public static final String ETC_DIR = "etc";
	
	/**
	 * The name of the data input file directory.
	 */
	public static final String DATA_INPUT_NAME = "input";
	
	/**
	 * The name of the data output file directory.
	 */
	public static final String DATA_OUTPUT_NAME = "output";
	
	/**
	 * The date and time format.
	 */
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd"; //= "yyyyMMdd-HH:mm:ss";
	
	
    /** The Constant DEFAULT_CONFIG_FILE_NAME. 
     * The default file name of the configuration file. */
    public static final String DEFAULT_CONFIG_FILE_NAME = "sequescan.conf";
    
    /**
     * The Constant CONFIG_COMMENT.
     * The character denoting a comment in the configuration file.
     */
    public static final char CONFIG_COMMENT = '#';
    
    /** The Constant DEFAULT_LOG_LEVEL. 
     * The default log level. */
    public static final String DEFAULT_LOG_LEVEL	 = "INFO";
    
    /** The Constant DEFAULT_V_FLAG. 
     * The default value to use verbose output or not. */
    public static final boolean DEFAULT_V_FLAG	= false;
    
    /** The Constant VERSION_NUMBER. 
     * The version number of this software package. */
    public static final String VERSION_NUMBER = 	"1.1";
    
    /** The Constant VERSION_MAJOR_NUMBER. 
     * The version number of this software package. */
    public static final String VERSION_MAJOR_NUMBER = 	"1";
    
    /** The Constant MIN_DATA_VERSION_NUMBER
     *  The minimum version number of data modules
     *  Earlier versions (or no version) will not work.
     */
    public static final String MIN_DATA_VERSION_NUMBER = "0.4";  
    
    /** The Constant DATA_VERSION_NUMBER
     *  The current version number of data modules
     */
    public static final String DATA_VERSION_NUMBER = "1.0";  
    
     /** The Constant MIN_CONFIG_FILE_VERSION_NUMBER
     *  The minimum version number of config file
     *  Earlier versions (or no version) will not work.
     */
    public static final String MIN_CONFIG_FILE_VERSION_NUMBER = "1.2";
    
    /** The Constant CONFIG_FILE_VERSION_NUMBER
     *  The current version number of config file
     */
    public static final String CONFIG_FILE_VERSION_NUMBER = "1.2";
    
 
    
    /**
	 * The character surrounding environmental variables (e.g. %SEQUEDEX_DATA)
	 */
    public static final String VAR_SUBSTITUTION_CHAR = "%";
    
    /** The Constant MEGABYTE. 
     * The definition of a megabyte. */
    public static final long MEGABYTE = 1024L * 1024L;
    
    //======================Constants used by J. Cohn's code======================//
    
    public static final String COMBINED_REPORT = "combined_report";
    
    // mode constants     
    public static final String RUN = "run";  // run sequescan
    public static final String DATA = "data";  // create, edit or query JarModules
    
    // additional keys or other constants for ConfigFile and saved options?
    public static final String LIST_FLAG = "LIST_FLAG";
    public static final String DBNAME = "DBNAME";
    public static final String INSUBDIR = "INSUBDIR";
    public static final String INFILE = "INFILE";
    public static final String LOG_DIR_PARENT = "LOG_DIR_PARENT";
    public static final String BASE_DIR = "BASE_DIR";
    public static final String MODULEDIR = "MODULEDIR";
    public static final String USERDIR = "USERDIR";
    public static final String FUNCTIONSET = "FUNCTIONSET";  // replaces "SCHEME"
    public static final String FUNCTIONSETLIST = "FUNCTIONSETLIST"; // replaces "SCHEMELIST"
//    public static final String NEWSCHEME = "NEWSCHEME";  // ???
    
    
    // keys in ConfigFile
    public static final String PROGRESS_INTERVAL = "progress_interval_long";
    public static final String NCPUS = "nCPUS_int";
    public static final String PROGRESS_WRITER = "progress_writer_list";
 //   public static final String ANALYSIS_WRITER = "analysis_writer_list";
    public static final String ANALYSIS_WRITER_STRING = "analysis_writer_string_list";
    public static final String ANALYSIS_WRITER_LONG = "analysis_writer_long_list";
    public static final String ANALYSIS_WRITER_M = "analysis_writer_m_list";
    public static final String RESULT_DISPLAY_PATH = "result_display_path";
    public static final String ANALYSIS_OUTPUT_TYPE = "analysis_output_type";
    public static final String FASTA_EXT_LIST = "fasta_ext_list";
    public static final String ANALYSIS_TOP_NODE_CONFIG = "analysis_top_node_int";
    public static final String MIN_PROT_FRAG = "min_prot_frag_len_int";
    public static final String LOG_FILE_NAME = "log_file_name";  // not in config file - dynamically set or not used ?
    public static final String OUT_DIR_EXT = "out_dir_ext";
    public static final String WHO = "who";
    public static final String WHAT = "what";
    public static final String WDW = "who_does_what";
    public static final String WHAT_STATS = "what_stats";   // not used?
    public static final String STATS = "stats";
    public static final String DB = "db";
    public static final String CONFIG_FILE_VERSION = "config_file_version";
    public static final String LOG_LEVEL = "log_level";
    public static final String DATA_MODULE_DIR = "data_module_dir";
    
    // keys not currently in ConfigFile ?
    public static final String WRITE_DB = "write_db_bool";
    public static final String WRITE_WDW = "write_wdw_bool";
    
    // common ConfigFile keys
    public static final String APP_VERSION = "APP_VERSION";
    public static final String APP_VERSION_MAJOR = "APP_VERSION_MAJOR";
    public static final String DATETIME = "DATETIME";
 
    // misc constants
    public static final String NONE = "none";
    public static final String INPUT_LOCATION = "input";
    
    // tree constants
    public static final String TREE_DIR = "trees";
    public static final String TREE_PHYLOXML = "tree.phyloxml";
    public static final String DEFAULT_ARCHY_CONFIG = "_aptx_configuration_file";
    // public static final String NOVALID_ARCHY_CONFIG = "_aptx_config_life2550";
    
    
}
