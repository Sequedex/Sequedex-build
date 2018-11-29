package gov.lanl.sequtils.sequence;

import java.util.Set;

/**
 * 
 * @author J. Cohn <jcohn@lanl.gov>
 * 
 */
public class KmerSet<K> extends java.util.HashSet<K> {
    
    private static final long serialVersionUID = 1L;
    protected int offset;
    protected int length;
    protected int frameID;
    protected Set<Short> functionSet;
	
    public KmerSet( int offset, int frameID, int length){
        this.offset = offset;
        this.frameID = frameID;
        this.length = length;
        functionSet = null;
    }
    
    // what if instance is created with reading frame outside of 1-6?
    public int getFrameID() {
        return frameID;
    }
	
    public int getOffset(){
            return this.offset;
    }

    public boolean getIsFwd(){
        return frameID < 4;
    }

    public int getLength(){
            return this.length;
    }
    
    public void setFunctionSet(Set<Short> functSet) {
        functionSet = functSet;
    }
    
    public Set<Short> getFunctionSet() {
        return functionSet;
    }
    
}
