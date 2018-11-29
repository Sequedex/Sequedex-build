/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.gui.util;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author jcohn
 */
public class QueryDialog extends JDialog implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    
    protected boolean answer = false;
    protected JButton yesButton, noButton;
    protected List<String> msgLines;
    protected String yesString, noString;
    protected JTextArea displayTA;

    public QueryDialog(JFrame frame, boolean modal, ArrayList<String> lines, String yesStr, String noStr) {
        
        super(frame, modal);
        msgLines = lines;
        yesString = yesStr;
        noString = noStr;
        init();
 
    }
    
    public QueryDialog(JDialog dialog, boolean modal, ArrayList<String> lines, 
        String yesStr, String noStr) {
        super(dialog, modal);
        msgLines = lines;
        yesString = yesStr;
        noString = noStr;
        init();
    }
    
    public QueryDialog(JFrame frame, boolean modal, ArrayList<String> lines) {       
        this(frame,modal,lines,"Yes","No");
    }
    
    public QueryDialog(JDialog dialog, boolean modal, ArrayList<String> lines) {
        this(dialog, modal, lines, "Yes","No");
    }
    
    protected final void init() {
        initComponents();
    }
    
    protected void initComponents() {
            addWindowListener(new WindowListener() {
            
            @Override
            public void windowClosing(WindowEvent event) {
                answer = false;
                setVisible(false);
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
 
        JPanel queryPanel = new JPanel();
        getContentPane().add(queryPanel);
        queryPanel.setLayout(new GridLayout(2,1));
        queryPanel.setBorder(new EmptyBorder(20,20,20,20));
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
        queryPanel.add(scroller);
        JPanel buttonPanel = new JPanel();
        yesButton = new JButton(yesString);
        yesButton.addActionListener(this);
        buttonPanel.add(yesButton); 
        noButton = new JButton(noString);
        noButton.addActionListener(this);
        buttonPanel.add(noButton); 
        queryPanel.add(buttonPanel);
        pack();
        setLocationRelativeTo(getOwner());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(yesButton == e.getSource()) {
            answer = true;
            setVisible(false);
        }
        else if(noButton == e.getSource()) {
            answer = false;
            setVisible(false);
        }
    }
    
    public boolean getAnswer() { return answer; }   
    
}

