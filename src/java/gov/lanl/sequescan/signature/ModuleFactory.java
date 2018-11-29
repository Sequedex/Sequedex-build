/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

/**
 *
 * @author jcohn
 */
public class ModuleFactory {
    
    public static SignatureModule getSignatureModule(SignatureProperties props) {
        String modulePath = props.getJarModule();
        if (modulePath != null) {
            JarModule jmodule = new JarModule(modulePath);
            String functionSetName = props.getFunctionSetName();
            jmodule.setSelectedFunctionSet(functionSetName);
            boolean okay = jmodule.openJarFile();
            if (!okay)
                return null;
            else
                return jmodule;
        }
        else {
            PropertiesModule pmodule = new PropertiesModule(props);
            return pmodule;
        }
            
    }
    
    public static SignatureModule getSignatureModule(String modulePath) {
        return getSignatureModule(modulePath,"none");
    }
    
    public static SignatureModule getSignatureModule(String modulePath, String functionSetName) {
        if (modulePath != null) {
            JarModule jmodule = new JarModule(modulePath);
            jmodule.setSelectedFunctionSet(functionSetName);
            boolean okay = jmodule.openJarFile();
            if (!okay)
                return null;
            else
                return jmodule;
        }
        else {
            return null;
        }
            
    }
    
}
