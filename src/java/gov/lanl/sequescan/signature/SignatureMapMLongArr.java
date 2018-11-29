/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.signature;

/**
 *
 * @author jcohn
 */
public class SignatureMapMLongArr extends SignatureMapM<Long,short[]>{
    
    public SignatureMapMLongArr(Boolean hFlag, Integer cntEstimate) {       
         super(hFlag,cntEstimate);
        
    }
   
    public SignatureMapMLongArr(SignatureModule module) {
        super(module);
    }

    @Override
    protected short[] getNewValueListWithNode(Short node) {
        if (node == null)
            return null;
        short[] list = new short[1];     
        list[0] = node;
        return list;
    }

    @Override
    public Long convertKmerStr(String kmer) {
        return getKmerHash(kmer);
    }

    @Override
    protected Short getMapValue(short[] values, int indx) {
        if (values == null)
            return null;
        else
            return values[indx];
    }

    @Override
    protected int getMapValuesLength(short[] values) {
        if (values == null)
            return 0;
        else
            return values.length;
    }

    @Override
    protected short[] getEmptyMapValues(int length) {
        return new short[length];
    }

    @Override
    protected void setMapValue(short[] values, int indx, Short val) {
        if (values == null)
            return;
        values[indx] = val;
    }

    @Override
    protected Long getSignatureObj(String signatureStr) {
        return fromHexString(signatureStr);
    }

}