/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.log.MessageManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author jcohn
 */
public class ReportUtilities extends MessageManager {
    
    public static boolean writeCntMatrixToFile(File outFile,
        double[][] cntMatrix, String[] rowNames, String[] colNames) {

        return writeMatrixToFile(outFile, cntMatrix, rowNames, colNames, 0, "id");

    }

    public static boolean writeMatrixToFile(File outFile, 
        double[][] matrix, String[] rowNames, String[] colNames, int fracPlaces,
        String cornerStrng) {

        int m = matrix.length;		//number data rows
        int n = matrix[0].length;	//number data columns
        
        if (m != rowNames.length) {
            String msg = "matrix rows="+m+"   rowNames="+rowNames.length;
            publish(msg,true,Level.WARN);
        }
        if (n != colNames.length) {
            String msg = "matrix cols="+n+"   colNames="+colNames.length;
            publish(msg,true,Level.WARN); 
        }

        DecimalFormat dformat = new DecimalFormat();
        dformat.setRoundingMode(RoundingMode.HALF_UP);
        dformat.setMaximumFractionDigits(fracPlaces);
        dformat.setGroupingUsed(false);
        String delim = "\t";

        try {

            FileWriter fileOut = new FileWriter(outFile);
            BufferedWriter dataOut = new BufferedWriter(fileOut);
            String cornerStr = cornerStrng; //#tsv "+m+"x"+n;  //#tsv<rowNum>x<colNum>
            dataOut.write( cornerStr);
            for (int i=0; i<n; i++) {
                dataOut.write(delim);
                dataOut.write(colNames[i]);
            }
            dataOut.newLine();
            
            for (int i=0; i<m; i++) {              
                dataOut.write(rowNames[i]);              
                for (int j=0; j<n; j++) {
                    dataOut.write(delim);
                    dataOut.write(dformat.format(matrix[i][j]));
                }

                dataOut.newLine();
            }

            dataOut.close();

        } catch (Exception e) {        
            String msg = "Problem writing count matrix to "+outFile.getAbsolutePath();
            publish(msg,true,Level.ERROR);
            publish(e.getMessage(),true,Level.DEBUG);
            return false;
        }

        return true;
    }
    
    public static List<String> getMatrixLines(double[][] cntMatrix, String[] rowNames, String[] colNames) {

        return getMatrixLines(cntMatrix, rowNames, colNames, 0, "id");

    }

    public static List<String> getMatrixLines( 
        double[][] matrix, String[] rowNames, String[] colNames, int fracPlaces,
        String cornerStrng) {
       

        int m = matrix.length;		//number data rows
        int n = matrix[0].length;	//number data columns
        
        if (m != rowNames.length) {
            String msg = "matrix rows="+m+"   rowNames="+rowNames.length;
            publish(msg,true,Level.WARN);
        }
        if (n != colNames.length) {
            String msg = "matrix cols="+n+"   colNames="+colNames.length;
            publish(msg,true,Level.WARN);
        }

        DecimalFormat dformat = new DecimalFormat();
        dformat.setRoundingMode(RoundingMode.HALF_UP);
        dformat.setMaximumFractionDigits(fracPlaces);
        dformat.setGroupingUsed(false);
        String delim = "\t";
        
        List<String> lines = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        buf.append(cornerStrng);
        for (int i=0; i<n; i++) {
            buf.append(delim);
            buf.append(colNames[i]);
        }
        lines.add(buf.toString());

        for (int i=0; i<m; i++) { 
            buf.setLength(0);
            buf.append(rowNames[i]);              
            for (int j=0; j<n; j++) {
                buf.append(delim);
                buf.append(dformat.format(matrix[i][j]));
            }

            lines.add(buf.toString());
        }

        return lines;
    }
    
  
     public static double[] getMatrixRowSumVec(double[][] matrix) {

        int m = matrix.length;
        int n = matrix[0].length;
        
        double[] rowSumVec = new double[m];
        for (int r=0; r<m; r++) {
            double rowSum = 0;
            for (int c=0; c<n; c++) {
                rowSum += matrix[r][c];
            }
            rowSumVec[r] = rowSum;
        }

        return rowSumVec;
    }
     
   
    public static double[] getMatrixColSumVec(double[][] matrix) {

        int m = matrix.length;
        int n = matrix[0].length;

        double[] colSumVec = new double[n];
        for (int c=0; c<n; c++) {
            double colSum = 0;
            for (int r=0; r<m; r++) {
               colSum += matrix[r][c];
            }
            colSumVec[c] = colSum;
        }

        return colSumVec;
    }
    
   

    
}
