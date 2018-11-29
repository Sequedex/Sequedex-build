/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.analysis;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author jcohn
 */
public class AnalysisRunnerTimerTask extends TimerTask {
    
    AnalysisRunner runner;
    Timer timer;
    
    public AnalysisRunnerTimerTask(AnalysisRunner r, Timer t) {
        super();
        runner = r;
        timer = t;
    }

    @Override
    public void run() {
        if (runner.isTerminated()) {
            cancel();
            timer.cancel();
        }
        
    }
    
}
