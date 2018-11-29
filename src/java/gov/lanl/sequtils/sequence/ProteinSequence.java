       /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.sequence;



/**
 *
 * @author jcohn
 */
public class ProteinSequence extends Sequence {
   
    /** Creates a new instance of ProteinSequence */
     public static final char[] STANDARD_PROTEIN_CHARS =
        "ARNDCQEGHILKMFPSTWYV".toCharArray();
    
    public static final String STANDARD_PROTEIN_ALPHABET = "Standard Protein Alphabet";
    
    public static final String[] STANDARD_PROTEIN_HETERO_CODES =
    {"ALA","ARG","ASN","ASP","CYS","GLN","GLU","GLY","HIS","ILE",
     "LEU","LYS","MET","PHE","PRO","SER","THR","TRP","TYR","VAL"};
    
    public ProteinSequence (String name, String charArr) {
        super(name, charArr);
        init();
    }
    
    public ProteinSequence(SequencingRecord record) {
        this(record.getShortHeader(), record.getSequence());
    }
    
    public final void init() {
        createAlphabet();
    }

    @Override
    protected void createAlphabet() {
        alphabet = new Alphabet(STANDARD_PROTEIN_ALPHABET,STANDARD_PROTEIN_CHARS);
    }  
      

}