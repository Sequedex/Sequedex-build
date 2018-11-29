package gov.lanl.sequtils.util;

//import gov.lanl.sequescan.constants.SequescanConstants;
import gov.lanl.sequtils.log.MessageManager;
import static gov.lanl.sequtils.log.MessageManager.publish;
import java.io.*;
import java.nio.channels.FileChannel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Java class with variety of static methods related to Files/Directories 
 *
 * @since     0.9.0  
 * 2017 Oct:  renamed FileUtilities
 */
public class FileUtilities extends MessageManager {
    
    // jc (8/9/12):  changed all methods to static then merged this class with
    // everything in FileUtilities
    
    // constants for action
    public static final int OPEN = 0;
    public static final int SAVE = 1;
    public static final int DELETE = 2;
    public static final int CHOOSEDIR = 3;
    
    // other constants
    public static final String JAVA_APPS = "javaApps";   
    protected static File lastDir = null;
    
    /**
        * Calculates the size of a file in KB
        * @param file the file for which the size is being calculated.
        * @return the file size.
        */
    public static long getFileSizeInKB( File file){
  //      File file = new File(filePath);
        long fileSize = file.length();
        return fileSize / (long) 1024;
    }
    
    public static long getFileSizeInKB(String filePath) {
        return getFileSizeInKB(new File(filePath));
    }

    /**
        * Calculates the size of a file using the File.length() method
        * @param file the file for which the size is being calculated.
        * @return the file size.
        */
    public static long getFileSize( File file){
        return file.length();
    }
    
    public static long getFileSize(String filePath) {
        return getFileSize(new File(filePath));
    }

    /**
        * Method that determines the current directory and returns it.
        * @return the current directory.
        */
    public static String getCanonicalPathOfCurrDir(){
            File directory = new File (".");
            String currDir = null;
            try{
                    currDir = directory.getCanonicalPath();
            }
            catch( IOException ioe){
                    publish("[FileAndDirHandler.getCanonicalPathOfCurrDir()] - Error:" + ioe.getMessage());
            }
            return currDir;
    }

      
    public static File selectLocalFile(String titleStr) {
        return selectLocalFile(titleStr,OPEN);
    }
    
    public static File selectLocalFile(String titleStr,int action) {
        return selectLocalFile(titleStr,action,null);
    }
    
    public static File createTempFile(String prefix,String suffix) {
        return createTempFile(prefix,suffix,"temp",true);
    }
    
    public static File createTempFile(String prefix,String suffix, String subdirName,
        boolean deleteOnExit) {
        boolean okay;
        File javaAppDir = new File(System.getProperty("user.home"),JAVA_APPS);
        if (!javaAppDir.isDirectory()) {
            okay = javaAppDir.mkdir();
            if (!okay) {
                String msg = "Problem making javaApp folder for temp files";
                publish(msg);
                return null;
            }
        }
        File tempDir = new File(javaAppDir, subdirName);
        if (!tempDir.isDirectory()) {
            okay = tempDir.mkdir();
            if (!okay) {
                String msg = "Problem making javaApp/"+subdirName;
                publish(msg);
                return null;
            }
        }
        File tempFile;
        try {
            tempFile = File.createTempFile(prefix,suffix,tempDir);
            if (deleteOnExit)
                tempFile.deleteOnExit();
            return tempFile;
        } catch (Exception e) {
            String msg = "Problem creating temporary file";
            publish(msg);
            publish(e.toString());
            return null;
        }
    }
    
    public static File selectLocalFile(String titleStr,int action,File defaultDir) {
        JFileChooser chooser;
        if (defaultDir == null && lastDir == null)
            chooser = new JFileChooser();
        else if (defaultDir != null)
            chooser = new JFileChooser(defaultDir);
        else
            chooser = new JFileChooser(lastDir);
        chooser.setDialogTitle(titleStr);
        int returnVal;
        switch (action) {
            case OPEN:
                returnVal = chooser.showOpenDialog(new JFrame());
                break;
            case CHOOSEDIR:
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                returnVal = chooser.showDialog(new JFrame(),"Select Directory");
                break;
            case SAVE:
                returnVal = chooser.showSaveDialog(new JFrame());
                break;
            default:
                return null;
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            lastDir = new File(selectedFile.getParent());
            return selectedFile;
        }
        else
            return null;    
    }
    
    public static String copyFile(File srcFile, File copyFile, boolean overwriteFlag) {
        if (!srcFile.exists())
            return "Source file "+srcFile.getAbsolutePath()+" does not exist";
        if (copyFile.exists()) {
            if (overwriteFlag)
                copyFile.delete();
            else
                return "Destination file "+copyFile.getAbsolutePath()+" exists";
        }
        try {
            FileChannel copyChannel;
            try (FileChannel sourceChannel = new FileInputStream(srcFile).getChannel()) {
                copyChannel = new FileOutputStream(copyFile).getChannel();
                sourceChannel.transferTo(0, sourceChannel.size(), copyChannel);
            }
            copyChannel.close();
        } catch (IOException ex) {
            return "Problem with copy file: "+ex.getMessage();
        }
        return null;
    }
 
   
}
