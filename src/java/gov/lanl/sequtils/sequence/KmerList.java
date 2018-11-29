package gov.lanl.sequtils.sequence;


/* /**
 * 
 * @author J. Cohn 
 * similar to KmerSet
 * 
 */
public class KmerList<K> extends java.util.ArrayList<String> {
    
    private static final long serialVersionUID = 1L;
    protected int frameID;
	
    public KmerList(int frameID){
        this.frameID = frameID;
    }
    
    public int getFrameID() {
        return frameID;
    }

    public boolean getIsFwd(){
            if (frameID < 4)
        return true;
    else
        return false;
    }

}

