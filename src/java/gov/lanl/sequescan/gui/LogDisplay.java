/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.gui;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.log.LogManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;

/**
 *
 * @author jcohn
 */
public class LogDisplay implements Runnable {
    
    protected File processOutFile;
    protected File processErrFile;
    protected JTextArea progressTA;
    protected FileReader outReader = null;
    protected FileReader errReader = null;
    protected BufferedReader out = null;
    protected BufferedReader err = null;
    protected boolean startOutput = false;
    protected volatile boolean stopThread = false;
    
    
    public LogDisplay(JTextArea ta) {           
        progressTA = ta;
    }
    
    public void setLogFiles(File outFile, File errFile) {
        closeOpenLogFiles();
        startOutput = false;
        processOutFile = outFile;
        processErrFile = errFile;      
    }
    
    public void stopDisplay() {
        stopThread = true;
    }
    
    protected void closeOpenLogFiles() {
        
        if (out != null) {
            try {
                outReader.close();
            } catch (IOException ex) {
                LogManager.publish("Problem closing "+
                    processOutFile.getAbsolutePath(), this, Level.ERROR);
                LogManager.publish(ex.getMessage(),this,Level.ERROR);
            }
            outReader = null;
            out = null;
            processOutFile = null;
        }
        if (err != null) {
            try {
                errReader.close();
             } catch (IOException ex) {
                LogManager.publish("Problem closing "+
                    processErrFile.getAbsolutePath(), this, Level.ERROR);
                LogManager.publish(ex.getMessage(),this,Level.ERROR);
            }
            errReader = null;
            err = null;
        }
        
        processOutFile = null;
        processErrFile = null;
        
        
    }

    @Override
    public void run() {
      
        String outLine, errLine;
  
        try {
            while (!stopThread) {
                
                if (processOutFile == null || processErrFile == null)
                    continue;
                
                if (outReader == null && processOutFile.exists())
                    outReader = new FileReader(processOutFile);   
                
                if (errReader == null && processErrFile.exists())
                    errReader = new FileReader(processErrFile);
                             
                if (out == null && outReader != null)
                    out = new BufferedReader(outReader);
                
                if (err == null && errReader != null)
                    err = new BufferedReader(errReader);
    
                if (err != null) {
                    errLine = err.readLine();
                    if (errLine != null) {
                        progressTA.append("\n");
                        progressTA.append(errLine);
                        progressTA.repaint();
                    }
                }
                
                if (out != null) {
                    outLine = out.readLine();
                    if (outLine != null) {   
                        if (startOutput) {
                            progressTA.append("\n");
                            progressTA.append(outLine);
                            progressTA.repaint();
                        }
                        else {
                            if (!outLine.startsWith("LOGBACK") && !outLine.startsWith("Running with config file")
                                && !outLine.startsWith("Writing log files to"))
                                startOutput = true;
                        }
                    }
                    else {
                        try {
                            Thread.sleep(10000);
                        }catch(InterruptedException sleepEx) {
                            String emsg = sleepEx.getMessage();
                            if (!emsg.startsWith("sleep interrupted"))
                                LogManager.publish(sleepEx.getMessage(),this, Level.ERROR);
                        }
                    }
                }                
                
            }
        } catch(IOException ex) {
            String exMsg = "Problem reading output or err file: "+ex.getMessage();
            LogManager.publish(exMsg, this, Level.ERROR);
        }
        
        closeOpenLogFiles();
        
    } 
    
}
