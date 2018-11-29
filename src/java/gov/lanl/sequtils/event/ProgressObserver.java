/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

/**
 *
 * @author jcohn
 */
public interface ProgressObserver {
    
    public void observeProgressEvent(ProgressEvent event);
    // next two methods are a kludge to allow ProgressWriters to record thread pool size
    public int getThreadPoolSize();
    public void setThreadPoolSize(int size);
    
}
