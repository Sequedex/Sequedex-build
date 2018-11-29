/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.analysis;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.constants.BuildConstants;
import gov.lanl.sequescan.constants.PropertyConstants;
import gov.lanl.sequescan.signature.ModuleFactory;
import gov.lanl.sequescan.signature.SignatureMap;
import gov.lanl.sequescan.signature.SignatureModule;
import gov.lanl.sequescan.signature.SignatureProperties;
import gov.lanl.sequescan.tree.TreeManager;
import gov.lanl.sequtils.event.AnalysisObserver;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.sequence.SequencingReader;
import gov.lanl.sequtils.util.FileObj;
import gov.lanl.sequtils.util.StringOps;
import gov.lanl.sequtils.writer.AnalysisFileWriter;
import gov.lanl.sequtils.writer.ProgressFileWriter;
import gov.lanl.sequtils.writer.ProgressWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import gov.lanl.sequescan.constants.AppConstants;
import gov.lanl.sequescan.constants.ReportConstants;
import gov.lanl.sequtils.event.ProgressEvent;
import gov.lanl.sequtils.util.ConfigFile;




/**
 *
 * @author jcohn
 */
public class AnalysisRunner /*extends SwingWorker<List<String>,String>*/
    extends MessageManager implements AppConstants, BuildConstants, ReportConstants { 
 
    // instance variables    
    protected ThreadPoolExecutor threadPool = null;
    final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    protected SignatureProperties sigProps;
    protected File inputFile;
    protected String outputDirStr;
    protected Map<Short,String[]> functionDetail;
    protected Map<Short,String[]> nodeDetail;
    protected String[][] nodeDetailColumns;
    protected String[][] functionDetailColumns;  
    protected boolean functionFlag;
    protected List<ProgressObserver> progressObservers;
    protected DateFormat timeFormatter;
    protected int writeToDB;
    protected boolean verbose;
    protected ConfigFile config;
    
    
   
    public AnalysisRunner (SignatureProperties sprops,ConfigFile cfile, String inputArg, 
        String outputArg, int writeDBFlag, boolean verboseFlag) { 
        sigProps = sprops;
        inputFile = new File(inputArg);
        outputDirStr = outputArg;
        if (outputDirStr == null)
            outputDirStr = INPUT_LOCATION;
        writeToDB = writeDBFlag;
        verbose = verboseFlag;
        config = cfile;
        init();
    }
    
    
    public final void init() {
        createThreadPool();
        initObserverLists();  
        Locale currentLocale = Locale.getDefault();
        timeFormatter = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, 
            DateFormat.MEDIUM,
            currentLocale);
    }
    
    
    protected void initObserverLists() {
        progressObservers = new ArrayList<>();
 //       analysisObservers = new ArrayList<AnalysisObserver>();
 //       threadRunnerObservers = new ArrayList<ThreadRunnerObserver>();
    }
    
     
    
    public void addProgressObserver(ProgressObserver observer) { 
        progressObservers.add(observer);
    }
    
//    public void addThreadRunnerObserver(ThreadRunnerObserver observer) {
//        threadRunnerObservers.add(observer);
//    }
    
    protected void createThreadPool() {
        int threadNum = sigProps.getThreadNum();
        logger.info("Creating ThreadPool with "+threadNum+" analysis threads");
        threadPool = new ThreadPoolExecutor(threadNum,threadNum,10,
            TimeUnit.SECONDS,queue);
    }
    
  
    public void shutdown() {
        if (threadPool != null)
            threadPool.shutdown();
    }
    
    public boolean isTerminated() {
        return threadPool.isTerminated();        
    }
    
//    public void addAnalysisObserver(AnalysisObserver observer) {
//        analysisObservers.add(observer);
//    }
     
    public void runAnalysis() {
    	logger.debug("\t==========Running Analysis==========\n");
        String action = sigProps.getAction();
        if (action == null) {
            logger.error("Missing ACTION parameter");
        }
        else {
            long heapMaxSize = Runtime.getRuntime().maxMemory();
            logger.info("Running "+action+
                " with maximum heap="+heapMaxSize+", inputFileOrDir="+
                inputFile.getAbsolutePath()+" outputDir="+outputDirStr);
            if (action.equals(PropertyConstants.COMBINED_REPORT)) 
                runCombinedReports(); 
            else 
                logger.error("Unknown action: "+action);
            
        }
    }
    
    protected void notifyProgressObservers(int action) {
        ProgressEvent event = new ProgressEvent(action);
        Iterator<ProgressObserver> observerIter = progressObservers.iterator();
        while (observerIter.hasNext()) {
            observerIter.next().observeProgressEvent(event);
        }
    }
    
      
    protected void runCombinedReports() {
      
        List<String> completedList = new ArrayList<>();
        
   // 	logger.debug("\t==========Combined Reports==========\n");
        if (!inputFile.exists()) {
            String msg = inputFile.getAbsolutePath()+" does not exist";
            publish(msg,this,Level.ERROR);
            return;
        }
        
        String listFlagStr = config.getProperty(LIST_FLAG);
        String baseDir = config.getProperty(BASE_DIR);
        if (baseDir != null && baseDir.equals(NONE))
            baseDir = null;
   
        boolean listFlag;
        listFlag = listFlagStr.trim().equals("true");
        
        // get list of input files and check if valid extension
        List<File> inputFileList;
        if (listFlag) {
            FileObj inputFileObj = new FileObj(inputFile);
            Collection<String> fileNameList = inputFileObj.readLines();
            if (fileNameList == null || fileNameList.isEmpty())
                inputFileList = new ArrayList<>(0);
            else {
                inputFileList = new ArrayList<>(fileNameList.size());
                Iterator<String> nameIter = fileNameList.iterator();
                while (nameIter.hasNext()) {
                    String fileName = nameIter.next();
                    File file;
                    if (baseDir == null)
                        file = new File(fileName);  // i.e. already absolute file name
                    else {
                        String absFileName = baseDir + SEP + fileName;
                        file = new File(absFileName);
                    }
                    inputFileList.add(file);
                }
            }          
 //           multiInputFlag = true;
        }
        else if (inputFile.isDirectory()) {  
            File[] fileArr = inputFile.listFiles();
            inputFileList = new ArrayList<>(fileArr.length);
            inputFileList.addAll(Arrays.asList(fileArr));
  //          multiInputFlag = true;
        }
        else {
            // inputFile is a single file
            inputFileList = new ArrayList<>(1);
            inputFileList.add(inputFile);
 //           multiInputFlag = false;
        }
            
        List<File> processFileList = getProcessFileList(inputFileList,inputFile.isDirectory());
        
        // note:  getProcessFileList will write logger error if no valid files
        if (processFileList == null || processFileList.size() < 1) {
            return;
        }

        
        Date sigBegin = new Date();
        SignatureModule module = ModuleFactory.getSignatureModule(sigProps);
        String moduleVersion = module.getVersion();
        String minVersion = MIN_DATA_VERSION_NUMBER;
        boolean dataVersionProblem;// = false;
        String dataVersionMsg;// = null;
        if (moduleVersion == null) {
            dataVersionMsg = "Data module version is not supported ("+moduleVersion+");  version "+
                minVersion + " or greater is requred";
            dataVersionProblem = true;
        }
        else {
            Double moduleVersionNum = StringOps.getDouble(moduleVersion);
            Double minVersionNum = StringOps.getDouble(minVersion);
            if (moduleVersion == null || minVersion == null) {
                dataVersionMsg = "Minimum Data Version or Selected Module Data Version is not a number";
                dataVersionProblem = true;
            }
            else {
                if (moduleVersionNum >= minVersionNum) {
                    dataVersionMsg = null;
                    dataVersionProblem = false;
                }
                else {
                    dataVersionMsg = "Data module version is unsupported ("+moduleVersion+")";
                    dataVersionProblem = true;
                }
            
            }
            
        }
        if (dataVersionProblem) {
            publish(dataVersionMsg,this, Level.ERROR);
            return;
//            logger.error("Data module version number is "+moduleVersion+
//            ", which is not the current version used by program; some features may not be supported");           
        }
        else {
            publish("Reading signature map from data module",this);
        }

        
        SignatureMap sigMap = CombinedReport.readMapFromModule(module);
        Date sigEnd = new Date();
        long sigMapReadTime = sigEnd.getTime() - sigBegin.getTime();
        
        if (sigMap == null) {
            String msg = "Empty signature map";
            publish(msg,this,Level.ERROR);
            return;
        }
        else 
            logger.info("SignatureMap Read Time: "+sigMapReadTime+" ms");
    
        Date treeBegin = new Date();
        TreeManager treeMgr = CombinedReport.readTreeFromModule(module);
        Date treeEnd = new Date();
        long treeReadTime = treeEnd.getTime() - treeBegin.getTime();
        
        if (treeMgr == null)
            return;
        else
            logger.info("Tree Read Time: "+treeReadTime+" ms");
        
        // get optional node and/or function detail
        if (verbose)
             publish("Reading metadata from data module",this);
        nodeDetail =
            SignatureMap.getNodeDetailFromModule(module);
        nodeDetailColumns = module.getNodeColumns();
        int functionCount;
        if (sigMap.hasFunctions()) {
            functionDetail = 
                SignatureMap.getFunctionDetailFromModule(module);
            functionDetailColumns = module.getFunctionColumns();
            functionFlag = true;
            functionCount = getFunctionCount(module);
            
        }
        else {
            functionDetail = null;
            functionDetailColumns = null;
            functionFlag = false;
            functionCount = -1;
        }
        
        if (verbose)
            publish("Done reading from data module");
    
        Long progressInterval;
        String progressIntervalStr = config.getProperty(PROGRESS_INTERVAL);
        if (progressIntervalStr == null)
            progressInterval = new Long(0);
        else {
            progressInterval = Long.valueOf(progressIntervalStr);
            if (progressInterval == null)
                progressInterval = new Long(0);
        }
        
        logger.info("Progress Interval is "+progressInterval.toString()+" reads");
        logger.info("Number of files to process: "+processFileList.size());
         
        
        Iterator<File> processFileIter = processFileList.iterator();
        int processFileCnt = 0;
        while (processFileIter.hasNext()) {
            processFileCnt++;
            File file = processFileIter.next();
            runCombinedReport(file,sigMap,treeMgr,progressInterval, listFlag, baseDir, functionCount);
        }
        
        
    }
    
    protected int getFunctionCount(SignatureModule dataModule) {
        
        int functionCnt;
        Integer functionCntInt = dataModule.getFunctionCount();
        if (functionCntInt == null) {
            logger.error("Function Count is missing from module");
            functionCnt = -1;
        }
        else      
           functionCnt = functionCntInt;
  
        return functionCnt;
    }
    
        
    protected void runCombinedReport(File fnaFile, SignatureMap sigMap, 
        TreeManager treeMgr, Long progressInterval, boolean listFlag, String baseDir,
        int functionCount) {
        
        String outputExt = config.getProperty(OUT_DIR_EXT);
        String extStr;
        if (outputExt != null)
            extStr = "."+outputExt;
        else
            extStr = "";
        
        File outputDirParent = getOutputDirParent(fnaFile, listFlag, baseDir);
        
        File fullOutputDir =
            new File(outputDirParent, fnaFile.getName()+extStr);
        
        CombinedReport report; 
        SignatureModule dataModule = ModuleFactory.getSignatureModule(sigProps);
        boolean hexFlag = dataModule.getHexFlag();
        
        if (hexFlag) {
        // note:  when using CombinedReportM (and SequencingFileWriterM), program
        // terminates somehow after first call to generateKmerSetList ???
        
            if (writeToDB > 1)
                report = new CombinedReportM(sigProps,fnaFile,dataModule,progressInterval);
            else   
                report = new CombinedReportLong(sigProps,fnaFile,dataModule,progressInterval);
        }
        else
            report = new CombinedReportString(sigProps,fnaFile,dataModule,progressInterval);
        
        publish("Adding "+ fnaFile.getAbsolutePath()+
                " to the queue as "+report.getReaderType(),this,Level.INFO);

        
        boolean okay = addProgressObserversFromConfig(report,fullOutputDir);
        if (!okay) {
            publish("Could not add progress writer(s) for "+fnaFile.getAbsolutePath(),
                this, Level.ERROR);
            return;
        }
        
//        publish("Add output dir to history files for "+fullOutputDir, this);
//        String errorMsg = addOutputDirToHistory(fullOutputDir);
//        if (errorMsg != null) {
//            publish("Failed to add output directory to history files for "+fnaFile.getAbsolutePath()+": "+errorMsg,
//                this, Level.ERROR);
//            return;
//        }
        
        okay = addAnalysisObserversfromConfig(report, fullOutputDir, functionCount);
        if (!okay) {
            publish("Could not add analysis writer(s) for "+fnaFile.getAbsolutePath()+
                "; continuing without analysis observers",
                this, Level.ERROR);
        }        

        addCommonObserversToReport(report);
        report.setSigMap(sigMap);
        report.setTreeManager(treeMgr);
        if (threadPool == null)
            createThreadPool();
        threadPool.execute(report);  
   
    }
   
   
    public List<File> getProcessFileList(Collection<File> fileList, boolean dirFlag) {
        
        
        List<String> validExtList = config.getStringList(FASTA_EXT_LIST,ConfigFile.CONFIG_LIST_DELIM);
        Integer aaCutoff = sigProps.getCutoff();
        int minbp = 3*aaCutoff;
        
        if (verbose) {
            
            String validExtMsg = "Valid input file extensions:";
            if (validExtList != null) {
                Iterator<String> iter = validExtList.iterator();
                while(iter.hasNext()) {
                    String ext = iter.next();        
                    validExtMsg += (" "+ext);
                    validExtMsg += (" "+ext+".gz");
                }
            }

            publish(validExtMsg,this,Level.INFO);
 
//            String validCharMsg = "Sequences in files with extension faa or faa.gz will be treated as protein.  "+
//                    "Sequences in files with extension .fna, .fna.gz will be treated as DNA where "+
//                "only A,C,T,G will be translated.";
            
            String validCharMsg = "Content of input files with extension .fna, .ffn, .fq, or .fastq "+
                "will be treated as DNA sequences. Content of input files with extension .faa "+
                "will be treated as protein sequences. All other input files with a valid extension will be "+
                "tested to determne if DNA ot ptotein.";
            
            publish(validCharMsg,this,Level.INFO);

            String minLengthMsg = "Minimum protein fragment length is "+sigProps.getCutoff().toString()+
                "; if reads have less than "+minbp+" bp, you should decrease this value";
            publish(minLengthMsg,this,Level.INFO);

        }
        
        List<File> processFileList = new ArrayList<>();
        Iterator<File> fileIter = fileList.iterator();
        while (fileIter.hasNext()) {
            File file = fileIter.next();
            String rawFileName = file.getName();
            // removes .gz
            String fileName = SequencingReader.getProcessFileName(rawFileName);
            boolean validExt = SequencingReader.validInputFile(fileName,validExtList);
//            if (!validExt) {
//                if (!dirFlag)
//                    logger.info(file.getAbsolutePath()+" does not have an allowed extension and will not be processed");
//            }
            if (validExt) {
 //               logger.info(file.getAbsolutePath() +" has a valid extension for analysis");
                processFileList.add(file);
            }
  
        }
        if (processFileList.size() < 1) {
            String msg = "There are no input files with valid extension for processing";//; valid extensions are:";
            publish(msg,this,Level.ERROR);
//            for (int i=0; i<validExtArr.length; i++)
//                msg += " ."+validExtArr;
//            logger.error(msg);
//            ThreadRunnerEvent trEvent = new ThreadRunnerEvent(this,msg,1);
//            notifyThreadRunnerObservers(trEvent);
        }      
   
            
        return processFileList;      
       
    }
     
    
    protected boolean addProgressObserversFromConfig(CombinedReport report, File fullOutputDir) {
        
        List<String> progressObserverList = config.getStringList(PROGRESS_WRITER,
            ConfigFile.CONFIG_LIST_DELIM);
        if (progressObserverList == null || progressObserverList.isEmpty()) {
            logger.error("There are no pogress observers");
            return false;
        }
        int writerCnt = 0;
        String classStr = "";
        Iterator<String> iter = progressObserverList.iterator();
        while (iter.hasNext()) {
            try {         
                // this is not very elegant;  need to work on this
                classStr = iter.next();
   //             System.out.println("class to add use as ProgressObserver: "+classStr);
   //             config.writePropertiesToConsole();
                Class theClass = Class.forName(classStr);
                Object obj = theClass.newInstance();
                if (obj != null && obj instanceof ProgressObserver) {
                    writerCnt++;
                    ProgressObserver observer = (ProgressObserver) obj;
                    observer.setThreadPoolSize(sigProps.getThreadNum());
                    if (observer instanceof ProgressWriter) {
                        ProgressWriter progressWriter = (ProgressWriter) observer;
                        progressWriter.setConfig(config);
                        progressWriter.setNodeDetail(nodeDetail, nodeDetailColumns);                  
                        progressWriter.setFunctionDetail(functionDetail, functionDetailColumns);                      
                    }
                    if (observer instanceof ProgressFileWriter) {
                        ProgressFileWriter fileWriter = (ProgressFileWriter) observer;
                        fileWriter.setOutputDir(fullOutputDir);
                    } 
                    report.addProgressObserver(observer);   
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                publish("Problem creating instance of "+classStr,this,Level.ERROR);
                publish("Error msg: "+ ex.getMessage(),this,Level.ERROR); 
                return false;
            }
        }
        return writerCnt >= 1;
    }
    
    @SuppressWarnings("unchecked")
    public boolean addAnalysisObserversfromConfig(CombinedReport report, File fullOutputDir,
        int functionCount) {
        
        boolean addFlag = (writeToDB > 0);
        if (!addFlag)
            return true;
        
        boolean hexFlag = report.getDataModule().getHexFlag();
        String analysisWriterClassKey;
   
        if (hexFlag){
            if (writeToDB == 2) 
                analysisWriterClassKey = ANALYSIS_WRITER_M; 
            else 
                analysisWriterClassKey = ANALYSIS_WRITER_LONG;
        }
        else
            analysisWriterClassKey = ANALYSIS_WRITER_STRING;      
                
        List<String> analysisObserverList = config.getStringList(analysisWriterClassKey,
            ConfigFile.CONFIG_LIST_DELIM);
        if (analysisObserverList == null || analysisObserverList.isEmpty()) {
            logger.info("There are no analysis observers for "+analysisWriterClassKey);      
            logger.info("Sequence file will not be written");
            return true;
        }
        int writerCnt = 0;
        String className = "";
        Iterator<String> iter = analysisObserverList.iterator();
        while (iter.hasNext()) {
            try {   
                className = iter.next();
                String msg = "Adding analysis observer "+className+" for "+report.getJob();
                publish(msg,this,Level.INFO);
                Class theClass = Class.forName(className);
                Object obj = theClass.newInstance();
                if (obj != null && obj instanceof AnalysisObserver) {
                    writerCnt++;
                    AnalysisObserver observer = (AnalysisObserver) obj;
                    if (observer instanceof AnalysisFileWriter) {
                        AnalysisFileWriter analysisWriter = (AnalysisFileWriter) observer;
                        if (writeToDB > 1)
                            analysisWriter.setKmerFlag(true);
                        else
                            analysisWriter.setKmerFlag(false);
                        if (sigProps.getTranslateFlag())
                            analysisWriter.setAnalysisOutputType(FASTA_FAA);
                        // need to modify code so I don't get unchecked call warning
                        // because using AnalysisFileWriter rather than specific version
                        // i.e. AnalysisObserver and AnalysisFileWriter have parameter K
                   
                        analysisWriter.setNodeDetail(nodeDetail);                  
                        analysisWriter.setFunctionDetail(functionDetail);  
                        String db = config.getProperty(DB);
                        if (db == null || db.trim().equals("")) {
                           db = "default";
                            publish("Config file is missing db field - setting to 'default'",
                                this, Level.WARN);
                        }
                        analysisWriter.setOutput(db,fullOutputDir, report.getJob(),
                            report.getReaderType());
                        // note:  job is currently being set to input file name
                        analysisWriter.setFunctionCount(functionCount);  
                        analysisWriter.setAnalysisOutputType(config.getProperty(ANALYSIS_OUTPUT_TYPE));
                    }
                    report.addAnalysisObserver(observer);   
                }
                else {
                    logger.error("Problem with analysis file writer in config: "+className);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                logger.error("Problem creating instance of "+className+": "+
                    ex.getMessage()); 
                return false;
            }
        }
        return writerCnt >= 1;
    }    
 
    protected void addCommonObserversToReport(CombinedReport report) {
        
        Iterator<ProgressObserver> progressIter = progressObservers.iterator();
        while (progressIter.hasNext())
            report.addProgressObserver(progressIter.next());
        
    }
    protected File getOutputDirParent(File inputFile, boolean listFlag, 
            String baseDir) {
         
         File canonicalInputFile;
         if(baseDir != null && baseDir.equals(NONE))
             baseDir = null;
         
         try {
            canonicalInputFile = inputFile.getCanonicalFile();
         } catch (IOException ex) {
             String msg = "Could not get canonical file from "+inputFile.getAbsolutePath();
             logger.error(msg);
             if (verbose) 
                 publish(msg,this);
             return null;
         }
         
        File outputDir = null;
       
        if (outputDirStr == null || outputDirStr.equals(INPUT_LOCATION)) {
            return canonicalInputFile.getParentFile();
        }
        else {          
            if (listFlag && baseDir != null) {
                int baseDirStrLen = baseDir.length();
                String parent = canonicalInputFile.getParent();
                String name = canonicalInputFile.getName();
                int nameLen = name.length();
                String absPath = canonicalInputFile.getAbsolutePath();
                String stripBase = absPath.substring(baseDirStrLen+1);
                String stripName = stripBase.substring(0,stripBase.length()-nameLen);
                return new File(outputDirStr,stripName);
            }
            else
                return new File(outputDirStr);
            
        }
    }
        
}
 

        
   

