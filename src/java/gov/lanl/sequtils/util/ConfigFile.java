/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.AppConstants;
import gov.lanl.sequtils.constants.BuildConstants;
import gov.lanl.sequtils.log.MessageManager;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jcohn
 * @since 2017 Oct 18
 * 
 */
public class ConfigFile extends BaseProperties implements AppConstants,
    BuildConstants {
    
  public static final String CONFIG_LIST_DELIM = ",";
    
  protected static String jarPath = null;
  protected static String distribDir = null;
  protected static String fullVersion = null;
    
  public ConfigFile() {
      super();
      init();
  }
  
  protected final void init() {
      addCommonProperties();
      setListDelim(CONFIG_LIST_DELIM);
  }
  
  public String getLogLevel() {
      return getProperty(LOG_LEVEL);
  }
 
  
  
     /* should keys in this method be added to AppConstants?  */
     protected void addCommonProperties() {
 
        setProperty(APP_VERSION, VERSION_NUMBER);
        setProperty(APP_VERSION_MAJOR, VERSION_MAJOR_NUMBER);
        String dateTime = LocalDateTime.now().toString();
        setProperty( DATETIME, dateTime);  // time stamp of program execution, without spaces.

    }
     
  
    public static String getBuildString() {
        return BUILDSTRING;
    }
    
    public static String getDistribDir() {
        if (distribDir != null)
            return distribDir;
        else {
            File jarFile = new File(getJarPath());
            distribDir = jarFile.getParentFile().getParentFile().getPath();
            return distribDir;
        }
    }
    
    public static String getJarPath() {
       
        if (jarPath != null)
            return jarPath;
        else {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                boolean isWindows;
                isWindows = os.contains("win");
                CodeSource codeSrc =
                ConfigFile.class.getProtectionDomain().getCodeSource();
                jarPath = codeSrc.getLocation().toURI().getPath();
                if (isWindows && jarPath.startsWith("/"))
                    jarPath = jarPath.substring(1);
                return jarPath;
            } catch(URISyntaxException ex) {
                MessageManager.publish(ex.getMessage(),true, Level.ERROR);
                MessageManager.publish("Could not get jar file path",
                                       true, Level.ERROR);
                return null;
            }
        }
    }
    
    public static String getEtcDir() {
        String homeDirStr = getDistribDir();
        if (homeDirStr == null)
            return null;
        else
            return homeDirStr + SEP + ETC_DIR;
    }
    
    public static String getDefaultDataModuleDir() {
        String homeDirStr = getDistribDir();
        if (homeDirStr == null)
            return null;
        else
            return homeDirStr + SEP + DATA_DIR;
    } 
    
    public static String getDefaultConfigFilePath() {
        String etcDir = getEtcDir();
        if (etcDir == null)
            return null;
        else
            return etcDir + SEP + PROGRAM_NAME + SEP +
            DEFAULT_CONFIG_FILE_NAME;
    }
    
    public static String getSequedexVersion() {
        return SEQUEDEX_VERSION;
    }
    
//    public static String getFullVersion() {
//        if (fullVersion != null)
//            return fullVersion;
//        else {
//            String homeDirStr = getDistribDir();
//            if (homeDirStr == null) {
//                return "Unknown";
//            }
//            // String homeDir = System.getenv( SequedexEnvVars.SEQUEDEX_HOME);
//            File versionFile = new File(homeDirStr,"VERSION.txt");
//            
//            Collection<String> lines = null;
//            if (versionFile.exists()) {
//                FileObj fileObj = new FileObj(versionFile);
//                lines = fileObj.readLines();
//            }
//            if (lines == null)
//                lines = new ArrayList<>();
//            
//            if (lines.size() < 1)
//                lines.add("Unknown");
//            return lines.iterator().next();
//        }
//    }

  
  public static ConfigFile getConfigFromFile(String configFilePath) {
      ConfigFile config = new ConfigFile();
      boolean okay = config.loadPropertiesFromFile(configFilePath);
      if (!okay)
          return null;
      else  {
          config.trimProperties();
          return config;
      }
  }
  
  protected void trimProperties() {
      Set<String> keys = getKeys();
      Iterator<String> iter = keys.iterator();
      while (iter.hasNext()) {
          String key = iter.next();
          String val = getProperty(key);
          String trimVal = StringOps.getFirstToken(val," ");
          setProperty(key,trimVal);     
      }
  }
  
  public void writePropertiesToConsole() {
      Set<String> keys = getKeys();
      Iterator<String> iter = keys.iterator();
      while (iter.hasNext()) {
          String key = iter.next();
          String val = getProperty(key);
          System.out.println(key+" = "+val+"; len="+val.length());
      }
  }
  
  /* testing */
  public static void main(String[] args) {
      
      String configFilePath = ConfigFile.getDefaultConfigFilePath();
      BaseProperties config = ConfigFile.getConfigFromFile(configFilePath);
      if (config != null) {
          Iterator<String> iter = config.getKeys().iterator();
          while (iter.hasNext())
              System.out.println(iter.next());
      }
      else
          System.out.println("Problem getting properties from "+configFilePath);
      
  }
    
    
}
