/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.cl;

import gov.lanl.sequescan.mode.ModeFactoryPublic;

/**
 *
 * @author jcohn
 */
public class SequescanPublic extends SequescanCL {
    
    public SequescanPublic(String[] appArgs) {
        super(appArgs, false);
    }
    
    @Override
    protected void setModeFactory() {
        modeFactory = new ModeFactoryPublic();
    }
  
    
}
