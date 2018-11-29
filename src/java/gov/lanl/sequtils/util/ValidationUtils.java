package gov.lanl.sequtils.util;

import gov.lanl.sequtils.constants.GeneralConstants;

import java.io.File;
import java.util.Iterator;

/**
 * Java class that performs validation on command line arguments and 
 * data coming in from a configuration file. 
 * renamed from InputPort
 * 
 * jc 6/17/14:  replace all \n with GeneralConstants.EOL
 * jc 10/19/17:  rename from InputPort to ValidationUtils and all methods
 * changed to static

 * @since     0.9.0  
 */
public class ValidationUtils implements GeneralConstants {
    
	
	/**
	 * Method that tests that a file exists, is not empty and is readable.
	 * File content is not evaluated.
	 * (e.g. configuration file or database file).
	 * @param file absolute path to the file to be processed.
	 * @return the generated error message or, if no error occurred, and empty string.
	 */
	public static String checkFile( String file){
		String errorMessage = "";
		File f = new File( file);
		
		if( !f.exists()) {
			errorMessage += "\tFile '" + file + "' does not exist"+EOL;
		}if( f.isDirectory()) {
				errorMessage += "\tFile '" + file + "' is a directory"+EOL;
		}if( f.exists() && f.length() == 0){
			errorMessage += "\tFile '" + file + "' is empty"+EOL;
		}if( f.exists() && !f.canRead()){
			errorMessage += "\tFile '" + file + "' is not readable"+EOL;
		}
		return errorMessage;

	}
	
	/**
	 * Tests that a specified directory exists, is readable and writable.
	 *
	 * @param dir the directory to be tested.
	 * @return the generated error message or, if no error occurred, and empty string.
	 */
	public static String checkDir( String dir){
		String errorMessage = "";
		if( dir.startsWith("file:/"))
			dir = dir.substring( 5, dir.length());
		File f = new File( dir);
		if( !f.isDirectory()){
			errorMessage += "\tOutput directory '" + dir + "' is not a directory'"+EOL;
		}
		if( !f.exists()){
			errorMessage += "\tOutput directory '" + dir + "' does not exist'"+EOL;
		}
		if( f.exists() && !f.canRead()){
			errorMessage += "\tOutput directory '" + dir + "' is not readable'"+EOL;
                                
		}
		if( f.exists() && !f.canWrite()){	
			errorMessage += "\tOutput directory '" + dir + "' is not writable'"+EOL;
		}
		return errorMessage;
	}
	
	/**
	 * Method that verifies the allowed data types of specified parameters.
	 * @return the generated error message or, if no error occurred, and empty string.
	 */
	//this whole method needs expansion!!!
        // jc (8/10/12):  changed first variable to Map so that any Map can be checked
	public static String testDataTypes( ConfigFile config){
		String errorMessage = "";
                Iterator<String> iter = config.getKeys().iterator();
                while (iter.hasNext()) {
			String key = iter.next();
			String value = config.getProperty(key);
     //                   System.out.println("key="+key+" val="+value+" len="+value.length());
			//if it's not int, bool, float, list then it's a string
			//since the HashMap is of type <String, Object> I can't use Java's instanceOf 
			//method for testing or getClass().getName()
			if( key.toLowerCase().endsWith( "_int")){
				try {
		            Integer.parseInt( value);
		        } catch (NumberFormatException ex) {
		            errorMessage += "Incorrect int value '" + value + "' for '" + key + "'";
		        }
			}else if( key.toLowerCase().endsWith( "bool")){
				if( !value.toLowerCase().equals( "f") && !value.toLowerCase().equals( "t") &&
					!value.toLowerCase().equals( "false") && !value.toLowerCase().equals( "true")){
					errorMessage += "Incorrect boolean value '" + value + "' for '" + key + "'";
				}	
			//this test isn't sufficient!!!!
			}else if( key.toLowerCase().endsWith( "float")){
				try {
		            Float.parseFloat( value);
		        } catch (NumberFormatException ex) {
		            errorMessage += "Incorrect float value '" + value + "' for '" + key + "'";
		        }
                        }else if( key.toLowerCase().endsWith( "long")){
				try {
		            Long.parseLong( value);
		        } catch (NumberFormatException ex) {
		            errorMessage += "Incorrect long value '" + value + "' for '" + key + "'";
		        }
			}else{
				//it's a string or list
			}
		}
		return errorMessage;
	}
}
