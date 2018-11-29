/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.cl;

import gov.lanl.sequescan.mode.Mode;
import gov.lanl.sequescan.mode.ModeFactory;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jcohn
 */
abstract public class SequescanCL {
    
     protected String[] commandLineArgs = null;
     protected ModeFactory modeFactory = null;
     protected boolean internalFlag;
	
    /**
	 * Default constructor
     * 
     * @param appArgs array of argument strings from command line
     */
    public SequescanCL(String[] appArgs, boolean isInternal) {
        commandLineArgs = appArgs;
        internalFlag = isInternal;
        init();
    }
    
    public final void init() {
        setModeFactory();
    }
      
    public boolean runMode() {

        if (commandLineArgs == null || commandLineArgs.length == 0) {
            usage();
            return true;
        }

        else if (commandLineArgs.length == 1 && commandLineArgs[0].equals("-h")) {
            usage();
            return true;
        }
        
        Mode mode = modeFactory.getMode(commandLineArgs);
        if (mode == null) 
            return false;
        else {
            boolean okay = mode.init();
            if (!okay) 
                return false;
            else {
                mode.execute();
                return true;    // does not mean that it was successful...
            }
        }
    }
  
	/**
	 * Method that prints the program's general usage to the screen.
         * This class is a wrapper for all the children of the Mode class, 
         * which run appropriate code for various sequescan functionality.
         * Each instantiation of the abstract Mode class must have a usage 
         * method as well.
         * 
	 */
    public void usage(){
            
        Set<String> allowedModes = modeFactory.getAllowedModes();
        String modeStr,runStr;
        if (internalFlag) {
            modeStr = "internal mode";
            runStr = "internal run";
        }
        else {
            modeStr = "mode";
            runStr = "run";
        }

        String usage = "\n\nCommand line execution of sequescan:\n";
        usage += "java -jar <distrib_dir>/lib/sequescan.jar "+modeStr+" [mode_qualifiers] mode_argument\n\n";
        usage += "Available modes:\n";
        if (allowedModes != null && allowedModes.size() > 0) {
            Iterator<String> modeIter = allowedModes.iterator();
            while (modeIter.hasNext()) {
                usage += (modeIter.next()+"\n");
            }
            usage += "\n";
        }
        else
            usage += "no modes available\n\n";
        usage += "To display mode usage:\n";
        usage += "sequescan "+modeStr+" -h\n";
        usage += "Example:  sequescan "+runStr+" -h\n\n";
        System.out.println( usage);
    }
    
    abstract protected void setModeFactory();
    
}
