/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

//import java.awt.AWTEvent;
import java.util.Date;

/**
 *
 * @author jcohn
 */
public class ThreadRunnerEvent { // extends AWTEvent {
    // note:  making this an AWTEvent did not help update progress text area any sooner
    
    private static final long serialVersionUID = 1L;
 
    // instance variables
    protected int relativeID;
    protected String message;
    protected Date eventTime;
    
    
    public ThreadRunnerEvent(Object src, String msg,int relID) {
   //     super(src,RESERVED_ID_MAX+relID);
        message = msg;
        relativeID = relID;
        eventTime = new Date();
        
    }
    
    public String getMessage() {
        return message;
    }
    
    public Date getTime() {
        return eventTime;
    }
    
    
}
