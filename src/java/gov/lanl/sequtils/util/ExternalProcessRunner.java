/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.event.QuitListener;
import gov.lanl.sequtils.log.LogManager;
import static gov.lanl.sequtils.log.MessageManager.publish;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *
 * @author jcohn
 */
public class ExternalProcessRunner extends LogManager implements Callable<Integer> {
    
    public static String PROCESS_OUT_EXT = ".processOut.txt";
    public static String PROCESS_ERR_EXT = ".processErr.txt";
        
    protected String externalCommand;
    protected String logFileName;
  //  protected QuitListener processCaller;
    
    public ExternalProcessRunner (String cmd, String logFile) { //, QuitListener qlistener) {
        logFileName = logFile;
        externalCommand = cmd;
  //      processCaller = qlistener;
    }
    
    @Override
    public Integer call() throws InterruptedException {
        Integer exitStatus;
        try {
            String[] cmdArr = externalCommand.split("\\s+");  
            // ProcessBuilder does not work with single command string with spaces
            // Perhaps I should modify the function which generates the command to
            // create array or list rather than a single string
            ProcessBuilder pbuilder = new ProcessBuilder(cmdArr);
            File outputFile = new File(logFileName + PROCESS_OUT_EXT);
            File errFile = new File(logFileName + PROCESS_ERR_EXT);
            pbuilder.redirectOutput(outputFile);
            pbuilder.redirectError(errFile);
            Process p = pbuilder.start();     
      //      processCaller.addProcess(p);
      //      Process p = Runtime.getRuntime().exec(externalCommand);  // works with single string
            exitStatus = p.waitFor();
        } catch (IOException ex) {
            publish("Problem running sequescan with auto max heap",this,Level.ERROR);
            publish(ex.getMessage(), this, Level.ERROR);
            exitStatus = -1;
        } catch (InterruptedException ex) {
            publish("InterrutedException while running sequescan with auto max heap",this, Level.ERROR);
            publish(ex.getMessage(),this,Level.ERROR);
            exitStatus = -2;
        } 
        return exitStatus;
        
    }
    
}
