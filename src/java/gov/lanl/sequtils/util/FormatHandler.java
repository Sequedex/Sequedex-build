package gov.lanl.sequtils.util;

import java.text.DecimalFormat;


public class FormatHandler {
	public FormatHandler(){
		
	}
	
	public DecimalFormat getDecimalFormat( int fractionDigits, boolean groupingUsed){
	    DecimalFormat dformat = new DecimalFormat();
	    //dformat.setRoundingMode(RoundingMode.HALF_UP);
	    dformat.setMaximumFractionDigits( fractionDigits);
	    dformat.setGroupingUsed( groupingUsed);
	    return dformat;
	}
}
