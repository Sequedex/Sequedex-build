/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.cl;

/**
 *
 * @author jcohn
 */
public class SequescanCLFactory {
    
    public final static String INTERNAL = "internal";
    
    public static SequescanCL getSequescanTopLevel(String[] args) {
        
        if (args == null || args.length < 1)
            return null;
        
        
        String firstArg = args[0];
        if (firstArg.equals(INTERNAL)) {
            if (args.length < 2) {
                System.out.println("You must include a mode or -h to get help");
                return null;
            }
            String[] newArgs = new String[args.length - 1];
            for (int i=1; i<args.length; i++)
                newArgs[i-1] = args[i];
            return new SequescanInternal(newArgs);
        }
        else
            return new SequescanPublic(args);
   
    }
    
}
