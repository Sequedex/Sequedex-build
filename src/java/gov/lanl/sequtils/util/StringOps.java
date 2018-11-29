/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequtils.util;

import ch.qos.logback.classic.Level;
import gov.lanl.sequtils.constants.GeneralConstants;
import gov.lanl.sequtils.log.MessageManager;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.text.NumberFormatter;

/**
 * Old class going back to 1996.  Much of this could be replaced by newer
 * standard java functionality.
 * @author jcohn
 */
public class StringOps extends MessageManager implements GeneralConstants {

  // methods

  //----------------------------------------------------

  public static String leftFill(String s,int fieldWidth) {
    return leftFill(s,fieldWidth,' ');
  }

  //----------------------------------------------------

  public static String leftFill(String s,int fieldWidth,char fillChar) {
    // left fill method for non-proportional fonts only
    String temp = s;
    int diff = fieldWidth - s.length();
    for(int i=0;i<diff;i++)
      temp = fillChar+temp;
    return temp;
  }

  //----------------------------------------------------

  public static String rightFill(String s,int fieldWidth) {
    return rightFill(s,fieldWidth,' ');
  }

  //----------------------------------------------------

  public static String rightFill(String s,int fieldWidth,char fillChar) {
    // right fill method for non-proportional fonts only
    String temp = s;
    int diff = fieldWidth - s.length();
    for(int i=0;i<diff;i++)
      temp = temp + fillChar;
    return temp;
  }

  //----------------------------------------------------

  public static String strTab(int rows, int cols, int[] colWidths,
			      String[][] data) {

    StringBuilder sb = new StringBuilder(1000);

    // check if correct number of rows and cols in data
    if (rows != data.length)
      return "";
    if (cols != data[0].length)
      return "";
    if (colWidths.length != cols)
      return "";

    for (int i=0;i<rows;i++) {
      for (int j=0;j<cols;j++)
	sb.append(rightFill(data[i][j],colWidths[j]));
      sb.append(EOL);
    }

    return sb.toString();
  }

  //----------------------------------------------------
    
  public static String[] strTabToArr(int rows, int cols, int[] colWidths,
				     String[][] data) {
    // check if correct number of rows and cols in data
    if (rows != data.length)
      return null;
    if (cols != data[0].length)
      return null;
    if (colWidths.length != cols)
      return null;
    String[] arr = new String[rows];
    for (int i=0;i<rows;i++) {
      String line = "";
      for (int j=0;j<cols;j++) {
	String txt = data[i][j];
	if (txt == null)
	  txt = " ";
	line = line + rightFill(txt,colWidths[j]);
      }
      arr[i] = line;
    }
    return arr;
  }
    
  //----------------------------------------------------

  public static String numTab(int rows, int cols, int[] colWidths,
			      Number[][] data) {

    StringBuilder sb = new StringBuilder(1000);

    // check if correct number of rows and cols in data
    if (rows != data.length)
      return "";
    if (cols != data[0].length)
      return "";
    if (colWidths.length != cols)
      return "";

    for (int i=0;i<rows;i++) {
      for (int j=0;j<cols;j++)
	sb.append(leftFill(data[i][j].toString(),colWidths[j]));
      sb.append(EOL);
    }

    return sb.toString();
  }

  //----------------------------------------------------

  public static String concatStringArray(int startIndex,String[] words,String delim) {

    String txt = words[startIndex];
    for (int i=startIndex+1; i<words.length; i++)
      txt = txt + delim + words[i];
    return txt;
  }
  
  public static String concatStringVec(int startIndex,List<String> strVec, String delim) {
      String txt = strVec.get(startIndex);
      for (int i=startIndex+1; i<strVec.size(); i++)
          txt = txt + delim + strVec.get(i);
      return txt;
  }

  //----------------------------------------------------
    
  // ignores case
  public static int findPattern(char[] charArr,String pattern,int startIndex) {
    return findPattern(charArr,pattern,startIndex,true);
  }

  //----------------------------------------------------

  public static int findPattern(char[] charArr,String pattern,int startIndex,
				boolean ignoreCase) {
    String str = new String(charArr);
    return findPattern(str,pattern,startIndex,ignoreCase);
  }

  //----------------------------------------------------
    
  public static int findPattern(String str,String pattern,int startIndex,
				boolean ignoreCase) {
    int diff = str.length()-pattern.length()+1;
    if (startIndex >= diff)
      return -1;
    for(int i=startIndex;i<diff;i++)
      if (str.regionMatches(ignoreCase,i,pattern,0,pattern.length()))
	return i;
    return -1;
  }

  //----------------------------------------------------

  public static boolean matchToken(String str,int tokenNum,String delim,String matchStr,
				   boolean sameCase,boolean startOnly) {
    String token = null;
    StringTokenizer st = new StringTokenizer(str,delim);
    if (st.countTokens() < tokenNum)
      return false;
    for (int i=0;i<tokenNum;i++) 
        token = st.nextToken();
    if (token == null)
        return false;
    if (sameCase && startOnly) {
      return token.startsWith(matchStr);
    }
    else if (sameCase && !startOnly) {
      return token.equals(matchStr);
    }
    else if (!sameCase && startOnly) {
      return token.toUpperCase().startsWith(matchStr.toUpperCase());
    }
    else {   // !sameCase && !startOnly
      return token.toUpperCase().equals(matchStr.toUpperCase());
    }
  }

  //----------------------------------------------------

  public static boolean matchToken(String str,int tokenNum,String delim,
				   String matchStr,boolean sameCase) {
    return matchToken(str,tokenNum,delim,matchStr,sameCase,false);
  }

  //----------------------------------------------------
    
  public static boolean matchToken(String str,int tokenNum,String delim,
				   String[] matchStrs,boolean sameCase) {
    if (matchStrs == null)
      return false;
    boolean matchFound;
      for (String matchStr : matchStrs) {
          matchFound = matchToken(str, tokenNum, delim, matchStr, sameCase, false);
          if (matchFound)
              return true;
      }
    return false;
  }

  //----------------------------------------------------

  public static boolean matchToken(String str,int tokenNum,String delim,String matchStr) {
    if (matchStr.startsWith("num")) {
      String token = null;
      StringTokenizer st = new StringTokenizer(str,delim);
      if (st.countTokens() < tokenNum)
	return false;
      for (int i=0;i<tokenNum;i++)
	token = st.nextToken();
      for (int i=0;i<token.length();i++) {
	if (!Character.isDigit(token.charAt(i)))
	  return false;
      }
      return true;
    }
    else return matchToken(str,tokenNum,delim,matchStr,false,false);
  }

  //----------------------------------------------------
    
  public static int countTokens(String str,String delim) {
    StringTokenizer st = new StringTokenizer(str,delim);
    return st.countTokens();
  }
  
  //----------------------------------------------------
  
  public static String[] getTokens(String str, String delim) {
      if (str == null || str.equals(""))
          return null;
      StringTokenizer stok;
      if (delim == null)
          stok = new StringTokenizer(str);
      else
          stok = new StringTokenizer(str,delim);
      int num = stok.countTokens();
      String[] arr = new String[num];
      for (int i=0; i<num;  i++)
          arr[i] = stok.nextToken();
      return arr;
  }

  public static LinkedHashSet<String> getOrderedTokenSet(String str, String delim) {
      StringTokenizer stok;
      if (delim == null)
          stok = new StringTokenizer(str);
      else
          stok = new StringTokenizer(str,delim);
      int num = stok.countTokens();
      LinkedHashSet<String> set = new LinkedHashSet<>(num);
      for (int i=0; i<num;  i++)
          set.add(stok.nextToken());
      return set;
  }

  
  public static int[] getIntArr(String str, String delim) {
      String[] tokens = getTokens(str, delim);
      int[] intArr = new int[tokens.length];
      for (int i=0; i<tokens.length; i++) {
          Integer numObj = getInteger(tokens[i]);
          if (numObj == null)
              return null;
          intArr[i] = numObj;
      }
      return intArr;
  }

  //----------------------------------------------------
    
  public static String getLowerCaseRegion(String str,int begin) {
    char[] charArray = str.toCharArray();
    int end = getLowerCaseEnd(charArray,begin);
    return str.substring(begin,end);
  }

  //----------------------------------------------------
    
  public static int getLowerCaseEnd(char[] str,int begin) {
    int i;
    for (i=begin; i<str.length; i++) {
      char c = str[i];
      if (!Character.isLowerCase(c))
	return i;
    }
    return i;
  }

  //----------------------------------------------------
    
  public static String getUpperCaseRegion(String str,int begin) {
    char[] charArray = str.toCharArray();
    int end = getUpperCaseEnd(charArray,begin);
    return str.substring(begin,end);
  }

  //----------------------------------------------------
    
  public static int getUpperCaseEnd(char[] str,int begin) {
    int i;
    for (i=begin; i<str.length; i++) {
      char c = str[i];
      if (!Character.isUpperCase(c))
	return i;
    }
    return i;
  }
    
  //----------------------------------------------------
    
  public static String getLetterRegion(String str,int begin) {
    char[] charArray = str.toCharArray();
    int end = getLetterEnd(charArray,begin);
    return str.substring(begin,end);
  }

  //----------------------------------------------------
        
  public static int getLetterEnd(char[] str,int begin) {
    int i;
    for (i=begin; i<str.length; i++) {
      char c = str[i];
      if (!Character.isLetter(c))
	return i;
    }
    return i;
  }

  //----------------------------------------------------
    
  public static String getDigitRegion(String str,int begin) {
    char[] charArray = str.toCharArray();
    int end = getDigitEnd(charArray,begin);
    return str.substring(begin,end);
  }

  //----------------------------------------------------
    
  public static int getDigitEnd(char[] str, int begin) {
      int i;
      for ( i = begin; i<str.length; i++) {
      char c = str[i];
      if (!Character.isDigit(c))        
        return i;
    }
    return i;
  }

  //----------------------------------------------------
    
  public static int getNextChar(char ch,char[] str,int begin) {
    int i;
    for (i=begin; i<str.length; i++) {
      char c = str[i];
      if (c == ch)
	return i;
    }
    return -1;
  }  

  //----------------------------------------------------
    
  public static boolean isInteger(String str) {
    /*      try {
            Integer numObj = Integer.decode(str);
            if (numObj != null)
	    return true;
            else return false;
	    } catch (NumberFormatException e) {
            return false;
	    }  */
    try {
      int num = Integer.parseInt(str);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }
  
  
  public static Integer getInteger(String str) {
      try {
          return Integer.valueOf(str);
      } catch (NumberFormatException e) {
         
          String msg = "Bad number format: "+str;
          publish(msg,true,Level.WARN);
          return null;
      }
  }
  
  public static Short getShort(String str) {
      try {
          return Short.valueOf(str);
      } catch (NumberFormatException e) {
          
          String msg = "Bad number format: "+str;
          publish(msg,true,Level.WARN);
          return null;
      }
  }
  
  public static boolean isDouble(String str) {
    try {
      Double num = Double.valueOf(str);
      return true;
    }
    catch(NumberFormatException e) {
      return false;
    }
  }

  
  
  
  public static Double getDouble(String str) {
      try {
          return Double.valueOf(str);
      } catch (NumberFormatException e) {
          
          String msg = "Bad number format: "+str;
          publish(msg,true,Level.WARN);
          return null;
      }
  }
  

  //----------------------------------------------------
    
  public static boolean isFloat(String str) {
    try {
      Float num = Float.valueOf(str);
      return true;
    }
    catch(NumberFormatException e) {
      return false;
    }
  }

  //----------------------------------------------------
        
  public static String[] parseDelimString(String strs,String delim) {
    StringTokenizer st = new StringTokenizer(strs,delim);
    int num = st.countTokens();
    String[] values = new String[num];
    for (int i=0; i<num; i++)
      values[i] = st.nextToken();
    return values;
  }

  //----------------------------------------------------
    
  public static String getField(String str,String delim,int fieldNum) {
    StringTokenizer st = new StringTokenizer(str,delim);
    if (st.countTokens() < fieldNum || fieldNum < 1)
      return null;
    for (int i=0; i<fieldNum-1; i++)
      st.nextToken();
    return st.nextToken();
  }
    
  //----------------------------------------------------    

  public static String[] split( String str, String delim ) {
    return parseDelimString( str, delim );
  }

  //----------------------------------------------------   
  
  
  public static String[] split(String str, int len) {
      int strLen = str.length();
      int num = strLen/len;
      int remainder = strLen%len;
      String[] arr;
      if (remainder == 0)
          arr = new String[num];
      else
          arr = new String[num+1];
      if (num > 0) {
          for (int i=0; i<num; i++) {
              arr[i] = str.substring(len*i,len*(i+1));
          }
      }
      if (remainder > 0) {
          arr[num] = str.substring(len*num);
      }
      return arr;
  }
  
  // functionality of this method overlaps reformatInLines - I should probably merge at some point
  public static String insertCR(String str, int len, boolean beginFlag) {
      int strLen = str.length();
      int num = strLen/len;
      int remainder = strLen%len;
      StringBuffer buf;
      if (remainder == 0)
          buf = new StringBuffer(strLen+num);
      else
          buf = new StringBuffer(strLen+num+1); 
      if (num > 0) {
          for (int i=0; i<num; i++) {
              String beginChar;
              if (i!=0 || beginFlag)
                  buf.append(EOL);
              buf.append(str.substring(len*i,len*(i+1)));
          }
      }
      if (remainder > 0) {
          buf.append(EOL+str.substring(len*num));
      }      
      return buf.toString();
  }
  
  public static String reformatInLines(String str, int maxLen,
          boolean initCapFlag) {
      return reformatInLines(str,maxLen,initCapFlag,true);
  }
  
  
  // no check to ensure that all "words" are <= maxLen
  // if wordFlag is false, just splits text into lines by maxLen
  public static String reformatInLines(String str, int maxLen,
    boolean initCapFlag,boolean wordFlag) {
      if (str == null)
          return null;
      int strLen = str.length();
      if (strLen <= maxLen)
          return str;
      StringBuilder newStr = new StringBuilder(str.length());
      StringBuilder line;
      
      if (wordFlag) {
          StringTokenizer stok = new StringTokenizer(str);
          line = new StringBuilder(maxLen);
          while (stok.hasMoreTokens()) {
              int lineLen = line.length();
              String token = stok.nextToken();
              int tokenLen = token.length();
              if (lineLen + tokenLen + 1 < maxLen) {
                  if  (lineLen == 0)
                    line.append(token);
                  else
                    line.append(" "); line.append(token);
              }
              else {
                  if (newStr.length() == 0)
                      newStr.append(line);
                  else
                    newStr.append(EOL); newStr.append(line);
                  line = new StringBuilder(maxLen);
                  line.append(token);        
              }         
          }

          if (line.length() > 0) {
              if (newStr.length() == 0)
                  newStr.append(line);
              else
                  newStr.append(EOL); newStr.append(line);
          } 
      }
      
      else {
          int len = str.length();
          for (int i=0; i<len; i+=maxLen) {
              if (i + maxLen >= len) {
                  line = new StringBuilder(str.substring(i));
              }
              else {
                  line = new StringBuilder(str.substring(i,i+maxLen));
              } 
              String delim;
              if (newStr.length() == 0)
                  delim = "";
              else
                  delim = EOL;
              newStr.append(delim);  newStr.append(line);
          }
      }
      
      if (initCapFlag) {
          char newFirstChar = Character.toUpperCase(newStr.charAt(0));
          newStr.setCharAt(0,newFirstChar);
      }          
      return newStr.toString();
  }
  
  public static List<String> strToVec(String str, int maxLen) {
      return strToVec(str,maxLen,true);
  }
  
    // no check to ensure that all "words" are <= maxLen
    // does not preserve end of line or other white space 
    // tried to fix it to work with end of line - it is not working yet...
    // if wordFlag == false, words are ignored and String split into sections of maxLen
  public static List<String> strToVec(String str, int maxLen,boolean wordFlag) {
      if (str == null)
          return null;
      List<String> strVec = new ArrayList<String>();
      if (str.length() <= maxLen) {
          strVec.add(str);
          return strVec;
      }
      
      if (wordFlag) {
          StringTokenizer stok = new StringTokenizer(str,EOL);
          StringBuilder newLine = new StringBuilder(maxLen);
          while (stok.hasMoreTokens()) {
            String line = stok.nextToken();
    //        System.out.println(line);
            StringTokenizer stok2 = new StringTokenizer(line," ");
            while (stok2.hasMoreTokens()) {
              int lineLen = newLine.length();
              String token = stok2.nextToken();
              int tokenLen = token.length();
              if (lineLen + tokenLen + 1 <= maxLen) {
                  if  (lineLen == 0)
                    newLine.append(token);
                  else
                    newLine.append(" "); newLine.append(token);
              }
              else {
    //              System.out.println("newLine: "+newLine.toString());
                  strVec.add(newLine.toString());
                  newLine = new StringBuilder(maxLen);
                  newLine.append(token);        
              } 
            }
          }
      
          if (newLine.length() > 0) {
              strVec.add(newLine.toString());
          } 
      }
      else {
          int len = str.length();
          for (int i=0; i<len; i+=maxLen) {
              if (i + maxLen >= len) {
                  strVec.add(str.substring(i));
              }
              else {
                  strVec.add(str.substring(i,i+maxLen));
              }                            
          }
      }
      return strVec;
  }
  
  
  public static List<String> concatVecs(List<String> vec1, List<String> vec2) {
      List<String> newVec = new ArrayList<String>(vec1.size()+vec2.size());
      Iterator<String> iter = vec1.iterator();
      while (iter.hasNext()) 
          newVec.add(iter.next());
      iter = vec2.iterator();
      while (iter.hasNext())
          newVec.add(iter.next());
      return newVec;
  }
  
  public static String formatAsDecimalNumber(Number num, String pattern) {
      NumberFormatter formatter = new NumberFormatter(new DecimalFormat(pattern));
      String str;
      try {
          str = formatter.valueToString(num);
      } catch (Exception e) {
          String msg = "Problem parsing number object using DecimalFormat";
          publish(msg,true,Level.WARN);
          return null;
      }
      return str;
  }
  
  // copied from online example: http://javaalmanac.com/egs/java.lang/ReplaceString.html
  public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuilder result = new StringBuilder();
    
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }
  
  public static String getLastToken(String str, String delim) {
      StringTokenizer stok = new StringTokenizer(str,delim);
      int num = stok.countTokens();
      num--;
      if (num > 0) {
        for (int i=0; i<num; i++)
            stok.nextToken();
      }
      return stok.nextToken();
  }
  
  public static String getFirstToken(String str, String delim) {
      StringTokenizer stok = new StringTokenizer(str,delim);
      return stok.nextToken();
  }
  
  public static String[] getCorePlusLastToken(String str, String delim) {
      StringTokenizer stok = new StringTokenizer(str,delim);
      int num = stok.countTokens();
      num--;
      String[] result = new String[2];
      if (num > 0) {
          StringBuilder buf = new StringBuilder();
          for (int i=0; i<num; i++) {
              String appendStr;
              if (i==num-1)
                  appendStr = stok.nextToken();
              else
                  appendStr = stok.nextToken() + delim;
              buf.append(appendStr);
          }
          result[0] = buf.toString();
      }
      else
          result[0] = "";
      result[1] = stok.nextToken();
      return result;
  }
      
    
  // some random tests
  public static void main(String[] args) {
      String str = "_a_b_c_d_e_";
      StringTokenizer stok = new StringTokenizer(str,"_");
      System.out.println(stok.countTokens());
//      String[] result = getCorePlusLastToken(str,"_");
//      System.out.println(str+"  "+result[0]+"  "+result[1]);
      System.exit(0);
  }

}
