/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcohn
 */
public class SignatureMapMStrList extends SignatureMapM<String,List<Short>> {
    
    public SignatureMapMStrList(Boolean hFlag, Integer cntEstimate) {       
         super(hFlag,cntEstimate);
        
    }
   
    public SignatureMapMStrList(SignatureModule module) {
        super(module);
    }

    @Override
    protected List<Short> getNewValueListWithNode(Short node) {
        List<Short> list = new ArrayList<>();      
        list.add(node);
        return list;
    }

    @Override
    public String convertKmerStr(String kmer) {
        return kmer;
    }

    @Override
    protected Short getMapValue(List<Short> values, int indx) {
        if (values == null)
            return null;
        else
            return values.get(indx);
    }

    @Override
    protected int getMapValuesLength(List<Short> values) {
        if (values == null)
            return 0;
        else
            return values.size();
    }

    @Override
    protected List<Short> getEmptyMapValues(int length) {
         List<Short> emptyList = new ArrayList<>(length);
        for (int i=0; i<length; i++)
            emptyList.add(null);
        return emptyList;
    }

    @Override
    protected void setMapValue(List<Short> values, int indx, Short val) {
        if (values == null || values.isEmpty())
            return;
        values.set(indx, val);
    }

    @Override
    protected String getSignatureObj(String signatureStr) {
        return signatureStr;
    }
    
}
