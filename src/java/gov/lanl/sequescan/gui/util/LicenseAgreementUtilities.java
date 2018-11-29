package gov.lanl.sequescan.gui.util;

import gov.lanl.sequtils.log.MessageManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import gov.lanl.sequescan.constants.AppConstants;
import gov.lanl.sequtils.util.ConfigFile;

public class LicenseAgreementUtilities  {
    

    public static String getLicenseAgreementFilePath() {
        return ConfigFile.getDistribDir() + AppConstants.SEP + 
            "LICENSE.txt";
    }
    
    public static Collection<String> getLicenseAgreementLines() {
		
        boolean fatalError = false;
        
	File licenseFile = new File(getLicenseAgreementFilePath()); 
//        System.out.println("license agreement file: "+licenseFile.getAbsolutePath());
        if (!licenseFile.exists()) {
            MessageManager.publish("License agreement file "+licenseFile.getAbsolutePath()+" does not exist");
            return null;
        }
        else {
            Collection<String> lines = new ArrayList<>(100);
            
            try {
                BufferedReader in = new BufferedReader(new FileReader( licenseFile));
                String s; // = new String();  
                while((s = in.readLine()) != null) {  
                    lines.add(s);       
                }
            } catch (FileNotFoundException e) {
                String msg = "\nProblems reading Sequescan license agreement file - terminating program execution.";
                System.out.println(msg);
                System.out.println( e.getMessage());
                fatalError = true;
            }  catch (IOException e) {
                String msg = "\nProblems reading Sequescan license agreement file - terminating program execution.";
                System.out.println(msg);
                System.out.println( e.getMessage());
                fatalError = true;
            }  
		
            if( !fatalError){
                return lines;
            }
            else {           
                return null;
            }
        }
        
    }

}
