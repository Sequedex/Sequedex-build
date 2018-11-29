/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.mode;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.analysis.AnalysisRunner;
import gov.lanl.sequescan.constants.PropertyConstants;
import gov.lanl.sequescan.signature.SignatureProperties;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.util.FileObj;
import gnu.getopt.Getopt;
import gov.lanl.sequtils.util.ConfigFile;
import gov.lanl.sequtils.util.ProgramStats;
import gov.lanl.sequtils.util.StringOps;
import gov.lanl.sequtils.util.SystemUtilities;
import gov.lanl.sequtils.util.ValidationUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class extending Mode for running sequescan analysis.
 * Bulk of the raw code comes from earlier version of
 * gov.lanl.sequescan.Sequescan class
 * Code has been reorganized by appropriately splitting into base Mode class 
 * and this run-specific extension of Mode class as well as reorganizing methods 
 * within those classes to fit multi-mode application.
 * 
 * @author J. Cohn <jcohn@lanl.gov>
 * @modified M. Dimitrijevic to make compatible with release Python wrapper etc.
 * @modified J. Cohn for changes due to removal of Python wrapper, licensing
 * 
 */
public class RunMode extends Mode {
    
  

    protected String sigDbName = null; 
    protected String sigDbDirAndName = null;
    protected String outDir = null;
    protected String functionSetName = null; 
    protected String newScheme = null;
    protected String dbDirAndName = null;
    protected boolean listFlag = false;
    protected int threadNumArg = -1;
    protected int wdwWriteFlag = 0;  // 0 = don't output, 1 = output;  default = 0
    protected int dbWriterFlag = 0;   //  0 = don't output, 1 = output; default = 0
//    protected int topNode = -1;  // -1 not set (i.e. use config setting)
    protected int minAAFragLength = -1;  // -1 not set (i.e. use config setting)
    protected String baseDir = null;
//    protected boolean writeNoFuncFlag = false;
    protected boolean sequescanInput = false;
    protected boolean translateFlag = false;
    
    
    public RunMode(String[] appArgs, boolean isInternal) {
        super(appArgs, isInternal);
    }   

    /**
        * Method that takes an absolute path and extracts the file name from it.
        * @param fullPath 
        * @return the file name
        */
    protected String getInfileName( String fullPath){
        File file = new File(fullPath);
        return file.getName();
    }
    

    
    protected boolean getListFlag() {
        return listFlag;
    }
    
    protected void setListFlag(boolean flag) {
        listFlag = flag;
    }
    
    public void setSequescanInputFlag(boolean flag) {
        sequescanInput = flag;
    }
    
    public boolean getSequescanInputFlag() {
        return sequescanInput;
    }
    
    @Override
    protected String checkInputFiles() {
        String errorMessage = super.checkInputFiles();
        errorMessage += ValidationUtils.checkFile( sigDbDirAndName);
        return errorMessage;
    }
    
    @Override
    public boolean init() {
        // note:  includes minimally reading config file and initiating log file
        boolean okay = super.init();
        if (!okay)
            return false;
                
        okay = addMoreConfig();
        if (!okay)
            return false;
        
        okay = processConfigValues();
        if (!okay)
            return false;
       
        if (outDir == null)
            outDir = INPUT_LOCATION;
        
        boolean outputStatus = checkOutputPermissions();
        if (!outputStatus) {
            String msg;
            if (outDir.equals(INPUT_LOCATION))
                msg = "Write permission does not exist for at least one of the input locations";
            else
                msg = "Problem with write permission for chosen output directory: "+outDir;
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        okay = checkMemoryRequirements();
        if (!okay)
            return false;
        
        if (autoMaxHeapFlag) {
            generateExternalRunCommand();
            ArrayList<String> msgList = new ArrayList<>();
            publish("Sequescan will run externally executing the following command:",this,Level.INFO);
            publish(externalRunCommand,this,Level.INFO);
        }
//        else {
//            publish("Sequescan called with Manual Max Heap",this,Level.INFO);
//            if (verbose) {
//                String msg = "Output directory is "+outDir;
//                publish(msg,this);
//            }
//        }
       
        return true;
    } 
    
    public void generateExternalRunCommand() {
        String jarFilePath = ConfigFile.getJarPath();
        int moduleMaxHeapMb = getJarModuleMaxHeapMb(sigDbName);
        StringBuilder cmd = new StringBuilder();
        StringBuilder output = new StringBuilder();
             
        cmd.append("java -Xmx");
        cmd.append(Integer.toString(moduleMaxHeapMb));
        cmd.append("m -jar ");
        cmd.append(jarFilePath);
        cmd.append(" run -m -n -g ");
        cmd.append(logFileName);
        for (String currentOption : modeOptionArr) {
            cmd.append(" ");
            cmd.append(currentOption);
        }
        externalRunCommand = cmd.toString();
    }
    
   @Override
    public void runWithAutoMaxHeap() {
        
        if (externalRunCommand != null)
            runExternalCommand(externalRunCommand);
        else
            publish("externalRunCommand is null",this, Level.ERROR);
    }
    
    protected boolean checkOutputPermissions() {
        
        if (outDir.equals(INPUT_LOCATION)) {
            if (listFlag) {
                String path;
                if (baseDir != null && !baseDir.equals(NONE))
                    path = baseDir;
                else
                    path = null;
                FileObj inputListFile = new FileObj(inputArg);
                Collection<String> inputFiles = inputListFile.readLines();
                Iterator<String> inIter = inputFiles.iterator();
                while (inIter.hasNext()) {
                    String fileName = inIter.next();
                    File file;
                    if (path == null) {
                        file = new File(fileName);
                    }
                    else {
                       File base = new File(path);
                       file = new File(base, fileName);
                    }
                    File parent = new File(file.getParent());
                    boolean writeStatus = parent.canWrite();
                    if (!writeStatus) 
                        return false;
                }
                return true;
                
            }
            else {
                return logDirParentStr != null; // i.e. successfully created log dir in outDir
//                    File topOutputDir = new File(logDirParentStr);
//                    return topOutputDir.canWrite();
            }
        }
        else {
            return logDirParentStr != null; // i.e. successfully created log dir in outDir
//                File topOutputDir = new File(logDirParentStr);
//                return topOutputDir.canWrite();
        }
    }
    
 
    
    @Override
    protected String testConfigDataTypes() {

        //======================do type checking on all variables======================//
        // this is may be something that needs to be added to Mode rather than RunMode
    	logger.debug( "Checking input parameters.");
        String errorMessage = ValidationUtils.testDataTypes( config);
	if(errorMessage.trim().length() > 0)
            return errorMessage;
        else
            return "";
    } 
    
    

    @Override
    public void execute() {
        
        if (autoMaxHeapFlag) {
            runWithAutoMaxHeap();
            return;
        }
      
        // jc 7/17/12:  set properties directly to SignatureProperties object rather than
        // creating temporary physical file.  

        SignatureProperties sigProps = new SignatureProperties();
        sigProps.setModuleName(sigDbName);
        sigProps.setJarModule(sigDbDirAndName);
        if (functionSetName != null && !functionSetName.equals(""))
            sigProps.setFunctionSetName(functionSetName);
        Integer aaCutoff;
        if (minAAFragLength > 0)
            aaCutoff = minAAFragLength;
        else {
            String minProtFragStr = config.getProperty(MIN_PROT_FRAG);
            aaCutoff = StringOps.getInteger(minProtFragStr);
        }
        if (aaCutoff == null)  
            aaCutoff = 15;
   
        sigProps.setCutoff(aaCutoff);
        sigProps.setWDWFlag(wdwWriteFlag);
        sigProps.setTranslateFlag(translateFlag);
 
//            Integer topNodeConfig;
//            String topNodeStr = (String) GlobalConfig.get(ANALYSIS_TOP_NODE_CONFIG);
//            if (topNodeStr != null)
//                topNodeConfig = StringOps.getInteger(topNodeStr);
//            else
//                topNodeConfig = null;
//            if (topNode >= 0)
//                topNodeParam = topNode;
//            else {
//                if (topNodeConfig != null && topNodeConfig.intValue() >= 0)
//                    topNodeParam = topNodeConfig.intValue();
//                else
//                    topNodeParam = 0;
//            }               

        Integer threadNum;
        if (threadNumArg < 1) {
            String threadNumStr = (String) config.getProperty(NCPUS);
            if (threadNumStr != null) {
                threadNum = StringOps.getInteger(threadNumStr);
                if (threadNum == null || threadNum < 1)
                    threadNum = 1;
            }
            else
                threadNum = 1;
        }
        else
            threadNum = threadNumArg;

//        if (threadNumArg > threadNum) // && !writeToDBStr.equals("T"))
//            threadNum = threadNumArg;

 
        sigProps.setThreadNum(threadNum);  
//        if (verbose && !guiFlag) {  
//            publish("Threadpool size = " + threadNum,this);
//        }
//        sigProps.setTopNode(topNodeParam);
        sigProps.setAction(PropertyConstants.COMBINED_REPORT);  // in future there may be other actions
                                                                // which may be determined by command line option

        String callingMsg = "Running Sequescan with the following parameters:\n";
        if (listFlag) {
            callingMsg += "list_flag=true\n";
            callingMsg += "base_dir="+baseDir+"\n";
        }
        callingMsg += "input_file = "+inputArg+"\n";
        callingMsg += "output_dir = "+ outDir + "\n";
        callingMsg += "config_file = " + configFilePath + "\n";
        callingMsg += "signature_db = " + sigDbName + "\n";
        if (functionSetName != null && !functionSetName.equals(""))
            callingMsg += "function_set = " + functionSetName + "\n";
        callingMsg += "protein_fragment_cutoff = "+ aaCutoff + "\n";
        callingMsg += "thread_num = "+threadNum + "\n";
        callingMsg += "wdwFlag = "+wdwWriteFlag + "\n";
        callingMsg += "dbWriterFlag = "+dbWriterFlag + "\n";
        callingMsg += "translateFlag = " + sigProps.getTranslateFlag();
//        if (dbWriterFlag)
//            callingMsg += "top_db_node = " + topNodeParam + "\n";
//        if (dbWriterFlag)
//            callingMsg += "include_no_function_flag = " + writeNoFuncFlag + "\n";

        if (verbose) 
            publish(callingMsg,this, Level.INFO);
       
        AnalysisRunner runner = new AnalysisRunner( sigProps,config, inputArg, outDir,
             dbWriterFlag,verbose);
        addObserversToRunner(runner);
        // jc (8/9/12): for the moment pass all three variable maps to runner until I figure out which
        // variables are needed

        runner.runAnalysis();
        runner.shutdown();  // this tells Threadpool that it should not accept any more jobs
        // and to shutdown when current job(s) - e.g. jobs started in run analysis - are done 
        while (!runner.isTerminated()) {
           try {     
                Thread.sleep(10000);
           }  catch (InterruptedException ex) {
                String msg = "Problem checking if thread pool is terminated: "+
                    ex.getMessage();
                publish(msg, this, Level.WARN);
           }               
        }

//        With timer below, it appears that runner gets terminated before it is done?
//        Timer timer = new Timer();      
//        AnalysisRunnerTimerTask timerTask  = new AnalysisRunnerTimerTask(runner,timer);
//        try {
//            timer.schedule(timerTask,10000,10000);
//        } catch (Exception ex) {
//            String msg = "Problem with AnalysisRunnerTimerTask: "+ex.getMessage();
//            publish(msg,this,Level.ERROR);
//        }
  
        String endMsg = "End Sequescan Program"; 
        publish(endMsg, this, Level.INFO);
     
        ProgramStats ps = new ProgramStats();
        logger.info( ps.calcCPUusage());
        logger.info( ps.calcMemoryUsage().toString());
        logger.info( ps.calcProgramDuration(startTime));
        
 
    }
    
    protected void addObserversToRunner(AnalysisRunner runner) {
        
        Iterator<ProgressObserver> progressIter = progressObservers.iterator();
        while (progressIter.hasNext())
            runner.addProgressObserver(progressIter.next());
        
//        Iterator<AnalysisObserver> analysisIter = analysisObservers.iterator();
//        while (analysisIter.hasNext())
//            runner.addAnalysisObserver(analysisIter.next());
        
//        Iterator<ThreadRunnerObserver> threadIter = threadRunnerObservers.iterator();
//        while (threadIter.hasNext())
//            runner.addThreadRunnerObserver(threadIter.next());
    }

    @Override
    public void usage() {   
        List<String> usage = getUsageList(config, internalFlag);
        Iterator<String> usageIter = usage.iterator();
        while (usageIter.hasNext())
            publish(usageIter.next(),this);
    }
    
    public static List<String> getUsageList(ConfigFile config, boolean internalFlag) {
        
        String runStr = "run";
        String fullRunStr;
        if (internalFlag)
            fullRunStr = "internal "+runStr;
        else
            fullRunStr = runStr;
      
        List<String> usage = new ArrayList<>();      
        usage.add("\nSequescan Build "+BUILDSTRING);
        usage.add("Command line execution of sequescan run mode:");
        usage.add("java -jar <distrib_dir>/lib/sequescan.jar "+fullRunStr+" [-h] [-q] [-c config_file] -d data_module");
        usage.add("[-o output_directory] [-s function_set] [-a min_prot_frag_length] [-t thread_num] [-f database_writer_flag] [-l] INFILE");
        usage.add("");
        usage.add("Example:");
	usage.add("java -jar /Users/jsmith/sequedex/lib/sequescan.jar "+fullRunStr+" -d Life2550-4GB.0 -s seed_0911.m1 ");
        usage.add("-f 1 /Users/jsmith/mgData");
        usage.add("");
        usage.add("Option descriptions:");
        usage.add("-a    minimum protein fragment length (overrides configuration file; default is 15)");
        usage.add("-c    user-defined configuration file (overrides system configuation file)");
        usage.add("-d    name of data module");
        usage.add("-f    database writer flag (arguments:  0 = no, 1 = yes; 2 = yes with kmers,");
        usage.add("      3 = yes, translate DNA to Protein, 4 = yes with kmers, translate DNA to Protein,");
        usage.add("      default is 0);  analysis_writer_list in config determines type of database");
        usage.add("      (currently fasta/fastq file); options 2 to 4 may be slower");
//        usage.add("      In the current implementation 2 (yes with kmers) runs slower than 1 (yes)");
//        usage.add("-g    name of log file - default name is sequescan_run_yyyyMMdd_HHmmss");
//        usage.add("      with default name using integer time");
        usage.add("-h    mode help");
        usage.add("-l    required if INFILE contains list of fasta/fastq files;  argument is required for this option");
        usage.add("      if the list file contains absolute paths, the argument should be the string \"none\"");
        usage.add("      otherwise,  it is assumed that the entries in the list file are relative to this argument -");
        usage.add("      i.e. the argument should be the base directory path which is appended to the front of each entry"); 
        usage.add("      when paths are relative to a base directory and the -o option is set, output will include relative paths");
//        usage.add("");
        usage.add("-m    manual max heap flag - if present sequescan is run directly and thus");
        usage.add("      with specified max heap;  default is to execute another call to sequescan");
        usage.add("      using max heap assigned to data module");
        usage.add("-o    user-defined directory for data output (default is directory where input is located)");
        usage.add("-q    quiet option - if present, suppresses non-essential messages to console or progress window");
        usage.add("-s    name of function set");       
        usage.add("-t    maximum number of threads in threadpool (default = 1)");
        usage.add("-w    whodoeswhat flag (arguments:  0 = no, 1 = yes;  default is 0");
 
//        usage.add("-n    write sequences with no function assigned (no argument); default is not to write these sequences");
//                 "top phylogeny node for database output (0 = all nodes); not working yet");
        usage.add("");
        usage.add("INFILE may be a fasta or fastq file, a directory with fasta/fastq files,");
        usage.add("or a file containing a list of fasta/fastq files.  However, only fasta/fastq");
        usage.add("files or their gzipped (.gz) versions with a valid extension will be processed.");
        usage.add("");
        
        ConfigFile helpConfig = config;
        if (helpConfig == null)
            helpConfig = new ConfigFile();
        List<String> validExtList = helpConfig.getStringList(FASTA_EXT_LIST,ConfigFile.CONFIG_LIST_DELIM);
        usage.add("Current valid extensions are:");
        String validExtMsg = "";
        if (validExtList != null) {
            Iterator<String> iter = validExtList.iterator();
            boolean startFlag = true;
            while(iter.hasNext()) {
                String ext = iter.next();  
                if (startFlag) {
                    startFlag = false;
                }
                else
                    validExtMsg += " ";
                validExtMsg += ext;
            }
            validExtMsg += " and their gzipped (.gz) versions";
        }
        usage.add(validExtMsg);
        usage.add("Content of input files with extension .fna, .ffn, .fq, or .fastq");
        usage.add("will be treated as DNA sequences.");
        usage.add("Content of input files with extension .faa will be treated as protein sequences. ");
        usage.add("All other input files with a valid extension will be tested to determne if DNA ot ptotein.");
        return usage;
        
    }

    @Override
    protected Getopt generateGetoptInstance(String[] optionsArr) {
        Getopt go = new Getopt("sequescan",optionsArr, "hqmnl:c:d:g:o:s:t:f:a:w:");
	go.setOpterr(true);
        return go;
    }
    
    @Override
    protected boolean setVariablesFromOptions(Getopt go) {
        
        if( modeOptionArr.length < 3){
            if (modeOptionArr.length == 1 && modeOptionArr[0].equals("-h")) {
                usage();
                return false;
            }
            else {
                publish("Insufficient number of command line arguments: " + (modeOptionArr.length +1),this);
                usage();
                return false;
            }
        }
        
        int argCnt = 0;
        int ch;
 
        while ((ch = go.getopt()) != -1) {
            //    System.out.println("ch = "+ch);
            switch ((char)ch) {
                case 'h':
                    usage();
                    return true;
                case 'g':
                    logFileName = go.getOptarg();
                    argCnt += 2;
                    break;
                case 'm':
                    autoMaxHeapFlag = false;
                    argCnt++;
                    break;
                case 'n':
                    nonInteractiveFlag = true;
                    argCnt++;
                    break;
                case 'q':                 
                    verbose = false;
                    argCnt++;
                    break;
                case 'c':
                    configFilePath = go.getOptarg();
                    argCnt += 2;
                    break;
                case 'd':
                    sigDbName = go.getOptarg(); //evh.expandEnvVar( args[i]);
                    argCnt += 2;
                    break;
                case 's':
                    functionSetName = go.getOptarg();
                    argCnt += 2;
                    break;
                case 'o':
                    outDir = go.getOptarg();
                    argCnt += 2;
                    break;
                case 'l':
                    setListFlag(true);
                    baseDir = go.getOptarg();
                    argCnt += 2;
                    break;
                case 't':
                    String numStr = go.getOptarg();
                    Integer numObj = StringOps.getInteger(numStr);
                    if (numObj != null) {
                        threadNumArg = numObj;
                    }   argCnt += 2;
                    break;
                case 'f':
                    String dbWriterStr = go.getOptarg();
                    switch (dbWriterStr) {
                        case "0":
                            dbWriterFlag = 0;
                            translateFlag = false;
                            break;
                        case "1":
                            dbWriterFlag = 1;
                            translateFlag = false;
                            break;
                        case "2":
                            dbWriterFlag = 2;
                            translateFlag = false;
                            break;
                        case "3":
                            dbWriterFlag = 1;
                            translateFlag = true;
                            break;
                        case "4":
                            dbWriterFlag = 2;
                            translateFlag = true;
                            break;
                        default:
                            dbWriterFlag = 0;
                            break;
                    }
                    argCnt += 2;
                    break;
                case 'w':
                    String wdwWriterStr = go.getOptarg();
                    switch (wdwWriterStr) {
                        case "0":
                            wdwWriteFlag = 0;
                            break;
                        case "1":
                            wdwWriteFlag = 1;
                            break;
                        default:
                            wdwWriteFlag = 0;
                            break;
                    }
                    argCnt += 2;
                    break;
                case 'a':
                    String minAAFragLenStr = go.getOptarg();
                    Integer minAAFragLenInt = StringOps.getInteger(minAAFragLenStr);
                    if (minAAFragLenInt == null)
                        minAAFragLength = -1;
                    else
                        minAAFragLength = minAAFragLenInt;
                    argCnt += 2;
                    break;
                default:
        //            publish( "\n*********Unknown option: '" + ch + "'",this);
                    usage();
                    return false;                     // undefined option
            }

        }
        
        if (argCnt != commandLineArr.length - 2) {
            usage();
            return false;
        }
        
        if (outDir == null)
            outDir = INPUT_LOCATION;
        
        
        // jc (7/19/12):  inputArg (formerly inFile) is set in parent init() method before
        // this method is called.

        //required input file or database file was not specified
        if( (inputArg == null) || (sigDbName == null)){
            publish("input path and/or signature database path and name must be specified:",this);
            publish("\tinput path: " + inputArg,this);
            publish("\tdata module: " + sigDbName,this);
            usage();
            return false;	
        }
        
        // get full path for Jar File
        sigDbDirAndName = getJarFilePath(this,sigDbName);
        
        return true;
    }

    @Override
    protected String getLogDirParentStr() {
        
        String baseDirValue;
        
        if (baseDir == null || baseDir.equals(NONE))
            baseDirValue = null;
        else 
            baseDirValue = baseDir;
        
        
        if (outDir == null || outDir.equals(INPUT_LOCATION)) {
        
            File inputFile = new File(inputArg);
            File canonicalInputFile;
            String canonicalInputPath;

            try {
                canonicalInputFile = inputFile.getCanonicalFile();
                canonicalInputPath = inputFile.getCanonicalPath();
            } catch (IOException ex) {
                String msg = "Problem getting canoncial path for input argument: "+ex.getMessage();
                publish(msg,this);
                return null;
            }

            if (listFlag) {  // input is list file
                if (baseDirValue == null)
                    return canonicalInputFile.getParent();
                else
                    return baseDirValue;
            }
            else if (inputFile.isFile()) 
                return canonicalInputFile.getParent();
            else  // input is directory
                return canonicalInputPath;
        }
        else 
            return outDir;
        
    }
    
    @Override
    protected boolean processConfigValues() {
        
        Map<String,String> replaceMap = genConfigReplaceMap();       
        Set<String> configKeys = config.getKeys();
        Iterator<String> iter = configKeys.iterator();
        boolean replaceFlag = false;
        while (iter.hasNext()) {
            String key = iter.next();
            String val = config.getProperty(key);
            Set<String> replaceKeys = replaceMap.keySet();
            Iterator<String> replaceIter = replaceKeys.iterator();
            while (replaceIter.hasNext()) {
                String replaceKey = replaceIter.next();
                String replaceVal = replaceMap.get(replaceKey);
                if (val.contains(replaceKey)) {
                    if (replaceVal == null)
                        replaceVal="none";
                    val = val.replace(replaceKey, replaceVal);  
                    replaceFlag = true;
                }
            }
            if (replaceFlag)
                config.setProperty(key, val);
        }
        return true;
    }
    
    @Override
    protected Map<String,String> genConfigReplaceMap() {
        
        Map<String,String> replaceMap = new HashMap<>();
        replaceMap.put(VAR_SUBSTITUTION_CHAR + DBNAME + VAR_SUBSTITUTION_CHAR,
            sigDbName);
        replaceMap.put(VAR_SUBSTITUTION_CHAR + FUNCTIONSET + VAR_SUBSTITUTION_CHAR,
            functionSetName);
        return replaceMap;
    }

    protected boolean addMoreConfig() {
        
        // rewrite of M. Dimitrijevic's getAdditionalVars() method to write
        // directly ConfigFile rather than HashMap (which was required
        // by the since deleted GlobalConfig
        
        if (config == null)
            return false;
        
        config.setProperty(LIST_FLAG, Boolean.toString(listFlag));
        if (listFlag)
            config.setProperty(BASE_DIR,baseDir);
        
        // remove everything but file name
        if( sigDbName.contains( SEP))
            sigDbName = sigDbName.substring( sigDbName.lastIndexOf( SEP)+1, sigDbName.length());
        //trim off the .jar extension from the sigDbName
        if( sigDbName.endsWith(".jar"))
            sigDbName = sigDbName.substring( 0, sigDbName.length() - 4);
        config.setProperty(DBNAME,sigDbName);   
        
        config.setProperty(INSUBDIR,"");   // jc:  What is this? need to track down
        config.setProperty(INFILE, getInfileName(inputArg));
        if (functionSetName == null) {
            config.setProperty(FUNCTIONSETLIST,NONE);  
                // at present FUNCTIONSETLIST is always same as FUNCTIONSET
                // since only one functionset allowed at a time ?
            config.setProperty(FUNCTIONSET,NONE);           
        }
        else {
            config.setProperty(FUNCTIONSETLIST,functionSetName);
            config.setProperty(FUNCTIONSET,functionSetName);
        }
        
        return true;
        
    }

    @Override
    protected boolean checkMemoryRequirements() {
             
        if (nonInteractiveFlag)  /* running from in a separate java process */
            return true;
        else
                  publish("Checking memory requirements");
        long mb = 1024*1024;
        int moduleMaxHeapMb = getJarModuleMaxHeapMb(sigDbName);
        long requiredMaxHeap = moduleMaxHeapMb*mb;
        long[] HWMemory = SystemUtilities.getMemory();
        long totalMem = HWMemory[SystemUtilities.TOTAL];
        long availableMem = HWMemory[SystemUtilities.AVAILABLE];
        ArrayList<String> lines = new ArrayList<>();
        if (autoMaxHeapFlag) {
            if (requiredMaxHeap > availableMem) {
                lines.add("Available RAM: "+availableMem);
                lines.add("Total RAM: "+totalMem);
                lines.add("Required Max Heap for data module "+sigDbName+": "+ requiredMaxHeap);
                lines.add("Running sequescan with Max Heap > Available and/or Total RAM ");
                lines.add("may use Virtual Memory, which is very slow.");
                lines.add("Continue?");
            }
            else 
                return true;
        }
        else {
            Runtime rt = Runtime.getRuntime();
            long maxHeap = rt.maxMemory();
            if (requiredMaxHeap > maxHeap) {
                lines.add("Current Max Heap: "+maxHeap);
                lines.add("Available RAM: "+availableMem);
                lines.add("Total RAM: "+totalMem);
                lines.add("Required Max Heap for data module "+sigDbName+": "+ requiredMaxHeap);
                lines.add("Running sequescan with Max Heap > Available and/or Total RAM ");
                lines.add("may use Virtual Memory, which is very slow.");
                lines.add("Continue?");
            }
            else
                return true;
        }
        return displayContinueDialog(lines);
                
    }  
    
}
