/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import gov.lanl.sequescan.constants.SignatureConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *
 * @author jcohn
 */
public class JarModule extends SignatureModule 
    implements SignatureConstants {
    
    // instance variables
    protected JarFile jarFile = null;
    protected ModuleProperties nodeProperties = null;
    protected Map<String,ModuleProperties> functionProperties = null;
    protected ModuleProperties dataProperties = null;
    protected String selectedFunctionSet;
    
    public JarModule(String modulePathStr) {
        super(modulePathStr);
        init();
    }
    
    @Override
    protected final void init() {
        boolean okay = openJarFile();
        if (!okay) {
            logger.error("JarModule "+getModulePath()+" did not open successfully");
            goodModule = false;
        }
        else
            goodModule = true;
    }
    
    public void setSelectedFunctionSet(String name) {
        selectedFunctionSet = name;
    }
    
    @Override
    public String getSelectedFunctionSet() {
        return selectedFunctionSet;
    }
    
    public boolean openJarFile() {
        
        if (jarFile != null)
            return true;
        
        try {
            jarFile = new JarFile(modulePath);
        } catch (IOException e) {
            logger.error("Problem opening jar file "+modulePath+
                ": "+e.getMessage());
            jarFile = null;
            return false;
        }
        
        return readAllProperties();
  
    }
     
    protected boolean readAllProperties() {
        
        dataProperties = readProperties(PROPERTIES);
        if (dataProperties == null) {
            logger.error("dataProperties is null in "+getModulePath());
            return false;
        }
        nodeProperties = readProperties(NODES+PROPERTIES_EXT);
        if (nodeProperties == null) {
            logger.error("nodeProperties is null in "+getModulePath());
            return false;
        }
        functionProperties = new TreeMap<String,ModuleProperties>();
        Enumeration<JarEntry> jarEnum = jarFile.entries();
        while (jarEnum.hasMoreElements()) {
             JarEntry entry = jarEnum.nextElement();
             String entryName = entry.getName();
             if (entryName.startsWith(FUNCTIONS) && entryName.endsWith(PROPERTIES_EXT)) {
                 ModuleProperties functionProps = readProperties(entryName);
                 int startIndx = FUNCTIONS.length();
                 int endIndx = entryName.length() - PROPERTIES_EXT.length();
                 String setName = entryName.substring(startIndx, endIndx);
                 functionProperties.put(setName,functionProps);
             }
        }
        if (getHexFlag() == null || getKmerSize() == null) {
            logger.error("Bad JarModule "+getModulePath()+":  either HexFlag (" + getHexFlag() +
                ") or kmerSize (" + getKmerSize()+") is missing");
            return false;
        }
        else return true;
    }
    
    @Override
    public Integer getKmerSize() {
        if (dataProperties == null)
            return null;
        else
            return dataProperties.getKmerSize();
    }
      
    
    @Override
    public BufferedReader getReader(String entryName) {
        boolean okay = openJarFile();
        if (!okay)
            return null;
        
        try {
            ZipEntry propEntry = jarFile.getEntry(entryName);
            if (propEntry == null) {
                return null;
            }
            InputStream in = jarFile.getInputStream(propEntry);
            return new BufferedReader(new InputStreamReader(in));
        } catch (IOException e) {
            logger.error("Problem getting reader for "+
                entryName+": "+e.getMessage());
            return null;
        }
    }
    
    @Override
    public BufferedReader getSignatureReader() {
        Boolean hexFlag = getHexFlag();
        if (hexFlag == null) {
            logger.error("Cannot read signature map: Jar Module "+
                getModulePath()+" does not have hexFlag");
            return null;
        }
        else if (Objects.equals(hexFlag, Boolean.TRUE))
            return getReader(NODES+HEX_SIG_EXT);
        else
            return getReader(NODES+RAW_SIG_EXT);
    }
    
    @Override
    public BufferedReader getNodeDetailReader() {
        return getReader(NODES+DETAIL_EXT);
    }
    
    @Override
    public BufferedReader getFunctionReader() {
        if (selectedFunctionSet == null) {
            logger.info("No functionSetName - analysis will only include phylogeny");
            return null;
        }
        Boolean hexFlag = getHexFlag();
        if (hexFlag == null) {
            logger.error("Cannot read function map: Jar Module "+
                getModulePath()+" does not have hexFlag");
            return null;
        }
        else if (Objects.equals(hexFlag, Boolean.TRUE))
            return getReader(FUNCTIONS+selectedFunctionSet+HEX_SIG_EXT);
        else     
            return getReader(FUNCTIONS + selectedFunctionSet + RAW_SIG_EXT);
    }
    
    @Override
    public BufferedReader getFunctionDetailReader() {
        return getReader(FUNCTIONS + selectedFunctionSet + DETAIL_EXT);
    }
    
    @Override
    public BufferedReader getTreeReader() {
        return getReader(TREE);
    }
    
   
    protected ModuleProperties readProperties(String entryName) {
        
        boolean okay = openJarFile();
        if (!okay)
            return null;
        
        try {
            Reader reader = getReader(entryName);
            if (reader == null)
                return null;
            ModuleProperties props = new ModuleProperties(entryName);
            props.load(reader);
            Set<String> propertySet = props.stringPropertyNames();
            if (propertySet == null || propertySet.isEmpty()) {
                logger.error("Properties for "+entryName+" are missing or empty");
                return null;
            }
            else
                return props;
        } catch (Exception e) {
            logger.error("Problem getting properties for "+entryName+": "+
                e.getMessage());
            return null;
        }
    }
    
    public ModuleProperties getSelectedFunctionProperties() {
        return getFunctionProperties(selectedFunctionSet);
    }
    
    public ModuleProperties getFunctionProperties(String setName) {
        if (functionProperties == null || setName == null)
            return null;
        else
            return functionProperties.get(setName);
    }
    
    public Set<String> getFunctionSetNames() {
        return functionProperties.keySet();
    }
    

    @Override
    public Integer getFunctionCount() {
        ModuleProperties props = getSelectedFunctionProperties();
        if (props == null)
            return null;
        else
            return props.getCount();
   
    }
    
    @Override
    public String getVersion() {
        return dataProperties.getVersion();
    }


    @Override
    public Integer getNodeCount() {
        if (nodeProperties == null)
            return null;
        else
            return nodeProperties.getCount();
    }
    
    // for testing purposes only
    protected boolean describe() {
        
        boolean okay = openJarFile();
        if (!okay) 
            return false;
        
        else
            System.out.println("Describing "+modulePath);
        
        try {                                                                     
        
            // Get the manifest
            Manifest manifest = jarFile.getManifest();

            // Get the manifest entries
            Map map = manifest.getEntries();
            System.out.println("Manifest: "+map.size());

            // Enumerate each entry
            for (Iterator it=map.keySet().iterator(); it.hasNext(); ) {
                // Get entry name
                String entryName = (String)it.next();
                System.out.println("Entry Name = "+entryName);

                // Get all attributes for the entry
                Attributes attrs = (Attributes)map.get(entryName);

                // Enumerate each attribute
                for (Iterator it2=attrs.keySet().iterator(); it2.hasNext(); ) {
                    // Get attribute name
                    Attributes.Name attrName = (Attributes.Name)it2.next();

                    // Get attribute value
                    String attrValue = attrs.getValue(attrName);
                    System.out.println("attrName = "+attrName+"  attrValue = "+attrValue);
                }
            }
            
            System.out.println("Jar Entries");
            Enumeration<JarEntry> jarEnum = jarFile.entries();
            while (jarEnum.hasMoreElements()) {
                JarEntry entry = jarEnum.nextElement();
                System.out.println("Name = "+entry.getName());
            }
            
            return true;
            
            
        } catch (IOException e) {
            logger.error("Problem describing jar file "+modulePath+
                ": "+e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean getHexFlag() {
        if (dataProperties == null)
            return null;
        else
            return dataProperties.getHexFlag();
    }
    
     @Override
    protected InputStream getInputStream(String entryName) {
         boolean okay = openJarFile();
        if (!okay)
            return null;
        
        try {
            ZipEntry propEntry = jarFile.getEntry(entryName);
            if (propEntry == null) {
                return null;
            }
            return jarFile.getInputStream(propEntry);
     
        } catch (IOException e) {
            logger.error("Problem getting input stream for "+
                entryName+": "+e.getMessage());
            return null;
        }
    }

    @Override
    public InputStream getTreeInputStream() {
        return getInputStream(TREE);
    }
    
    protected String[][] getColNameMatrix (ModuleProperties props) {
        if (props == null)
            return null;
        else {
            List<String> varNames =  props.getVarNames();
            List<String> prettyNames = props.getPrettyNames();
            if (prettyNames == null)
                prettyNames = varNames;
            if (varNames == null)
                return null;
            int colNum = varNames.size();
            String[][] nodeColumnNames = new String[2][colNum];
            for (int i=0; i<colNum; i++) {
                nodeColumnNames[0][i] = varNames.get(i);
                nodeColumnNames[1][i] = prettyNames.get(i);
    //            System.out.println(i+" "+nodeColumnNames[0][i]+" "+nodeColumnNames[1][i]);
            }
            return nodeColumnNames;              
        }
        
    }
    
    @Override
    public String[][] getNodeColumns() {
        return getColNameMatrix(nodeProperties);
    }

    @Override
    public String[][] getFunctionColumns() {
        if (selectedFunctionSet == null )
            return null;
        ModuleProperties fprops = getSelectedFunctionProperties();
        return getColNameMatrix(fprops);
    }
 
    
    public static void main(String[] args) {
        
        String propsfile = "/Users/jcohn/Desktop/test.params";
        SignatureProperties props = new SignatureProperties();  
        boolean okay = props.loadPropertiesFromFile(propsfile);
        SignatureModule module = ModuleFactory.getSignatureModule(props);
        String funcSetName = module.getSelectedFunctionSet();
        System.out.println("selected function set: "+funcSetName);
        Integer fcnt = module.getFunctionCount();
        System.out.println(fcnt);
        System.exit(0);
    }

    @Override
    public Integer getSignatureCount() {
        if (nodeProperties == null)
            return null;
        else
            return nodeProperties.getSignatureCount();
    }

    @Override
    public String getMaxHeap() {
        return dataProperties.getMaxHeap();
    }
    
    public ModuleProperties getNodeProperties() {
        return nodeProperties;
    }
    
    
}
