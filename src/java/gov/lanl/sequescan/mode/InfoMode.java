/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.mode;

import gov.lanl.sequescan.signature.JarModule;
//import gov.lanl.sequtils.util.GetOpt;
import gnu.getopt.Getopt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class InfoMode extends Mode {
    
    protected String selectedDataModule = null;
    protected boolean longOutput = true;
    
    
    public InfoMode(String[] appArgs, boolean isInternal) {
        super(appArgs, isInternal);
        for (int i=0; i<appArgs.length; i++)
            System.out.println("arg "+i+": "+appArgs[i]);
    }
    
    public void setLongOutput(boolean flag) {
        longOutput = flag;
    }
    
    public boolean getLongOutput() {
        return longOutput;
    }

    @Override
    public void execute() {
        
//        System.out.println("verbose: "+verbose);
            
        String infoType = inputArg.toLowerCase();
        if (selectedDataModule != null) {
            displayInfoForModule(selectedDataModule, infoType,longOutput);
        }
        else {  // get info for all data modules
            List<String> moduleNames = getDataModuleList(this,true);
            if (moduleNames == null || moduleNames.isEmpty())
                return;
            Iterator<String> moduleIter = moduleNames.iterator();
            boolean firstFlag = true;
            while (moduleIter.hasNext()) {
                if (!firstFlag)
                    System.out.println("");
                else
                    firstFlag = false;
                displayInfoForModule(moduleIter.next(), infoType,true);
            }
        }
            
    }
    
    protected void displayInfoForModule(String dataModuleName,String infoRequest, boolean nameFlag) {
        
        String jarFilePath = getJarFilePath(this,dataModuleName);
        JarModule dataModule = new JarModule(jarFilePath);
        boolean status = dataModule.openJarFile();
        if (!status)
            return;
        if (nameFlag) {
            System.out.println("data module: "+dataModuleName);
        }
        List<String> results;
        if (!inputArg.equals(ALL)) {
            results = getModuleInfo(dataModuleName,dataModule, inputArg,nameFlag);
            displayResults(results);
        }
        else {
            Set<String> allowedInfoTypes = getAllowedInfoTypes();
            Iterator<String> infoIter = allowedInfoTypes.iterator();
            while (infoIter.hasNext()) {
                String infoType = infoIter.next();
                if (!infoType.equals(ALL)) {
                    results = getModuleInfo(dataModuleName,dataModule,infoType,true);
                    displayResults(results);
                }
            }
        }
          
    }
    
    protected void displayResults(List<String> results) {
        
        Iterator<String> resultIter = results.iterator();
        while (resultIter.hasNext()) {
            System.out.println(resultIter.next());           
        }
    }
    
    protected List<String> getModuleInfo(String moduleName, JarModule module, 
        String infoType, boolean descrFlag) {
        // assumes module has been "opened"
        List<String> results = new ArrayList<>();
        
        String infoTypeStr;
        if (descrFlag)
            infoTypeStr = infoType + ": ";
        else
            infoTypeStr = "";
       
        String infoValue;
        switch (infoType) {
            case FUNCTIONSET_LIST:
                // at some point, I should add function count for each function set
                Set<String> functionSets = module.getFunctionSetNames();
                if (functionSets == null || functionSets.isEmpty())
                    infoValue = "none";
                else {
                    Iterator<String> funcIter = functionSets.iterator();
                    infoValue = funcIter.next();
                    while (funcIter.hasNext())
                        infoValue += VALUE_DELIM+funcIter.next();
                        results.add(infoTypeStr + infoValue);
                }
                break;
            case MAX_HEAP:
                results.add(infoTypeStr+module.getMaxHeap());
                break;
            case SIGNATURE_COUNT:              
                results.add(infoTypeStr + Integer.toString(module.getSignatureCount()));
                break;
            case NODE_COUNT:
                results.add(infoTypeStr +Integer.toString(module.getNodeCount()));
                break;
            case MODULE_VERSION:
                results.add(infoTypeStr + module.getVersion());
                break;
            default:
                results.add("Unknown InfoType: "+infoType);
                break;
        }
        
        
        return results;
              
    }

    @Override
   public void usage() {   
        List<String> usage = getUsageList(modeStr, internalFlag);
        Iterator<String> usageIter = usage.iterator();
        while (usageIter.hasNext())
            System.out.println(usageIter.next());
    }
    
    public static List<String> getUsageList(String modeStr, boolean internalFlag) {
        
        List<String> usage = new ArrayList<>(); 
        Set<String> allowedQueryTypes = getAllowedInfoTypes();
        String fullModeStr;
        if (internalFlag)
            fullModeStr = "internal " + modeStr;
        else
            fullModeStr = modeStr;
        
 
        usage.add("\n\nCommand line execution of sequescan "+fullModeStr+" mode:");
        usage.add("java -jar <distrib_dir>/lib/sequescan.jar "+fullModeStr+" [-h] [-q] [-c config_file] [-d data_module] INFOTYPE");
        usage.add("");
        usage.add("Example:");
            usage.add("java -jar <distrib_dir>/lib/sequescan.jar "+fullModeStr+" -d bact403.2 functionset_list");
        usage.add("");
        usage.add("Option descriptions:");
        usage.add("-h    mode help");
        usage.add("-c    user-defined configuration file (overrides system configuation file)");
        usage.add("-d    name of data module");
        usage.add("-q    quiet option (returns simple value when -q and -d are used)");
        usage.add("");
        usage.add("Available INFOTYPE:");
        Iterator<String> queryIter = allowedQueryTypes.iterator();
        while (queryIter.hasNext())
            usage.add(queryIter.next());           
        usage.add("");
        usage.add("If the -d option is absent, INFOTYPE will be displayed for all data modules");
        usage.add("");
        return usage;
        
    }


  @Override
    protected Getopt generateGetoptInstance(String[] optionsArr) {
  
        Getopt go = new Getopt("sequescan",optionsArr, "qhc:d:");
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
        
        verbose = false;
        
        int argCnt = 0;
   
        while ((ch = go.getopt()) != -1) {
            switch ((char)ch) {
                case 'c':
                    configFilePath = go.getOptarg();
                    argCnt += 2;
                    break;
                case 'd':
                    selectedDataModule = go.getOptarg();
                    argCnt+= 2;
                    break;
                case 'q':
                    argCnt++;
                    longOutput = false;
                    break;                     
                default:
    //                System.out.println( "\n*********Unknown option: '" + ch + "'");
                    usage();
                    return false;
            }

        }
        
        if (argCnt != commandLineArr.length - 2) {
  //          System.out.println("Wrong number of command line arguments");
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
    
    public static Set<String> getAllowedInfoTypes() {
        
        Set<String> allowedQueryTypes = new TreeSet<>();
        allowedQueryTypes.add(FUNCTIONSET_LIST);
        allowedQueryTypes.add(MAX_HEAP);
        allowedQueryTypes.add(SIGNATURE_COUNT);
        allowedQueryTypes.add(NODE_COUNT);
        allowedQueryTypes.add(MODULE_VERSION);
        allowedQueryTypes.add(ALL);
        return allowedQueryTypes;
        
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