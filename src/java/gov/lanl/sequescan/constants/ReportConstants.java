/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.constants;

import gov.lanl.sequtils.constants.GeneralConstants;

/**
 *
 * @author jcohn
 */
public interface ReportConstants extends GeneralConstants {
    
    // count types
    public static final int READ = 0;
    public static final int RF = 1;
    public static final int FRAG = 2;
    public static final int PHYL_FRAG = 3;
    public static final int FUNC_FRAG = 4;

    // count subtypes
    public static final int TOTAL = 0;
    public static final int SINGLE_KMER = 1;
    public static final int SINGLE_NODE = 2;
    public static final int MONOPHYL = 3;
    public static final int NON_MONOPHYL = 4;
    
    // file extensions
    public static final String JSON = ".json";
    public static final String TSV = ".tsv";
    public static final String FASTA_EXT = ".fa";
    public static final String FASTQ_EXT = ".fq";
    public static final String FASTA_FAA_EXT = ".faa";
    
    // file types
    public static final String FASTA = "fasta";
    public static final String FASTQ = "fastq";
    public static final String FASTA_FAA = "fasta_faa";
    public static final String SAME_AS_INPUT = "same_as_input";
    
    // record start
    public static final String FASTA_BEGIN = ">";
    public static final String FASTQ_BEGIN = "@";
    public static final String FASTQ_QBEGIN = "+";
   
    // if set to "column" file output is by input column NOT per node
    public static final String OUTPUT_ORDER = "column";
}
