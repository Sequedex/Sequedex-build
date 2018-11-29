
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.event;

import gov.lanl.sequtils.data.AnalysisEventData;
import java.util.Date;

/**
 *
 * @author jcohn
 */
public class AnalysisEvent<W> {
    
    // event actions
    public static final int OPEN = 0;
    public static final int PROCESS_DATA = 1;
    public static final int CLOSE = 2;
    
    protected AnalysisEventData<W> data;
    protected Date eventTime;
    protected int action;
    
    public AnalysisEvent(AnalysisEventData<W> analysisData) {
        data = analysisData;
        eventTime = new Date();
        action = PROCESS_DATA;
    }
    
    public AnalysisEvent(int actn) {
        data = null;
        eventTime = new Date();
        action = actn;
    }
    
    public AnalysisEventData<W> getData() {
        return data;
    }
    
    public Date getEventTime() {
        return eventTime;
    }
    
    public int getAction() {
        return action;
    }
    
}
