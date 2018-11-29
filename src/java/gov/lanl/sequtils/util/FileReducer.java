/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author jcohn
 */
public class FileReducer {
    
    protected File inFile;
    
    public FileReducer(String fileName) {
        inFile = new File(fileName);
    }
    
    public boolean simpleReduce(String outPath, long reduction) {
        
        if (!inFile.exists()) {
            System.out.println(inFile.getAbsolutePath()+" does not exist");
            return false;
        }
        
        File outFile = new File(outPath);
        if (outFile.exists()) {
            System.out.println(outPath + " already exists");
            return false;
        }
        
        long readLineCount = 0;
        long writeLineCount = 0;
        
        try {
            
            FileReader reader = new FileReader(inFile);
            BufferedReader in = new BufferedReader(reader);
            FileWriter writer = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(writer);
   
            String firstLine = in.readLine();
            if (firstLine == null) {
                System.out.println(inFile.getAbsolutePath()+" is empty");
                return false;
            }
            readLineCount++;
            writeLineCount++;
            out.write(firstLine);
            out.newLine();
            String line = in.readLine();
            while(line != null) {
                readLineCount++;
                if (readLineCount % reduction == 0) {
                    out.write(line);
                    out.newLine();
                    writeLineCount++;
                }
                if (readLineCount % 100000 == 0) {
                    System.out.print(readLineCount+" lines read");
                }
                line = in.readLine();
            } 
            
            in.close();
            out.close();
            
        } catch (Exception ex) {
            System.out.println("Problem executing simpleReduce: "+ex.getMessage());
            return false;
        }
        
        System.out.println("Reduction: "+reduction);
        System.out.println("Lines Read: "+readLineCount);
        System.out.println("Lines written: "+writeLineCount);
        
        return true;
        
    }
    
    public static void main(String[] args) {
        
        String inFileName,outFileName,reductionStr;
        if (args.length < 3) {
            inFileName =  "/Users/jcohn/mg2012/autoModuleDev/sig/tol/tol-all.tsv";
            outFileName = "/Users/jcohn/mg2012/autoModuleDev/sig/tol/tol-4.tsv";
            reductionStr = "4";
        }
        else {
            inFileName = args[0];
            outFileName = args[1];
            reductionStr = args[2];
        }
        
        Long reductionNum = Long.decode(reductionStr);
        
        FileReducer reducer = new FileReducer(inFileName);
        boolean okay = reducer.simpleReduce(outFileName, reductionNum.longValue());
        
        System.out.println("Done: "+okay);
            
    }
    
}
