/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequtils.util;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author jcohn
 */
public class FasterByteArrayOutputStream extends ByteArrayOutputStream {
    
    public FasterByteArrayOutputStream() {
        super();
    }
    
    public FasterByteArrayOutputStream(int capacity) {
        super(capacity);
    }
    
    public byte[] getByteArray() {
        return buf;
    }
    
    public int[] getTrimIndices() {
        
        // used algorithm from String.trim()
        // returns starting position and length for "trimmed" bytes
        
        int[] trimIndices = new int[2];
	    int end = buf.length;
	    int indx = 0;
	    
	    while ((indx < end) && ((char) buf[indx] <= ' ')) 
	        indx++;
	    while ((indx < end) && ((char) buf[end - 1] <= ' ')) 
	        end--;
	        
	    int len = end - indx;
	    
	    if (len <= 0 || indx < 0 || end < 0 ) {
	        trimIndices[0] = -1;
	        trimIndices[1] = -1;
	    }
	    else {
	        trimIndices[0] = indx;
	        trimIndices[1] = len;
	    }
	    
	    return trimIndices;
    }
        

}
