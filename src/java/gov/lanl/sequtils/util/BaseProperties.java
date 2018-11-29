/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import gov.lanl.sequtils.log.MessageManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import ch.qos.logback.classic.Level;
import java.io.File;

/**
 *
 * @author jcohn
 */
public class BaseProperties extends Properties {
    
    // constants
    public static final String DEFAULT_LIST_DELIM = ":";
    private static final long serialVersionUID = 1L;
    
    protected String filePath = null;
    protected String list_delim = DEFAULT_LIST_DELIM;
    
    public BaseProperties() {
        super();
    }
    
    public void setListDelim(String d) {
        list_delim = d;
    }
    
    public String getListDelim() {
        return list_delim;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public Set<String> getKeys() {
        return stringPropertyNames();
    }
   
    public Object setFlag(String key, boolean flag) {
        return setProperty(key, Boolean.toString(flag));
    }
    
    public Boolean getFlag(String key) {
        if (key == null)
            return null;
        else {
            String value = getProperty(key);
            if (value == null)
                return null;
            else {
                if (value.toUpperCase().equals("FALSE"))
                    return Boolean.FALSE;
                else
                    return Boolean.TRUE;
            }
                
        }
    }
    
    public Integer getInteger(String key) {
        if (key == null)
            return null;
        String value = getProperty(key);
        if (value == null)
            return null;
        else 
            return StringOps.getInteger(value);
    }
    
    public void setInteger(String key, int value) {
        String intStr = Integer.toString(value);
        setProperty(key,intStr);
    }
    
    public Object setFloat(String paramName, float num) {
        return setProperty(paramName, Float.toString(num));
    }
    
    public Float getFloat(String paramName) {
        String valueStr = getProperty(paramName);
        if (valueStr == null)
            return null;
        else {
            try {
                return Float.valueOf(valueStr);
            } catch(NumberFormatException e) {
                return null;
            }
        }     
    }
    
    public Double getDouble(String paramName) {
        String valueStr = getProperty(paramName);
        if (valueStr == null)
            return null;
        else {
            try {
                return Double.valueOf(valueStr);
            } catch(NumberFormatException e) {
                return null;
            }
        }     
    }
    
    public Long getLong(String paramName) {
        String valueStr = getProperty(paramName);
        if (valueStr == null)
            return null;
        else {
            try {
                return Long.valueOf(valueStr);
            } catch(NumberFormatException e) {
                return null;
            }
        }   
    }
    
    public void setStringList(String key, List<String> listValues) {
        setStringList(key, listValues, list_delim);
    }
    
    public void setStringList(String key, List<String> listValues, String delim) {
        if (listValues.isEmpty()) {
            setProperty(key,null);
        }
        else {
            Iterator<String> listIter = listValues.iterator();
            String listStr = null;
            while (listIter.hasNext()) {
                if (listStr == null)
                    listStr = listIter.next();
                else
                    listStr += delim + listIter.next();          
            }
            setProperty(key,listStr);
  //          System.out.println("setting list: key="+key+" value = "+listStr);
        }
    }
    
    public List<String> getStringList(String key) {
        return getStringList(key,list_delim);
    }
    
    public List<String> getStringList(String key, String delim) {
        String strValue = getProperty(key);
        if (strValue == null)
            return null;
        StringTokenizer stok = new StringTokenizer(strValue, delim);
        ArrayList<String> strList = new ArrayList<>(stok.countTokens());
        while (stok.hasMoreTokens()) {
            strList.add(stok.nextToken());
        }
        return strList;
    }
    
    public boolean loadPropertiesFromFile(String fPath) {
        
        filePath = fPath;
        try {
            try (FileInputStream in = new FileInputStream(filePath)) {
                load(in);
            }
        } catch(IOException e) {
            String msg = "Problem reading properties from "+filePath+": "+e.toString();
            MessageManager.publish(msg,this,Level.ERROR);
            filePath = null;
            return false;
        }
        return true;
    }
    
    public boolean writePropertiesToFile(String fPath) {
        
        filePath = fPath;
        try {
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                Date now = new Date();
                store(out,"Written by BaseProperties");
            }
            
        } catch (IOException ex) {
            MessageManager.publish("Problem writing Properties to file: "+filePath,this,Level.ERROR);
            MessageManager.publish(ex.getMessage(),this,Level.DEBUG);
            return false;
        }
        return true;
    }
    
    public String getDefaultValue(String key) {
        return null;
    }
    
    @Override
    public String getProperty(String key) {
        String propValue = super.getProperty(key);
        if (propValue == null)
            return getDefaultValue(key);
        else
            return propValue;
    }
    
    public File getExistingFile(String paramName) {

        String fileStr = getProperty(paramName);
        if (fileStr == null)
            return null;
        File file = new File(fileStr.trim());
        if (file.exists())
            return file;
        else
            return null;
    }
    
    public File getExistingDir(String paramName) {

        String dirStr = getProperty(paramName);
        if (dirStr == null)
            return null;
        File dir = new File(dirStr.trim());
        if (dir.isDirectory())
            return dir;
        else
            return null;
    }  
    
    public static void main(String[] args) {
        
        ArrayList<String> list = new ArrayList<>();
        list.add("abc");
        list.add("id");
        list.add("qrs");
        BaseProperties props = new BaseProperties();
        props.setStringList("TEST_LIST", list);
        props.writePropertiesToFile("/Users/jcohn/Desktop/test.prop");
        BaseProperties props2 = new BaseProperties();
        props2.loadPropertiesFromFile("/Users/jcohn/Desktop/test.prop");
        String valueStr = props2.getProperty("TEST_LIST");
        System.out.println("valueStr: "+valueStr);
        List<String> valueList = props.getStringList("TEST_LIST");
        Iterator<String> listIter = valueList.iterator();
        System.out.println("valueList:");
        while (listIter.hasNext())
            System.out.println(listIter.next());
        System.exit(0);
    }
    
    
   
    
}
