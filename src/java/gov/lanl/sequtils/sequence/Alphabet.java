/*
 * Started with gov.lanl.common.seq.data.Alphabet (from 2003?)
 * 
 */
package gov.lanl.sequtils.sequence;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class Alphabet {

  protected String name;
  protected char[] letters;

  /** Creates a new instance of Alphabet */
  public Alphabet(String name,char[] letters) {
    this.name = name;
    this.letters = letters;
  }

  public String[] genWordList(int wordSize) {    
 
      if (wordSize < 1)
          return null;
      
      int count,innercount,block,rep;
      int wordNum = (int) Math.pow(size(),wordSize);       
      String[] wordList = new String[wordNum];
      
      // init wordList
//      count = 0;
      for (int i=0; i<wordNum; i++)
          wordList[i] = "";
      
      // generate wordList
      for (int i=0; i<wordSize; i++) {
          count = 0;
          block = (int) Math.pow(size(),wordSize-i-1);
          rep = (int) Math.pow(size(),i);
          innercount = 0;
          while (innercount < rep) {
            for (int j=0; j<size(); j++) {
                String currentChar = String.valueOf(letters[j]);
                for (int k=0; k<block; k++) {
                    wordList[count] += currentChar;
                    count++;
                }
            }
            innercount++;
          }
      }
      return wordList;    
  }
  
  public int size() {
      return letters.length;
  }

  public char[] getLetters() {
    return letters;
  }
  
  public String getName() {
      return name;
  }
  
  public Set<Character> getLetterSet() {
      return getLetterSet(false);
  }
  
  public Set<Character> getLetterSet(boolean upperCaseFlag) {
      
      Set<Character> charset = new TreeSet<>();
      for (int c=0; c<letters.length; c++) {
          if (upperCaseFlag)
              charset.add(Character.toUpperCase(letters[c]));
          else
            charset.add(letters[c]);
      }
      return charset;
      
  }
  
  public static Set<Character> getUniqueLetters(Alphabet a, Alphabet b) {
      Set<Character> setA = a.getLetterSet();
      Set<Character> setB = b.getLetterSet();
      setA.removeAll(setB);
      return setA;
  }
  
  public static Map<Character,Integer> getCharFrequency(String seq) {
      return null;
  }
  
    @Override
  public String toString() {
      return new String(letters);
  }
  
  public static void main(String[] args) {
      char[] dna = "ACGTMRWSYKVHDBN-".toCharArray();
      char[] prot = "ARNDCQEGHILKMFPSTWYV".toCharArray();
      Alphabet dnaAlphabet = new Alphabet("dna",dna);
      Alphabet protAlphabet = new Alphabet("prot",prot);
      Set<Character> uniqToDNA = Alphabet.getUniqueLetters(dnaAlphabet,protAlphabet);
      Set<Character> uniqToProt = Alphabet.getUniqueLetters(protAlphabet, dnaAlphabet);
      System.out.println("dna: "+uniqToDNA);
      System.out.println("prot: "+uniqToProt);
  }
    
}
