package gov.lanl.sequescan.gui.util;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class MakePanel {
	
	public MakePanel(){
	}
	
	public JPanel getFlowLayoutPanel(){
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout( new FlowLayout());
		return p;
	}
	
	public JPanel getBorderLayoutPanel(){
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout( new BorderLayout());
		return p;
	}
	
	public JPanel getGridLayoutPanel(int row, int col){
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout( new GridLayout( row, col, 5, 5));
		return p;
	}
	
	public JPanel getGridBagLayoutPanel(){
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setLayout( new GridBagLayout());
		return p;
	}
}
