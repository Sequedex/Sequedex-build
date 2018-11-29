
package gov.lanl.sequtils.pfam;

import gov.lanl.sequtils.sequence.FastaFileReader;
import gov.lanl.sequtils.sequence.FastaFileWriter;
import gov.lanl.sequtils.sequence.SequencingRecord;
import gov.lanl.sequtils.util.FileObj;
import gov.lanl.sequtils.util.StringOps;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jcohn
 */
public class FastaSplitter {
    
    // constants
    public static final String TAB = "\t";
    public static final String HEADER_BEGIN = ">";
    public static final String FAA_EXT = ".faa";
    public static final String FAMILY_FILE_NAME = "pfam.detail";
    public static final String FAA_DEFAULT_PREFIX = "pi";
    
    // header indices
    public static final int FAMILY_ID = 0;
    public static final int FAMILY_VERSION = 1;
    public static final int FAMILY_NAME = 2;
    // info indices
    public static final int NAME = 0;
    public static final int FAA_FILE_NAME = 1;
    public static final int INDX = 2;
    
    // instance variables
    protected FastaFileReader reader;
    protected Map<String,String[]> familyMap;
    
    public FastaSplitter(String pfamFAFilePath) {
        reader = new FastaFileReader(pfamFAFilePath);   
    }
    
    public boolean splitFileIntoFamilies(String destDirPath) {
        return splitFileIntoFamilies(destDirPath,null);
    }
    public boolean splitFileIntoFamilies(String destDirPath, String prefix) {
        return splitFileIntoFamilies(destDirPath,prefix,60,5);
    }
    
    public boolean splitFileIntoFamilies(String destDirPath, String prefix, int lineLength, int maxDigits) {
        
        if (prefix == null)
            prefix = FAA_DEFAULT_PREFIX;
        int familyIndx = 0;
        
        familyMap = new LinkedHashMap<>();
        File destDir = new File(destDirPath);
        if (destDir.isDirectory()) {
            Date now = new Date();
            String timeStr = Long.toString(now.getTime());
            File newDir = new File(destDirPath+"."+timeStr);
            destDir.renameTo(newDir);
        }  
        destDir = new File(destDirPath);
        destDir.mkdirs();
        boolean okay = reader.open();
        if (!okay)
            return false;
        SequencingRecord record = reader.nextRecord();
        while (record != null) {
            // parse header into seq and familyInfo
            String seq = record.getSequence();
            String header = record.getHeader();
            String[] familyInfo = getFamilyInfoFromHeader(header);
            if (familyInfo == null) {
                System.out.println("Problem getting familyInfo from header: "+header);
                return false;
            }
            // add family info to familyMap
            String[] infoArr = familyMap.get(familyInfo[FAMILY_ID]);
            if (infoArr == null) {
                String faaFileName = prefix+"_"+StringOps.leftFill(Integer.toString(familyIndx),maxDigits,'0');
                infoArr = new String[3];
                infoArr[NAME] = familyInfo[FAMILY_NAME];
                infoArr[FAA_FILE_NAME] = faaFileName;
                infoArr[INDX] = Integer.toString(familyIndx++);
                familyMap.put(familyInfo[FAMILY_ID], infoArr);
                System.out.println("Adding family "+familyInfo[FAMILY_ID]+"; faa name = "+infoArr[FAA_FILE_NAME]+
                    "; descr = "+infoArr[NAME]+" index = "+infoArr[INDX]);
            }
            else {
                if (!infoArr[NAME].equals(familyInfo[FAMILY_NAME])) {
                    System.out.println(familyInfo[FAMILY_ID]+
                        " descriptions are not consistent: "+ infoArr[NAME] + "  " + familyInfo[FAMILY_NAME]);
                    return false;
                }
            }
  
            // write to family fa file
            File familyFile = new File(destDir,infoArr[FAA_FILE_NAME]+FAA_EXT);
            FileObj familyFileObj = new FileObj(familyFile);
            header = header.replace(';',' ');
            header = header.trim();
            okay = familyFileObj.appendLine(HEADER_BEGIN+header);
            if (!okay) {
                System.out.println("Problem writing header to "+familyFile.getAbsolutePath()+
                    ": "+header);
                return false;
            }
            Collection<String> seqLines = FastaFileWriter.chopSequence(seq, lineLength);
            okay = familyFileObj.appendLines(seqLines);
            if (!okay) {
                System.out.println("Problem writing sequence to "+familyFile.getAbsolutePath()+
                    " for header: "+header);
                return false;
            }
//            else {
//                System.out.println("Record for header "+header+" has been written to "+familyFile.getAbsolutePath());
//            }          
            record = reader.nextRecord();
        }
        okay = reader.close();
        if (!okay)
            return false;
        // write family map to destination directory
        File writeFile = new File(destDir,FAMILY_FILE_NAME);
        return writeFamilyMap(writeFile,maxDigits,prefix);
    }
    
    protected void displayFamilyInfo(String[] familyInfo) {
        for (String familyInfo1 : familyInfo) {
            System.out.println(familyInfo1);
        }
    }
    
    protected String[] getFamilyInfoFromHeader(String header) {        
        String[] tokens = StringOps.getTokens(header," ");
        String[] familyInfo = new String[3];
        if (tokens == null || tokens.length != 3) {
            System.out.println("PFAM header does not have 3 fields (delim=space): "+header);
            return null;
        }
        String familyStr = tokens[2];
        String[] familyTokens = StringOps.getTokens(familyStr, ";.");
        if (familyTokens.length != 3) {
            System.out.println("Third field of header does not have 3 fields (delim=;.): "+familyStr);
            return null;
        }
        familyInfo[FAMILY_ID]  = familyTokens[0];
        familyInfo[FAMILY_VERSION] = familyTokens[1];
        familyInfo[FAMILY_NAME] = familyTokens[2];
                
        return familyInfo;
    }
    
    public boolean writeFamilyMap(File writeFile, int maxDigits, String prefix) {
        
        FileObj writeObj = new FileObj(writeFile);
        writeObj.appendLine("indx"+TAB+"id"+TAB+"pfam_id"+TAB+"pfam_name");
        Iterator<String> mapKeyIter = familyMap.keySet().iterator();
        while (mapKeyIter.hasNext()) {
            String familyID = mapKeyIter.next();
            String[] infoArr = familyMap.get(familyID);
            writeObj.appendLine(infoArr[INDX]+TAB+infoArr[FAA_FILE_NAME]+TAB+familyID+TAB+infoArr[NAME]);
        }
        // next few lines have not been tested
        // purpose:  add extra line to detail file for NOMATCH, which is
        // required when adding functions to module
        // until this point - have been doing this manually
        String lastIndxStr = Integer.toString(familyMap.size());
        String noMatchIDStr = prefix+"_"+StringOps.leftFill(lastIndxStr,maxDigits,'0');
        writeObj.appendLine(lastIndxStr+TAB+noMatchIDStr+TAB+"NOMATCH"+TAB+"no_match");
        return true;
    }
    
    public static final void main(String[] args) {
        String pfamFAFilePath, destDirPath,prefix; 
        int maxDigits;
        
//        if (args.length < 4) {
//            pfamFAFilePath = "/Volumes/venus/pfam27/Pfam-A.fasta";
//            destDirPath = "/Volumes/venus/pfam27Split-2";  
//            prefix = "pi";
//            maxDigits = 5;
//        }
        // pfam 31 (released Mar 2017, downloaded Aug 2018)
        if (args.length < 4) {
            pfamFAFilePath = "/Volumes/moreblue2/pfam31/Pfam-A.fasta";
            destDirPath = "/Volumes/moreblue2/pfam31/pfam31Split";  
            prefix = "pm";
            maxDigits = 5;
        }
        else {
            pfamFAFilePath = args[0];
            destDirPath = args[1];
            prefix = args[2];
            Integer digitNum = StringOps.getInteger(args[3]);
            if (digitNum == null)
                maxDigits = 5;
            else
                maxDigits = digitNum;
        }
        
        FastaSplitter splitter = new FastaSplitter(pfamFAFilePath);
        System.out.println("done: "+splitter.splitFileIntoFamilies(destDirPath,prefix,60,maxDigits));
        System.exit(0);
    }
    
    
    
}
