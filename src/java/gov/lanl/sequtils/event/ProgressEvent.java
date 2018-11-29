/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

import gov.lanl.sequtils.data.ProgressEventData;
import java.util.Date;


/**
 * Lightweight "event" class which passes ProgressEventData to ProgressListeners
 * @author jcohn
 */
public class ProgressEvent {
    
    // event actions - BEGIN and END not currently in use
    public static final int PROGRESS_DATA = 0;
    public static final int BEGIN = 1;
    public static final int END = 2;
  
    
    protected ProgressEventData data;
    protected Date eventTime;
    protected int action;
    protected String logFile;
    
    public ProgressEvent(ProgressEventData progressData) {
        data = progressData;
        eventTime = new Date();
        action = PROGRESS_DATA;
        logFile = null;
    }
    
    public ProgressEvent(int actn) {
        this(actn, null);
    }
    
    public ProgressEvent(int actn, String lfile) {
        data = null;
        eventTime = new Date();
        action = actn;
        logFile = lfile;
    }
    
    public int getAction() {
        return action;
    }
    
    public ProgressEventData getData() {
        return data;
    }
    
    public Date getEventTime() {
        return eventTime;
    }
    
    public String getLogFile() {
        return logFile;
    }
    
}
