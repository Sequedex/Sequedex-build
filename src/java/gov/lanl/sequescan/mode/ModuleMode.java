/*
 * Need to generalize reading and using ModuleProperties and/or GeneratorProperties
 * or other future Properties classes based on BaseProperties, rather than
 * having separate methods 
 *
 */
package gov.lanl.sequescan.mode;

import ch.qos.logback.classic.Level;
import java.nio.file.Path;
import gov.lanl.sequescan.analysis.CombinedReport;
import gov.lanl.sequescan.constants.SignatureConstants;
import static gov.lanl.sequescan.constants.SignatureConstants.DETAIL_EXT;
import static gov.lanl.sequescan.constants.SignatureConstants.FUNCTIONS;
import static gov.lanl.sequescan.constants.SignatureConstants.FUNCT_DIR;
import static gov.lanl.sequescan.constants.SignatureConstants.NODES;
import static gov.lanl.sequescan.constants.SignatureConstants.PROPERTIES_EXT;
import static gov.lanl.sequescan.constants.SignatureConstants.SIG_DIR;
import gov.lanl.sequescan.constants.TreeConstants;
import gov.lanl.sequescan.signature.JarModule;
import gov.lanl.sequescan.signature.ModuleProperties;
import gov.lanl.sequescan.signature.SignatureMap;
import gov.lanl.sequescan.tree.TreeManager;
import static gov.lanl.sequtils.log.MessageManager.publish;
import gov.lanl.sequtils.util.FileUtilities;
import gov.lanl.sequtils.util.FileObj;
//import gov.lanl.sequtils.util.GetOpt;
import gnu.getopt.Getopt;
import gov.lanl.sequtils.util.StringOps;
import gov.lanl.signaturemap.Generator;
import gov.lanl.signaturemap.GeneratorConstants;
import gov.lanl.signaturemap.GeneratorProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jcohn
 */
public class ModuleMode extends Mode implements SignatureConstants, 
    TreeConstants, GeneratorConstants {
      
    
    public ModuleMode(String[] appArgs, boolean isInternal) {
        super(appArgs, isInternal);
    }

    @Override
    public void execute() {
        
        String msg;
        ModuleProperties moduleProps;
        GeneratorProperties genProps;
        
        
        switch (modeStr) {
            case CREATE_MODULE:
                moduleProps = getModuleProperties();
                if (moduleProps == null)
                    return;
                createModule(moduleProps);
                break;
//            case ADD_DETAIL:
//                addDetail();
//                break;
            case ADD_FUNCTION:
                moduleProps = getModuleProperties();
                if (moduleProps == null)
                    return;
                addFunctionSet(moduleProps);
                break;
            case ADD_FUNCTION_DETAIL:
                moduleProps = getModuleProperties();
                if (moduleProps == null)
                    return;
                addFunctionDetail(moduleProps);
                break;
            case KMER_MAP:
                genProps = getGeneratorProperties();
                kmerMap(genProps);
                break;
            default:
                // should not reach this since readParams should return false
                msg = modeStr + " has not been implemented yet";
                logger.error(msg);
                publish(msg,this);
                break;
        }
        
        msg = "End Sequescan Program (mode="+modeStr+"): " + new Date();
        publish(msg,this,Level.INFO);
       
    }
    
   
    protected boolean createModule(ModuleProperties moduleProps) {
        
        File moduleDir = createFramework(moduleProps);
        if (moduleDir == null)
            return false;
       
        boolean okay = createNodeFiles(moduleProps, moduleDir);
        if (!okay)
            return false;
        
        okay = createModuleJarFile(moduleDir);
        return okay;
        
    }
    
//    protected boolean addDetail() {
//        // Why is this empty?
//        return false;
//    }
    
    // need to test...
    protected boolean addFunctionDetail(ModuleProperties moduleProps) {
          
        String msg;
   
        // get scratch directory
        String scratchDirStr = moduleProps.getScratchDir();
        if (scratchDirStr == null) {
            msg = "SCRATCH_DIR param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        File scratchDir = new File(scratchDirStr);
        if (!scratchDir.exists()) {
            boolean okay = scratchDir.mkdirs();
            if (!okay) {
                msg = "Problem creating scratch dir "+scratchDirStr;
                publish(msg,this,Level.ERROR);
                return false;
            }
        }
      
        // get JarModule object
        String modulePath = moduleProps.getModulePath();
        if (modulePath == null) {
            msg = "MODULE_PATH param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        } 
        if (!modulePath.endsWith(".jar")) {
            msg = "MODULE_PATH "+modulePath+" does not have extension .jar";
            publish(msg,this,Level.ERROR);
            return false;
        }
        File moduleFile = new File(modulePath);
        if (!moduleFile.exists()) {
            msg = moduleFile.getAbsolutePath()+" does not exist";
            publish(msg,this,Level.ERROR);
            return false;
        }
        String moduleName = moduleFile.getName();
        String moduleNameCore = moduleName.substring(0,moduleName.length()-4);
        File updateScratchDir = new File(scratchDir,moduleNameCore + ".update");
        if (!updateScratchDir.exists())
            updateScratchDir.mkdirs();
        
        File scratchFunctDir = new File(updateScratchDir, FUNCT_DIR);
        if (!scratchFunctDir.exists());
            scratchFunctDir.mkdirs();
            
        String functionSetName = moduleProps.getFunctionsetName();
        if (functionSetName == null) {
            msg = "FUNCTIONSET_NAME param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        JarModule module = new JarModule(modulePath);
        
        Set<String> functionNames = module.getFunctionSetNames();
        if (!functionNames.contains(functionSetName)) {
            msg = "FunctionSet "+functionSetName+" does not exist - exiting";
            publish(msg,this,Level.WARN);
        }
       
        msg = "Adding/overwriting function detail for function set "+functionSetName+" to "+modulePath;      
        publish(msg,this,Level.INFO);            
        
        File funcDetailFile = new File(scratchFunctDir, functionSetName + DETAIL_EXT);
        if (funcDetailFile.exists())
            funcDetailFile.delete();
        
        File funcPropsFile = new File(scratchFunctDir, functionSetName + PROPERTIES_EXT);
        if (funcPropsFile.exists())
            funcDetailFile.delete();
        
        Integer functCnt = module.getFunctionProperties(functionSetName).getCount();
        
        // function array has number of functions (as determined by fasta files) + 1 (where last
        // number is reserved for reads with no match to any function
        File[] functionFiles = createFunctionFiles(moduleProps, scratchFunctDir, functionSetName, functCnt);
        if (functionFiles == null || functionFiles.length != 2)
            return false;

        
        // write new function files to jar file - overwriting existing files if they exist
        // note:  thus far no way to "delete" function files only overwrite
        
        // note:  I tried and failed to use a wild card (".*") rather
        // than a -C clause for each file
        String[] commandArr = new String[9];
        commandArr[0] = "jar";
        commandArr[1] = "uvf";
        commandArr[2] = modulePath;
        commandArr[3] = "-C";
        commandArr[4] = updateScratchDir.getAbsolutePath();
        commandArr[5] = FUNCTIONS + functionFiles[0].getName();  // function detail file
        commandArr[6] = "-C";
        commandArr[7] = updateScratchDir.getAbsolutePath();
        commandArr[8] = FUNCTIONS + functionFiles[1].getName();  // function prop file
    
        try {
            Process child = Runtime.getRuntime().exec(commandArr);
            int status = child.waitFor();
            msg = "Create jar file status: "+status;
            publish(msg,this,Level.INFO);
        } catch (IOException | InterruptedException ex) {
            msg = "Problem running os command to generate jar file: "+ex.getMessage();
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        msg = "Functionset "+functionSetName+" details have been added/overwritten for "+modulePath;
        publish(msg,this,Level.INFO);
        
        return true;
        
        
    }
    
    protected boolean addFunctionSet(ModuleProperties moduleProps) {
          
        String msg;
   
        // get scratch directory
        String scratchDirStr = moduleProps.getScratchDir();
        if (scratchDirStr == null) {
            msg = "SCRATCH_DIR param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        File scratchDir = new File(scratchDirStr);
        if (!scratchDir.exists()) {
            boolean okay = scratchDir.mkdirs();
            if (!okay) {
                msg = "Problem creating scratch dir "+scratchDirStr;
                publish(msg,this,Level.ERROR);
                return false;
            }
        }
      
        // get JarModule object
        String modulePath = moduleProps.getModulePath();
        if (modulePath == null) {
            msg = "MODULE_PATH param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        } 
        if (!modulePath.endsWith(".jar")) {
            msg = "MODULE_PATH "+modulePath+" does not have extension .jar";
            publish(msg,this,Level.ERROR);
            return false;
        }
        File moduleFile = new File(modulePath);
        if (!moduleFile.exists()) {
            msg = moduleFile.getAbsolutePath()+" does not exist";
            publish(msg,this,Level.ERROR);
            return false;
        }
        String moduleName = moduleFile.getName();
        String moduleNameCore = moduleName.substring(0,moduleName.length()-4);
        File updateScratchDir = new File(scratchDir,moduleNameCore + ".update");
        if (!updateScratchDir.exists())
            updateScratchDir.mkdirs();
       
        JarModule module = new JarModule(modulePath);
         
        // create function scratch directories and check if fasta dir exists
        
        File scratchFunctDir = new File(updateScratchDir, FUNCT_DIR);
        if (!scratchFunctDir.exists()) 
            scratchFunctDir.mkdirs();
            
        String functionSetName = moduleProps.getFunctionsetName();
        if (functionSetName == null) {
            msg = "FUNCTIONSET_NAME param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        Set<String> functionNames = module.getFunctionSetNames();
        if (functionNames.contains(functionSetName)) {
            msg = "FunctionSet "+functionSetName+" already exists - it will be overwritten";
            publish(msg,this,Level.WARN);
        }
        
        String fastaPath = moduleProps.getFastaPath();
        if (fastaPath == null) {
            msg = "FASTA_PATH param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        File fastaDir = new File(fastaPath);
        if (!fastaDir.exists()) {
            msg = "Fasta Directory " + fastaPath+" does not exists"; 
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        // add functions to signature map and export function map file 
        
        msg = "Adding functionset "+functionSetName+" to "+modulePath+" from fasta dir "+
            fastaPath;      
        publish(msg,this,Level.INFO);
        
        
        String extStr;
        Boolean moduleHexFlag = module.getHexFlag();
        if (moduleHexFlag)
            extStr = HEX_SIG_EXT;
        else
            extStr = RAW_SIG_EXT;
        File functMapFile = new File(scratchFunctDir, functionSetName + extStr);
        msg = "Reading signature map from module";
        publish(msg,this,Level.INFO);
        // create SignatureMapL object
        SignatureMap sigMap = CombinedReport.readMapFromModule(module,false, false);
        if (sigMap == null) {
            msg = "Problem reading phylo map from module "+modulePath;
            publish(msg,this,Level.ERROR);
            return false;
        }
        else {
            msg = "Signatures read into map: "+sigMap.getSignatureCount();
            publish(msg,this,Level.INFO);
        }
        msg = "Adding functions from fasta files";
        publish(msg,this,Level.INFO);
        int fastaFilesRead = sigMap.addFunctionsFromFastaFiles(fastaDir);
        if (fastaFilesRead < 1) {
            msg = "Problem reading fasta files - see log";
            publish(msg,this);
            return false;
        }
        Integer functCnt = fastaFilesRead;
   
        // note:  I need to write the writeFunctionMapFile method (currently does nothing)
        boolean okay = sigMap.writeFunctionMapFile(functMapFile);
        if (!okay)
            return false;
        
        // function array has number of functions (as determined by fasta files) + 1 (where last
        // number is reserved for reads with no match to any function
        File[] functionFiles = createFunctionFiles(moduleProps,scratchFunctDir, functionSetName, functCnt);
        if (functionFiles == null || functionFiles.length != 2)
            return false;

        
        // write new function files to jar file - overwriting existing files if they exist
        // note:  thus far no way to "delete" function files only overwrite
        
        // note:  I tried and failed to use a wild card (".*") rather
        // than a -C clause for each file
        String[] commandArr = new String[12];
        commandArr[0] = "jar";
        commandArr[1] = "uvf";
        commandArr[2] = modulePath;
        commandArr[3] = "-C";
        commandArr[4] = updateScratchDir.getAbsolutePath();
        commandArr[5] = FUNCTIONS + functMapFile.getName();
        commandArr[6] = "-C";
        commandArr[7] = updateScratchDir.getAbsolutePath();
        commandArr[8] = FUNCTIONS + functionFiles[0].getName();  // function detail file
        commandArr[9] = "-C";
        commandArr[10] = updateScratchDir.getAbsolutePath();
        commandArr[11] = FUNCTIONS + functionFiles[1].getName();  // function prop file
    
        try {
            Process child = Runtime.getRuntime().exec(commandArr);
            int status = child.waitFor();
            msg = "Create jar file status: "+status;
            publish(msg,this,Level.INFO);
        } catch (IOException | InterruptedException ex) {
            msg = "Problem running os command to generate jar file: "+ex.getMessage();
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        msg = "Functionset "+functionSetName+" has been added to "+modulePath;
        publish(msg,this,Level.INFO);
        
        return true;
        
    }
    
    protected boolean kmerMap(GeneratorProperties genProps) {
        Generator gen = new Generator(genProps);
        return gen.execute();
    }
      
    protected File createFramework(ModuleProperties moduleProps) {
        
        // create directory structure and data props file for new module in scratch dir
        String msg = "Creating framework, including new module directory and data properties file";
        publish(msg,this,Level.INFO);
        
        // scratch directory
        String scratchDirStr = moduleProps.getScratchDir();
        if (scratchDirStr == null) {
            msg = "SCRATCH_DIR param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return null;
        }
        File scratchDir = new File(scratchDirStr);
        if (!scratchDir.exists()) {
            boolean okay = scratchDir.mkdirs();
            if (!okay) {
                msg = "Problem creating scratch dir "+scratchDirStr;
                publish(msg,this,Level.ERROR);
                return null;
            }
        }
      
        // new module directory
        String moduleName = moduleProps.getModuleName();
        if (moduleName == null) {
            msg = "MODULE_NAME param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return null;
        }
 
        File moduleDir = new File(scratchDir, moduleName);
        if (moduleDir.exists()) {
            msg = "Module directory "+moduleDir.getAbsolutePath()+" already exists";
            publish(msg,this,Level.ERROR);
            return null;
        }
        else {
            msg = "Creating module "+moduleName+" in directory "+scratchDir.getAbsolutePath();
            publish(msg,this,Level.INFO);       
        }
        boolean okay = moduleDir.mkdirs();
        if (!okay) {
            msg = "Problem making new module directory "+moduleDir.getAbsolutePath();
            publish(msg,this,Level.ERROR);
            return null;
        }
        
        // new module sub directories
        File sigDir = new File(moduleDir, SIG_DIR);
        okay = sigDir.mkdir();
        if (!okay) {
            msg = "Problem making signatures directory in "+moduleDir.getAbsolutePath();
            publish(msg,this,Level.ERROR);
            return null;
        }
        File functDir = new File(moduleDir, FUNCT_DIR);
               okay = functDir.mkdir();
        if (!okay) {
            msg = "Problem making functions directory in "+moduleDir.getAbsolutePath();
            publish(msg,this,Level.ERROR);
            return null;
        }     
        
        // data.prop file
        ModuleProperties dataProps = new ModuleProperties();
        dataProps.setVersion(DATA_VERSION_NUMBER);
        Boolean hexFlag = moduleProps.getHexFlag();
        if (hexFlag == null) {
            msg = "HEX_FLAG param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return null;
        }
        dataProps.setHexFlag(hexFlag);
        Integer kmerSize = moduleProps.getKmerSize();
        if (kmerSize == null) {
            msg = "KMER_SIZE param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return null;
        }
        dataProps.setKmerSize(kmerSize);
        String maxHeapStr = moduleProps.getMaxHeap();
        if (maxHeapStr != null)
            dataProps.setMaxHeap(maxHeapStr);
        else {
            msg = "MAX_HEAP param is missing from param file "+inputArg;
            publish(msg,this,Level.WARN);
        }
        File dataPropFile = new File(moduleDir, PROPERTIES);
        okay = dataProps.writePropertiesToFile(dataPropFile.getAbsolutePath());
        if (okay)
            return moduleDir;
        else
            return null;
    }
 
//    version found at least since 16 Oct 2017 ????
//    protected boolean createNodeFiles(File moduleDir) {
//        // why is this empty  16 Oct 2017???
//        return false;  
//    } 

    // version from May 19, 2014 (why was empty above?
    protected boolean createNodeFiles(ModuleProperties moduleProps, File moduleDir) {
        
        String msg = "Checking source and tree source";
        publish(msg,this,Level.INFO);
        
        String treeSrc = moduleProps.getTreeSource();
        if (treeSrc == null) {
            msg = "TREE_SOURCE param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        File treeSrcFile = new File(treeSrc);
        if (!treeSrcFile.exists()) {
            msg = "Tree file "+treeSrc+" does not exist";
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        String src = moduleProps.getSource();
        if (src == null) {
            msg = "SOURCE param is missing from param file "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            msg = "Signature source file "+src+" does not exist";
            publish(msg,this,Level.ERROR);
            return false;
        }
        
        Boolean hexFlag = moduleProps.getHexFlag(); 
        if (hexFlag == null) {
            msg = "HEX_FLAG param is missing from "+inputArg;
            publish(msg,this,Level.ERROR);
            return false;
        }
      
  //      msg = "Importing and modifying tree for module";
        msg = "Importing tree for module";
        publish(msg,this,Level.INFO);
        // jc 2018 Oct 01:  this has already been done in order to map 
        // kmers to nodes using aakbar output
//        Integer internalNodeCnt = createParentSetTreeFile(moduleDir, treeSrc);
        Integer internalNodeCnt = copyTreeFileToModuleDir(moduleDir, treeSrc);
        if (internalNodeCnt == null) 
            return false;
         
        msg = "Importing signatures into module with output hexFlag = "+hexFlag;
        publish(msg,this,Level.INFO);
        
        Integer actualSigCount = createSigFile(moduleProps, moduleDir, src, hexFlag);
        if (actualSigCount == null)
            return false;
        
        // add details, if any - otherwise create default detail file
        List<String> prettyNameList, varNameList;
        File nodeDetailFile = new File(moduleDir, NODES + DETAIL_EXT);
        String detailSrc = moduleProps.getDetailSource();
        prettyNameList= moduleProps.getPrettyNames();
        varNameList = moduleProps.getVarNames();
        // note:  currently I am not checking if pretty and var name lists make sense
        // with reference to detail src file - just take on faith
        boolean badDetailFlag = false;
        if (detailSrc != null && prettyNameList != null && varNameList != null) {
            File detailSrcFile = new File(detailSrc);
            if (!detailSrcFile.exists()) {              
                badDetailFlag = true;
                msg = detailSrc + " does not exist";
                publish(msg,this,Level.WARN);
            }
            else {
                try {
                    Path detailPath = Files.copy(detailSrcFile.toPath(), nodeDetailFile.toPath());
                } catch (IOException ex) {
                        msg = "Could not copy detail file "+ detailSrcFile.getAbsolutePath();
                        publish(msg,this,Level.ERROR);
                        badDetailFlag = true;
                    }
            }
                
        }
        else {
            badDetailFlag = true;
            if (detailSrc == null) {
                msg = "DETAIL_SOURCE is empty in param file "+inputArg;
                publish(msg,this,Level.WARN);               
            }
            if (prettyNameList == null) {
                msg = "PRETTY_NAMES is empty in param file "+inputArg;
                publish(msg,this,Level.WARN);
                
            }
            if (varNameList == null) {
                msg = "VAR_NAMES is empty in param file "+inputArg;
                publish(msg,this,Level.WARN);               
            }
        }
        
        if (badDetailFlag) {
            msg = "Problem with node detail; using default node detail";
            publish(msg,this,Level.WARN);
            Integer fillNum = moduleProps.getFillNum();
            if (fillNum == null) {
                double logValue = Math.log10(internalNodeCnt.doubleValue());
                Double ceil = Math.ceil(logValue);
                fillNum = ceil.intValue();
            }
            String prefix = moduleProps.getPrefix();
            if (prefix == null)
                prefix = "n";
          
            List<List<String>> detailLists = createDefaultNodeDetailFile(nodeDetailFile, internalNodeCnt, 
                prefix, fillNum);
            
            if (detailLists == null || detailLists.size() != 2) {
                msg = "Problem generating default details - module will contain no node details";
                publish(msg,this,Level.WARN);
                detailSrc = null;
                varNameList = null;
                prettyNameList = null;
                if (nodeDetailFile.exists())
                    nodeDetailFile.delete();
            }
            
            else {
                detailSrc = "default";
                Iterator<List<String>> listIter = detailLists.iterator();
                varNameList = listIter.next();
                prettyNameList = listIter.next();
            }
            
        }
        
        // create nodes.prop file
        ModuleProperties nodeProps = new ModuleProperties();
        nodeProps.setCount(internalNodeCnt);
        nodeProps.setSignatureCount(actualSigCount);
        if (detailSrc != null)
            nodeProps.setDetailSource(detailSrc);
        if (varNameList != null)
            nodeProps.setVarNames(varNameList);
        if (prettyNameList != null)
            nodeProps.setPrettyNames(prettyNameList);
        nodeProps.setSource(src);
        nodeProps.setTreeSource(treeSrc);
        File nodePropFile = new File(moduleDir, NODES + PROPERTIES_EXT);
        return nodeProps.writePropertiesToFile(nodePropFile.getAbsolutePath());
 
    }
    
    
    // used for both node and function default details
    protected List<List<String>> createDefaultNodeDetailFile(File nodeDetailFile, int nodeCnt,
        String prefix, int fillNum) {
        
        List<String> varNameList = new ArrayList<>(2);
        varNameList.add("indx");
        varNameList.add("id");
        List<String> prettyNameList = new ArrayList<>(2);
        prettyNameList.add("Index");
        prettyNameList.add("ID");
        List<List<String>> lists = new ArrayList<>(2);
        lists.add(varNameList);
        lists.add(prettyNameList);
        FileObj nodeDetailOut = new FileObj(nodeDetailFile);
        nodeDetailOut.appendLine("indx"+TAB+"id");
        for (int i=0; i<nodeCnt; i++) {
           boolean okay = nodeDetailOut.appendLine(Integer.toString(i) + TAB + 
            prefix + StringOps.leftFill(Integer.toString(i), fillNum, '0'));
           if (!okay) {
               String msg = "Problem appending line to default node detail file - module will not contain node details";
               publish(msg,this,Level.WARN);
               return null;
           }
        }       
        return lists;
      
    }
    
    protected Integer createSigFile(ModuleProperties moduleProps, File moduleDir, 
        String srcFilePath, boolean outputHexFlag) {
        
        String outfileName;
        if (outputHexFlag) {
            outfileName = NODES + HEX_SIG_EXT;
        }     
        else {
            outfileName = NODES + RAW_SIG_EXT;
        }
        
        File outputFile = new File(moduleDir, outfileName);
        File sigFile = new File(srcFilePath);  // assumes it exists (tested before method called)
        
        Integer sigCountEst = moduleProps.getSignatureCount();
        publish("Signature Count: "+sigCountEst,this);
        
        File nodeFile = SignatureMap.convertRawSignatureFile(sigFile.getAbsolutePath(),
            moduleDir.getAbsolutePath(), outputHexFlag, sigCountEst);
        
        if (nodeFile == null)
            return null;
        
        // move nodeFile to module directory
        nodeFile.renameTo(outputFile);
        
        return sigCountEst;
    }
    
    protected Integer createParentSetTreeFile(File moduleDir, String treefileName) {
        
        TreeManager mgr = new TreeManager(treefileName);
        boolean okay = mgr.readTreeFromFile(treefileName);
        if (!okay) {
            String msg = "Problem reading tree file "+treefileName+" as phyloxml";
            publish(msg,this,Level.ERROR);
            return null;
        }
        mgr.nextPropertyGrp(PARENT_NAME_SET);
        okay = mgr.setNamesToInternalNodeIds();
        if (!okay) {
            String msg = "Problem setting tree names to internal node ids";
            publish(msg,this,Level.ERROR);
            return null;   
        }
        okay = mgr.createParentNameSets();
        if (!okay) {
            String msg = "Problem creating parent name sets";
            publish(msg,this,Level.ERROR);
            return null;
        }
        
        File outfile = new File(moduleDir, TREE);          
        logger.info("Writing parent set tree to file "+outfile.getAbsolutePath());
        okay = mgr.writeTree(outfile);
        if (!okay) {
            String msg = "Problem writing parent set tree file "+outfile.getAbsolutePath();
            publish(msg,this,Level.ERROR);
            return null;
        }
        else
            return mgr.getNumberInternalNodes();
    }
    
    protected Integer copyTreeFileToModuleDir(File moduleDir, String treefileName) {
               TreeManager mgr = new TreeManager(treefileName);
        boolean okay = mgr.readTreeFromFile(treefileName);
        if (!okay) {
            String msg = "Problem reading tree file "+treefileName+" as phyloxml";
            publish(msg,this,Level.ERROR);
            return null;
        }
      
        File outfile = new File(moduleDir, TREE);          
        logger.info("Copying tree to "+outfile.getAbsolutePath());
        okay = mgr.writeTree(outfile);
        if (!okay) {
            String msg = "Problem writing to "+outfile.getAbsolutePath();
            publish(msg,this,Level.ERROR);
            return null;
        }
        else
            return mgr.getNumberInternalNodes();
    }
    
    
    protected boolean createModuleJarFile(File moduleDir) {
         
        String[] commandArr = new String[6];
        String msg;
        commandArr[0] = "jar";
        commandArr[1] = "cvf";
        commandArr[2] = moduleDir.getAbsolutePath() + ".jar";
        commandArr[3] = "-C";
        commandArr[4] = moduleDir.getAbsolutePath();
        commandArr[5] = ".";
        try {
            Process child = Runtime.getRuntime().exec(commandArr);
            int status = child.waitFor();
            msg = "Create jar file status: "+status;
            publish(msg,this,Level.INFO);
        } catch (IOException | InterruptedException ex) {
            msg = "Problem running os command to create jar file: "+ex.getMessage();
            publish(msg,this,Level.ERROR);
            return false;
        }
        return true;
    }
    
    // create function detail and prop files
    protected File[] createFunctionFiles(ModuleProperties moduleProps, 
        File functScratchDir, String functionSetName, Integer functionCnt) {
        
        File[] functionFiles = new File[2];
        
        File functDetailFile = new File(functScratchDir, functionSetName + DETAIL_EXT);
        File functPropFile = new File(functScratchDir, functionSetName + PROPERTIES_EXT);
        
        functionFiles[0] = functDetailFile;
        functionFiles[1] = functPropFile;
 
        List<String> prettyNameList, varNameList;
        String detailSrc = moduleProps.getDetailSource();
        prettyNameList= moduleProps.getPrettyNames();
        varNameList = moduleProps.getVarNames();
        
        String msg;
   
        // note:  currently I am not checking if pretty and var name lists make sense
        // with reference to detail src file - just take on faith
        if (detailSrc != null && prettyNameList != null && varNameList != null) {
            File detailSrcFile = new File(detailSrc);
            if (!detailSrcFile.exists()) 
                detailSrc = null;
            else {
                msg = FileUtilities.copyFile(detailSrcFile, functDetailFile, true);
                if (msg != null) {
                    msg = "Could not copy function detail file from detail source to scratch dir: "+msg+"; using default details";
                    publish(msg,this,Level.ERROR);
                    detailSrc = null;
                    prettyNameList = null;
                    varNameList = null;
                }
                else {
                    msg = "Function detail source file "+detailSrcFile+" successfully copied to scratch dir";
                    publish(msg,this,Level.INFO);   
                }
                    
            }
                
        }
        
        if (detailSrc == null) {
            msg = "Function detail source, var names or pretty names are missing from param file "+
                inputArg+"; using default function detail";
            publish(msg,this,Level.WARN);
            Integer fillNum = moduleProps.getFillNum();
            Integer functionCntPlusOne = functionCnt+1;
            
            if (fillNum == null) {
                double logValue = Math.log10(functionCntPlusOne.doubleValue());
                Double ceil = Math.ceil(logValue);
                fillNum = ceil.intValue();
            }
            String prefix = moduleProps.getPrefix();
            if (prefix == null)
                prefix = "f";
          
            List<List<String>> detailLists = createDefaultNodeDetailFile(functDetailFile, functionCntPlusOne, 
                prefix, fillNum);
            
            if (detailLists == null || detailLists.size() != 2) {
                msg = "Problem generating default function details in scratch dir";
                publish(msg,this,Level.WARN);
                detailSrc = null;
                varNameList = null;
                prettyNameList = null;
                if (functDetailFile.exists())
                    functDetailFile.delete();
            }
            
            else {
                detailSrc = "default";
                Iterator<List<String>> listIter = detailLists.iterator();
                varNameList = listIter.next();
                prettyNameList = listIter.next();
            }
            
        }
    
        
        ModuleProperties functProps = new ModuleProperties();
        functProps.setDetailSource(detailSrc);
        functProps.setVarNames(varNameList);
        functProps.setPrettyNames(prettyNameList);
        functProps.setSource(detailSrc);
        functProps.setCount(functionCnt);

        boolean okay = functProps.writePropertiesToFile(functPropFile.getAbsolutePath());
        if (!okay) {
            msg = "Problem writing new function properties file in scratch dir;  functions will not be added";
            publish(msg,this,Level.ERROR);
            return null;
        }
        
        return functionFiles;
    }
    
      
    protected ModuleProperties getModuleProperties() {
        
        ModuleProperties moduleProps = new ModuleProperties();
        boolean okay = moduleProps.loadPropertiesFromFile(inputArg);
        if (!okay)
            return null;
        okay = checkModuleProperties(moduleProps);
        if (!okay)
            return null;
        else
            return moduleProps;
        
    }
    
    protected GeneratorProperties getGeneratorProperties() {
        
        GeneratorProperties genProps = new GeneratorProperties();
        boolean okay = genProps.loadPropertiesFromFile(inputArg);
        if (!okay)
            return null;
        else
            return genProps;   
        // note:  need to check properties similar to checkModuleProperties
    }
    
    
    public boolean checkModuleProperties(ModuleProperties moduleProps) {
        
        // note:  
        // this methd may not be complete
        // checks have been left in various other method calls until it is ascertained
        // this is really complete
 
        
        String msg = "";
        
        if (modeStr.equals(CREATE_MODULE) || modeStr.equals(ADD_DETAIL) ||
            modeStr.equals(ADD_FUNCTION)) {
        
            String scratchDirStr = moduleProps.getScratchDir();
            if (scratchDirStr == null)
                msg += "SCRATCH_DIR param is missing from param file\n";

            switch (modeStr) {
                case CREATE_MODULE:
                    String moduleName = moduleProps.getModuleName();
                    if (moduleName == null)
                        msg += "MODULE_NAME param is missing from param file.  ";
                    Boolean hexFlag = moduleProps.getHexFlag();
                    if (hexFlag == null)
                        msg += "HEX_FLAG param is missing from param file.  ";
                    Integer kmerSize = moduleProps.getKmerSize();
                    if (kmerSize == null)
                        msg = "KMER_SIZE param is missing from param file.  ";
                    String treeSrc = moduleProps.getTreeSource();
                    if (treeSrc == null)
                        msg = "TREE_SOURCE param is missing from param file.  ";
                    else {
                        File treeSrcFile = new File(treeSrc);
                        if (!treeSrcFile.exists())
                            msg += "Tree file "+treeSrc+" does not exist.  ";
                    }   String src = moduleProps.getSource();
                    if (src == null)
                        msg += "SOURCE param is missing from param file.  ";
                    else {
                        File srcFile = new File(src);
                        if (!srcFile.exists())
                            msg += "Signature source file "+src+" does not exist.  ";
                    }   break;
                case ADD_DETAIL:
                    String modulePath = moduleProps.getModulePath();
                    if (modulePath == null)
                        msg += "MODULE_PATH param is missing from param file.  ";
                    else  {
                        if (!modulePath.endsWith(".jar"))
                            msg += "MODULE_PATH "+modulePath+" does not have extension .jar.  ";
                        else {
                            File moduleFile = new File(modulePath);
                            if (!moduleFile.exists())
                                msg += moduleFile.getAbsolutePath()+" does not exist.  ";
                        }
                    }   break;
            //  add_function in development
                case ADD_FUNCTION:
                    break;
            // do nothing
                default:
                    break;
            }
            
            if (msg.trim().equals(""))
                return true;
            else {
                publish(msg,this,Level.ERROR);
                return false;
            } 
        }
        else {
            msg = modeStr + " is not yet implemented - cannot check params";
            publish(msg,this,Level.ERROR);
            return false;
        }    
        
    }

    @Override
    public void usage() {   
        List<String> usage = getUsageList(modeStr, internalFlag);
        Iterator<String> usageIter = usage.iterator();
        while (usageIter.hasNext())
            publish(usageIter.next(),this);
    }
    
    public static List<String> getUsageList(String modeStr, boolean isInternal) {
        
        List<String> usage = new ArrayList<>(); 
        
        String fullModeStr;
        if (isInternal)
            fullModeStr = "internal "+modeStr;
        else
            fullModeStr = modeStr;
 
        usage.add("\n\nCommand line execution of sequescan "+fullModeStr);
        usage.add("java -jar <distrib_dir>/lib/sequescan.jar "+ fullModeStr+" [-h] [-q] [-c config_file] PARAMFILE");
        usage.add("");
        usage.add("Example:");
        usage.add("java -jar <distrib_dir>/lib/sequescan.jar "+fullModeStr+" createBact403.params");
        usage.add("");
        usage.add("Option descriptions:");
        usage.add("-h    mode help");
        usage.add("-q    quiet mode");
        usage.add("-c    user-defined configuration file (overrides system configuation file)");
        return usage;
        
    }

    @Override
    protected Getopt generateGetoptInstance(String[] optionsArr) {
  
        Getopt go = new Getopt("sequescan",optionsArr, "hqc:");
	go.setOpterr(true);
        return go;
    }
    
    @Override
    protected boolean setVariablesFromOptions(Getopt go) {

        if (modeOptionArr.length == 1 && modeOptionArr[0].equals("-h")) {
            usage();
            return false;
        }

        int ch;
        int argCnt = 0;
        
        verbose = true;
        
   
        while ((ch = go.getopt()) != -1) {
            if ((char)ch == 'c'){
                configFilePath = go.getOptarg();
            }else{
 //               publish( "\n*********Unknown option: '" + ch + "'",this); 
                usage();
                return false;                     
            }

        }
        
        // command line arguments include mode (first arg) and inputArg (last arg)
        // note:  if first arg is internal, it has already been eliminated (see Sequescan class)
        if (argCnt != commandLineArr.length - 2) {
            usage();
            return false;
        }
     
        else 
            return true;
    }

    @Override
    protected String getLogDirParentStr() { 
        // log file will go into directory where sequescan was called
        return System.getProperty("user.dir");
        
    }

    @Override
    // for the moment don't test anything 
    protected String testConfigDataTypes() {
        return "";
    }
    
    @Override
    protected boolean checkMemoryRequirements() {
        // for now do nothing 
        return true;
    }
    
     @Override
    public void runWithAutoMaxHeap() {
        // do nothing for now
    }

    
}
