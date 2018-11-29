/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author jcohn
 */
public class DisplayDialog extends JDialog implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    
    protected boolean answer = false;
    protected JButton closeButton;
    protected Collection<String> msgLines;
    protected JTextArea displayTA;

    public DisplayDialog(Frame frame, boolean modal, Collection<String> lines) {
        
        super(frame, modal);
        msgLines = lines;
        init();
    }
    
    public DisplayDialog(Frame frame, boolean modal, String line) {
        super(frame,modal);
        msgLines = new ArrayList<>();
        msgLines.add(line);
        init();
    }   
    
    public DisplayDialog(Dialog frame, boolean modal, Collection<String> lines) {
        
        super(frame, modal);
        msgLines = lines;
        init();
    }
    
    public DisplayDialog(Dialog frame, boolean modal, String line) {
        super(frame,modal);
        msgLines = new ArrayList<>();
        msgLines.add(line);
        init();
    }
    
    protected final void init() {
        initComponents();
    }
    
    protected void initComponents() {
            addWindowListener(new WindowListener() {
            
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowIconified(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // do nothing
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // do nothing
            }
        });        
        displayTA = new JTextArea();
        displayTA.setBorder(new EmptyBorder(10,10,10,10));
        Font monofont = new Font("Courier", Font.PLAIN, 14);
        displayTA.setFont(monofont);
        
        JScrollPane scroller = new JScrollPane(displayTA);

       // System.out.println("lines: "+msgLines.size());
        Iterator<String> msgIter = msgLines.iterator();
      //  int cnt = 0;
        while (msgIter.hasNext()) {
            displayTA.append("\n"+msgIter.next());
        }
        displayTA.append("\n"); 

        displayTA.setEditable(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
//        displayPanel.add(buttonPanel, BorderLayout.SOUTH);   
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroller, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        displayTA.setCaretPosition(0);
        setLocationRelativeTo(getParent());
        setLocation(100,100);
        pack();
    }
    
    public void setButtonText(String buttonTxt) {
        closeButton.setText(buttonTxt);
    }
    
    public void setLineWrap(boolean flag) {
        displayTA.setLineWrap(flag);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(closeButton == e.getSource()) {
            dispose();
        }
    } 
    
}


