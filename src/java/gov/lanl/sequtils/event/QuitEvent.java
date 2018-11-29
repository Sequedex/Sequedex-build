/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

/**
 *
 * @author jcohn
 */
public class QuitEvent {
    
    // constants
    public static final String SEQUESCAN_APP = "Sequescan App";
    
    // instance variables
    protected String activity;
     
    public QuitEvent(String aStr) {
        activity = aStr;
    }
    
    public String getActivity() {
        return activity;
    }
    
}
