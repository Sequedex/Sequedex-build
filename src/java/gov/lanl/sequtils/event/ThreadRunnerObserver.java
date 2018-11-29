/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

/**
 *
 * @author jcohn
 */
public interface ThreadRunnerObserver { // extends EventListener {
 
    public void observeThreadRunnerEvent(ThreadRunnerEvent event);
 
    
}
