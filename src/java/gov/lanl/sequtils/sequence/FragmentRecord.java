

package gov.lanl.sequtils.sequence;

public class FragmentRecord {
    protected String seq;
    protected int offset;
    protected boolean isFwd;
    protected int frameID;
	
    public FragmentRecord( String seq, int offset, int frameID) {
        this.seq = seq;
        this.offset = offset;
        this.frameID = frameID;
        isFwd = frameID < 4;
    }

    public String getSeq(){
        return seq;
    }

    public int getOffset(){
        return offset;
    }

    public boolean getIsFwd(){
        return isFwd;
    }
    
    public int getFrameID() {
        return frameID;
    }
    
}
