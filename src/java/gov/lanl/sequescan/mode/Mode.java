/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.mode;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.UserConstants;
import gov.lanl.sequtils.event.AnalysisObserver;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.log.LogManager;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.ValidationUtils;
import java.io.File;
import java.util.*;
import gnu.getopt.Getopt;
import gov.lanl.sequescan.constants.AppConstants;
import gov.lanl.sequescan.constants.ModeConstants;
import gov.lanl.sequescan.gui.util.QueryDialog;
// import gov.lanl.sequtils.event.QuitListener;
import gov.lanl.sequescan.signature.JarModule;
import gov.lanl.sequtils.constants.BuildConstants;
import gov.lanl.sequtils.event.ProgressEvent;
// import gov.lanl.sequtils.event.QuitEvent;
import static gov.lanl.sequtils.log.MessageManager.publish;
import gov.lanl.sequtils.util.ConfigFile;
import gov.lanl.sequtils.util.ExternalProcessRunner;
import gov.lanl.sequtils.util.StringOps;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Base class for execution of various sequescan functionality.  
 * This allows access to all sequescan functionality from one top-level class
 * (gov.lanl.sequescan.Sequescan).  Much of the code was split off from
 * the old version of gov.lanl.sequescan.Sequence and reorganized as necessary between
 * Mode and RunMode classes.
 * 
 * @author Judith Cohn <jcohn@lanl.gov>
 */
public abstract class Mode extends MessageManager implements Runnable, 
    AppConstants, ModeConstants, BuildConstants { //, QuitListener {
    
    // constants
    public static int MISSING_MODULE = -1;
    public static int INVALID_MAX_HEAP = -100;
   
    // instance variables
    protected Boolean verbose = true;
    protected String[] modeOptionArr = null;
    protected String[] commandLineArr = null;
    protected String modeStr = null; 
    protected String configFilePath = null;  // the sequescan configuration file
    protected ConfigFile config = null;
    protected String inputArg = null;  // the last string in the command line argument array
    protected String externalRunCommand = null;
    protected long startTime;   
    protected List<ProgressObserver> progressObservers;
    protected List<AnalysisObserver> analysisObservers;
    protected String logDirParentStr = null;
    protected boolean autoMaxHeapFlag = true;
    protected boolean nonInteractiveFlag = false;
    protected String logFileName = null;
    protected ExecutorService executor = null;
    protected boolean internalFlag;
   
    public Mode(String[] commandLineArgs, boolean isInternal) {
        
        commandLineArr = commandLineArgs;
        internalFlag = isInternal;
        
        if (commandLineArgs == null || commandLineArgs.length < 1) {
            modeOptionArr = null;
            modeStr = null;
        }
        else {
            modeStr = commandLineArgs[0];
            if (commandLineArgs.length == 1)
                modeOptionArr = null;
            else {
                // remove mode from command line argument array
                // so that rest of arguments can be parsed by GetArgs as before
                modeOptionArr = new String[commandLineArgs.length-1];
                for (int i=0; i<modeOptionArr.length; i++) {
                    modeOptionArr[i] = commandLineArgs[i+1];
   //                 System.out.println("modeOptionArr["+i+"]: "+modeOptionArr[i]);
                }
            }
        }
//        threadRunnerObservers = new ArrayList<ThreadRunnerObserver>();
        progressObservers = new ArrayList<>();
        analysisObservers = new ArrayList<>();
     //   runProcesses = new ArrayList<>();
 
    }
    
    public boolean getInternalFlag() {
        return internalFlag;
    }
    
//    public void addRunProcess(Process p) {
//        runProcesses.add(p);
//    }

    public void addProgressObserver(ProgressObserver observer) {
        progressObservers.add(observer);
    }
    
    public void addAnalysisObserver(AnalysisObserver observer) {
        analysisObservers.add(observer);
    }
    
    public String getModeStr() {
        return modeStr;
    }
    
    public String getLogFileName() {
        return logFileName;
    }
    
    /**
    * Method that initializes the system for program execution.
    * Initialization includes:
    * 1. check environmental vars were set by user (e.g. SEQUEDEX_DATA).
    * 2. check user-specified input files are readable and user-specified input directory exists and is writable.
    * 3. build  HashMap from the configuration file plus additional variables
    * 4. do type checking on all collected variables.
    * 5. any mode-specific initialization will be included in an overwritten init() method which
    *    will begin by calling super.init()
    * 6. set up logging
    * @return true if successful init
    */
    public boolean init() {
        
        // set Locale to language = English and country = US
        // this avoid problems with numbers reversing , and . etc.
        Locale locale = new Locale("en","US");      
        Locale.setDefault(locale);
        
  
 	
        //======================VARIABLES======================//

        //1. check that user specified necessary environmental variables
        
        String sequedexHomeDir = ConfigFile.getDistribDir();
        if(sequedexHomeDir == null)
            {
            String errorMessage = 
                "\n\nFATAL ERROR: could not get distribution directory for Sequedex";
            publish( errorMessage,this);
            return false;
        }
      
        //2. check validity of command line arguments
        if (modeStr == null) {
            publish("Null command line arguments",this,null);
            return false;
        }
        else if (modeOptionArr == null || modeOptionArr.length < 1) {
            usage();
            return false;
        }
        
        inputArg =  modeOptionArr[modeOptionArr.length-1];
        Getopt go = generateGetoptInstance(modeOptionArr);
        if (go == null || inputArg == null) {
            usage();
            return false;
        }     
        else {
            boolean okay = setVariablesFromOptions(go);
            if (!okay)
                return false;      
//            else if (verbose)
//                System.out.println(runCommand);
        }
                     
        //======================set the default configuration file======================// 
        if( configFilePath == null){
            configFilePath = ConfigFile.getDefaultConfigFilePath();	
   
            if (configFilePath == null) {
                publish("No path for config file",this,Level.ERROR);
                return false;     
            }
        }
        
        String errorMessage = checkInputFiles();       
        if( errorMessage.length() > 0){
                publish( "Fatal error when checking input files: " + errorMessage,this, Level.ERROR);
                return false;
        }
        
        //=====build configVarsHM HashMap from the configuration file======        
        
        if (verbose && !nonInteractiveFlag) {
            String msg = "Running with config file "+configFilePath;
            publish(msg, this, Level.INFO);
        }
      
        config = ConfigFile.getConfigFromFile(configFilePath);
        
        // create local version of config vars
        // Need to write body for the abstract methods referred to in the next 2 lines
  
        
        boolean badConfigVersion; // = false;
        String badMsg;// = null;
        String configVersionStr = config.getProperty(CONFIG_FILE_VERSION);
        if (configVersionStr == null) {
            badMsg = "Missing version in config file;  ending run";
            badConfigVersion = true;
        }
        else {
            Double configVersionNum = StringOps.getDouble(configVersionStr);
            Double minConfigVersionNum = StringOps.getDouble(MIN_CONFIG_FILE_VERSION_NUMBER);
            if (minConfigVersionNum == null) {
                badMsg = "Missing MIN_CONFIG_FILE_VERSION_NUMBER;  ending run";
                badConfigVersion = true;
            }
            else if (configVersionNum == null) {
                badMsg = "Version in config file is not a number: "+configVersionStr;
                badConfigVersion = true;
            }
            else {
//                System.out.println("configVersion = "+configVersionNum+"  minConfigVersionNum = "+minConfigVersionNum);
                if (configVersionNum >= minConfigVersionNum) {
                    badMsg = null;
                    badConfigVersion = false;
                }
                else {
                    badMsg = "Config file version is "+configVersionStr+
                        ", minimum version required for running sequescan is "+minConfigVersionNum+": ending run";
                    badConfigVersion = true;
                }
            }
              
        }
        if (badConfigVersion) {
//            if (verbose && !guiFlag)
//                System.out.println(badMsg);
//            logger.error(badMsg);
//            if (guiFlag) {
//                ThreadRunnerEvent ev = new ThreadRunnerEvent(this,badMsg,1);
//                notifyThreadRunnerObservers(ev);
//            }
            publish(badMsg,this);
            return false;
        }
        
   
        // check input parameters (from config file?)
        errorMessage = testConfigDataTypes();
        if( errorMessage.length() > 0){
            String errMsg = "Fatal error when checking input variables: " + errorMessage;
            publish(errMsg,this);
            return false;
        } 
            
        // create sequedex user directory 
        File sequedexUserDir = UserConstants.getUserDir();
        if (sequedexUserDir == null) {
            String errMsg = "Fatal error - null sequedexUserDir";
            publish(errMsg, this);
            return false;
        }
        
        String logLevelStr = config.getLogLevel();
        boolean logOkay = initLog(logLevelStr);
        if (!logOkay) {
            publish("Problem initializing logger", this, null);
            return false;
        }
        else return true;
        
  
    }
       
    
    /**
    * Method to define environmental variables based on the values specified in the SequescanContants.java file
    * user variables based on the command line and runtime settings:
    * Some variables are set in Mode version of this method, while variables
    * specific to a particular mode are set in the appropriate overwritten
    * method for a child class (with the first line being a call to super.setUserVars().
    * This allows that strings enclosed in matching pairs of percent signs will be passed for environmental 
    * variable expansion. 
    * @return HashMap<String,Object>
    */
 

    protected String checkInputFiles() {
        
        String errorMessage = ValidationUtils.checkFile( configFilePath);
      
        if (!modeStr.equals(MODULE_INFO)) {
            File inputArgFile = new File(inputArg);
            if (!inputArgFile.exists())
                errorMessage += inputArg+" does not exist\n";
        }
  
        return errorMessage;
    }
    
    public boolean initLog(String logLevelStr) {
            		
        String logDirName = LOG_DIR;   //config.getProperty(LOG_DIR);

        if (logLevelStr == null)
            logLevelStr = DEFAULT_LOG_LEVEL;
        else if (logLevelStr.startsWith("WARN"))
            logLevelStr = "WARN";
        
        logDirParentStr = getLogDirParentStr();
        if (logDirParentStr == null) {
            publish("logDirParentStr is null",this);
            return false;
        }
        else
            config.setProperty(LOG_DIR_PARENT,logDirParentStr);
        
        File logDirParent = new File(logDirParentStr);
        String errorMsg = null;
        File logDir = new File(logDirParent,logDirName);
        boolean logDirFlag;
        if (!logDir.exists()) {
            logDirFlag = logDir.mkdirs();
            if (!logDirFlag) {
                errorMsg = "Problem making logDir: "+logDir.getAbsolutePath();  
                logDirParentStr = null;
                config.setProperty(LOG_DIR_PARENT, null);
            }             
        }
        if (errorMsg != null) {
            publish(errorMsg,this);
            return false;
        }      
        String location = logDir.getAbsolutePath();
        
        if (logFileName == null)
            logFileName = LogManager.getDefaultLogFileName(location,modeStr);
        if (logFileName == null)
            return false;
//        else {  // note:  at the moment log file is html - probably need to be a bit
                // more exacting here and check if it is still .html before adding extension ???
//            if (!logFileName.endsWith(".html"))
//                logFileName = logFileName + ".html";
//               
//        }
  
        LogManager.initModeLogger(logLevelStr,logFileName);
        if (verbose && !nonInteractiveFlag) {
            String msg = "Writing log files to: " + logFileName + " with log level " + logLevelStr;
            publish(msg,this);
        } 
        config.setProperty(LOG_FILE_NAME, logFileName);
        startTime = System.currentTimeMillis(); //for calculating program duration
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        logger.info("Java version is "+javaVersion);
        logger.info("Operating System: "+osName+" "+osArch+" "+osVersion);
        String beginMsg = "Start Sequescan Program (mode="+modeStr+")";
        logger.info(beginMsg);
        if (verbose)
            publish(beginMsg,this);
        return true;
    }
    
//    protected void notifyThreadRunnerObservers(ThreadRunnerEvent event) {
//    
//        Iterator<ThreadRunnerObserver> observerIter = threadRunnerObservers.iterator();
//        while (observerIter.hasNext())
//            observerIter.next().observeThreadRunnerEvent(event);
//    }
    
    @Override
    public void run() {
        execute();
    }
    
    public static List<String> getDataModuleList(Object src, boolean publishFlag) {
     
        File dataDir = getSequedexDataDir(src, publishFlag);
        if (dataDir == null)
            return new ArrayList<>();
        
        String[] files = dataDir.list();
        ArrayList<String> jarFiles = new ArrayList<>();
        for (String file : files) {
            if (file.endsWith(".jar")) {
                jarFiles.add(file.substring(0, file.length() - 4));
            }
        }
        return jarFiles;
    }
    
    public static String getJarFilePath(Object src, String moduleName) {
        
        boolean publishFlag;
        publishFlag = !guiFlag;
        File dataDir = getSequedexDataDir(src,publishFlag);
        File moduleFile;
        if (dataDir != null) {
            moduleFile = new File(dataDir, moduleName+".jar");
            return moduleFile.getAbsolutePath();
        }
        else 
            return null;
    }
    
    public static File getSequedexDataDir(Object src, boolean publishFlag) {
        // for the moment sequedexData is hard-coded to default
        // in future, will use -D option to set sequedexData
        String sequedexData = ConfigFile.getDefaultDataModuleDir();
        if (sequedexData == null || sequedexData.isEmpty()) {
            String msg = "Using default data module directory";
            if (publishFlag)
                publish(msg, src);
  //          sequedexData = GlobalConfig.getDefaultDataModuleDir();
        }
        
        File dataDir = new File(sequedexData);
        if (!dataDir.exists()) {
            String msg = sequedexData + " does not exist";
            if (publishFlag)
                publish(msg, src);
            dataDir = null;
        }
        
        return dataDir;
        
    }
    
    /* for future use - currently RunMode only */
    protected Map<String,String> genConfigReplaceMap() {
        return null;
    }
    
    /* for future use - currently RunMode only */
    protected boolean processConfigValues() {
        return true;
    }
    
    protected int getJarModuleMaxHeapMb(String moduleName) {
        String jarFilePath = getJarFilePath(this, moduleName);
        JarModule jarModule = new JarModule(jarFilePath);
        boolean status = jarModule.openJarFile();
        if (!status)
            return MISSING_MODULE;
        else {
            Integer maxHeapMb;
            try {
                String maxHeapStr = jarModule.getMaxHeap();
                // this is kludge to make Virus module work without fixing the module itself
                // since for some reason m was put at end of numeric (before
                // this number was used to automatically set max heap)
                if (maxHeapStr.endsWith("m")) {
                    maxHeapStr = maxHeapStr.substring(0,maxHeapStr.length()-1);
                }                   
                maxHeapMb = Integer.parseInt(maxHeapStr);
            } catch (NumberFormatException ex) {
                logger.error("Data Module "+moduleName+" does not have valid max heap");
                return INVALID_MAX_HEAP;
            }
            return maxHeapMb;
        }
    }
    
    public boolean getAutoMaxHeapFlag() {
        return autoMaxHeapFlag;
    }
    
    protected void setAutoMaxHeapFlag(boolean flag) {
        autoMaxHeapFlag = flag;
    }
    
    public boolean getNonInteractiveFlag() {
        return nonInteractiveFlag;
    }
    
    protected void setNonInteractiveFlag(boolean flag) {
        autoMaxHeapFlag = flag;
    }
    
    protected boolean displayContinueDialog(ArrayList<String> msgLines) {
        String msg = null;
        boolean continueFlag;
        if (msgLines == null || msgLines.isEmpty()) {
            msg = "Empty continue dialog message;  execution stopped";
            continueFlag = false;
        }
        else if (Mode.getGuiFlag()) {
            QueryDialog dialog = new QueryDialog(
                Mode.getParentFrame(),true,msgLines);
            dialog.setTitle("Potential Problem with Max Heap");
            dialog.setVisible(true);
            continueFlag = dialog.getAnswer();
            if (!continueFlag)
                msg = "Max Heap is too small;  user requested program exit";
        }
        else {           
            msgLines.forEach((msgLine) -> {
                System.out.println(msgLine);
            });
            System.out.println("Enter y to continue, n to stop: ");
            String response = System.console().readLine();
            if (response.toLowerCase().equals("y")) {
                continueFlag = true;
                msg = null;
            }
            else {               
                continueFlag = false;
                msg = "Max Heap is too small;  user requested program exit";
            }
        }
        if (msg != null)
            publish(msg,this, Level.INFO);
        return continueFlag;
    }
   
    protected void runExternalCommand(String cmd) {
        // should I check if executor is not null?
        if (executorIsLive()) {
            publish("Executor is still running - cannot execute command: cmd", this, Level.ERROR);
            return;
        }
        executor = Executors.newSingleThreadExecutor();
        Callable<Integer> runner = new ExternalProcessRunner(cmd, logFileName); //, this);          
        Future<Integer> future = executor.submit(runner);
        executor.shutdown();
        Integer returnStatus = -100;
        try {
            returnStatus = future.get();
        } catch (InterruptedException | ExecutionException ex) {
            publish("Problem getting return status", this, Level.ERROR);
            publish(ex.getMessage(),this,Level.ERROR);
        }
        publish("AutoMaxHeap run exited with status: "+returnStatus+"\n",this,Level.INFO);
        notifyProgressObservers(ProgressEvent.END);
        
    }
    
    public boolean executorIsLive() {
        if (executor == null)
            return false;
        else {
            return !executor.isTerminated();
        }
    }
    
    public void shutdownExecutor() {
        // this is not working currently
        if (!executorIsLive()) {
            publish("Executor is not running - nothing to shutdown",this, Level.INFO);
            return;
        }
//        try {
//            publish("Attempting to shutdown Mode executor",this,Level.INFO);
//            executor.shutdown();
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) {
//            publish("InterruptedException: "+e.getMessage(),this,Level.ERROR);
//        }
//        finally {
//        if (!executor.isTerminated()) {
//            publish("external sequescan process not terminated yet", this, Level.INFO);
//        }
//        executor.shutdownNow();
//        publish("Shutdown finished",this,Level.INFO);
//        }

        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Executor did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
 
    }
    
    protected void notifyProgressObservers(int action) {
        ProgressEvent event = new ProgressEvent(action);
        Iterator<ProgressObserver> observerIter = progressObservers.iterator();
        while (observerIter.hasNext()) {
            observerIter.next().observeProgressEvent(event);
        }
    }
    

    // abstract method
    abstract public void execute();
    abstract public void usage();
    abstract protected Getopt generateGetoptInstance(String[] optionsArr); 
    abstract protected boolean setVariablesFromOptions(Getopt go);
    abstract protected String testConfigDataTypes();
    abstract protected String getLogDirParentStr();
    abstract protected boolean checkMemoryRequirements();
    abstract public void runWithAutoMaxHeap();
   
}
