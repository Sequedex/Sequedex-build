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
public interface QuitListener {
    
    public void addProcess(Process p);
    public void quitResponse(QuitEvent qevent);
    
}
