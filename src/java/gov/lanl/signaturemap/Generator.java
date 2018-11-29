/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.signaturemap;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.tree.TreeManager;
import gov.lanl.sequtils.log.LogManager;
import gov.lanl.sequtils.util.FileUtilities;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class Generator implements GeneratorConstants {
  
    protected GeneratorProperties sigProps;
    protected long sigDirMapCnt = 0;
    
    public Generator(GeneratorProperties props) {
        sigProps = props;
    }
    
    public boolean execute() {
        
        // copied and modified from main to be called from other classes
        long millis = System.currentTimeMillis()/1000000;
        String outputDirStr = sigProps.getProperty(OUTPUT_DIR);
        File outputDir = sigProps.getExistingDir(OUTPUT_DIR);
        if (outputDir == null) {
            outputDir = new File(outputDirStr);
            boolean okay = outputDir.mkdirs();
            if (!okay) {
                LogManager.publish("Could not create output directory "+outputDirStr,
                    this, Level.ERROR);
                return false;
            }
        }
  
        LogManager.publish("Output will go to directory "+ outputDirStr, this, Level.INFO);
//        String filePath = sigProps.getFilePath();
//        File propFile = new File(filePath);
//        String propFileName = propFile.getName().replace('.','_'); 
//        File logFile = new File(outputDir,"log_"+propFileName+"_"+millis+".html");
//        LogManager.initModeLogger(null,logFile.getAbsolutePath());
        String action = sigProps.getAction();
        if (action == null)
            action = BOTH;
        
        File inputDir = null;
        if (action.equals(BOTH) || action.equals(KMER_DIR_MAP)) {
            String inputDirStr = sigProps.getProperty(INPUT_DIR);
            inputDir = sigProps.getExistingDir(INPUT_DIR);
            if (inputDir == null) {
                System.out.println("Input directory "+inputDirStr+" does not exist");
                System.exit(1);
            }
        }

        
        boolean okay = runAction(inputDir, outputDir, action);
        
  
        if (okay) {
            if (action.equals(BOTH) || action.equals(KMER_DIR_MAP)) {
                long mapCnt = getSigDirMapCnt();    
                LogManager.publish(action + " completed: "+mapCnt+
                    " signature/directory records",this,Level.INFO);
                return true;
            }
            else
                return true;
        }
        else {
             LogManager.publish("Problem with action: "+action,this,Level.ERROR);
             return false;
        }
    }
    
    public boolean runAction(File inputDir, File outputDir, String action) {
        
        Map<String,Set<String>> sigDirMap;
        if (outputDir == null || !outputDir.isDirectory()) {
            LogManager.publish("outputDir is missing or does not exist", Level.ERROR);
            return false;
        }
        
        switch (action) {
            case BOTH:
            case KMER_DIR_MAP:
                {
                    sigDirMap = generateSigDirMap(inputDir);
                    if (sigDirMap == null) {
                        LogManager.publish("Problem generating SigDirMap",this,Level.ERROR);
                        return false;
                    }       
                    String sigDirMapFileStr = sigProps.getSigDirMapFileName();
                    if (sigDirMapFileStr != null) {
                        
                        File sigDirMapFile = new File(outputDir, sigDirMapFileStr);
                        if (sigDirMapFile.exists()) {
                            LogManager.publish(sigDirMapFile.getAbsolutePath() + " already exists; deleting...",
                                    this, Level.ERROR);
                            sigDirMapFile.delete();
                        }
                        boolean status = writeSigDirMap(sigDirMap, sigDirMapFile);
                        if (!status) {
                            LogManager.publish("Problem writing sigDirMap to "+sigDirMapFile.getAbsolutePath(),
                                    this, Level.ERROR);
                            return false;
                        }
                    }       break;
                }
            case KMER_NODE_MAP:
                {
                    String sigDirMapFileStr = sigProps.getSigDirMapFileName();
                    if (sigDirMapFileStr != null) {
                        File sigDirMapFile = new File(outputDir, sigDirMapFileStr);
                        if (sigDirMapFile.exists()) {
                            sigDirMap =  getSigDirMapFromFile(sigDirMapFile);
                            if (sigDirMap == null) {
                                LogManager.publish("getSigDirMapFromFile returned null",
                                        Level.ERROR);
                                return false;
                            }
                        }
                        else {
                            LogManager.publish(sigDirMapFile.getAbsolutePath() +
                                    " does not exist", Level.ERROR);
                            return false;
                        }
                    }
                    else {
                        LogManager.publish("Config file does not have SIG_DIR_MAP_FILE",
                                Level.ERROR);
                        return false;
                    }       break;
                }
            default:
                LogManager.publish("Unknown generator action: "+action,Level.ERROR);
                return false;
        }
        
        if (action.equals(BOTH) || action.equals(KMER_NODE_MAP)) {
            return mapSigsToTreeNodes(outputDir, sigDirMap);
        }
        else
            return true;
        
    }
   
    
    protected boolean writeSigDirMap(Map<String,Set<String>> map, File writeFile) {
        
        try {
            FileWriter fwriter = new FileWriter(writeFile);
            try (BufferedWriter writer = new BufferedWriter(fwriter)) {
                Set<String> sigs = map.keySet();
                Iterator<String> sigIter = sigs.iterator();
                while (sigIter.hasNext()) {
                    String sig = sigIter.next();
                    Set<String> dirSet = map.get(sig);
                    Iterator<String> dirIter = dirSet.iterator();
                    while (dirIter.hasNext()) {
                        String dir = dirIter.next();
                        writer.write(sig+"\t"+dir);
                        writer.newLine();
                    }
                    
                }
                writer.flush();
                writer.close();
            }
        } catch (IOException ex){
            LogManager.publish("Problem writing SigDirMap to "+
                writeFile.getAbsolutePath(),this,Level.ERROR);
            LogManager.publish(ex.getMessage(),this,Level.ERROR);
            return false;
        }
        
        return true;
    }
    
    protected Map<String,Set<String>> generateSigDirMap(File inputDir) {
        
        if (inputDir == null || !inputDir.exists()) {
            LogManager.publish("Input dir "+sigProps.getProperty(INPUT_DIR)+
                    " does not exist",this,Level.ERROR);
            return null;
        }
        File combinedSigDir = new File(inputDir,"signatures");
        if (!combinedSigDir.isDirectory()) {
            LogManager.publish("Combined signatures directory "+
               combinedSigDir.getAbsolutePath()+" does not exist",this,Level.ERROR);
            return null;
        }
        File combinedSigFile = new File(combinedSigDir,COMBINED_SIG_LIST);
        if (!combinedSigFile.exists()) {
            LogManager.publish("Combined signatures file "+
            combinedSigFile.getAbsolutePath()+" does not exist",this,Level.ERROR);
            return null;
        }
   
        Map<String,Set<String>> map = new TreeMap<>();
        long combinedSigNum = initMapFromCombinedSigFile(map,combinedSigFile);
        LogManager.publish("Map initialized with "+combinedSigNum+" signatures",this,Level.INFO);
        
        // iteratively process directories
        
        sigDirMapCnt = 0;
//        boolean recursiveFlag = sigProps.getRecursiveFlag();
//        LogManager.publish("RecursiveFlag is "+recursiveFlag);
//        long mapCnt = processSigDir(map, inputDir, sigDirMapCnt, recursiveFlag); 
        long mapCnt = processSigDir(map, inputDir, sigDirMapCnt); 
        sigDirMapCnt = mapCnt;
        return map;
        
    }
    
    public long getSigDirMapCnt() {
        return sigDirMapCnt;
    }
    
    @SuppressWarnings("InfiniteRecursion")
    protected long processSigDir(Map<String,Set<String>> map, File processDir, 
        long mapCnt) {
        
        return processSigDir(map,processDir,mapCnt,false);
    }
    
    // it turns out, recursive is not needed - but leave argument as
    // possibility just in case
    protected long processSigDir(Map<String,Set<String>> map, File processDir, 
        long mapCnt, boolean recursive) {
        
        long newMapCnt,totalMapCnt;
        totalMapCnt = mapCnt;
        File[] fileList = processDir.listFiles();
        for (File file : fileList) {
            String fileName = file.getName();
            if (!fileName.equals("logs") && file.isDirectory() && !fileName.equals(SIGNATURES_DIR) 
                    && !fileName.startsWith(".")) {
                newMapCnt = processLocalSigFile(map, file);
                if (newMapCnt < 0) {
                    LogManager.publish("Ignoring "+fileName, this, Level.INFO);
                }
                else {   
                    totalMapCnt = totalMapCnt + newMapCnt;
                    LogManager.publish(file.getName()+" processed; "+newMapCnt+" signatures added to map lists",this,Level.INFO);
                }
//                if (recursive) {
//                    totalMapCnt = processSigDir(map, file, totalMapCnt,true);
//                }
            }
            else
                LogManager.publish("Ignoring "+fileName,this,Level.INFO);
        }
        return totalMapCnt;
    }
    
    protected long processLocalSigFile(Map<String,Set<String>> map, File processDir) {
        
        File localSigFile = new File(processDir,LOCAL_SIG_LIST);
        if (!localSigFile.exists()) {
            LogManager.publish(localSigFile.getAbsolutePath()+" does not exist",
                this,Level.ERROR);
            return -1;
        }
        BufferedReader reader = openFile(localSigFile);
        String line;
        String processDirName = processDir.getName();
        int sig_dir_pair_cnt = 0;
        try {
            line = reader.readLine();
            if (line == null) {
                LogManager.publish(localSigFile.getAbsolutePath()+" is empty",
                this,Level.WARN);
                return sig_dir_pair_cnt;
            }
            else if (!line.startsWith("signature")) {
                LogManager.publish(localSigFile.getAbsolutePath()+
                    " does not have correct headerline",this,Level.ERROR);
                return sig_dir_pair_cnt;
            }
            else
                line = reader.readLine();
            while (line != null) {
                String[] tokens = line.split("\t");
                String sig = tokens[0];
                Set<String> sigSet = map.get(sig);
                if (sigSet == null) {
                    LogManager.publish("There is no set for signature "+sig+" in directory"+processDirName);
                    line = reader.readLine();
                    continue;
                }
                boolean notInSetAlready = sigSet.add(processDirName);
                if (notInSetAlready)
                    sig_dir_pair_cnt++;
                line = reader.readLine();
            }
            
         } catch (IOException ex) {
            LogManager.publish("Problem reading next record: "+
                ex.getMessage(), null, Level.ERROR); 
         }
        
        boolean status = closeFile(reader);       
        return sig_dir_pair_cnt;
    }
    
    protected long initMapFromCombinedSigFile(Map<String,Set<String>> map, File sigFile) {
        
        BufferedReader reader = openFile(sigFile);
        int sig_cnt = 0;
        String line;
        
        try {
            line = reader.readLine();
            if (line == null) {
                LogManager.publish(sigFile.getAbsolutePath()+" is empty",
                this,Level.WARN);
                return 0;
            }
            else if (!line.contains("intersection")) {
                LogManager.publish(sigFile.getAbsolutePath()+
                    " does not have correct header",this,Level.ERROR);
                return 0;
            }
            else
                line = reader.readLine();
            while (line != null) {
                String[] tokens = line.split("\t");
                String sig = tokens[0];
                Set<String> emptyDirSet = new TreeSet<>();
                map.put(sig, emptyDirSet);
                sig_cnt++;
                line = reader.readLine();
            }
            
         } catch (IOException ex) {
            LogManager.publish("Problem reading next record: "+
                ex.getMessage(), null, Level.ERROR); 
         }
        
        boolean status = closeFile(reader);       
        return sig_cnt;

    }
    
    protected Map<String,Set<String>> getSigDirMapFromFile(File mapFile) {
        
        String mapFileStr = mapFile.getAbsolutePath();
        
        Map<String,Set<String>> map = new TreeMap<>();
        if (!mapFile.exists()) {
            LogManager.publish(mapFileStr+" does not exist", Level.ERROR);
            return null;
        }
        
        BufferedReader reader = openFile(mapFile);
        String line;
        int lineCnt = 0;

        try {
            
            line = reader.readLine();
            if (line == null) {
                LogManager.publish(mapFile.getAbsolutePath()+" is empty",
                this,Level.ERROR);
                return null;
            }
            while (line != null) {
                lineCnt++;
                String[] tokens = line.split("\t");
                String sig = tokens[0];
                String dirName = tokens[1];
                Set<String> sigSet = map.get(sig);
                if (sigSet == null) {
                    sigSet = new TreeSet<>();
                    map.put(sig, sigSet);
                }
                sigSet.add(dirName);
                line = reader.readLine();
            }
            
         } catch (IOException ex) {
            LogManager.publish("Problem reading next record: "+
                ex.getMessage(), null, Level.ERROR); 
         }
        
        LogManager.publish(lineCnt+" lines read into map from "+mapFileStr, Level.INFO);
        boolean status = closeFile(reader);         
        return map;
    }
    
    protected boolean mapSigsToTreeNodes(File outputDir, Map<String,Set<String>> sigDirMap) {  
        
        String sigNodeMapFileStr = sigProps.getSigNodeMapFileName();
        File sigNodeMapFile = new File(outputDir, sigNodeMapFileStr);
        
        if (sigNodeMapFile.exists()) {
            LogManager.publish(sigNodeMapFile.getAbsolutePath()+" already exists - deleting",
                Level.INFO);
            boolean okay = sigNodeMapFile.delete();
            if (!okay) {
                LogManager.publish("Could not delete "+sigNodeMapFileStr, Level.ERROR);
                return false;
            } 
        }
        
        File treeFile = sigProps.getPhyXml();
        String treeFileStr = sigProps.getProperty(SIG_PHYXML_FILE);
        if (treeFile == null) {
            LogManager.publish("Phyloxml file: "+treeFileStr+
              " does not exist", Level.ERROR);
            return false;
        }
        TreeManager treeMgr = new TreeManager(treeFile.getName());
        boolean okay = treeMgr.readTreeFromFile(treeFile.getAbsolutePath());
        if (!okay) {
            LogManager.publish("Could not read tree from "+treeFileStr, Level.ERROR);
            return false;
        }
        
        Map<String,List<Short>> pathMap = treeMgr.getLeafPathMap();
        LogManager.publish("Path Map has "+pathMap.size()+" entries",this, Level.INFO);
        
        try {
            FileWriter fwriter = new FileWriter(sigNodeMapFile);
            try (BufferedWriter writer = new BufferedWriter(fwriter)) {
                Set<String> kmers = sigDirMap.keySet();
                Iterator<String> kiter = kmers.iterator();
                while (kiter.hasNext()) {
                    String kmer = kiter.next();
                    Set<String> dirSet = sigDirMap.get(kmer);
                    Short nodeAssignment = assignNodeToKmer(this, kmer,dirSet,
                        pathMap);
                    if (nodeAssignment == null) {
                        LogManager.publish("No node assignment for "+kmer, this, Level.INFO);
                    }
                    else {
                        writer.append(kmer);
                        writer.append("\t");
                        writer.append(Short.toString(nodeAssignment));
                        writer.newLine();
                    }
                }
                writer.flush();
                writer.close();
            }
        } catch (IOException ex){
            LogManager.publish("Problem writing SigNodeMap to "+
                sigNodeMapFileStr,this,Level.ERROR);
            LogManager.publish(ex.getMessage(),this,Level.ERROR);
            return false;
        }
      
        return true;
    }
    
    
    // copied from CombinedReport - need to reverse engineer for assigning tree
    // node to kmer based on directory names associated with kmer and their place
    // on tree;  this method will be called by mapSigsToTreeNodes...
    public static Short assignNodeToKmer(Object callingObj, String kmer, Set<String> dirSet, Map<String, List<Short>> pathMap) {
        
        Short lowestSharedNode = null;
        String dirName;
        List<List<Short>> pathList = new ArrayList<>();
        
        Iterator<String> dirIter = dirSet.iterator();
        while (dirIter.hasNext()) {
            dirName = dirIter.next();
            List<Short> path = pathMap.get(dirName);
            if (path == null) {
                LogManager.publish(kmer+": "+dirName+" does not have path", callingObj, Level.ERROR);
            }
            else
                pathList.add(path);
        }
               
        // cycle through path arrays
        if (pathList.size() < 2) {
            LogManager.publish(kmer+" - there are <2 nodes with paths",callingObj, Level.INFO);
            return null;
        }
        Iterator<List<Short>> pathIter = pathList.iterator();
        List<Short> currentSharedPath = null;
        List<Short> newSharedPath;
        while (pathIter.hasNext()) {
            List<Short> nextPath = pathIter.next();
            if (currentSharedPath == null)
                currentSharedPath = nextPath;
            else {
                newSharedPath = new ArrayList<>();
                int srchLen = Math.min(currentSharedPath.size(),nextPath.size());
                for (int i=0; i<srchLen; i++) {
                    Short elem = currentSharedPath.get(i);
                    if (elem.equals(nextPath.get(i)))
                        newSharedPath.add(elem);
                    else
                        break;                           
                }
                currentSharedPath = newSharedPath;
            }          
        } 
        if (currentSharedPath == null)
            return null;
        else
            return currentSharedPath.get(currentSharedPath.size()-1);

    }
 
    
    public BufferedReader openFile(File file) {
        
        BufferedReader reader;
        
        try {
            FileReader freader = new FileReader(file);
            reader = new BufferedReader(freader);
         
        } catch (IOException ex) {
            LogManager.publish("Problem opening file "+file,this,Level.ERROR);
            return null;          
        }
        
        return reader;
            
    }
    
//    public BufferedWriter openFileForWrite
    
   
    public boolean closeFile(Reader reader) {
        if (reader == null) {
            LogManager.publish("Cannot close null reader",this,Level.ERROR);
            return false;
        }
        else {
            try {
                reader.close();
            } catch (IOException ex) {
                LogManager.publish("Problem closing reader: "+ex.getMessage(),this,Level.ERROR);                 
               return false;
            }
            return true;
        }
    }
    
    public void writeSigPropsToLog() {
        
        Set<String> propKeys = sigProps.getKeys();
        Iterator<String>iter = propKeys.iterator();
        LogManager.publish("Running signaturemap.Generator using "+sigProps.getFilePath(),
            this, Level.INFO);
        while (iter.hasNext()) {
            String key = iter.next();
            String value = sigProps.getProperty(key);
            LogManager.publish(key+" = "+value,this,Level.INFO);
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        File propsFile;
        if (args.length < 1) {
            propsFile = FileUtilities.selectLocalFile("Select File with Signature Generator Run Properties");
            if (propsFile == null) {
                System.out.println("No run properties file");
                System.exit(1);
            }
        }
        else 
            propsFile = new File(args[0]);
        
        String propsPath = propsFile.getAbsolutePath();
        
        if (!propsFile.exists()) {
            System.out.println(propsPath+" does not exist");
            System.exit(1);
        }
        
            
        GeneratorProperties props = new GeneratorProperties();
        boolean okay = props.loadPropertiesFromFile(propsPath);
        if (!okay) {
            System.out.println("Problem loading signature generator properties from "+propsPath);
            System.exit(1);
        }
        Generator sigGen = new Generator(props);
        long millis = System.currentTimeMillis()/1000000;
        String outputDirStr = props.getProperty(OUTPUT_DIR);
        File outputDir = props.getExistingDir(OUTPUT_DIR);
        if (outputDir == null) {
            outputDir = new File(outputDirStr);
            okay = outputDir.mkdirs();
            if (!okay) {
                System.out.println("Could not create output directory "+outputDirStr);
                System.exit(1);
            }
        }
  
        System.out.println("Output will go to directory "+ outputDirStr);
        String propFileName = propsFile.getName().replace('.','_'); 
        File logFile = new File(outputDir,"log_"+propFileName+"_"+millis+".html");
        LogManager.initModeLogger(null,logFile.getAbsolutePath());
        String action = props.getAction();
        if (action == null)
            action = BOTH;
        
        File inputDir = null;
        if (action.equals(BOTH) || action.equals(KMER_DIR_MAP)) {
            String inputDirStr = props.getProperty(INPUT_DIR);
            inputDir = props.getExistingDir(INPUT_DIR);
            if (inputDir == null) {
                System.out.println("Input directory "+inputDirStr+" does not exist");
                System.exit(1);
            }
        }

        
        okay = sigGen.runAction(inputDir, outputDir, action);
        
  
        if (okay) {
            if (action.equals(BOTH) || action.equals(KMER_DIR_MAP)) {
                long mapCnt = sigGen.getSigDirMapCnt();    
                LogManager.publish(action + " completed: "+mapCnt+
                    " signature/directory records",sigGen,Level.INFO);
                System.exit(0);
            }
            else
                System.exit(0);
        }
        else {
             LogManager.publish("Problem with action: "+action,sigGen,Level.ERROR);
             System.exit(2);
        }
        
    }
    
}
