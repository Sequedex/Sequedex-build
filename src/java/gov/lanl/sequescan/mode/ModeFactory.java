/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.mode;

import gov.lanl.sequescan.constants.ModeConstants;
import gov.lanl.sequtils.log.MessageManager;
import java.util.Set;

/**
 *
 * @author jcohn
 */
abstract public class ModeFactory implements ModeConstants {
    
    protected boolean internalFlag = false;
 
    public ModeFactory() {
        this(false);
        
    }
    
    public ModeFactory(boolean isInternal) {
        super();
        internalFlag = isInternal;
    }

    public Mode getMode(String[] appArgs) {
    
        if (appArgs == null || appArgs.length < 1) {
            MessageManager.publish("Missing command line arguments",this);
            return null;
        }
        
        String modeStr = appArgs[0].toLowerCase();
        Set<String> allowedModes = getAllowedModes();
        
        if (!allowedModes.contains(modeStr)) {
            MessageManager.publish("Unknown mode: "+modeStr,this);
            return null;  
        }
        else {
            switch (modeStr) {
                case RUN:
                    return new RunMode(appArgs,internalFlag);
                case MODULE_INFO:
                    return new InfoMode(appArgs,internalFlag);
                case CREATE_MODULE:
                    return new ModuleMode(appArgs,internalFlag);
                case ADD_DETAIL:
                    return new ModuleMode(appArgs,internalFlag);
                case ADD_FUNCTION:
                    return new ModuleMode(appArgs,internalFlag);
                case ADD_FUNCTION_DETAIL:
                    return new ModuleMode(appArgs,internalFlag);
                case KMER_MAP:
                    return new ModuleMode(appArgs,internalFlag);
                default:
                    MessageManager.publish(modeStr+" is not currently supported",this);
                    return null;
            }
        }
    
    }
    
    abstract public Set<String> getAllowedModes();
    
    
}
