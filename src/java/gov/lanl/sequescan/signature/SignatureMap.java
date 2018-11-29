/*
 * Copied current version of CombinedMapOrig in attempt to make a "generic" abstract
 * class whose children
 * can store map data in multiple formats
 * Note:  This is not the same SignatureMap from a much older version of sequescan
 *
 * JC 2018 May 30:  going forward, new data modules will no longer use numeric
 * hash of kmers;  leaving this in code for the moment in order to support
 * older data modules which may be out in the wild
 */
package gov.lanl.sequescan.signature;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.SignatureConstants;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.sequence.FastaFileReader;
import gov.lanl.sequtils.sequence.SequencingRecord;
import gov.lanl.sequtils.util.FileObj;
import gov.lanl.sequtils.util.StringOps;
import gov.lanl.sequtils.util.MurmurHash;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author jcohn
 */
abstract public class SignatureMap<K,V> extends MessageManager implements SignatureConstants {
    
    // hex file constants
    public static final String BOTH = ".both";
//    public static final String HEX = HEX_SIG_EXT;
//    public static final String RAW = RAW_SIG_EXT;
    public static final String TAB = "\t";
    
    // hex file output types
    public static final int HEX_BOTH = 0;
    public static final int HEX_PHYLO = 1;
    public static final int HEX_FUNC = 2;
    
    
    // delimiters
    public static final String FIELD_DELIM = "\t";
    public static final String LINE_DELIM = "\n";
    public static final String LIST_DELIM = ":";
    
    // jc (7/19/12):  constants for function detail (lookup) indices
    // these fixed indices are needed for current code
    // but will be replaced with
    // a few required plus a variable number of optional
    // values determined by the function lookup
    // columns optionally included in a SignatureModule.
    // I already started to code the new version but need
    // to backtrack to support current code requirements.
    
    public static final int SHORT_NAME = 0;
    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;
    public static final int SUBSYSTEM = 3;
    
    // actions
    public static final String FUNC_FROM_FASTA = "func_from_fasta";
    public static final String WRITE_HEX_FILES = "write_hex_files";
    public static final String TEST_MODULE = "test_module";
    
    // other constants
    public static final short NO_VALUE = -1;
    
    // signature map data structure
   // Map<Object,V> map;
  
    // other instance variables
    protected SignatureModule dataModule;
    protected String functionSetName = null;
    protected Boolean hexFlag;
    protected Integer sigCountEstimate = null;
    
    
    // for creating map from signature files (for purposes of generating signature file for jar module
    // it might be necessary to change cntEstimate to Long
   // for creating map from signature files (for purposes of generating signature file for jar module
    public SignatureMap(Boolean hFlag, Integer cntEstimate) {
        dataModule = null;
        hexFlag = hFlag;
        sigCountEstimate = cntEstimate;
    }

   
    public SignatureMap(SignatureModule module) {
        dataModule = module;
        hexFlag = dataModule.getHexFlag();
    }
   

    
    public boolean getHexFlag() {
        return hexFlag;
    }
    
    public Integer getKmerSize() {
        if (dataModule == null)
            return null;
        else
            return dataModule.getKmerSize();
    }
   
//    public int getSignatureCount() {
//        return getSize();
//    }
//    
//    // needs to return Set<Object> because signature can be String or Short,
//    // depending upon application
//    public Set<Object> getSignatureSet() {
//        if (map == null)
//            return null;
//        else
//            return map.keySet();
//    }   

    public String getFunctionSetName() {
        return functionSetName;
    }
    
    public boolean hasFunctions() {
        return functionSetName != null && !functionSetName.equals("");
    }
    
    public boolean getFunctionSignatures(File fastaFileDir, File outputFile) {
        
        boolean okay = readPhyloMapFromModule();
        if(!okay)
            return false;
        if (functionSetName != null) {
            logger.error("Functions have already been added to map from "+
                functionSetName+" in module "+dataModule.getModulePath());
            return false;
                
        }
        int filesRead = addFunctionsFromFastaFiles(fastaFileDir);
        if (filesRead < 1)
            return false;
        return writeFunctionsToTextFile(outputFile);
    }
    
//    public boolean readPhyloMapFromSignatureFile(String filePath) {
//        
//        if (map != null && !map.isEmpty()) {
//            logger.info("Map is already populated with phylo data");
//            return true;
//        }
//        
//        
//        String msg = "Start reading phylo map from "+
//            filePath+" with input hexFlag "+hexFlag;
//  //      publish(this,msg,Level.INFO);
//        logger.info(msg);
////        System.out.println(msg);
//        
//        if (hexFlag == null) {
//            msg = "Hex Flag is null";
// //           publish(this,msg,Level.ERROR);
//            logger.error(msg);
//            return false;
//        }
//       
//        
//        int cnt;
//        
//        initMap();
//       
//        try {
//            FileReader freader = new FileReader(filePath);
//            BufferedReader in = new BufferedReader(freader);
//    
//  
//            in.readLine();  // header
//            String line = in.readLine();
//            cnt = 0;
//            while (line != null) {
//       //         StringTokenizer stok = new StringTokenizer(line,FIELD_DELIM);
//                String[] tokenArr = StringOps.getTokens(line, FIELD_DELIM);
//                if (tokenArr.length != 5) {
//                    msg = "Line does not have 5 tokens: "+line;
// //                   publish(this,msg,Level.ERROR);
//                    logger.error(msg);
//                    map = null;
//                    return false;
//                }
//                String signature = tokenArr[1];
//                Short node = StringOps.getShort(tokenArr[2]);
//                if (node == null) {
// //                   publish(this,"Problem parsing node field in line: "+line,Level.ERROR);
//                    logger.error("Problem parsing node field in line: "+
//                        line);
//                    return false;
//                }
//                
//                V list = getNewValueListWithNode(node);
//
//                Object signatureObj;
//                if (Objects.equals(hexFlag, Boolean.TRUE)) {
//                    signatureObj = fromHexString(signature);
//                }
//                else
//                    signatureObj = signature;
// 
//                map.put(signatureObj,list);
//                cnt++;
//
//                line = in.readLine();
//            }
//            
//            
//            
//        } catch (IOException ex) {
//            
//            msg = "Problem reading signatures from "+
//                filePath+": "+ex.getMessage();
// //           publish(this,msg,Level.ERROR);
//            logger.error(msg);
////            System.out.println(msg);
//            return false;
//        }
//        
//        logger.info(cnt+" signatures read with node assignment");
////        logger.info(map.size()+" elements in map");
//
//        return true;
//    
//    }
//    
//    public boolean readPhyloMapFromModule() {
//        
//        if (map != null && !map.isEmpty()) {
//            logger.info("Map is already populated with phylo data");
//            return true;
//        }
//        
//        
//        String msg = "Start reading phylo map from "+
//            dataModule.getModulePath();      
//        logger.info(msg);
//        
//        if (hexFlag == null) {
//            logger.error("Hex Flag is missing from data module "+
//                dataModule.getModulePath());
//            return false;
//        }
//        
//        int cnt;
//        
//        initMap();
//       
//        try {
//            BufferedReader phyloStream = dataModule.getSignatureReader();
//            if (phyloStream == null) 
//                return false;
//  
//            String line = phyloStream.readLine();
//            cnt = 0;
//            while (line != null) {
//                StringTokenizer stok = new StringTokenizer(line,FIELD_DELIM);
//                if (stok.countTokens() != 2) {
//                    logger.error("Line does not have 2 tokens: "+line);
//                    map = null;
//                    return false;
//                }
//                String signature = stok.nextToken();
//                Short node = StringOps.getShort(stok.nextToken());
//                if (node == null) {
//                    logger.error("Problem parsing node field in line: "+
//                        line);
//                    return false;
//                }
//                
//                V list = getNewValueListWithNode(node);
//
//                Object signatureObj;
//                if (Objects.equals(hexFlag, Boolean.TRUE)) {
//                    signatureObj = fromHexString(signature);
//                }
//                else
//                    signatureObj = signature;
// 
//                map.put(signatureObj,list);
//                cnt++;
//
//                line = phyloStream.readLine();
//            }
//            
//            
//            
//        } catch (IOException ex) {
//            logger.error("Problem reading signatures from "+
//                dataModule.getModulePath()+": "+ex.getMessage());
//            return false;
//        }
//        
//        logger.info(cnt+" signatures read with node assignment");
////        logger.info(map.size()+" elements in map");
//        
////        // Get current size of heap in bytes
////        long heapTotalSize = Runtime.getRuntime().totalMemory(); 
////
////        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
////        // Any attempt will result in an OutOfMemoryException.
////        long heapMaxSize = Runtime.getRuntime().maxMemory();
////
////         // Get amount of free memory within the heap in bytes. This size will increase 
////        // after garbage collection and decrease as new objects are created.
////        long heapFreeSize = Runtime.getRuntime().freeMemory(); 
////        long heapUsedSize = heapTotalSize - heapFreeSize;
////        logger.info("totalHeap = "+heapTotalSize+" ; maxHeap = "+heapMaxSize+" heapUsedSize = "+heapUsedSize+" ; heapFreeSize = "+heapFreeSize);
//        return true;
//    }
    
    public boolean addFunctionsFromModule() {
        
        if (functionSetName != null) {
            logger.info("Functions have already been added from functionSetName="+
                functionSetName);
            return true;
        }
        
        else if (dataModule == null) {
            logger.error("Missing SignatureModule");
            return false;
        }
        
        else if (dataModule.getSelectedFunctionSet() == null) {            
//            logger.info("FunctionSetName is null");
            return false;
        }
   
        logger.info("Start adding functions from functionSetName="+
            dataModule.getSelectedFunctionSet()+" in module "+dataModule.getModulePath());    
        
        int cnt, fcnt;
       
        try {
            BufferedReader in = dataModule.getFunctionReader();
            if (in == null) {
                logger.warn("Could not get reader for functionSetName "+
                    dataModule.getSelectedFunctionSet());
                functionSetName = null;
                return false;
            }
            String line = in.readLine();
            cnt = 0;
            while (line != null) {
                cnt++;
                StringTokenizer stok = new StringTokenizer(line,FIELD_DELIM);
                if (stok.countTokens() != 2) {
                    logger.error("Line does not have 2 tokens: "+line);
                    return false;
                }
                String signatureStr = stok.nextToken();
                String functionStr = stok.nextToken();  
                String[] functionTokens = StringOps.getTokens(functionStr,LIST_DELIM);
//                Object signatureObj;
//                if (Objects.equals(hexFlag, Boolean.TRUE)) 
//                    signatureObj = fromHexString(signature);
//                else
//                    signatureObj = signature;
                K signatureObj = getSignatureObj(signatureStr);
       
                String errorMsg = addFunctionsToList(signatureObj,functionTokens);
                if (errorMsg != null) {
                    logger.error(errorMsg +": \n"+line);
                    return false;
                }
                line = in.readLine();
            }
            
            
        } catch (IOException ex) {
            logger.error("Problem reading function set "+
                functionSetName+" from "+
                dataModule.getModulePath()+": "+ex.getMessage());
            return false;
        }
        
        logger.info(cnt+" signatures read with function assignment");
        functionSetName = dataModule.getSelectedFunctionSet();
        return true;
    
    }
    
    public int addFunctionsFromFastaFiles(File fastaFileDir) {
        return addFunctionsFromFastaFiles(fastaFileDir, true);
    }
    
    // return number of fasta files successfully read
    public int addFunctionsFromFastaFiles(File fastaFileDir,
        boolean fileNamesHavePrefix) {
       
        if (!fastaFileDir.isDirectory()) {
            logger.error(fastaFileDir.getAbsolutePath()+" is not a directory");
            return 0;
        }
        
        else {
            logger.info("Started adding functions from fasta file directory  "+
            fastaFileDir.getAbsolutePath());
        }
         
        File[] fileArr = fastaFileDir.listFiles();
        Set<K> phyloSignatures = getSignatureSet();  //map.keySet();
        for (File fileArr1 : fileArr) {
            String fastaFileName = fileArr1.getName(); 
            if (!fastaFileName.endsWith(".faa")) {
                logger.error(fastaFileName+" does not end in .faa;  ignoring");
                continue;
            }
            publish("Processing "+fastaFileName,this);
            String name = fastaFileName.substring(0,fastaFileName.length()-4);
            String indxStr;  
            if (fileNamesHavePrefix) {
                String[] tokens = StringOps.getTokens(name,"_");
                if (tokens.length != 2) {
                    logger.error(fastaFileName+"does not have correct format");
                    return 0;
                }
                else
                    indxStr = tokens[1];
            }
            else
                indxStr = name;
            Short functIndx = StringOps.getShort(indxStr);
            if (functIndx == null) {
                logger.error(indxStr+" is not a short in file name: "+
                        fastaFileName);
                return 0;
            }
            FastaFileReader reader = new FastaFileReader(fileArr1);
            boolean okay = reader.open();
            if (!okay) {
 //               map = null;
                setNullMap();
                return 0;
            }
            Integer kmerSizeInt = getKmerSize();
            if (kmerSizeInt == null) {
                logger.error("kmerSize is null");
                return 0;
            }
            int kmerSize = kmerSizeInt;
            if (kmerSize < 1) {
                logger.error("kmerSize is not valid: "+kmerSize);
                return 0;
            }
            SequencingRecord record = reader.nextRecord();      
            while (record != null) {
                String seq = record.getSequence();
                Set<K> kmerSet = generateKmerSet(seq,kmerSize);
                boolean status = kmerSet.retainAll(phyloSignatures);
                Iterator<K> matchIter = kmerSet.iterator();
                while (matchIter.hasNext()) {
                    K matchingKmer = matchIter.next();
                    addFunctionToKmer(matchingKmer, functIndx);  // functions only added if kmer is in map (from phylogeny)                  
                }
                record = reader.nextRecord();
            }
            reader.close();
        }
        
        logger.info("Finished adding functions from fasta file directory  "+
            fastaFileDir.getAbsolutePath());
        functionSetName = "fromFastaFile";
        return fileArr.length;
        
    }
    
 
    public Set<K> generateKmerSet(String sequence, int kmerSize) {
        
        // note:  kmers can be String or Long, depending upon map type
        Set<K> kmerSet = new HashSet<>(); 
        K signature;
        for (int i=0; i<=sequence.length()-kmerSize; i++) {
            String kmerStr = sequence.substring(i,i+kmerSize);
//            if (hexFlag)  // i.e. map from module has hex keys
//                signatureObj = getKmerHash(kmer);
//            else
//                signatureObj = kmer;
            signature = convertKmerStr(kmerStr);
            kmerSet.add(signature);
        }       
    
        return kmerSet;
    }
    
    // method name retained for backward compatibility
    public boolean writeFunctionsToTextFile(File outputFile) {
        return writeFunctionMapFile(outputFile);
    }
    
    public boolean writeFunctionMapFile(File outputFile) {
   
        K sigObj = null;
        
        try {
            FileWriter writer = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(writer);
            Set<K> sigSet = getSignatureSet();  // map.keySet();
            Iterator<K> sigIter = sigSet.iterator();
            int cnt = 0;
            int matchCnt = 0;
            while (sigIter.hasNext()) {
                cnt++;
                sigObj = sigIter.next();
                String sigStr;
                if (hexFlag) {
                    Long numValue = (Long) sigObj;
                    sigStr = toHexString(numValue);                   
                }
                else
                    sigStr = sigObj.toString();
                Set<Short> functions = getFunctions(sigObj);
                String functStr;
                if (functions != null && !functions.isEmpty()) {
                    matchCnt++;
                    Iterator<Short> fIter = functions.iterator();
                    functStr = fIter.next().toString();
                    while (fIter.hasNext()) {
                        functStr += LIST_DELIM + fIter.next().toString();
                    }
                    out.append(sigStr+FIELD_DELIM+functStr+LINE_DELIM);
                }
            }
            logger.info("sig count="+cnt+" sig with function match cnt="+matchCnt);
            out.close();
        } catch (IOException ex) {
            logger.error("Problem writing functions for "+sigObj.toString()+" to file "+
                outputFile.getAbsolutePath());
            return false;
        }
        return true;
    }
    
    public int displayMap(int maxEntryCount) {
   
        if (isMapNull())
            return 0;
        
        int cnt = 0;
        
        publish("Module source: "+dataModule.getModulePath(),this);
        publish("Module hex flag: "+dataModule.getHexFlag(),this);
        publish("Module kmer size: "+dataModule.getKmerSize(),this);
        publish("Map Entries: ",this);
        publish(" ",this);
        
        Set<K> sigSet = getSignatureSet(); 
        Iterator<K> sigIter = sigSet.iterator();
  
        while (sigIter.hasNext() && (maxEntryCount == 0 || cnt < maxEntryCount)) {

            cnt++;
            K sigObj = sigIter.next();
            Short node = getNode(sigObj);
            Set<Short> functions = getFunctions(sigObj);
            String functStr;
            if (functions != null && !functions.isEmpty()) {
                Iterator<Short> fIter = functions.iterator();
                functStr = fIter.next().toString();
                while (fIter.hasNext()) {
                    functStr += LIST_DELIM + fIter.next().toString();
                }                    
            }
            else
                functStr = "no_functions";
            if (Objects.equals(hexFlag, Boolean.FALSE))
                publish(sigObj.toString()+FIELD_DELIM+node.toString()+FIELD_DELIM+functStr,this);
            else {
                // kludge - assume sigObj is Long;  need to do this better
             //   Long longObj = (Long) sigObj;
                publish(sigObj.toString()+FIELD_DELIM+toHexString(sigObj)+
                    FIELD_DELIM+node.toString()+FIELD_DELIM+functStr,this);
            }
        }
        
        publish(" ",this);
        return getSize();

    }
    

    
    public static Map<Short,String[]> getFunctionDetailFromModule(SignatureModule module) {
        
        if (module == null) {
            logger.error("Null SignatureModule object");
            return null;
        }
        else if (module.getSelectedFunctionSet() == null) {
            logger.error("Function set name is null in module "+module.getModulePath());
            return null;
        }
        else if (module.getFunctionColumns() == null) {
            logger.warn("Column names for selected function set are null in module "+
                module.getModulePath()+"; function output will not include additional information (e.g. rollups).");
            return null;
        } 
            
        logger.info("Started reading function detail from module "+
            module.getModulePath());
        
        int cnt;
        Map<Short,String[]> functionInfo = new TreeMap<>();  
        String[][] detailColumnMatrix = module.getFunctionColumns();
        if (detailColumnMatrix == null) {
            logger.warn("Problem getting function column names for selected function set in module "+
                module.getModulePath()+"; function output will not include additional information (e.g. rollups).");
            return null;
        }
        int detailColumnCnt = detailColumnMatrix[0].length;
     
       
        try {
            BufferedReader in = module.getFunctionDetailReader();
            if (in == null) {
                logger.info("Detail Columns for "+module.getSelectedFunctionSet()+" do not exist in module "+
                    module.getModulePath());
                return null;
            }
            in.readLine();  // skip first line (which should include column names)
            String line = in.readLine();
            cnt = 0;
            while (line != null) {
                String[] tokens = StringOps.getTokens(line,FIELD_DELIM);
                if (tokens.length != detailColumnCnt) {
                    logger.error("Line does not have "+
                        detailColumnCnt + " tokens as expected: "+line);
                    return null;
                }
                Short findx = StringOps.getShort(tokens[0]);
                if (findx == null) {
                    logger.error("Problem parsing first field (function index) in line: "+
                        line);
                    return null;
                }
                String[] info = new String[detailColumnCnt-1];  
                for (int c = 0; c < info.length; c++) 
                    info[c] = tokens[c+1];

                functionInfo.put(findx,info);
                cnt++;
                line = in.readLine();
            }
            
            
            
        } catch (IOException ex) {
            logger.error("Problem reading "+module.getSelectedFunctionSet()+" from "+
                module.getModulePath()+": "+ex.getMessage());
            return null;
        }
        
        logger.info(cnt+" function lookup lines read");
        return functionInfo;
    }
    
    public static Map<Short,String[]> getNodeDetailFromModule(SignatureModule module) {
        
        if (module == null) {
            logger.error("Null SignatureModule object");
            return null;
        }
            
        logger.info("Started reading node detail from "+
            module.getModulePath());
        
        int cnt;
        Map<Short,String[]> nodeInfo = new TreeMap<>();  
        
       
        try {
            BufferedReader in = module.getNodeDetailReader();
            if (in == null) {
                logger.warn("Node detail columns do not exist in "+
                    module.getModulePath());
                return null;
                    
            }
            in.readLine();  // skip first line - headers
            String line = in.readLine();
            cnt = 0;
            while (line != null) {
                String[] tokens = StringOps.getTokens(line,FIELD_DELIM);
                if (tokens.length < 2) {
                    logger.error("Line does not have at least 2 tokens as expected: "+line);
                    return null;
                }
                Short nindx = StringOps.getShort(tokens[0]);
                if (nindx == null) {
                    logger.error("Problem parsing first field (node index) in line: "+
                        line);
                    return null;
                }
                
                String[] info = new String[tokens.length-1];  
                for (int c = 0; c < info.length; c++) 
                    info[c] = tokens[c+1];
     
                nodeInfo.put(nindx,info);
                cnt++;
                line = in.readLine();
            }
            
            
            
        } catch (IOException ex) {
            logger.error("Problem reading node detail from "+
                module.getModulePath()+": "+ex.getMessage());
            return null;
        }
        
        logger.info(cnt+" node detail lines read");
        return nodeInfo;
    }
    
    public File writePhyloMapFiles(String rootPath, boolean outputHexFlag) {
        
        if (Objects.equals(hexFlag, Boolean.TRUE)) {
            String msg = "Input hex flag of TRUE is not supported for writing";
            publish(msg,this,Level.ERROR);
            return null;
        }
        
        FileObj bothFile, nodeFile;
        List<String> nodeLines, bothLines;
        if (outputHexFlag) {
            bothFile = new FileObj(rootPath+BOTH);
            bothLines = new ArrayList<>();
            nodeFile = new FileObj(rootPath+HEX_SIG_EXT);
            nodeLines = new ArrayList<>();
        }
        else {
            bothFile = new FileObj(rootPath+BOTH);  //null;
            bothLines = new ArrayList<>();  // null;
            nodeFile = new FileObj(rootPath+RAW_SIG_EXT);
            nodeLines = new ArrayList<>();
        }
  
  
        String msg = "Start writing files with root name: "+
            rootPath+" and outputHexFlag = "+outputHexFlag;
        publish(msg,this,Level.INFO);
       
        int cnt = 0;
        Short nodeValue;
        String keyStr;
        String outputKeyValue;
        Set<Short> functionSet; 
        String funcStr = null;
        Set<K> keySet = getSignatureSet();  // map.keySet();
        Iterator<K> keyIter = keySet.iterator();
        while (keyIter.hasNext()) {
            K key = keyIter.next();
            if (!(key instanceof String)) {
                msg = key + " is not a String";
                publish(msg,this,Level.ERROR);
                return null;
            }
            cnt++;
     
            nodeValue = getNode(key);
            keyStr = key.toString();
            if (outputHexFlag) {
                long hash = getKmerHash(keyStr);
                String hexString = toHexString(hash);
                outputKeyValue = hexString;
            }
            else
                outputKeyValue = keyStr;
            String keyNodeStr = outputKeyValue+TAB+nodeValue.toString();
            nodeLines.add(keyNodeStr);
            if (outputHexFlag) {
                bothLines.add(keyStr+TAB+keyNodeStr);
            }
            
            if (cnt%1000 == 0) {
                
                nodeFile.appendLines(nodeLines);
                nodeLines.clear();
                if (outputHexFlag) {
                    bothFile.appendLines(bothLines);
                    bothLines.clear();
                }
            }
        }
        
        if (nodeLines.size() > 0) 
            nodeFile.appendLines(nodeLines);
        
        if (outputHexFlag && bothLines.size() > 0)
            bothFile.appendLines(bothLines);
   
        return nodeFile.getJavaFile();
    }
    
    // it might be necessary to change estSigCount to long? 
    public static File convertRawSignatureFile(String sigFilePath, String rootPath,
        boolean outputHexFlag, int estSigCount) { 
        
        // does similar conversion to using readPhyloMapFromSignatureFile and writePhyloMapFiles -
        // but does not need to create the intermediate Map object (which takes up lots of memory
        // and is unecessary for a direct conversion
        
        String msg;
         
            // kludge to pass source object from static method
        
        File sigFile = new File(sigFilePath);
        if (!sigFile.exists()) {
            msg = sigFilePath + " does not exist";          
            publish(msg,true,Level.ERROR);
            return null;
        }
        
        FileObj bothFile, nodeFile;
        List<String> nodeLines, bothLines;
        
        bothFile = new FileObj(rootPath+BOTH);
        bothLines = new ArrayList<>();
        nodeLines = new ArrayList<>();
        
        
        if (outputHexFlag) {
            nodeFile = new FileObj(rootPath+HEX_SIG_EXT);
        }
        else {
            nodeFile = new FileObj(rootPath+RAW_SIG_EXT);
        }
  
  
        msg = "Start converting "+sigFilePath+
            " with outputHexFlag = "+outputHexFlag;
        publish(msg,true,Level.INFO);
        
               int cnt;
       
        try {
            FileReader freader = new FileReader(sigFilePath);
            BufferedReader in = new BufferedReader(freader);
            String line = in.readLine();
            cnt = 0;
            
            // note:  works with output from Generator
            while (line != null) {
                String[] tokenArr = StringOps.getTokens(line, FIELD_DELIM);                   
                String signature = tokenArr[0];
                Short node = StringOps.getShort(tokenArr[1]);
    
                if (node == null) {
                    logger.error("Problem parsing node field in line: "+
                        line);
                    return null;
                }
                
                String outputKeyValue;
                if (outputHexFlag) {
                    long hash = getKmerHash(signature,estSigCount);
                    String hexString = toHexString(hash);
                    outputKeyValue = hexString;
                }
                else
                    outputKeyValue = signature;
                String keyNodeStr = outputKeyValue+TAB+node.toString();
                nodeLines.add(keyNodeStr);
                if (outputHexFlag) {
                    bothLines.add(signature+TAB+keyNodeStr);
                }

                if (cnt%1000 == 0) {

                    nodeFile.appendLines(nodeLines);
                    nodeLines.clear();
                    if (outputHexFlag) {
                        bothFile.appendLines(bothLines);
                        bothLines.clear();
                    }
                }
                
    
                cnt++;

                line = in.readLine();
            }
            
            
            
        } catch (IOException ex) {
            
            msg = "Problem converting signatures from "+
                sigFilePath+": "+ex.getMessage();
            publish(msg,true,Level.ERROR);
            return null;
        }
        
        if (nodeLines.size() > 0) 
            nodeFile.appendLines(nodeLines);
        
        if (outputHexFlag && bothLines.size() > 0)
            bothFile.appendLines(bothLines);
   
        if (estSigCount != cnt) {
            msg = "Actual sig count = "+cnt+"; sigCnt argument = "+estSigCount;
            publish(msg,true,Level.ERROR);
            nodeFile.getJavaFile().delete();
            if (bothFile.fileExists())
                bothFile.getJavaFile().delete();
            return null;
        }
       
        else {
            msg = cnt+" signatures converted";
            publish(msg,true,Level.INFO);         
            return nodeFile.getJavaFile();
        }
    }
    
    // deprecated - no longer supported but leave here for now ...
    public boolean genHexMapFiles(String hexRootName, int writeType) {
        
        if (hexFlag) {
            logger.error("hexFlag = "+hexFlag+"; cannot convert");
            return false;
        }
        
        FileObj bothFile, hexFile, funcHexFile;
        List<String> hexLines, bothLines, funcHexLines;
        
        
        if (writeType != HEX_FUNC) {
            bothFile = new FileObj(hexRootName+BOTH);
            hexFile = new FileObj(hexRootName+HEX_SIG_EXT);
            hexLines = new ArrayList<>();
            bothLines = new ArrayList<>();
        }
        else {
            bothFile = null;
            hexFile = null;
            hexLines = null;
            bothLines = null;
        }
        if (writeType != HEX_PHYLO) {
            if (functionSetName == null) {
                logger.error("WriteType = "+writeType+" and functionSetName is empty");
                return false;
            }
            funcHexFile = new FileObj(hexRootName+"."+functionSetName+HEX_SIG_EXT);
            funcHexLines = new ArrayList<>();
        }
        else {
            funcHexFile = null;
            funcHexLines = null;
        }
        
        logger.info("Start converting keys to hex files with root name: "+
            hexRootName);
        
        int cnt = 0;
        Short nodeValue = null;
        Set<Short> functionSet; 
        String funcStr = null;
        Set<K> keySet = getSignatureSet();  // map.keySet();
        Iterator<K> keyIter = keySet.iterator();
        while (keyIter.hasNext()) {
            K key = keyIter.next();
            if (!(key instanceof String)) {
                logger.error(key + " is not a String");
                return false;
            }
            cnt++;
            long hash = getKmerHash(key.toString());
            if (writeType != HEX_FUNC)
                nodeValue = getNode(key);
            if (writeType != HEX_PHYLO) {
                funcStr = null;
                functionSet = getFunctions(key);
                if (functionSet != null) {
                    Iterator<Short> functionIter = functionSet.iterator();
                    while (functionIter.hasNext()) {
                        Short function = functionIter.next();
                        if (funcStr == null)
                            funcStr = function.toString();
                        else
                            funcStr += (LIST_DELIM +function.toString());
                    }
                }
            }
            String hexString = toHexString(hash);
            String hexNodeStr = hexString+TAB+nodeValue;  // .toString();
            if (writeType != HEX_FUNC) {
                if (hexLines != null)
                    hexLines.add(hexNodeStr);
                if (bothLines != null)
                    bothLines.add(key+TAB+hexNodeStr);
            }
            if (writeType != HEX_PHYLO) {
                if (funcStr != null && funcHexLines != null)
                    funcHexLines.add(hexString+TAB+funcStr);
            }
            if (cnt%1000 == 0) {
                if (writeType != HEX_FUNC) {
                    if (hexFile != null && hexLines != null) {
                        hexFile.appendLines(hexLines);
                        hexLines.clear();
                    }
                    if (bothLines != null && bothFile != null) {
                        bothFile.appendLines(bothLines);
                        bothLines.clear();
                    }
                }
                if (writeType != HEX_PHYLO) {
                    if (funcHexFile != null && funcHexLines != null) {
                        funcHexFile.appendLines(funcHexLines);
                        funcHexLines.clear();
                    }
                }
            }
        }
        
        if (writeType != HEX_FUNC && hexLines != null && hexLines.size() > 0) {
            if (hexFile != null)
                hexFile.appendLines(hexLines);
            if (bothFile != null && bothLines != null)
                bothFile.appendLines(bothLines);
        }
        if (writeType != HEX_PHYLO && funcHexLines != null && funcHexLines.size() > 0) {
            if (funcHexFile != null)
                funcHexFile.appendLines(funcHexLines);
        }
        
        return true;
    }
    
    public long getKmerHash(String str) {
        return  getKmerHash(str, getSize());  //getKmerHash(str, map.size());
    }
    
    public static long getKmerHash(String str, int seed) {
        byte[] bytes = str.getBytes();
        return MurmurHash.hash64(bytes, bytes.length, seed);        
    }
    
    public static String toHexString(Object value) {  //(long value) {
        if (value instanceof Long) {
            return Long.toString((Long) value,16);
        }
        else
            return "NA";
    }
    
    public static Long fromHexString(String strValue) {
        return Long.valueOf(strValue, 16);
    }
    
    protected String addFunctionToKmer(K kmer, Short functionIndx) {
        Set<Short> currentFunctions = getFunctions(kmer);
        if (currentFunctions == null || !currentFunctions.contains(functionIndx)) {
            String[] functionTokens = new String[1];
            functionTokens[0] = functionIndx.toString();
            String errorMsg = addFunctionsToList(kmer, functionTokens);
            if (errorMsg != null) {
                return errorMsg;
            }
            else return null;
        }
        else
            return "Problem adding function index "+functionIndx.toString()+
                " to kmer "+kmer;
    }
    
//    protected V getNewValueListWithNode(Short node) {
//        if (node == null)
//            return null;
//        short[] list = new short[1];     
//        list[0] = node;
//        return list;
//    }
    
  
    
    // abstract methods
   
    abstract public Short getNode(K signature);
    abstract public Set<Short> getFunctions (K signature);
    abstract protected void initMap();
    abstract protected V getNewValueListWithNode(Short node);
    abstract protected String addFunctionsToList(K signatureObj, String[] functionTokens);  
    abstract public int getSize();
    abstract public int getSignatureCount();
    abstract public Set<K> getSignatureSet();
    abstract public K convertKmerStr(String kmer);
    //            if (hexFlag)  // i.e. map from module has hex keys
//                signatureObj = getKmerHash(kmer);
//            else
//                signatureObj = kmer;
    abstract public boolean readPhyloMapFromModule();
    abstract public boolean readPhyloMapFromSignatureFile(String filePath);
    abstract public void setNullMap();
    abstract public boolean isMapNull();
    abstract protected Short getMapValue(V values, int indx);
    abstract protected int getMapValuesLength(V values);
    abstract protected V getEmptyMapValues(int length);
    abstract protected void setMapValue(V values, int indx, Short val);
    abstract protected K getSignatureObj(String signatureStr);
    //                if (hexFlag == true) 
//                    signatureObj = fromHexString(signature);
//                }
//                else
//                    signatureObj = signature;
}
