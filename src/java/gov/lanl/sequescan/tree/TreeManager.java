/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequescan.tree;

import ch.qos.logback.classic.Level;
import gov.lanl.sequescan.constants.TreeConstants;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.StringOps;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.forester.io.parsers.phyloxml.PhyloXmlParser;
import org.forester.io.writers.PhylogenyWriter;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyMethods;
import org.forester.phylogeny.PhylogenyNode;
import org.forester.phylogeny.data.NodeData;
import org.forester.phylogeny.data.PropertiesList;
import org.forester.phylogeny.data.Property;
import org.forester.phylogeny.data.Taxonomy;
import org.forester.phylogeny.iterators.PhylogenyNodeIterator;

/**
 *
 * @author jcohn
 */
public class TreeManager extends MessageManager implements TreeConstants {
    
    // instance variables
    protected String treeSource;
    protected Phylogeny[] trees;
    
    public TreeManager(String treeSourceStr) {
        treeSource = treeSourceStr;
        trees = null;
    }
    
    public String getTreeSource() {
        return treeSource;
    }
      
    public Phylogeny[] getTrees() {
        return trees;
    }
    
    public boolean readTreeFromFile(String treeFileName) {
        if (treeFileName == null) {
            publish("Null tree file name",this,Level.ERROR);
            return false;
        }
        if (trees != null) {
            publish("Manager already has a tree",this,Level.INFO);
            return true;
        }
        File file = new File(treeFileName);
        if (!file.exists()) {
            publish("Tree file "+treeFileName+" does not exist",this,Level.ERROR);
            return false;
        }
        else
            publish("Parsing tree file "+treeFileName,this,Level.INFO);
        PhyloXmlParser parser = PhyloXmlParser.createPhyloXmlParser();
        parser.setSource(file);
  
        try {
            trees = parser.parse();
            publish("Number of trees in file "+treeFileName+": "+trees.length,this,Level.INFO);
            // What did hashIDs() do?  It no longer exists in forester1050
//            if (trees != null && trees.length > 0) {
//                for (Phylogeny tree : trees) {
//                    tree.hashIDs();
//                }
//            }
        } catch (IOException ex) {
            publish("TreeManager: Problem parsing "+treeFileName,this,Level.ERROR);
            publish(ex.getMessage(),this,Level.ERROR);
            trees = null;
            return false;
        }
        return true;
    }
    
    public boolean readTreeFromStream(InputStream in) {
   
        PhyloXmlParser parser = PhyloXmlParser.createPhyloXmlParser();
        parser.setSource(in);
  
        try {
            trees = parser.parse();
            publish("Number of trees from "+treeSource+
                ": "+trees.length,this,Level.INFO);
// What did hashIDs() do?  It no longer exists in forester1050
//            if (trees != null && trees.length > 0) {
//                for (Phylogeny tree : trees) {
//                    tree.hashIDs();
//                }
//            }
        } catch (IOException ex) {
            publish("TreeManager: Problem parsing tree from "+
                treeSource,this,Level.ERROR);
            publish(ex.getMessage(),this,Level.ERROR);
            trees = null;
            return false;
        }
        return true;
    }
    
    public boolean createParentNameSets() {
        return createParentNameSets(0);
    }
    
    public boolean createParentNameSets(int idx) {
   
        publish("Creating parent name sets",this,Level.INFO);
        if (trees == null) {
                publish("createParentSets: No phylogenies have been parsed",this,Level.ERROR);
                return false;            
        }
        else {
            Phylogeny tree = trees[idx]; 
            Integer lastPropertyGrp = getLastPropertyGrp(tree);
            String keyStr = lastPropertyGrp.toString()+":"+PARENT_NAME_SET;
            PhylogenyNodeIterator nodeIter = tree.iteratorPreorder();
            while (nodeIter.hasNext()) {
                PhylogenyNode node = nodeIter.next();
  //              long nodeID = node.getId();
                NodeData data = node.getNodeData();
                PropertiesList props = data.getProperties();
                if (props == null) {
                    props = new PropertiesList();
                    data.setProperties(props);
                }
                java.util.List<Property> parentSetProps = props.getProperties(keyStr);
                if (parentSetProps != null && !parentSetProps.isEmpty()) {
                    publish("Node "+node.getId()+": overwriting existing parent set for "+keyStr,
                        this,Level.INFO);
                    java.util.List<Property> allProps = props.getProperties();
                    Iterator<Property> piter = parentSetProps.iterator();
                    while (piter.hasNext()) 
                        allProps.remove(piter.next());
                }
 
                String parentNameStr = getParentNameStr(node); 
                if (parentNameStr == null|| parentNameStr.trim().equals("")) 
                    parentNameStr = "";
                Property newProp = new Property(keyStr,parentNameStr,
                    UNIT_SET,TYPE_STR,Property.AppliesTo.NODE);
                props.addProperty(newProp);
            }
            return true;
        }
    }    
    
    // note:  node names should have already been set to internal (non-terminal)
    // node ids (based on depth-first traversal)
    public String getParentNameStr(PhylogenyNode node) {
        StringBuilder buffer = new StringBuilder(1000);
        PhylogenyNode lastParent = node;
        PhylogenyNode parent = lastParent.getParent();
        while (parent != null) {
            String delim;
            if (buffer.length() == 0)
                delim = "";
            else
                delim = DELIM;   
            buffer.append(delim);
            buffer.append(parent.getName());
            lastParent = parent;
            parent = lastParent.getParent();            
        }
        return buffer.toString();
    }
    
    public String getPathPrefix() {
        return getPathPrefix(0);
    }
    
    public String getPathPrefix(int idx) {
        Phylogeny tree = getTree(idx);
        PhylogenyNode node = tree.getRoot();
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        Set<String> refSet = getPropertyRefSet(plist);
        Iterator<String> refIter = refSet.iterator();
        while (refIter.hasNext()) {
            String ref = refIter.next();
            if (ref.endsWith(GRP_ACTION)) {
                java.util.List<Property> actionProps = plist.getProperties(ref);
                if (actionProps == null || actionProps.size() != 1) {
                    publish("Problem getting prefix for ref="+ref,this,Level.ERROR);
                    return null;
                }
                Property p = actionProps.get(0);
                String value = p.getValue();
                if (value.equals(PARENT_NAME_SET)) {
                    StringTokenizer stok = new StringTokenizer(ref,":");
                    String prefix = stok.nextToken();
//                    System.out.println("parents info group has prefix: "+prefix);
                    return prefix;
                }
            }
        }
        return null;
    }

    
    public Map<Short,short[]> getPathMap() {
        return getPathMap(0);
    }
    
    
    public Map<Short,short[]> getPathMap(int idx) {
        
        String prefix = getPathPrefix(idx);
        if (prefix == null) {
            publish("Prefix for parentSet is missing",this,Level.ERROR);
            return null;
        }
            
       
        Map<Short,short[]> pathMap = new TreeMap<>();
        Phylogeny tree = getTree(idx);
        PhylogenyNodeIterator nodeIter = tree.iteratorPreorder();
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            boolean isInternal = node.isInternal();
            if (!isInternal)
                continue;
            String nodeName = node.getName();
            Short nodeNum = StringOps.getShort(nodeName);
            if (nodeNum == null) {
                publish("Node name is not a number: "+nodeName,this,Level.ERROR);
                return null;
            }
            NodeData data = node.getNodeData();
            if (data == null && !node.isRoot()) {
                publish(
                "NodeData for node name "+nodeName+" is missing",this,Level.WARN);
                continue;               
            }
            PropertiesList plist = data.getProperties();
            if (plist == null) {
                publish("PropertiesList for node name "+nodeName+"  is missing",this,Level.WARN);
                continue;
            }
            // for now assume parents property is labelled with info count of 0   ???
            java.util.List<Property> refProps = plist.getProperties(prefix+":"+PARENT_NAME_SET);
            if (refProps == null || refProps.size() != 1) {
                publish("Problem getting parent name set for node name "+nodeName,this,Level.ERROR);
                return null;
            }
//            if (p == null) {
//                    logger.error(
//                    "Parent Name Set for node name "+nodeName+"  is missing");
//                return null;
//            }
            Property p = refProps.get(0);
            String value = p.getValue();
            short[] path;
            String[] nodeArr = StringOps.getTokens(value,DELIM);
            if (nodeArr == null || nodeArr.length < 1) {
                path = new short[1];
                path[0] = nodeNum;
            }
            else {
                path = new short[nodeArr.length+1];
                for (int i=0; i<nodeArr.length; i++) {
                    Short pNodeNum = StringOps.getShort(nodeArr[nodeArr.length-i-1]);
                    path[i] = pNodeNum;
                }
                path[nodeArr.length] = nodeNum;
                    
            }

            pathMap.put(nodeNum, path);
            
            // display path
//            System.out.println("node: "+nodeNum.toString());
//            for (int i=0; i<path.length; i++)
//                System.out.println(path[i]);
        }
        
        return pathMap;
    }
    
    public Map<String,List<Short>> getLeafPathMap() {
        return getLeafPathMap(0);
    }
    
    
    public Map<String,List<Short>> getLeafPathMap(int idx) {
        
        String prefix = getPathPrefix(idx);
        if (prefix == null) {
            publish("Prefix for parentSet is missing",this,Level.ERROR);
            return null;
        }
            
       
        Map<String,List<Short>> pathMap = new TreeMap<>();
        Phylogeny tree = getTree(idx);
        PhylogenyNodeIterator nodeIter = tree.iteratorPreorder();
        int leafCnt = 0;
        int allCnt = 0;
        while (nodeIter.hasNext()) {
            allCnt++;
            PhylogenyNode node = nodeIter.next();
            boolean isInternal = node.isInternal();
            if (isInternal)
                continue;
            else
                leafCnt++;
            String nodeName = node.getName();
            NodeData data = node.getNodeData();
            if (data == null && !node.isRoot()) {
                publish(
                "NodeData for node name "+nodeName+" is missing",this,Level.WARN);
                continue;               
            }
            PropertiesList plist = data.getProperties();
            if (plist == null) {
                publish("PropertiesList for node name "+nodeName+"  is missing",this,Level.WARN);
                continue;
            }
            // for now assume parents property is labelled with info count of 0   ???
            java.util.List<Property> refProps = plist.getProperties(prefix+":"+PARENT_NAME_SET);
            if (refProps == null || refProps.size() != 1) {
                publish("Problem getting parent name set for node name "+nodeName,this,Level.ERROR);
                continue;
            }

            Property p = refProps.get(0);
            String value = p.getValue();
            List<Short> path;
            String[] nodeArr = StringOps.getTokens(value,DELIM);
            if (nodeArr == null || nodeArr.length < 1) {
                publish("Empty parent name set for node name "+nodeName,this,Level.ERROR);
                continue;
            }
            else {
                path = new ArrayList<>(nodeArr.length);
                for (int i=0; i<nodeArr.length; i++) {
                    Short pNodeNum = StringOps.getShort(nodeArr[nodeArr.length-i-1]);
                    path.add(pNodeNum);
                }
                    
            }

            List<Short> existingPath = pathMap.get(nodeName);
            if (existingPath != null)
                publish("Path for node "+nodeName+" already exists;  overwriting",
                    this, Level.ERROR);
            pathMap.put(nodeName, path);           
        }
        
        publish("Number of nodes traversed: "+allCnt,this,Level.INFO);
        publish("Number of leaf nodes: "+leafCnt,this,Level.INFO);
        publish("Number of nodes: "+tree.getNodeCount(),this,Level.INFO);
        
        return pathMap;
    }
    
    public boolean setNamesToInternalNodeIds() {
        return setNamesToInternalNodeIds(0);
    }

    public String getNodeName(int nodeID) {
        return getNodeName(0,nodeID);
    }
    
    public String getNodeName(int idx, int nodeID) {
        
        if (trees == null) {
            publish("No phylogenies have been parsed",this,Level.ERROR);
            return EMPTY_TREE;          
        }
   
        Phylogeny tree = trees[idx];
        PhylogenyNode node = tree.getNode(nodeID);
        return node.getName();
    }

    public boolean deleteNode(int nodeID) {
        return deleteNode(0, nodeID);
    }

    public boolean deleteNode(int idx, int nodeID) {

        if (trees == null) {
            publish("No phylogenies have been parsed",this,Level.ERROR);
            return false;
        }

        Phylogeny tree = trees[idx];
        if (tree == null) {
            publish("No tree at index "+idx,this,Level.ERROR);
            return false;
        }

        PhylogenyNode node = tree.getNode(nodeID);

        PhylogenyMethods.removeNode(node, tree);

        return true;
    }
    
    public boolean changeNodeName(int nodeID, String newName) {
        return changeNodeName(0,nodeID,newName);
    }

    public boolean changeNodeName(int idx, int nodeID, String newName) {

        if (trees == null) {
            publish("No phylogenies have been parsed",this,Level.ERROR);
            return false;
        }

        Phylogeny tree = trees[idx];
        if (tree == null) {
            publish("No tree at index "+idx,this,Level.ERROR);
            return false;
        }
        PhylogenyNode node = tree.getNode(nodeID);
        node.setName(newName);

        return true;
    }
    
    public boolean setNamesToInternalNodeIds(int idx) {
        publish("Set names to internal nodeIds",this,Level.INFO);
        if (trees == null) {
            publish("No phylogenies have been parsed",this,Level.ERROR);
            return false;            
        }
        else {
            Phylogeny tree = trees[idx];
            int internalNodeCount = getNumberInternalNodes();
            int padNum;
            if (internalNodeCount < 1000)
                padNum = 3;
            else if (internalNodeCount < 10000)
                padNum = 4;
            else
                padNum = 5;
            PhylogenyNodeIterator nodeIter = tree.iteratorPreorder();
            int count = 0;
            while (nodeIter.hasNext()) {
                PhylogenyNode node = nodeIter.next();
                boolean isInternal = node.isInternal();
                String nodeName = node.getName();
                long nodeID = node.getId();
                if (isInternal) {
                    String numStr = Integer.toString(count++);
                    String newNodeName = StringOps.leftFill(numStr,padNum,'0');
                    node.setName(newNodeName);
                    boolean isDouble = StringOps.isDouble(nodeName);
                    if (nodeName != null && !nodeName.equals("") && !isDouble) {
                        Taxonomy tax = new Taxonomy();
                        tax.setCommonName(nodeName);
                        NodeData ndata = node.getNodeData();
                        ndata.setTaxonomy(tax);
                    } 
                }
            }
            return true;
        }
    }
    
    public Phylogeny getTree() {
        return getTree(0);
    }
    
    public static String getNodeRefName(String nodeRef) {
        String[] fields = StringOps.getTokens(nodeRef,NODE_REF_DELIM);
        if (fields == null || fields.length < 2)
            return null;
        else
            return fields[1];
    }
    
    public static Integer getNodeRefNumber(String nodeRef) {
        String[] fields = StringOps.getTokens(nodeRef,NODE_REF_DELIM);
        if (fields == null || fields.length < 2)
            return null;
        else
            return StringOps.getInteger(fields[0]);
    }
    
    public int getNumberInternalNodes() {
        return getNumberInternalNodes(0);
    }
    
    public int getNumberInternalNodes(int idx) {
        PhylogenyNodeIterator nodeIter = getNodeIterator(idx);
        int internalCount = 0;
        while (nodeIter.hasNext()) {
            PhylogenyNode node = nodeIter.next();
            if (node.isInternal())
                internalCount++;
        }

        return internalCount;
    }
    
    public PhylogenyNodeIterator getNodeIterator() {
        return getNodeIterator(0,PREORDER);
    }
    
    public PhylogenyNodeIterator getNodeIterator(int idx) {
        return getNodeIterator(idx,PREORDER);
    }
    
    public PhylogenyNodeIterator getNodeIterator(int idx, int iteratorType) {
        Phylogeny tree = getTree(idx);
        switch(iteratorType) {
            case PREORDER:
                return tree.iteratorPreorder();
            case POSTORDER:
                return tree.iteratorPostorder();
            case LEVELORDER:
                return tree.iteratorLevelOrder();
            case EXTERNALFWD:
                return tree.iteratorExternalForward();
            default:
                publish("Unknown iterator type: "+iteratorType,this,Level.ERROR);
                return null;
        }
        
    }
        
    
    public Phylogeny getTree(int idx) {
        if (trees == null) {
            publish("No trees",this,Level.ERROR);
            return null;
        }
        else if (idx > trees.length - 1) {
            publish("No tree at index "+idx,this,Level.ERROR);
            return null;
        }
        return trees[idx];
    }
    
    public Property getPhylogenyProperty(String ref) {
        return getPhylogenyProperty(ref,0);
    }
    
    public Property getPhylogenyProperty(String ref, int idx) {
        Phylogeny tree = getTree(idx);
        PhylogenyNode rootNode = tree.getRoot();
        String nodeName = rootNode.getName();
        return getProperty(tree,nodeName,ref);
    }
    
    public Property getProperty(String nodeName,String ref) {
        return getProperty(nodeName, ref, 0); 
    }
    
    public Property getProperty(String nodeName, String ref, int idx) {
        Phylogeny tree = getTree(idx);
        return getProperty(tree,nodeName,ref);
    }
    
    public static Property getProperty(Phylogeny tree, String nodeName, String ref) {
        if (nodeName == null || ref == null || tree == null)
            return null;
        PhylogenyNode node = tree.getNode(nodeName);
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        Property p; 
        if (plist == null)
            return null;
        else {
            java.util.List<Property> refProps = null;
            try {
                refProps = plist.getProperties(ref);
                if (refProps == null || refProps.size() != 1) {
                    publish("Problem getting property with ref="+ref+" for nodeName "+nodeName,tree,Level.ERROR);
                    return null;
                }
                else
                    return refProps.get(0);
            }catch (IllegalArgumentException ex) {
                publish("Node "+
                    nodeName+": problem getting property "+ref+
                    " - "+ex.getMessage(),tree,Level.ERROR);
                return null;
            }
        }
    }
    
    public Set<String> getPropertyRefs(String nodeName) {
        return getPropertyRefs(nodeName, 0); 
    }
    
    public Set<String> getPropertyRefs(String nodeName, int idx) {
        Phylogeny tree = getTree(idx);
        return getPropertyRefs(tree,nodeName);
    }
    
    public static Set<String> getPropertyRefs(Phylogeny tree, String nodeName) {
        PhylogenyNode node = tree.getNode(nodeName);
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        if (plist == null)
            return null;
        else
            return getPropertyRefSet(plist);
    }
  
   
    public String getRootNodeName() {
        return getRootNodeName(0);
    }
    
    public String getRootNodeName(int idx) {
        Phylogeny tree = getTree(idx);
        return tree.getRoot().getName();
    }
    
    
    public boolean writeTree(File outfile) {
        return writeTree(outfile,0);
    }
    
    public boolean writeTree(File outfile, int idx) {
        
        File fullOutfile;
        
        if (outfile.exists()) {
            String outfileName = outfile.getAbsolutePath();
            publish("File "+outfileName+" exists - adding date hash to name",this,Level.INFO);
            java.util.Date today = new java.util.Date();
            String outfileRoot;
            if (outfileName.endsWith(PHYLOXML)) {
                outfileRoot = outfileName.substring(0,outfileName.length() - PHYLOXML.length());
                fullOutfile = new File(outfileRoot+"."+today.hashCode()+PHYLOXML);
            }
            else 
                fullOutfile = new File(outfileName+"."+today.hashCode());         
        }
        else
            fullOutfile = outfile;
        
        Phylogeny tree = getTree(idx);
        PhylogenyWriter writer = PhylogenyWriter.createPhylogenyWriter();
               try {
        writer.toPhyloXML(fullOutfile,tree,1);
        } catch (IOException ex) {
            publish("writing tree failed",this,Level.ERROR);
            publish(ex.getMessage(),this,Level.ERROR);
            return false;
        }
        
        return true;
    }
    
    public static java.util.Set<String> getPropertyRefSet(PropertiesList plist) {
        if (plist == null || plist.size() == 0)
            return null;
        java.util.Set<String> refSet = new java.util.TreeSet<>();
        java.util.List<Property> props = plist.getProperties();
        Iterator<Property> piter = props.iterator();
        while (piter.hasNext()) {
            Property p = piter.next();
            refSet.add(p.getRef());
        }
        return refSet;         
    }
    
    public Integer getLastPropertyGrp() {
        return getLastPropertyGrp(0);
    }
    
    public Integer getLastPropertyGrp(int ndx) {
        Phylogeny tree = getTree(ndx);
        return getLastPropertyGrp(tree);
    }
    
    public static Integer getLastPropertyGrp(Phylogeny tree) {
        PhylogenyNode node = tree.getRoot();
        if (node == null) {
            publish("getPhylogenyInfoNum: no root node exists",tree,Level.ERROR);
            return null;
        }
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        if (plist == null) 
            return null;
        else {
            java.util.Set<String> refSet = getPropertyRefSet(plist);
            TreeSet<Integer> nums = new TreeSet<>();
            Iterator<String> rIter = refSet.iterator();
            while (rIter.hasNext()) {
                String ref = rIter.next();
                if (ref.endsWith(":"+GRP_DATE)) {
                    StringTokenizer stok = new StringTokenizer(ref,":");
                    String prefix = stok.nextToken();
                    Integer num = StringOps.getInteger(prefix);
                    if (num != null)
                        nums.add(num);
                }
            }
            return nums.last();
        }
    }
  
    
    public Integer nextPropertyGrp(String action) {
        return nextPropertyGrp(action,0);
    }
    
    public Integer nextPropertyGrp(String action, int ndx) {
        Phylogeny tree = getTree(ndx);
        return nextPropertyGrp(treeSource,action,tree);
    }
    
    public static Integer nextPropertyGrp(String treeFileName,String action,
        Phylogeny tree) {
        PhylogenyNode node = tree.getRoot();
        if (node == null) {
            publish("incrPhylogenyInfoNum: no root node exists",tree,Level.ERROR);
            return null;
        }
        NodeData data = node.getNodeData();
        int nextGrp;
        PropertiesList plist = data.getProperties();
        if (plist == null) {
            plist = new PropertiesList();
            data.setProperties(plist);
            nextGrp = 0;
        }
        else {
            Integer lastGrp = getLastPropertyGrp(tree);
            nextGrp = lastGrp+1;
        }
        
        Integer nextGrpInt = nextGrp;
       
        java.util.Date today = new java.util.Date();
        Property dateProp = new Property(nextGrpInt.toString()+":"+GRP_DATE,
            today.toString(),UNIT_DATE, TYPE_STR, Property.AppliesTo.PHYLOGENY);
        plist.addProperty(dateProp);
        Property treeProp = new Property(nextGrpInt.toString()+":"+SRC_TREE,
            treeFileName,UNIT_INFO, TYPE_STR, Property.AppliesTo.PHYLOGENY);
        plist.addProperty(treeProp);
        Property actionProp = new Property(nextGrpInt.toString()+":"+GRP_ACTION,
            action,UNIT_INFO, TYPE_STR, Property.AppliesTo.PHYLOGENY);
        plist.addProperty(actionProp);
        return nextGrpInt;
        
    }
      
    public boolean addPhylogenyProperty(String key, String value,String unit, String dataType) {
        return addPhylogenyProperty(key,value,unit,dataType,0);
    }
    
       
    public boolean addPhylogenyProperty(String key, String value, String unit,
        String dataType,int idx) {
        Phylogeny tree = getTree(idx);
        return addPhylogenyProperty(tree,key,value,unit,dataType);
    }
    
    public static boolean addPhylogenyProperty(Phylogeny tree, String key, String value,
        String unit, String dataType)  {
        
        Integer lastPropertyGrp = getLastPropertyGrp(tree); 
        if (lastPropertyGrp == null) 
            lastPropertyGrp = nextPropertyGrp("UnknownTreeFile","UnknownAction",tree);
        String keyStr = lastPropertyGrp.toString()+":"+key;
        PhylogenyNode node = tree.getRoot();
        if (node == null) {
            logger.error("There is no root node for "+tree.getName());
            return false;
        }
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        if (plist == null) {
            plist = new PropertiesList();
            data.setProperties(plist);
        }
      
        java.util.List<Property> keyProps = plist.getProperties(keyStr);
        if (keyProps != null) {
            publish("Root property "+keyStr+
                " already exists - cannot overwrite",tree,Level.ERROR);
            return false;
        }
 
        Property newProp = new Property(keyStr,value,unit,dataType, Property.AppliesTo.PHYLOGENY);
        plist.addProperty(newProp); 
        return true;
        
    }
    
    public boolean addNodeProperty(String nodeName,String ref, String value, 
        String unit, String dataType) {
        return addNodeProperty(nodeName,ref,value,unit,dataType,0);
    }
    
    public boolean addNodeProperty(String nodeName,String ref, String value, 
        String unit, String dataType, int idx) {  
        Phylogeny tree = getTree(idx);
        return addNodeProperty(tree,nodeName,ref,value,unit,dataType,false);
    }
    
      public boolean addNodeProperty(String nodeName,String ref, String value, 
        String unit, String dataType, int idx,boolean refIncludesPropertyGrp) {  
        Phylogeny tree = getTree(idx);
        return addNodeProperty(tree,nodeName,ref,value,unit,dataType,
            refIncludesPropertyGrp);
    }
    
    public static boolean addNodeProperty(Phylogeny tree, String nodeName, String ref, String value,
        String unit, String dataType, boolean refIncludesPropertyGrp) {
        
        String keyStr;
        if (refIncludesPropertyGrp)
            keyStr = ref;
        else {
            Integer lastPropertyGrp = getLastPropertyGrp(tree);
            if (lastPropertyGrp == null) 
                lastPropertyGrp = 
                    nextPropertyGrp("UnknownTreeFile","UnknownAction",tree);
            keyStr = lastPropertyGrp.toString()+":"+ref;
        }
        PhylogenyNode node = tree.getNode(nodeName);
        NodeData data = node.getNodeData();
        PropertiesList plist = data.getProperties();
        if (plist == null) {
            plist = new PropertiesList();
            data.setProperties(plist);
        }
        java.util.List<Property> refProps = plist.getProperties(keyStr);
        if (refProps != null) {
            publish("Property "+keyStr+" for node "+
                node.getId()+" already exists - cannot overwrite",tree,Level.INFO);
            return false;
        }                    
        Property newProp = new Property(keyStr,value,unit,dataType, Property.AppliesTo.NODE);
        plist.addProperty(newProp);
        return true;
    }
    
    public static String reverseParentString(String parentStr) {
  
        if (parentStr == null)
            return null;
        StringBuilder builder = new StringBuilder();
        String[] nodeArr = StringOps.getTokens(parentStr,DELIM);
        for (int i=0; i<nodeArr.length; i++) {
            String nodeName = nodeArr[nodeArr.length-i-1];
            if (i==0) 
                builder.append(nodeName);
            else {
                builder.append(DELIM);
                builder.append(nodeName);
            }
        }
        return builder.toString();
    }
    
    public boolean writeNodeInfo(String outputFileName) {
        return writeNodeInfo(outputFileName, 0);
    }
    
    public boolean writeNodeInfo(String outputFileName, int idx) {
        publish("Write Node Info to "+outputFileName,this,Level.INFO);
        int cnt = 0;
        if (trees == null) {
            publish("No phylogenies have been parsed",this,Level.ERROR);
            return false;            
        }
        else {          
            try {
                FileWriter fwriter = new FileWriter(outputFileName);
                BufferedWriter writer = new BufferedWriter(fwriter);
                StringBuilder builder = new StringBuilder();
                builder.append("node_id");
                builder.append(TAB);
                builder.append("node_name");
                builder.append(TAB);
                builder.append("node_type");
                builder.append(TAB);
                builder.append("orig_node_name");
                builder.append(TAB);
                builder.append("parent_set");
                writer.append(builder.toString());
                writer.newLine();
                Phylogeny tree = trees[idx];
                publish("Number of nodes: "+tree.getNodeCount(),this,Level.INFO);
                PhylogenyNodeIterator nodeIter = tree.iteratorPreorder();
                while (nodeIter.hasNext()) {
                    cnt++;
                    builder = new StringBuilder();
                    PhylogenyNode node = nodeIter.next();
                    NodeData ndata = node.getNodeData();
                    boolean isInternal = node.isInternal();
                    String nodeName = node.getName();
                    long nodeID = node.getId();
                    String commonName;
                    Taxonomy tax = ndata.getTaxonomy();
                    if (tax == null)
                        commonName = "NA";
                    else
                        commonName = tax.getCommonName();
                    String parentStr = getParentNameStr(node);
                    if (parentStr == null || parentStr.trim().equals(""))
                        parentStr = "NA";
                    String reverseParentStr = reverseParentString(parentStr);
                    builder.append(nodeID);
                    builder.append(TAB);
                    builder.append(nodeName);
                    builder.append(TAB);
                    if (!isInternal) {
                        builder.append("leaf");
                    } else {
                        builder.append("internal");
                    }
                    builder.append(TAB);
                    builder.append(commonName);
                    builder.append(TAB);
                    builder.append(reverseParentStr);
                    writer.append(builder.toString());
                    writer.newLine();
                }
                writer.flush();
                writer.close();
            } catch (IOException | NoSuchElementException ex) {
                publish("Problem in writeNodeInfo", this, Level.ERROR);
                publish(ex.getMessage(), this, Level.ERROR);
                return false;
            }
            publish("nodes traversed: "+cnt);
            return true;
        }
   
    }
    
    public static void main(String[] args) {
        
        String action = "node_info";
        String treeFile = "/Users/jcohn/sequedexDev/dataModuleDev/input/virus10k.pset.phyloxml";
        String outputFileName = "/Users/jcohn/Desktop/tree4.txt";
        
//        if (args == null || args.length < 2) {
//            System.out.println("args:  action treeFile [other args]");
//            System.exit(1);
//        }
//        else {
//            action = args[0]; 
//            treeFile = args[1];
//        }
        
        File treeFileObj = new File(treeFile);
   
                       
        if (!treeFileObj.exists())  {
            System.out.println(treeFile+" does not exists");
            System.exit(2);
        }       
        else if (!treeFile.endsWith(PHYLOXML)) {
            System.out.println("File "+treeFile+" does not have extension "+PHYLOXML);
            System.exit(3);
        }
        else if (action.equals(PROCESS_TREE) && (treeFile.endsWith(NODE_EXT) || treeFile.endsWith(PSET_EXT))) {
            System.out.println("File "+treeFile+" has already been processed");
            System.exit(4);
        }
        
        TreeManager mgr = new TreeManager(treeFile);
        boolean okay = mgr.readTreeFromFile(treeFile);
        if (!okay)
            System.exit(5);
       
        switch(action) {
            case PROCESS_TREE:
                mgr.nextPropertyGrp(PARENT_NAME_SET);
                okay = mgr.setNamesToInternalNodeIds();
                if (!okay)
                    System.exit(2);
                okay = mgr.createParentNameSets();
                if (!okay)
                    System.exit(3);
                else if (treeFile.endsWith(PHYLOXML)) {
                    String outfileRoot = treeFile.substring(0,treeFile.length() - PHYLOXML.length()); 
                    outputFileName = outfileRoot+PSET_EXT;
                }
                else
                    outputFileName = treeFile + PSET_EXT;
                File outfile = new File(outputFileName);
                logger.info("Writing revised tree to file "+outfile.getAbsolutePath());
                okay = mgr.writeTree(outfile);
                break;
            case NODE_INFO:
//                if (args.length < 2) {
//                    System.out.println("args:  action outputFileName");
//                    System.exit(6);
//                }
//                else
//                    outputFileName = args[1];
                okay = mgr.writeNodeInfo(outputFileName);
                break;
            default:
                okay = true;
                System.out.println("Nothing to do");
        }
      
        if (!okay)
            System.exit(7);
        else
            System.exit(0);

    }

}
