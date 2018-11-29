/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.lanl.sequtils.util;

/**
 *
 * @author jcohn
 */
public class Counter {
    
    protected int count;

    public Counter() {
        count = 0;
    }

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public void reset() {
        count = 0;
    }

}
