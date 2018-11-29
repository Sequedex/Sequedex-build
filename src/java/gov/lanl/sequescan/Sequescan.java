package gov.lanl.sequescan;

import gov.lanl.sequescan.cl.SequescanCL;
import gov.lanl.sequescan.cl.SequescanCLFactory;
import gov.lanl.sequescan.gui.SequescanGUI;
import gov.lanl.sequtils.log.MessageManager;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;

/**
 * Top Level of Sequedex for sequescan functionality  
 * completely rewritten from original Sequescan class
 * by M. 
 * @author    J. Cohn <jcohn@lanl.gov>
 * @since     0.9.0    
 */
public class Sequescan {

    public static void main ( String[] args){
        
        boolean isMac = SystemUtils.IS_OS_MAC_OSX;
        String versionStr = System.getProperty("java.version");
        String[] strArr = versionStr.split("\\.");
        if (strArr.length < 2) {
            System.out.println("Problem with java version number: "+versionStr);
            System.exit(1);    
        }
        int versionNum = Integer.parseInt(strArr[1]);
        // note:  for java version 8, I am kludging Mac gui stuff by using
        // Mac-specific jar file created from version 7.  However, this does not
        // work for versions 9 and 10. At some point I need to figure out what
        // needs to be changed to work for 9 and 10.  In the meantime,
        // use generic Java GUI for all platforms
//        if (isMac && versionNum <= 8) {
//            System.setProperty("apple.awt.fileDialogForDirectories", "true");
//            System.setProperty("apple.laf.useScreenMenuBar", "true");  
//        }
//      
        if (args == null || args.length == 0 ||
            (args.length == 1 && args[0].startsWith("-psn_"))) {  
            // when run from Sequescan.app, there is always 1 argument
            // starting with -psn_ which looks like process id
            
            final String[] clArgs = args;
  
            try {
//                if (isMac) {
//                    SwingUtilities.invokeLater(() -> {
//                        SequescanMacGUI topLevel =
//                            new SequescanMacGUI(clArgs);
//                        topLevel.setVisible(true);
//                        MessageManager.setGuiFlag(true,topLevel);
//                    });
//                }
//                else {
                      SwingUtilities.invokeLater(() -> {
                          SequescanGUI topLevel =
                              new SequescanGUI(clArgs);
                          topLevel.setVisible(true);
                          MessageManager.setGuiFlag(true,topLevel);
                      });
//                }

 
            } catch (Exception ex) {
                final String msg = "os.name="+SystemUtils.OS_NAME+": creating generic SequescanGUI";
                SwingUtilities.invokeLater(() -> {
                    SequescanGUI topLevel = 
                        new SequescanGUI(clArgs);
                    topLevel.setVisible(true);
                    MessageManager.publish(msg, topLevel);
                });              
            }
        }
        else {
            // command line execution
            MessageManager.setGuiFlag(false,null);
            if (isMac)
                System.setProperty("java.awt.headless","true");  
            SequescanCL scan = SequescanCLFactory.getSequescanTopLevel(args);
            if (scan == null)
                System.exit(1);
            boolean okay = scan.runMode();  
            if (!okay) {
                System.exit(2);
            }
            else 
                System.exit(0);
        }
    }
}
