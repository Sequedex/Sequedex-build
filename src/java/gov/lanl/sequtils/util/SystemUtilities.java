/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequtils.util;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.GlobalMemory;

/**
 *
 * @author jcohn
 */
public class SystemUtilities {
    
    public static int TOTAL = 0;
    public static int AVAILABLE = 1;
    
    public static long[] getMemory() {        
        SystemInfo sysInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = sysInfo.getHardware();
        GlobalMemory mem = hardware.getMemory();       
        long[] memory = new long[2];
        memory[TOTAL] = mem.getTotal();
        memory[AVAILABLE] = mem.getAvailable();
        return memory;
    }
    
    public static long getTotalMemory() {
        SystemInfo sysInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = sysInfo.getHardware();
        GlobalMemory mem = hardware.getMemory();       
        return mem.getTotal();
    }
    
     public static long getAvailableMemory() {
        SystemInfo sysInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = sysInfo.getHardware();
        GlobalMemory mem = hardware.getMemory();       
        return mem.getAvailable();
    }
      
    public static void main(String[] arg) {
        long[] memory = SystemUtilities.getMemory();
        System.out.println("Total Memory: " + memory[TOTAL]);
        System.out.println("Available Memory: " + memory[AVAILABLE]);
    }
    
}
