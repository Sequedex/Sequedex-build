/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.mode;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class ModeFactoryPublic extends ModeFactory {
    
      public ModeFactoryPublic() {
        super();
    }
 
    @Override
    public Set<String> getAllowedModes() {       
        Set<String> allowedModes = new TreeSet<>();
        allowedModes.add(RUN);
        allowedModes.add(MODULE_INFO);
        return allowedModes;       
    }

    
  
}
