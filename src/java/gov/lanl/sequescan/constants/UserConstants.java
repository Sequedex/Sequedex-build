/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.constants;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.log.MessageManager;
import java.io.File;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author jcohn
 */
public class UserConstants {
    
    // sequedex user directory and properties file        
    public static final String SEQUEDEX_USER_PROPERTIES = "sequedex.properties";
       /**
     * The minimum version number of user properties file
     * Earlier versions will be deleted
     */
    public static final String MIN_USER_PROPS_VERSION_NUMBER = "0.1";
    
     /**
     * The current version number of user properties file
     * 
     */
    public static final String USER_PROPS_VERSION_NUMBER = "1.1";
    
    public static final String SEQUEDEX_USERDIR = initUserDir();
    
    public static final String initUserDir() {
        
        File userDir;
        String subdirName;
        
        boolean isWindows = SystemUtils.IS_OS_WINDOWS;
        if (isWindows) 
            subdirName = "sqdx";
        else
            subdirName = ".sqdx";
        
        userDir = new File(SystemUtils.USER_HOME, subdirName);       
 
        if (!userDir.exists()) {
            boolean okay = userDir.mkdirs();
            if (!okay) {
                String errMsg = "Problem creating sequedex user directory "+
                    userDir.getAbsolutePath();
                MessageManager.publish(errMsg,true,Level.ERROR);
                return null;
            }
        }
        return userDir.getAbsolutePath();
        
    }
    
    public static File getUserDir() {
        if (SEQUEDEX_USERDIR == null)
            return null;
        return new File(SEQUEDEX_USERDIR);
    }
          
    public static File getUserPropertiesFile() {
        File userDir = getUserDir();
        if (userDir == null)
            return null;
        else
            return new File(userDir, SEQUEDEX_USER_PROPERTIES);
    }
    
    
    
    
}
