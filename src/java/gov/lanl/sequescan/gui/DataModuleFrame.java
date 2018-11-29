 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.lanl.sequescan.gui;

import gov.lanl.sequtils.log.MessageManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


/**
 *
 * @author jcohn
 */
public class DataModuleFrame extends JFrame implements ActionListener {
    
    // constants
    protected static final String CHANGE_DATA_MODULE_DIR = "Change Data Module Directory";
    public static final String CANCEL = "Cancel";
    public static final String CHANGE = "Change";
    
    // instance variables
    protected ActionListener parent; 
    protected JTextArea noteArea;
    protected JTextField newDataModuleDirTF = null;
    
    
    public DataModuleFrame(ActionListener p) {
       parent = p;  
       setTitle(CHANGE_DATA_MODULE_DIR);
       setLocation(50,50);
       setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);     
       init();
    }
    
    public final void init() {
        initUIComponents();
    }
    
    protected void initUIComponents() {       
        
        JPanel changePanel = new JPanel();
        changePanel.setLayout(new BorderLayout());
        changePanel.setBorder(new LineBorder(Color.black));
        JPanel formPanel = genFormPanel();
        formPanel.setBorder(new EmptyBorder(20,20,20,20));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBorder(new EmptyBorder(10,10,10,10));
        
        JButton changeButton = new JButton(CHANGE);
        changeButton.setActionCommand(CHANGE);
        changeButton.addActionListener(this);
        changeButton.setOpaque(true);
        changeButton.setBackground(Color.GRAY);
        JButton cancelButton = new JButton(CANCEL);
        cancelButton.setActionCommand(CANCEL);
        cancelButton.addActionListener(this);
        cancelButton.setOpaque(true);
        cancelButton.setBackground(Color.GRAY); 

        buttonPanel.setBackground(Color.white);
        buttonPanel.add(changeButton); 
        buttonPanel.add(cancelButton);
        
        changePanel.add(formPanel, BorderLayout.CENTER);
        changePanel.add(buttonPanel, BorderLayout.SOUTH);
        JScrollPane formScroller = new JScrollPane(formPanel);  
        changePanel.add(formScroller, BorderLayout.CENTER);
        changePanel.add(buttonPanel, BorderLayout.SOUTH);
        add(changePanel);
        
               addWindowListener(new WindowListener() {
            
            @Override
            public void windowClosing(WindowEvent event) {
                quitAction(CANCEL);
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
        
        pack();
        setSize(1000,600);
        
    
    }
    
    protected JPanel genFormPanel() {
        
//        genFormComponents();
//     
//        FormLayout layout = new FormLayout(
//                "left:max(60dlu;pref), 5dlu,left:max(60dlu;pref)",
//                "");
//        
//        DefaultFormBuilder builder = new DefaultFormBuilder(layout); 
//        builder.append("Data Module",  dataModuleCmB);          
//        builder.append("Function Set", functionSetCmB);  
//        builder.append("Input Type", inputTypeCmB);
//        builder.append("Base Directory for List File", listPanel);
//        builder.append("Input", inputPanel); 
//        builder.append("Configuration File", configPanel); 
//        builder.append("Output Directory", outputPanel);  
//        builder.append("Write Database", sequenceOutCmB);
////        builder.append("Database Top Node", topNodeTF);
//        builder.append("Thread Number", threadNumTF);
//        builder.append("Protein Fragment Cutoff", aaCutoffTF);
//        builder.append("Quiet", quietCmB);                
//             
//        return builder.getPanel();
        
        return new JPanel();
        
    }
    
    protected void genFormComponents() {
        
//        Icon fileDetailIcon = UIManager.getIcon("Tree.expandedIcon");
//        
//        String[] boolArr = new String[2];
//        boolArr[0] = "false";
//        boolArr[1] = "true";
//        permFlagCmB = new JComboBox(boolArr);
//        String quietValue;
//        if (quiet != null)
//            quietValue = quiet;
//        else
//            quietValue = "false";
//        quietCmB.setSelectedItem(quietValue);
//        
//        sequenceOutCmB = new JComboBox(boolArr);
//        String seqOutValue;
//        if (seqOut != null)
//            seqOutValue = seqOut;
//        else
//            seqOutValue = "false";
//        sequenceOutCmB.setSelectedItem(seqOutValue);
//            
//        // get names (without .jar extension)
//        String[] jarFileArr;
//        java.util.List<String> moduleNames = Mode.getDataModuleList(this,true);
//        if (!moduleNames.isEmpty()) {
//            jarFileArr = new String[moduleNames.size()];
//            Iterator<String> fileIter = moduleNames.iterator();
//            int indx = 0;
//            while (fileIter.hasNext()) {
//                jarFileArr[indx] = fileIter.next();
//                indx++;
//            }      
//        }
//        else {
//            jarFileArr = new String[0];
//        }
//        dataModuleCmB = new JComboBox(jarFileArr); 
//        if (dataModule != null)
//            dataModuleCmB.setSelectedItem(dataModule);
//        
//        String[] functionSetArr = new String[2];
//        functionSetArr[0] = NO_FUNCTIONS;
//        functionSetArr[1] = "seed_0911";
//        functionSetCmB = new JComboBox(functionSetArr);
//        String funcValue;
//        if (func == null) 
//            funcValue = "seed_0911";
//        else if (func.equals("seed_0911") ||
//            func.equals(NO_FUNCTIONS))
//            funcValue = func;
//        else
//            funcValue = "seed_0911";
//        functionSetCmB.setSelectedItem(funcValue);
//        
//        String configValue = null;
//        if (config != null) {
//            File configfile = new File(config);
//            if (configfile.exists())
//                configValue = config;
//            else 
//                configValue = null;              
//        }
//        if (configValue == null)
//            configValue =  ConfigHandler.getDefaultConfigFile();
//        configFileTF = new JTextField(configValue); 
//        configFileTF.setFont(MONOFONT);
//        configFileTFDoc = configFileTF.getDocument();
//        configFileTFDoc.addDocumentListener(this);
//        configPanel = new JPanel();
//        configPanel.setBackground(Color.white);
//        configPanel.add(configFileTF);
//        JButton configFileButton = new JButton(fileDetailIcon);
//        configFileButton.setBackground(Color.white);
//        configFileButton.setActionCommand(CONFIG_FILE_CHOOSER);
//        configFileButton.addActionListener(this);
//        configPanel.add(configFileButton);  
//        JButton configDefaultButton = new JButton("Default");
//        configDefaultButton.setBackground(Color.white);
//        configDefaultButton.setActionCommand(DEFAULT_CONFIG);
//        configDefaultButton.addActionListener(this);
//        configPanel.add(configDefaultButton);
//        
//        String outputValue;
//        if (output != null) {
//            File outputDir = new File(output);
//            if (outputDir.isDirectory())
//                outputValue = output;
//            else
//                outputValue = INPUT_LOCATION;
//        }
//        else
//            outputValue = INPUT_LOCATION;
////        if (output == null) {
////            String defaultOutputTop = System.getProperty("user.dir");
////            if (defaultOutputTop.equals("/"))
////                defaultOutputTop = SequescanConstants.SEQUEDEX_USER_DIR;
////            File dataOutputDirObj = new File(defaultOutputTop, SequescanConstants.DATA_OUTPUT_NAME);
////            String defaultOutput = dataOutputDirObj.getAbsolutePath();
////            outputValue = defaultOutput;
////        }
////        if (output == null)
////            outputValue = "";
//        outputTF = new JTextField(StringOps.rightFill(outputValue,configValue.length(), ' '));
//        outputTF.setFont(MONOFONT);
//        outputPanel = new JPanel();
//        outputPanel.setBackground(Color.white);
//        outputPanel.add(outputTF);
//        JButton outputDirButton = new JButton(fileDetailIcon);
//        outputDirButton.setBackground(Color.white);
//        outputDirButton.setActionCommand(OUTPUT_DIR_CHOOSER);
//        outputDirButton.addActionListener(this);
//        outputPanel.add(outputDirButton); 
//        
//        String[] inputTypeArr = new String[3];
//        inputTypeArr[0] = DIRECTORY_ITEM;   
//        inputTypeArr[1] = SEQ_FILE_ITEM; 
//        inputTypeArr[2] = LIST_FILE_ITEM;    
//        inputTypeCmB = new JComboBox(inputTypeArr);
//        inputTypeCmB.addItemListener(this);
//        String selectedInputType;
//        if (inputType != null)
//            selectedInputType = inputType;
//        else
//            selectedInputType = SEQ_FILE_ITEM;
//        inputTypeCmB.setSelectedItem(selectedInputType);  
//        String inputValue;
//        if (input != null)
//            inputValue = input;
//        else inputValue = "";
//        inputTF = new JTextField(StringOps.rightFill(inputValue, configValue.length(), ' '));
//        inputTF.setFont(MONOFONT);
//        inputPanel = new JPanel();
//        inputPanel.setBackground(Color.white);
//        inputPanel.add(inputTF);
//        JButton inputButton = new JButton(fileDetailIcon);
//        inputButton.setBackground(Color.white);
//        inputButton.setActionCommand(INPUT_CHOOSER);
//        inputButton.addActionListener(this);
//        inputPanel.add(inputButton);
//        
//        String baseDirValue = null;
//        if (baseDir != null) {
//            File baseDirFile = new File(baseDir);
//            if (baseDirFile.exists())
//                baseDirValue = baseDir;
//            else 
//                baseDirValue = null;              
//        }
//        if (baseDirValue == null) {
//            if (!selectedInputType.equals(LIST_FILE_ITEM))
//                baseDirValue =  NONE;
//            
//            else
//                baseDirValue = "";
//        }
//        
//        baseDirTF = new JTextField(StringOps.rightFill(baseDirValue,configValue.length(),' '));         
//        baseDirTF.setFont(MONOFONT);
//        listPanel = new JPanel();
//        listPanel.setBackground(Color.white);
//        listPanel.add(baseDirTF);
//        listPanelButton = new JButton(fileDetailIcon);
//        listPanelButton.setBackground(Color.white);
//        listPanelButton.setActionCommand(BASE_DIR_CHOOSER);
//        listPanelButton.addActionListener(this);
//        listPanel.add(listPanelButton);  
//        
//        String threadValue;
//        if (threads != null)
//            threadValue = threads;
//        else
//            threadValue = "1";
//        threadNumTF = new JTextField(3);
//        threadNumTF.setText(threadValue);
//        threadNumTF.setHorizontalAlignment(JTextField.RIGHT);
//        
//        String cutoffValue;
//        if (aaCutoff != null)
//            cutoffValue = aaCutoff;
//        else 
//            cutoffValue = "15";
//        aaCutoffTF = new JTextField(3);
//        aaCutoffTF.setText(cutoffValue);
//        aaCutoffTF.setHorizontalAlignment(JTextField.RIGHT);
//            
//        
////        String topNodeValue;
////        if (seqTopNode != null)
////            topNodeValue = seqTopNode;
////        else
////            topNodeValue = "0";
////        topNodeTF = new JTextField(5);
////        topNodeTF.setText(topNodeValue);
////        topNodeTF.setHorizontalAlignment(JTextField.RIGHT);
////        topNodeTF.setEditable(false);
//   
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        String cmd = e.getActionCommand();
        Object src = e.getSource();
        
        MessageManager.publish("DataModuleFrame actionPerformed: "+cmd,this);
        
        if (cmd.equals(CANCEL))
            quitAction(cmd);
        else if (cmd.equals(CHANGE))
            quitAction(cmd);   // in fact, need to call select file then either cancel or change...
        else
            MessageManager.publish("Unknown action command: "+cmd,this);
        
    } 
 
    protected void quitAction(String cmd) {       
        
        ActionEvent ev = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,cmd);
        if (parent != null)
            parent.actionPerformed(ev);
        else
            dispose();
        
    }
    
    
    public String getSelectedDirPath() {
        if (newDataModuleDirTF == null)
            return null;
        else
            return newDataModuleDirTF.getSelectedText().trim();
       
    }
    
    protected File selectFile() {
       
//        JFileChooser chooser = new JFileChooser();
//        chooser.setDialogTitle("Choose Data Module Directory");
//        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);  
//  
//        int returnVal = chooser.showDialog(this,"Select");
//        if(returnVal == JFileChooser.APPROVE_OPTION) 
//            return chooser.getSelectedFile();
//        else 
            return null;
 
    }
 
    
}