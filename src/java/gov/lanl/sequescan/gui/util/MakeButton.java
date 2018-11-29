package gov.lanl.sequescan.gui.util;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JToolTip;

public class MakeButton {	
	public MakeButton(){
	}
	
	public JButton getButton( String title){
		JButton button = new JButton( title);
		button.setBackground( Color.WHITE);
		return button;
	}
	
	public JButton getToolTipButton( String title, String toolTipText){
		JButton button = getButton( title);
		button.setToolTipText( toolTipText);
		return button;
	}
}
