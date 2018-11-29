/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.cl;

import gov.lanl.sequescan.mode.ModeFactoryInternal;

/**
 *
 * @author jcohn
 */
public class SequescanInternal extends SequescanCL {
    
    public SequescanInternal(String[] appArgs) {
        super(appArgs, true);
    }
    
    @Override
    protected void setModeFactory() {
        modeFactory = new ModeFactoryInternal();
    }
  
    
}
