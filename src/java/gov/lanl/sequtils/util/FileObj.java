/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequtils.util;

import gov.lanl.sequtils.constants.GeneralConstants;
import java.io.*;
import java.util.*;


/**
 *
 * @author jcohn
 */
public class FileObj implements GeneralConstants {
    
    public static final int BUFFERSIZE = 8192;
    public static final int ALL_LINES = 0;
    public static final int FIRST_LINE = 1;
    
    protected String encoding = "UTF8";
    protected File javaFile; 
    protected BufferedReader currentReader = null;
  //  protected static boolean displayFlag = false;

    public FileObj(File jfile) {
        javaFile = jfile;
    }
    
    public FileObj(String fName) {
        javaFile = new File(fName);
    }
    
//    public static void setDisplayFlag(boolean flag) {
//        displayFlag = flag;
//    }
    
    public File getJavaFile() {
        return javaFile;
    }
    
    public String getFileName() {
        return javaFile.getAbsolutePath();
    }
    
    public boolean fileExists() {
        return javaFile.exists();
    }
    
    public long fileLength() {
        return javaFile.length();
    }
    
    public void deleteFile() {
        if (fileExists())
            javaFile.delete();
    }
    
    public void clearReader() {
        try {
            currentReader.close();
        } catch (Exception e) {
            System.out.println("Problem closing BufferedReader");
        }
        currentReader = null;
    }
    
    public String getParent() {
        return javaFile.getParent();
    }
    
    // put entire contents of file in a byte array
    public byte[] getAllBytes() {
        if (javaFile == null)
            return null;
        else if (!javaFile.isFile())
            return null;

        try {
            // create FileInputStream from File object
            FileInputStream fromFile = new FileInputStream(javaFile);
            // get size of file and create byte array
            int size = (int) javaFile.length();
            // create efficient ByteOutputArrayStream
            FasterByteArrayOutputStream byteStream = new FasterByteArrayOutputStream(size);
            byte[] buffer = new byte[BUFFERSIZE];
            // get data
            int bytesRead = fromFile.read(buffer);
            while (bytesRead > 0) {
                byteStream.write(buffer,0,bytesRead); 
                bytesRead = fromFile.read(buffer);
            }
            fromFile.close();
            return byteStream.getByteArray();
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return null;

        }
    }
    
    public Collection<String> readLines() {
        return readLines(100);
    }
    
    // read all lines from a text file
    public Collection<String> readLines(int estLineNum) {
        if (javaFile == null)
            return null;
        if (!javaFile.isFile()) 
            return null;
        ArrayList<String> lines = new ArrayList<String>(estLineNum);
        int count = 0;
        try {
            // create DataInputStream from File object
            FileReader fromFile = new FileReader(javaFile);
            BufferedReader dataIn = new BufferedReader(fromFile);
            // get lines
            String line = dataIn.readLine();
            while (line != null) {
                count++;
                lines.add(line);
                line = dataIn.readLine();
            }
            fromFile.close();
            return lines;
        } catch(Exception e) {
            System.out.println("Exception found in reading data at count "+count);
            System.out.println(e.toString());
            return null;
        }

    }
    
    public int getLineNum() {
        if (javaFile == null || !javaFile.isFile()) {
            System.out.println("No file for line count");
            return -1;
        }
        int count = 0;
        try {
            // create DataInputStream from File object
            FileReader fromFile = new FileReader(javaFile);
            BufferedReader dataIn = new BufferedReader(fromFile);
            // count lines
            while (dataIn.readLine() != null) 
                count++;
            fromFile.close();
            return count;
        } catch(Exception e) {
            System.out.println("Exception found in reading data at count "+count);
            System.out.println(e.toString());
            return -1;
        }
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encodingStr) {
        encoding = encodingStr;
    }
    
    public static Collection<String> readNextSection(BufferedReader reader,String startStr, String endStr) {
        return readNextSection(reader,startStr,endStr,"UTF8");
    }
    
    
    // need to change this to non-static function using new currentReader variable
    public static Collection<String> readNextSection(BufferedReader reader, String startStr,String endStr,
            String encodingStr) {
        ArrayList<String> lines = new ArrayList<>(100);
        try {
            String line = reader.readLine();
            boolean startReading = false;
            while (line != null) {
                if (line.startsWith(startStr))
                    startReading = true;
                if (startReading) {
                    String data = new String(line.getBytes(),encodingStr);
  //                  if(displayFlag) {
  //                      System.out.println(utf8data);
  //                  }
                    
                    lines.add(data);
                }
                if (line.startsWith(endStr))
                    break;
                line = reader.readLine();
            }
            
        } catch(Exception e) {
            System.out.println("Problem reading next section");
            return null;
        }
        return lines;
    }
    
    public Collection<String> readNextLines(int lineCount) {
        boolean status;
        ArrayList<String> lines = new ArrayList<>(100);
        if (javaFile == null)
            return null;
        if (!javaFile.isFile())
            return null;
        int count = 0;
        String line = null;
        try {
            if (currentReader == null) {
                FileReader fromFile = new FileReader(javaFile);
                currentReader = new BufferedReader(fromFile);
            }
            while (count < lineCount) {
                line = currentReader.readLine();
                if (line == null) 
                    break;
                else 
                    status = lines.add(line);
                count++;
            }
            return lines;            
        }  catch (Exception e) {
            System.out.println("Problem with readNextLines: "+line);
            return null;
        }                    
    }
    
    public Collection<String> readLines(int firstLine, int lastLine) {
        ArrayList<String> lines = new ArrayList<>(100);
        boolean status;
        if (javaFile == null)
            return null;
        if (!javaFile.isFile())
            return null;
        int count = 1;
        try {
            FileReader fromFile = new FileReader(javaFile);
            BufferedReader dataIn = new BufferedReader(fromFile);
            String line = dataIn.readLine();
            if (firstLine == 1)
                status = lines.add(line);
            count++;
            while (count++ <= lastLine) {
                line = dataIn.readLine();
                if (line == null)
                    break;
                else 
                    status = lines.add(line);
            }
            fromFile.close();
            return lines;            
        }  catch (Exception e) {
            System.out.println("Problem reading lines from file - count: "+count);
            return null;
        }                    
    }
    
    public Collection<String> readSpecifiedLines(String key) {
        return readSpecifiedLines(key,FIRST_LINE);
    }
    
    // return line in file starting with a particular String
    public Collection<String> readSpecifiedLines(String key,int requestType) {
        boolean status;
        ArrayList<String> lines = new ArrayList<>(5);
        if (javaFile == null)
            return null;
        if (!javaFile.isFile()) 
            return null;
        int count = 1;
        try {
            // create DataInputStream from File object
            FileReader fromFile = new FileReader(javaFile);
            BufferedReader dataIn = new BufferedReader(fromFile);
            // get line
            String line = dataIn.readLine();
            while (line != null) {
                if (line.startsWith(key)) {
                    status = lines.add(line);
                    if (requestType == FIRST_LINE)
                        break;
                }
                count++;
                line = dataIn.readLine();                
            }
            fromFile.close();
            return lines;
        } catch(Exception e) {
            System.out.println("Exception reading data at line "+count);
            System.out.println(e.toString());
            return null;
        }

    }
    
    
    // read partial contents of file into a byte array
    public byte[] getBytes(long offset,int byteNum) {

        RandomAccessFile fromFile;
        ByteArrayOutputStream dataBytes;
        byte[] buffer = new byte[BUFFERSIZE];
        
        try {
            if (javaFile.isFile()) {
                // open file for random access
                dataBytes = new ByteArrayOutputStream(byteNum);
                fromFile = new RandomAccessFile(javaFile,"r");
                // get bytenum if necessary
                if (offset == 0 && byteNum == 0) 
                    byteNum = (int) fileLength();
                int repeat = byteNum/BUFFERSIZE;
                int remainder = byteNum%BUFFERSIZE;
                fromFile.seek(offset);
                // get data
                if (repeat > 0) {
                for (int i=0; i<repeat; i++) {
                    fromFile.readFully(buffer);
                    dataBytes.write(buffer,0,BUFFERSIZE);
                }
                }
                if (remainder > 0) {
                    fromFile.readFully(buffer,0,remainder);
                    dataBytes.write(buffer,0,remainder);
                }
                
                fromFile.close();
                return dataBytes.toByteArray();
            }
            else {
                System.out.println("File "+javaFile.getName()+" "+javaFile.getPath());
                return null;
            }

        } catch(Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
    
    public boolean writeData(long offset, byte[] data) {
        return writeData(offset,data,false);
    }
    
    // write byte data to a specified position in the file
    public boolean writeData(long offset,byte[] data,boolean newFile) {
        RandomAccessFile toFile;
        int byteNum = data.length;
        ByteArrayInputStream byteStream;
        byte[] buffer = new byte[BUFFERSIZE];
        
        try {
            if (!javaFile.exists() && !newFile) 
                return false;
            // make ByteArrayInputStream from data array
            byteStream = new ByteArrayInputStream(data);
            // open RandomAccessFile
            toFile = getRandomAccessFile("rw");
            toFile.seek(offset);
            // write to file
            int repeat = byteNum/BUFFERSIZE;
            int remainder = byteNum%BUFFERSIZE;
            if (repeat > 0) {
                for (int i=0; i<repeat; i++) {
                    byteStream.read(buffer,0,BUFFERSIZE);
                    toFile.write(buffer);
                }
            }
            if (remainder > 0) {
                byteStream.read(buffer,0,remainder);
                toFile.write(buffer,0,remainder);
            }
            toFile.close();
            return true;
        } catch(Exception e) {
            System.out.println(e);
            return false;
        }
    }
    
    public RandomAccessFile getRandomAccessFile(String mode) throws IOException {
        return new RandomAccessFile(javaFile,mode);
    }
    
    public File getFile() {
        return javaFile;
    }
    
    public boolean appendLine(String line) {
        ArrayList<String> vec = new ArrayList<>(1);
        boolean status = vec.add(line);
        return appendLines(vec);
    }
    
    public boolean appendLines(Collection<String> lines) {
        Iterator<String> iter = lines.iterator();
        return appendLines(iter);
    }
            
    public boolean appendLines(Iterator<String> iter) {
        try {
            RandomAccessFile toFile = getRandomAccessFile("rw");
            long fileSize = toFile.length();
            toFile.seek(fileSize);
            while (iter.hasNext()) {
                String line = iter.next()+EOL;
//                String utf8 = new String(line.getBytes(),encoding);
              //  System.out.println("Line out: "+line);
                toFile.write(line.getBytes(encoding));
            }
            toFile.close();
            return true;
        } catch(Exception e) {
            System.out.println(e.toString());
            System.out.println("Problem appending lines to file "+javaFile.toString());
            return false;
        }
    }
      
    public static void main(String[] args) {
        String fileName = "c:\\seqApps\\seqIndices\\hs16b\\306C6";
        FileObj fileObj = new FileObj(fileName);
        Collection<String> lines = fileObj.readSpecifiedLines("306C6.0063.r.1.ne");
        if (lines == null)
            System.out.println("Problem reading file");
        else if (lines.isEmpty())
            System.out.println("No match");
        else {
            Iterator<String> iter = lines.iterator();
            while (iter.hasNext())
                System.out.println(iter.next());
        }
    }

}
