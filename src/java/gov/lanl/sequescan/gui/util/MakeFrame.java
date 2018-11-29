package gov.lanl.sequescan.gui.util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class MakeFrame {
	public JFrame f = null;
	
	public MakeFrame(){
	}
	
	public JFrame getFrame( String title, int width, int height, int xPos, int yPos){
	    f = new JFrame( title);
		f.setSize(width, height);
		f.setLocation( xPos, yPos);
	    
	    f.addWindowListener( new WindowAdapter() {
			// use System.exit(0) to exit the entire application - instead of f.dispose()
			public void windowClosing( WindowEvent e) { f.dispose(); } 
		});
		
		return f;
	}
}
