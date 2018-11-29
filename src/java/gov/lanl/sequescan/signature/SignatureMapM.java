/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import gov.lanl.sequtils.util.StringOps;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author jcohn
 */
public abstract class SignatureMapM<K,V> extends SignatureMap<K,V> {
    
    protected Map<K,V> map;
    
    public SignatureMapM(SignatureModule module) {
        super(module);
    }
    
    public SignatureMapM(Boolean hFlag, Integer sigCntEst) {
        super(hFlag,sigCntEst);
    }

    
    // implement abstract methods
   
    @Override
    public Short getNode(K signature) {
        if (map == null)
            return null;
        V values = map.get(signature);
        if (values == null)
            return null;
        else
            return getMapValue(values,0);
    }
    
    @Override
    public Set<Short> getFunctions (K signature) {
 
        if (map == null)
            return null;
        V values = map.get(signature);
        int valuesLen = getMapValuesLength(values);
        if (values == null || valuesLen < 2)
            return null;
        else {
            Set<Short> functionSet = new HashSet<>();
            for (int i=0; i< valuesLen-1; i++) {
                Short val = getMapValue(values,i+1);
                functionSet.add(val);
            }
            return functionSet;
        }
        
    }
    
    
    
    @Override
    protected void initMap() {
        if (sigCountEstimate == null && dataModule != null) 
            sigCountEstimate = dataModule.getSignatureCount();
        if (sigCountEstimate == null) {
            map = new HashMap<>(); // perhaps this should be set to some minimal number (e.g. number of signatures in virus list?)
            logger.info("Signature Count is missing from data module - signature map loading and/or memory allocation may be less efficient");
        }
        else {
            Double initCapacity = ((double) sigCountEstimate/0.75) + (double) 100;
            logger.debug("HashMap initial capacity is "+initCapacity);
            map = new HashMap<>(initCapacity.intValue());
        }

    }
    
  
    
    @Override
    protected String addFunctionsToList(K signature, String[] functionTokens) {
        
        V values = map.get(signature);
        if (values == null) 
            return "Signature is missing from phylogeny map";
        int valuesLen = getMapValuesLength(values);
        
        int functionCnt = functionTokens.length;
        int newListLength = valuesLen + functionCnt;
        if (newListLength == 0) {
            return signature.toString() +": No function tokens";
        }
        V newValues = getEmptyMapValues(newListLength);
        for (int i=0; i<valuesLen; i++) {
            setMapValue(newValues,i,getMapValue(values,i));
        }
        
        for (int i=0; i<functionCnt; i++) {
            Short functionVal = StringOps.getShort(functionTokens[i]);
            if (functionVal == null) 
                return "Non-numeric function value";
            else 
                setMapValue(newValues,valuesLen+i,functionVal);
        }
        map.put(signature, newValues);
        return null;
    }
    
 

    @Override
    public int getSize() {
        if (map == null)
            return -1;
        else return map.size();
    }


    @Override
    public int getSignatureCount() {
        return getSize();
    }
 
    @Override
    public Set<K> getSignatureSet() {
        if (map == null)
            return null;
        else
            return map.keySet();
    }  
 

    @Override
     public boolean readPhyloMapFromSignatureFile(String filePath) {
        
        if (map != null && !map.isEmpty()) {
            logger.info("Map is already populated with phylo data");
            return true;
        }
        
        
        String msg = "Start reading phylo map from "+
            filePath+" with input hexFlag "+hexFlag;
        logger.info(msg);
        
        if (hexFlag == null) {
            msg = "Hex Flag is null";
            logger.error(msg);
            return false;
        }
       
        
        int cnt;
        
        initMap();
       
        try {
            FileReader freader = new FileReader(filePath);
            BufferedReader in = new BufferedReader(freader);
    
  
            in.readLine();  // header
            String line = in.readLine();
            cnt = 0;
            while (line != null) {
                String[] tokenArr = StringOps.getTokens(line, FIELD_DELIM);
//                if (tokenArr.length != 5) {
//                    msg = "Line does not have 5 tokens: "+line;
//                    logger.error(msg);
//                    map = null;
//                    return false;
//                }
                String signature = tokenArr[0];
                Short node = StringOps.getShort(tokenArr[1]);
                if (node == null) {
                    logger.error("Problem parsing node field in line: "+
                        line);
                    return false;
                }
                
                V list = getNewValueListWithNode(node);

                K signatureObj = getSignatureObj(signature);

                map.put(signatureObj,list);
                cnt++;

                line = in.readLine();
            }
            
            
            
        } catch (IOException ex) {
            
            msg = "Problem reading signatures from "+
                filePath+": "+ex.getMessage();
            logger.error(msg);
            return false;
        }
        
        logger.info(cnt+" signatures read with node assignment");
        return true;
    
    }
    
    @Override
    public boolean readPhyloMapFromModule() {
        
        if (map != null && !map.isEmpty()) {
            logger.info("Map is already populated with phylo data");
            return true;
        }
        
        
        String msg = "Start reading phylo map from "+
            dataModule.getModulePath();      
        logger.info(msg);
        
        if (hexFlag == null) {
            logger.error("Hex Flag is missing from data module "+
                dataModule.getModulePath());
            return false;
        }
        
        int cnt;
        
        initMap();
       
        try {
            BufferedReader phyloStream = dataModule.getSignatureReader();
            if (phyloStream == null) 
                return false;
  
            String line = phyloStream.readLine();
            cnt = 0;
            while (line != null) {
                StringTokenizer stok = new StringTokenizer(line,FIELD_DELIM);
                if (stok.countTokens() != 2) {
                    logger.error("Line does not have 2 tokens: "+line);
                    map = null;
                    return false;
                }
                String signature = stok.nextToken();
                Short node = StringOps.getShort(stok.nextToken());
                if (node == null) {
                    logger.error("Problem parsing node field in line: "+
                        line);
                    return false;
                }
                
                V list = getNewValueListWithNode(node);

                K signatureObj = getSignatureObj(signature);
                
//                if (Objects.equals(hexFlag, Boolean.TRUE)) {
//                    signatureObj = fromHexString(signature);
//                }
//                else
//                    signatureObj = signature;
 
                map.put(signatureObj,list);
                cnt++;

                line = phyloStream.readLine();
            }
            
            
            
        } catch (IOException ex) {
            logger.error("Problem reading signatures from "+
                dataModule.getModulePath()+": "+ex.getMessage());
            return false;
        }
        
        logger.info(cnt+" signatures read with node assignment");
        logger.info(map.size()+" elements in map");
        

        return true;
    }

    @Override
    public void setNullMap() {
        map = null;
    }

    @Override
    public boolean isMapNull() {
        if (map == null)
            return true;
        else
            return false;
    }
 
    
}
