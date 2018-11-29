/*
 * started with gov.lanl.common.seq.data.Sequence
 * 
 * restricted to max size of int since a char[] is used in some methods
 * and length returns int
 * 
 */
package gov.lanl.sequtils.sequence;

import gov.lanl.sequtils.log.MessageManager;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author  jcohn
 */
abstract public class Sequence extends MessageManager {

  protected Alphabet alphabet;
  protected String name;
  protected Set features;
  protected String letters;

  /** Creates a new instance of Sequence */
  public Sequence(String name, String sequence, Alphabet alphabet) {
      if (name == null)
          name = "Unknown";
      this.name = name;
      this.letters = sequence;
      this.alphabet = alphabet;
      this.features = new TreeSet(); // SetFactory.getSet(SetFactory.NATURAL,false);
  }
  
  /** Creates a new instance of Sequence */
  public Sequence(String name, String sequence) {
      this(name,sequence,null);
  }
  
  public Sequence(String sequence) {
      this(null,sequence,null);
  }

  public Set getFeatures() {
    return features;
  }

  public String getName() {
    return name;
  }

  public Alphabet getAlphabet() {
    return alphabet;
  }
  
  public int length() {
      return letters.length();
  }
  
  public String sequence() {
        return letters;
  }
    
    public char[] sequenceArr() {
        return letters.toCharArray();
  }
  
  public char[] getUpperLetterArr() {
      return letters.toUpperCase().toCharArray();
  }
  
  @Override
  public String toString() {
      return letters;
  }
  
  
  public Double percentIdentity(Sequence otherSeq) {
      if (length() != otherSeq.length()) {
          System.out.println("Sequences have different lengths");
          return null;
      }
      char[] chars = sequenceArr();
      char[] otherChars = otherSeq.sequenceArr();
      int match = 0;
      for (int i=0; i<chars.length; i++) {
          if (chars[i] == otherChars[i])
              match++;
      }
      double percent = 100*((double)match/length());
      
      return percent;      
  }
 
  public static Set<Character> getNonAlphabetSet(Sequence seq) {
      char[] seqArr = seq.sequenceArr();
      Set<Character> alphabetLetters = seq.getAlphabet().getLetterSet(true);
      Set<Character> seqLetters = new TreeSet<>();
      for (int c=0; c<seqArr.length; c++)
          seqLetters.add(Character.toUpperCase(seqArr[c]));
      seqLetters.removeAll(alphabetLetters);
      return seqLetters;
  }
  
  public static float getPercentSeqLetters(Sequence seq, Set<Character> matchSet) {
      char[] seqArr = seq.sequenceArr();
      int matchNum = 0;
      for (int c=0; c<seqArr.length; c++) {
          boolean match = matchSet.contains(Character.toUpperCase(seqArr[c]));
          if (match)
              matchNum++;
      }
      return 100*matchNum/seqArr.length;

  }
  
  abstract protected void createAlphabet();
  
  
}
