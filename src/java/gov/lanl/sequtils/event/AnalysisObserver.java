/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

import java.util.Collection;

/**
 *
 * @author jcohn
 */
public interface AnalysisObserver<W> {
    
    public void observeAnalysisEvents(Collection<AnalysisEvent<W>> events);
    public void observeAnalysisEvent(AnalysisEvent<W> event);
    
}
