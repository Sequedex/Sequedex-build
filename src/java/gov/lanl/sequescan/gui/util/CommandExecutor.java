/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequescan.gui.util;

import gov.lanl.sequescan.constants.UserConstants;
import gov.lanl.sequtils.util.FileObj;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jcohn
 */
public class CommandExecutor {
    
    public static boolean runOSCommand(Frame parent, ArrayList<String> cmdList, String dialogTitle, String tempPrefix,
        boolean errorOnly, boolean showStatus, boolean noDisplay) {
      
        File userDir = UserConstants.getUserDir();
        File outputFile = new File(userDir,tempPrefix+System.currentTimeMillis()); 
        ProcessBuilder pbuilder = new ProcessBuilder(cmdList);
        pbuilder.redirectErrorStream(true);
        pbuilder.redirectOutput(outputFile); 
//        Map<String,String> env = pbuilder.environment();
//        env.put("SEQUEDEX_SEES_STDOUT","true");   // keep python messages for sequedex scripts from popping up
        
        
        int status;
        
        try {
            Process p = pbuilder.start();
            status = p.waitFor();
            if (noDisplay)
                return true;
            if (errorOnly && status == 0) {
                if (!showStatus) {
                    outputFile.deleteOnExit();
                    return true;
                }
            }
            ArrayList<String> outLines = new ArrayList<>();
            if (showStatus || status > 0) {
                if (status > 0)
                    outLines.add("Return Status: "+status);
                else
                    outLines.add("Command completed successfully");
            } else {
                // do nothing
            }
            FileObj outObj = new FileObj(outputFile);
            Collection<String> lines = outObj.readLines();
            if (lines != null && lines.size() > 0) {
                Iterator<String> lineIter = lines.iterator();
                while (lineIter.hasNext()) {
                    outLines.add(lineIter.next());
                }
            }
    
            DisplayDialog dialog = new DisplayDialog(parent,false,outLines);
            dialog.setTitle(dialogTitle);
            dialog.setLineWrap(true);
            dialog.pack();
            dialog.setVisible(true);
            outputFile.deleteOnExit();
      
        } catch (IOException | InterruptedException ex) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(ex.getMessage());
            DisplayDialog dialog = new DisplayDialog(parent,false,lines);
            dialog.setTitle("IO Exception");
            dialog.setVisible(true) ;  
            return false;
        }
        
        return status == 0;
    }

    
}
