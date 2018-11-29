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
public class ModeFactoryInternal extends ModeFactory {
    
   public ModeFactoryInternal() {
       super(true);
   }
   
    @Override
    public Set<String> getAllowedModes() {
        
        Set<String> allowedModes = new TreeSet<>();
        allowedModes.add(CREATE_MODULE);  // includes phylogeny nodes and detail
        allowedModes.add(ADD_FUNCTION);   // can add both function alone or with detail
   //     allowedModes.add(ADD_DETAIL);    // currently method for this is empty - need to fix
        allowedModes.add(ADD_FUNCTION_DETAIL);  // overwrite or add for the first time
        allowedModes.add(KMER_MAP);
        return allowedModes;
    }
    
}

