/*
 * Replaces LogUser/LogWriter class
 */
package gov.lanl.sequtils.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import gov.lanl.sequtils.event.ThreadRunnerEvent;
import gov.lanl.sequtils.event.ThreadRunnerObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jcohn
 */
public class MessageManager {
    
    // This class replaces LogUser
    
    protected static Logger logger = getRootLogger();
    protected static boolean guiFlag = false;
    protected static List<ThreadRunnerObserver> threadRunnerObservers = new ArrayList<>();
    protected static JFrame parentFrame = null;
  
    public static void setCurrentLogger(Logger lgr) {
        logger = lgr;
    }
    
    public static Logger getCurrentLogger() {
        return logger;
    }
    
    public static boolean getGuiFlag() {
        return guiFlag;
    }
    
    public static void setGuiFlag(boolean flag, JFrame frame) {
        guiFlag = flag;
        parentFrame = frame;
    }
    
    public static JFrame getParentFrame() {
        return parentFrame;
    }
    
    public static void addThreadRunnerObserver(ThreadRunnerObserver observer) {
        threadRunnerObservers.add(observer);
    }
    
    public static void removeThreadRunnerObserver(ThreadRunnerObserver observer) {
        threadRunnerObservers.remove(observer);
    } 
    
    public static void notifyThreadRunnerObservers(String msg, Object src) {
        
        ThreadRunnerEvent event = new ThreadRunnerEvent(src, msg, 2);
        notifyThreadRunnerObservers(event);
    }
        
    public static void notifyThreadRunnerObservers(ThreadRunnerEvent event) {
     
        Iterator<ThreadRunnerObserver> observerIter = threadRunnerObservers.iterator();
        while (observerIter.hasNext()) {
            ThreadRunnerObserver observer = observerIter.next();
            observer.observeThreadRunnerEvent(event);
        }
    }  
     
    public static Logger getLogger(String name) {
        LoggerContext context = getContext();
        return context.getLogger(name); 
        // following did not help the "no context given" warning
//        LoggerContext loggerContext = 
//        (LoggerContext) LoggerFactory.getILoggerFactory(); 
//        loggerContext.reset(); 
//        return loggerContext.getLogger(name);
 
    }
           
    public static LoggerContext getContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }
    
    public static Logger getRootLogger() {
        return getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        
    }
    
    public static void publish (String msg) {
        MessageManager mgr = new MessageManager();
        publish(msg, mgr);
    }
    
    public static void publish (String msg, Object src) {
        publish(msg,src,null);
    }
    
    public static void publish(String msg, boolean runDisplayFlag, Level logLevel) {
        Object src;
        if (runDisplayFlag)
            src = new MessageManager();
        else
            src = null;
        publish(msg, src, logLevel);
    }
    
    public static void publish(String msg,Object src, Level logLevel) {
        
        if (src != null) {
            if (guiFlag)
                notifyThreadRunnerObservers(msg,src);
            else
                System.out.println(msg);
        }

        if (logLevel != null) {
            if (logLevel == Level.DEBUG)
                logger.debug(msg);
            else if (logLevel == Level.WARN)
                logger.warn(msg);
            else if (logLevel == Level.ERROR)
                logger.error(msg);
            else if (logLevel == Level.INFO)
                logger.info(msg);
            else
                logger.info(msg);
        }
        
                 
    }
    
   
}
