/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequtils.data;

import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author jcohn
 */
public class NodeAssignment {
        
    protected short nodeNum;
    protected boolean notMonophylFlag;
    protected boolean singleKmerFlag;
    protected boolean singleNodeFlag;
    protected Map<String,TreeSet<Integer>> nodeKmerList;

    public NodeAssignment(short num, boolean notMonophyl,
        boolean singleKmer, boolean singleNode) {
        
        nodeNum = num;
        notMonophylFlag = notMonophyl;
        singleKmerFlag = singleKmer;
        singleNodeFlag = singleNode;
        nodeKmerList = null;
    }
    
    public short getNodeNum() {
        return nodeNum;
    }

    public boolean isNotMonophyl() {
        return notMonophylFlag;
    }
    
    public boolean hasSingleKmer() {
        return singleKmerFlag;
    }
    
    public boolean hasSingleNode() {
        return singleNodeFlag;
    }
    
    public void setNodeKmerList(Map<String,TreeSet<Integer>> nkList) {
        nodeKmerList = nkList;
    }

    public Map<String,TreeSet<Integer>> getNodeKmerList() {
        return nodeKmerList;
    }
    

}
