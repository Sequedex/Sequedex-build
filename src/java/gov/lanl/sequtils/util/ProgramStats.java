/** 
 * jc 6/17/14:  replace \n with GeneralConstants.EOL
 * 
*/

package gov.lanl.sequtils.util;

import gov.lanl.sequtils.constants.GeneralConstants;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import gov.lanl.sequescan.constants.AppConstants;

public class ProgramStats implements GeneralConstants {
    

	public ProgramStats(){
		
	}
	
	/**
	 * Function to calculate program duration.
	 *
	 * @param startTime the start time
	 * @return Message with calculated program duration for printing.
	 */
	public String calcProgramDuration( long startTime){
		long endTime = System.currentTimeMillis();
		long time = (endTime-startTime) / 1000;  
		String result;
		if( time < 1000){
			long minutes = time/60;
			long seconds = time-(minutes*60);
			result = EOL+EOL+ "Total elapsed time (m:s): "+ ( minutes + ":" + seconds);  
		}
		else{
			String seconds = Integer.toString((int)(time % 60));  
			String minutes = Integer.toString((int)((time % 3600) / 60));  
			String hours = Integer.toString((int)(time / 3600));  
			for (int i = 0; i < 2; i++) {  
				if (seconds.length() < 2) {  
					seconds = "0" + seconds;  
				}  
				if (minutes.length() < 2) {  
					minutes = "0" + minutes;  
				}  
				if (hours.length() < 2) {  
					hours = "0" + hours;  
				}  
			} 
			result = EOL+EOL+"Total elapsed time (h:m:s) "+ hours + ":" + minutes + ":" + seconds + EOL + EOL;
		}
		return result; 
	}
	
	/**
	 * Function to calculate memory usage.
	 * 
	 * @return Message with calculated memory usage for printing.
	 */
	public StringBuilder calcMemoryUsage(){
	    StringBuilder sb = new StringBuilder();

	    long maxMemoryBytes = Runtime.getRuntime().maxMemory();			//Returns the maximum amount of memory that the Java virtual machine will attempt to use.
	    long totMemoryBytes = Runtime.getRuntime().totalMemory();			//Returns the total amount of memory in the Java virtual machine
	    long freeMemoryBytes = Runtime.getRuntime().freeMemory();			//Returns the amount of free memory in the Java Virtual Machine.
	    long usedMem = Runtime.getRuntime().totalMemory() - freeMemoryBytes;
	       
	    sb.append(EOL+EOL+"Max. Memory: " + maxMemoryBytes + " = " + maxMemoryBytes/AppConstants.MEGABYTE + "M\t\t(maximum amount of memory that the JVM will attempt to use)"+EOL);
	    sb.append("Tot. Memory: " + totMemoryBytes + " = " + totMemoryBytes/AppConstants.MEGABYTE + "M\t\t(total amount of memory in the JVM)"+EOL);
	    sb.append("Used Memory: " + usedMem + " = " + usedMem / AppConstants.MEGABYTE + "M\t\t(used amount of memory in the JVM)"+EOL);
	    sb.append("Free Memory: " + freeMemoryBytes + " = " + freeMemoryBytes/AppConstants.MEGABYTE + "M\t\t(amount of free memory in the JVM"+EOL);
	  
	    return sb;
	}
	
	/**
	 * Function to calculate CPU usage.
	 * 
	 * @return Message with calculated CPU usage in percent for logging.
	 */
	//http://download.oracle.com/javase/1,5.0/docs/api/java/lang/management/ManagementFactory.html
	//http://www.java-forums.org/new-java/5303-how-determine-cpu-usage-using-java.html
	public String calcCPUusage(){
		ThreadMXBean TMB = ManagementFactory.getThreadMXBean();
		long time = new Date().getTime() * 1000000;
		long cput = 0;
		double cpuperc;
		String msg;
		 
		if( TMB.isThreadCpuTimeSupported() )
		{
                    if(new Date().getTime() * 1000000 - time > 1000000000) //Reset once per second
                    {
                        time = new Date().getTime() * 1000000;
                        cput = TMB.getCurrentThreadCpuTime();
                    }
	                 
                    //Tests if thread CPU time measurement is enabled
                    if(!TMB.isThreadCpuTimeEnabled())
                    {
                        TMB.setThreadCpuTimeEnabled(true);
                    }
	                     
                    if(new Date().getTime() * 1000000 - time != 0){
                                    //TMB.getCurrentThreadCpuTime() returns the total CPU time for the current thread in nanoseconds
                                    //cpuperc in this case holds the aprox. cpu usage in %
                                    cpuperc = (TMB.getCurrentThreadCpuTime() - cput) / (new Date().getTime() * 1000000.0 - time) * 100.0; 
                                    msg = "Thread CPU Time supported - cpuperc: " + cpuperc + "%";
                    }
                    else
                        msg = null;
                }
                else
                {
                    msg = "Thread CPU Time NOT supported!";
                }
		return msg;
	}
}
