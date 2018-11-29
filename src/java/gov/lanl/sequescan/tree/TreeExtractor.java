/*
 *   Not yet converted for use with GUI
 */
package gov.lanl.sequescan.tree;

import gov.lanl.sequescan.constants.TreeConstants;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.FileObj;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.NodeData;
import org.forester.phylogeny.data.Property;
import org.forester.phylogeny.data.Taxonomy;
import org.forester.phylogeny.iterators.PhylogenyNodeIterator;

/**
 *
 * @author jcohn
 */
public class TreeExtractor extends MessageManager implements TreeConstants {
           
    // instance variables
    protected TreeManager treeMgr;
    
    public TreeExtractor(String fileName) {
        readTreeFile(fileName);  
    }
    
    public TreeExtractor(TreeManager mgr) {
        treeMgr = mgr;
    }
   
    
    public final boolean readTreeFile(String treeFileName) {
        if (treeFileName == null) {
            logger.error("Null tree file name");
            return false;
        }
        logger.info("Reading phyloxml file "+ treeFileName);
        treeMgr = new TreeManager(treeFileName);
        boolean okay = treeMgr.readTreeFromFile(treeFileName);
        if (!okay) {
            logger.error("Treetractor readTreeFile failed");
            return false;
        }
        else { 
            int internalNodeCount = treeMgr.getNumberInternalNodes();
            logger.debug(internalNodeCount+" internal nodes in "+
                treeMgr.getTreeSource());
            return true;
        }
            
    }
    
   
    public boolean execute(String action, String outputfileName) {
        
        System.out.println("action = "+action);
        System.out.println("outputfileName = "+outputfileName);
        switch (action) {
            case WRITE_CHILDREN_ALL:
                return writeChildren(outputfileName, true);
            case WRITE_CHILDREN:
                return writeChildren(outputfileName, false);
            case WRITE_TAXONOMY:
                return writeTaxonomy(outputfileName);
            case WRITE_NODE_NAMES:
                return writeNames(outputfileName);
            case WRITE_LEAVES:
                return writeExternalNodes(outputfileName);
            case WRITE_DESC_COUNTS:
                return writeDescCounts(outputfileName);
            default:
                logger.error("Unknown TreeExtractor action: "+action);
                return false;
        }
    }
      
    protected void addDescendantNames(PhylogenyNode node, Set<String> descNames) {
        
        List<PhylogenyNode> descendants = node.getDescendants();
        Iterator<PhylogenyNode> descIter = descendants.iterator(); 
        while (descIter.hasNext()) {
            PhylogenyNode dnode = descIter.next();
            if (dnode.isExternal())
                continue;
            String dname = dnode.getName();
            descNames.add(dname);
            addDescendantNames(dnode,descNames);
        }
    }
      
 
    public boolean writeNames(String outputfileName) {
        
        logger.info("Writing node names from tree source "+
            treeMgr.getTreeSource()+" to "+
            outputfileName);
        
        File outfilef = new File(outputfileName);  
        if (outfilef.exists()) {
            java.util.Date today = new java.util.Date();
            String dateHash = Integer.toString(today.hashCode());
            outputfileName = outputfileName+"." +dateHash;
        }
        FileObj outfile = new FileObj(outputfileName);
        outfile.appendLine("node_id"+TAB+"node_type"+TAB+"type_cnt"+TAB+"node_name"+
            TAB+"create_nodeID"+TAB+"in_name");
        Phylogeny tree = treeMgr.getTree();
        PhylogenyNode rootNode = tree.getRoot();
        String rootNodeName = rootNode.getName();

        // typeCnt starts with 0 for internal nodes and 1 for leaf nodes
        int internalNum = 0;
        int leafNum = 0;
        int otherNum = 0;
        PhylogenyNodeIterator nodeIter = treeMgr.getNodeIterator();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            String nodeName = node.getName();
            if (nodeName == null)
                nodeName = "";
            boolean isInternal = node.isInternal();
            boolean isExternal = node.isExternal();
            String nodeType;
            if (isInternal && !isExternal)
                nodeType = "internal";
            else if (isExternal && !isInternal)
                nodeType = "leaf";
            else {
                nodeType = "other";
                System.out.println("other: "+isInternal+" "+isExternal);
            }
            long nodeId = node.getId(); 
            String createNodeID = "NA";
            String inName = "NA";
            Set<String> nodeRefs = treeMgr.getPropertyRefs(nodeName);
            if (nodeRefs != null) {
                Iterator<String> refIter = nodeRefs.iterator();
                while(refIter.hasNext()) {
                    String nodeRef = refIter.next();  
                    String nodeRefName = TreeManager.getNodeRefName(nodeRef);
                    if (nodeRefName.startsWith(INPUT_NAME) || 
                        nodeRefName.equals(CREATION_NODE_ID)) {
                        String value;
                        Property p = treeMgr.getProperty(nodeName,nodeRef);
                        if (p != null)
                            value = p.getValue();  
                        else 
                            value = "NA";
                        if (nodeRefName.startsWith(INPUT_NAME))
                            inName = value;
                        else
                            createNodeID = value;                       
                    }
                }
            }
            switch (nodeType) {
                case "internal":
                    outfile.appendLine(nodeId+TAB+nodeType+TAB+(internalNum++)+TAB+nodeName+
                        TAB+createNodeID+TAB+inName);
                    break;
                case "leaf":
                    outfile.appendLine(nodeId+TAB+nodeType+TAB+(leafNum++)+TAB+nodeName+
                        TAB+createNodeID+TAB+inName);
                    break;
                default:
                    outfile.appendLine(nodeId+TAB+nodeType+TAB+(otherNum++)+TAB+nodeName+
                        TAB+createNodeID+TAB+inName);
                    break;
            }
        }
        return true;
    }
    
    public boolean writeDescCounts(String outputfileName) {
        
        logger.info("Writing descdendant counts from tree file "+
            treeMgr.getTreeSource()+" to "+
            outputfileName);
        File outfilef = new File(outputfileName);  
        if (outfilef.exists()) {
            java.util.Date today = new java.util.Date();
            String dateHash = Integer.toString(today.hashCode());
            outputfileName = outputfileName+"." +dateHash;
        }
        FileObj outfile = new FileObj(outputfileName);
        outfile.appendLine("nodeID"+TAB+"nodeName"+TAB+"DescCnt"+
            TAB+"ExtCnt");
        Phylogeny tree = treeMgr.getTree();
        PhylogenyNode rootNode = tree.getRoot();
//        String rootNodeName = rootNode.getName();
        
        long internalCount = 0;
        PhylogenyNodeIterator nodeIter = treeMgr.getNodeIterator();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            boolean isExternal = node.isExternal();
            if (isExternal)
                continue;           
            long nodeId = node.getId(); 
            String nodeName = node.getName();
            int descCnt = node.getNumberOfDescendants();
            int externalCnt = node.getNumberOfExternalNodes();
            outfile.appendLine(nodeId+TAB+nodeName+TAB+descCnt+
                TAB+externalCnt);
        }
        return true;
    }
    
    public boolean writeExternalNodes(String outputfileName) {
        logger.info("Writing leaves for internal node from tree file "+
            treeMgr.getTreeSource()+" to "+
            outputfileName);
        File outfilef = new File(outputfileName);  
        if (outfilef.exists()) {
            java.util.Date today = new java.util.Date();
            String dateHash = Integer.toString(today.hashCode());
            outputfileName = outputfileName+"." +dateHash;
        }
        FileObj outfile = new FileObj(outputfileName);
        outfile.appendLine("nodeID"+TAB+"nodeName"+TAB+"leaf_nodeID"+TAB+"leaf_name");
        Phylogeny tree = treeMgr.getTree();
        PhylogenyNode rootNode = tree.getRoot();
//        String rootNodeName = rootNode.getName();
        
 //       int internalCount = 0;
        PhylogenyNodeIterator nodeIter = treeMgr.getNodeIterator();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            String nodeName = node.getName();
            boolean isInternal = node.isInternal();
            long nodeId = node.getId();  
            if (!isInternal)
                continue;
            List<PhylogenyNode> leaves = node.getAllExternalDescendants();
            if (leaves == null) {
                System.out.println("N"+nodeId+TAB+nodeName+TAB+0);
            }
            else {
                System.out.println("N"+nodeId+TAB+nodeName+TAB+node.getNumberOfExternalNodes());
                Iterator<PhylogenyNode>  dIter = leaves.iterator();
                while (dIter.hasNext()) {
                    PhylogenyNode leafNode = dIter.next();
                    outfile.appendLine(nodeId+TAB+nodeName+TAB+
                        leafNode.getId()+TAB+leafNode.getName());
                }
            }
 
        }
        return true;
    }

    public boolean writeChildren(String outputfileName, boolean iterate) {
        logger.info("Writing children for each node from tree file "+
            treeMgr.getTreeSource()+" to "+
            outputfileName);
        File outfilef = new File(outputfileName);
        if (outfilef.exists()) {
            java.util.Date today = new java.util.Date();
            String dateHash = Integer.toString(today.hashCode());
            outputfileName = outputfileName+"." +dateHash;
        }
        FileObj outfile = new FileObj(outputfileName);
        outfile.appendLine("parentNodeName"+TAB+"nodeName"+TAB+"childName");
        Phylogeny tree = treeMgr.getTree();
        PhylogenyNode rootNode = tree.getRoot();
 //       String rootNodeName = rootNode.getName();

 //       int internalCount = 0;
        PhylogenyNodeIterator nodeIter = treeMgr.getNodeIterator();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            writeChildrenLines(outfile, node, node.getName(),
                iterate);
        }
        return true;
    }
    
    protected void writeChildrenLines(FileObj outfile, PhylogenyNode node, 
        String parentNameStr, boolean iterate) {
   
        boolean isInternal = node.isInternal();
        if (!isInternal)
            return;
        String nodeName = node.getName();        
        List<PhylogenyNode> children = node.getDescendants();
        if (children != null) {
            Iterator<PhylogenyNode>  dIter = children.iterator();
            while (dIter.hasNext()) {
                PhylogenyNode childNode = dIter.next();
                outfile.appendLine(parentNameStr+TAB+nodeName+TAB+childNode.getName());
                if (iterate) {
                    writeChildrenLines(outfile, childNode, parentNameStr,true);
                }
            }
        }       
    }
    
    public boolean writeDir(String outputfileName) {
        return writeTaxonomy(outputfileName, true, false);
    }
    
    public boolean writeTaxonomy(String outputfileName) {
        return writeTaxonomy(outputfileName, false, false);       
    }
    
    public boolean writeTaxonomy(String outputfileName, boolean withDir, 
        boolean allFlag) {
        logger.info(
            "Writing node taxonomy values from tree file (withDir="+
            withDir+")"+
            treeMgr.getTreeSource()+" to "+
            outputfileName);
        File outfilef = new File(outputfileName);  
        if (outfilef.exists()) {
            java.util.Date today = new java.util.Date();
            String dateHash = Integer.toString(today.hashCode());
            outputfileName = outputfileName+"." +dateHash;
        }
        FileObj outfile = new FileObj(outputfileName);
        String header = "nodeID"+TAB+"nodeName"+TAB+"tax_name"+TAB+"tax_rank";
        if (withDir)
            header += TAB + "dir";
        outfile.appendLine(header);
        Phylogeny tree = treeMgr.getTree();
        PhylogenyNode rootNode = tree.getRoot();
 //       String rootNodeName = rootNode.getName();
        PhylogenyNodeIterator nodeIter = treeMgr.getNodeIterator();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            if (!allFlag && node.isInternal())
                continue;
            String nodeName = node.getName();
            long nodeID = node.getId();
            NodeData data = node.getNodeData();
            if (data == null) {
                logger.warn("Node "+nodeID+" "+
                    nodeName+" does not have NodeData");
                continue;
            }
            Taxonomy tax = data.getTaxonomy();
            if (tax == null) {
                logger.warn("Node "+
                    nodeName+" does not have Taxonomy");
                continue;
            }
            Set<String> propertyNames = treeMgr.getPropertyRefs(nodeName);
            String sciName = tax.getScientificName();
            String rank = tax.getRank();
            long nodeId = node.getId();  
            String line = nodeId+TAB+nodeName+TAB+sciName+TAB+rank;
            if (withDir) {
                String dirName = "NA";
                if (propertyNames == null || propertyNames.isEmpty()) {
                    logger.warn("Node "+nodeID+" "+nodeName+
                    " does not have any properties");
                    dirName = "NA";
                }
                else {
                    Iterator<String> refIter = propertyNames.iterator();
                    while (refIter.hasNext()) {
                        String pName = refIter.next();
                        if (pName.endsWith(":dir")) {
                            Property p = treeMgr.getProperty(nodeName,
                                pName);
                            dirName = p.getValue();
                            break;
                        }
                    }
                }
                line += TAB+dirName;
            }
            outfile.appendLine(line);
        }
        return true;
    }
    
//       public static final String WRITE_CHILDREN = "write_children";
//    public static final String WRITE_TAXONOMY = "write_taxonomy";
//    public static final String WRITE_NODE_NAMES = "write_node_names";
//    public static final String WRITE_LEAVES = "write_leaves";
//    public static final String WRITE_DESC_COUNTS = "write_descendent_counts";
//    
//    
    public static void main(String[] args) {
        
        String treefileName, action, outfileName;
        Integer countType, kmerSize;
        
        if (args.length < 3) {
            System.out.println("args: treefile action outfile");
            System.out.println("Current action list: ");
            System.out.println(WRITE_CHILDREN);
            System.out.println(WRITE_CHILDREN_ALL);
            System.out.println(WRITE_DESC_COUNTS);
            System.out.println(WRITE_LEAVES);
            System.out.println(WRITE_NODE_NAMES);
            System.out.println(WRITE_TAXONOMY);
            
            System.exit(1);
        }
  
        treefileName = args[0];
        action = args[1];
        outfileName = args[2];
                
        TreeExtractor extractor = new TreeExtractor(treefileName);  
        boolean okay = extractor.execute(action,outfileName);
        if (okay) {
            System.out.println("Successful extraction");
            System.exit(0);
        }
        else {
            System.out.println("Extraction failed");
            System.exit(2);
        }
    }
    
}
