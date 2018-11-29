/*
 * 
 */
package gov.lanl.sequtils.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import gov.lanl.sequtils.constants.LoggingConstants;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import gov.lanl.sequescan.constants.AppConstants;


/**
 *
 * @author jcohn
 */
public class LogManager extends MessageManager implements AppConstants, LoggingConstants {

/**
 * Java class with static methods for managing logging using logback-classic package
 * (which in turn uses slf4j api).  
 * 
 * @author    Judith Cohn <jcohn@lanl.gov>
 * @since     0.9.1 
 * 
 */
    
  // global variables 
    public static String LOG_EXT = ".html";  
     
    public static void initModeLogger(String level, String URL) {  
        
        
        Logger modeLogger =  logger;  //getLogger("ModeLogger"); 
        Level levelValue = Level.valueOf(level);
        if (levelValue == null) {
            LogManager mgr = new LogManager();
            String msg = level+" is not a known log level for log4j - setting level to DEBUG";
            publish(msg,mgr);
            levelValue = Level.DEBUG;
        }
        modeLogger.setLevel(levelValue); 
  
        if (URL == null)
            URL = DEFAULT_LOGFILE_NAME;  // not a URL at present, just a string
        
        try {           
            Appender appender = logger.getAppender("FILE");
             
            if (appender == null) {
                String msg = "No appender with name FILE was found; appender properties will not be initialized";
                LogManager mgr = new LogManager();
                publish(msg,mgr);
                return;
            }
    
            if (appender instanceof FileAppender) {
                FileAppender fappender = (FileAppender) appender;
                fappender.setAppend(true);
                if (fappender.isStarted())
                    fappender.stop();
                fappender.setFile(URL);
                fappender.start(); 
            }
            else {
                String msg = "Appender FILE in logback configuration is not a FileAppender; properties cannot be changed";
                LogManager mgr = new LogManager();
                publish(msg,mgr);
            }
                           
            
        } catch (Exception ex) {
            String msg = "Problem initializing log file to "+URL;
            LogManager mgr = new LogManager();
            publish(msg,mgr);
            publish(ex.getMessage(),mgr);
        } 
            
        
        setCurrentLogger(modeLogger);
            
        
    }
    
    public static String getDefaultLogFileName(String logDirPath, String modeStr) {
        
  
        File logDir = new File(logDirPath);
        boolean success = true;
        if (!logDir.exists())
            logDir.mkdirs();
        if (!success) {
            // kludge - need Object as source for publish
            LogManager mgr = new LogManager();
            publish("Problem creating log directory: "+logDirPath,mgr);
            return null;
        }
           
         
        //tag on the log file name (Note:  presume .html (need to pass this as argument or otherwise...)
        String ext = LOG_EXT;         
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String nowStr = dateFormat.format(now);
        String logFileNm = PROGRAM_NAME + "_" + modeStr+"_"+nowStr + ext;
        File logFile = new File(logDirPath, logFileNm);
        return logFile.getAbsolutePath();      
   
    } 
  
    
}
