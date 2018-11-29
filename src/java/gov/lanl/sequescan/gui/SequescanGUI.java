/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *  jc 6/24/14:  need to make sure that values are correctly set by values in (this order):  
 *  saved options, current config file, defaults - in particular need to make sure that values are 
 *  changed if config file is changed and there are no saved options ???
 *  jc 6/25/14:  need to break this up into smaller classes (still not done)
 *  jc 10/19/16:  numerous changes to support non-licensed, non-python version of sequescan
 */
package gov.lanl.sequescan.gui;

import ch.qos.logback.classic.Level;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import gov.lanl.sequescan.analysis.CombinedReport;
import gov.lanl.sequescan.constants.UserConstants;
import gov.lanl.sequtils.constants.BuildConstants;
import gov.lanl.sequescan.gui.util.DisplayDialog;
import gov.lanl.sequescan.gui.util.InputFileFilter;
import gov.lanl.sequescan.gui.util.LicenseAgreementUtilities;
import gov.lanl.sequescan.gui.util.QueryDialog;
import gov.lanl.sequescan.mode.Mode;
import gov.lanl.sequescan.mode.ModeFactory;
import gov.lanl.sequescan.mode.ModeFactoryPublic;
import gov.lanl.sequescan.mode.RunMode;
import gov.lanl.sequescan.signature.JarModule;
import gov.lanl.sequtils.data.ProgressEventData;
import gov.lanl.sequtils.event.ProgressEvent;
import gov.lanl.sequtils.event.ProgressObserver;
import gov.lanl.sequtils.event.ThreadRunnerEvent;
import gov.lanl.sequtils.event.ThreadRunnerObserver;
import gov.lanl.sequtils.log.MessageManager;
import gov.lanl.sequtils.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import gov.lanl.sequescan.constants.AppConstants;
import gov.lanl.sequescan.signature.ModuleFactory;
import gov.lanl.sequescan.signature.SignatureModule;
import gov.lanl.sequescan.tree.TreeManager;
import gov.lanl.sequtils.event.QuitListener;
import org.forester.archaeopteryx.Archaeopteryx;
import org.forester.archaeopteryx.MainFrame;
import org.forester.phylogeny.Phylogeny;


/**
 *
 * @author jcohn
 */
public class SequescanGUI extends JFrame implements ActionListener,
    DocumentListener, ItemListener, ProgressObserver, ThreadRunnerObserver, 
    AppConstants, BuildConstants {
    
    // constants
    
    private static final long serialVersionUID = 1L;
    public static final Font MONOFONT = new Font("Monospaced", Font.PLAIN, 14);

    // menu names
    public static final String HELP = "Help";
    public static final String UTILITIES = "Utilities";
    
    // Sequedex menu options for non-Mac (Mac has these options as defaults)
    public static final String ABOUT = "About Sequedex";
    public static final String QUIT = "Quit Sequedex";
    
    // license agreement
    public static final String DISPLAY_LICENSE = "Display License Terms";
    
    // utilities options
    public static final String VIEW_TREE = "View Tree";
    public static final String SAVE_PROGRESS_PANEL = "Save Progress Panel to File";
  
    // help options
    public static final String BUILD = "Sequescan Build Version";
    public static final String RUN_MODE_CMD_LINE_HELP = "Sequescan Command-Line Help";
    public static final String SEQUEDEX_HELP = "Sequedex Help";
    public static final String SEQUEDEX_HELP_CMD = "sequedex-help";
    
    // constants for various components on main screen
    public static final String SEQUESCAN_BUTTON_LABEL = "Run Sequescan";
    public static final String SAVE = "Save Options";
    public static final String RESET = "Reset Options";
    public static final String INPUT_CHOOSER = "input chooser";
    public static final String OUTPUT_DIR_CHOOSER = "output dir chooser";
    public static final String CONFIG_FILE_CHOOSER = "config file chooser";
    public static final String DISPLAY_DIR_CHOOSER = "display dir chooser";
    public static final String BASE_DIR_CHOOSER = "base dir chooser";
    public static final String DATA_MODULE_DIR_CHOOSER = "data module dir chooser";
    public static final String DIRECTORY_ITEM = "directory";
    public static final String LIST_FILE_ITEM = "list file";
    public static final String SEQ_FILE_ITEM = "sequencing file";
    public static final String NO_FUNCTIONS = "none";
    public static final String USER_LICENSE_AGREEMENT = "user_license_agreement";
    public static final String DEFAULT_CONFIG = "default config";
    
    // userProps keys (not already in SequescanConstants)
    public static final String FUNCTION = "function_set";
    public static final String INPUT_TYPE = "input_type";
    public static final String INPUT = "input";
    public static final String CONFIG = "config_file";
    public static final String OUTPUT = "output_dir";
    public static final String SEQUENCE_OUTPUT = "sequence_output";
    public static final String WDW_OUTPUT = "wdw_output";
    public static final String THREAD_NUM = "thread_num";
    public static final String AACUTOFF = "protein_fragment_cutoff";
    public static final String QUIET = "quiet";
    public static final String PROP_VERSION = "prop_version";
    public static final String WDW_FLAG = "wdw_flag";
    
    // values for sequenceOutCmB
    public static final String SEQ_OPTION_0 = "0 (No)";
    public static final String SEQ_OPTION_1 = "1 (Yes)";
    public static final String SEQ_OPTION_2 = "2 (Yes with Kmers)";
    public static final String SEQ_OPTION_3 = "3 (Yes, Translate DNA)";
    public static final String SEQ_OPTION_4 = "4 (Yes with Kmers, Translate DNA)";
            

    // instance variables
    protected boolean runFlag = false;
    protected JButton runButton, listPanelButton;
    protected int threadPoolSize = 0;
    protected JComboBox<String> quietCmB;
    protected JComboBox<String> dataModuleCmB;
    protected JComboBox<String> functionSetCmB;
    protected JComboBox<String> inputTypeCmB;
    protected JTextField inputTF, outputTF, configFileTF, baseDirTF;
    protected Document configFileTFDoc, baseDirTFDoc;
    protected JPanel inputPanel, outputPanel, configPanel, listPanel;
    protected JTextArea progressTA;
    protected JMenuBar menubar;
    protected JMenu utilitiesMenu, /*licenseMenu,*/ helpMenu;
    protected JTextField threadNumTF;
    protected JTextField aaCutoffTF;
    protected JComboBox sequenceOutCmB;
    protected JComboBox wdwOutCmB;
    protected DateFormat timeFormatter;
    protected Thread runThread = null;
    protected BaseProperties userProps;
    protected ConfigFile config;
    protected String defaultUserPropsMsg = null;
    protected String[] cmdLineArgs;
    protected boolean savedFlag = false;  
    protected boolean isWindows = false;
    protected ArrayList<QuitListener> quitListeners;
    protected Thread logDisplayThread = null;
    protected LogDisplay logDisplay = null;

    
    public SequescanGUI(String args[]) {
       cmdLineArgs = args;  // usually null - unless Mac App
 
       Locale currentLocale = Locale.getDefault();
       timeFormatter = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, 
            DateFormat.MEDIUM,
            currentLocale); 
 //      extProcModeList = new ArrayList<>();
       init();
    }
    
    public final void init() {
        boolean okay = getUserLicenseAgreement();
        
 //       setTitle("Sequedex "+ ConfigFile.getFullVersion()); 
        setTitle("Sequedex " + SEQUEDEX_VERSION);
        setLocation(50,50);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); 
        MessageManager.addThreadRunnerObserver(this);
        getSavedOptions();
        initUIComponents();
        readCurrentConfigFile();
        String osName = System.getProperty("os.name");
        isWindows = osName.startsWith("Windows");
   
    }
    
    protected void readCurrentConfigFile() {
        String configFileStr = configFileTF.getText();
        if (configFileStr == null || configFileStr.equals("")) 
            return;
        File cfile = new File(configFileStr);
        if (!cfile.exists() || !cfile.isFile())
            return;
        String msg = "Reading current config file: "+configFileStr;
        ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
        observeThreadRunnerEvent(event);
                 
        config = ConfigFile.getConfigFromFile(configFileStr);
        if (config == null) {
            String configMsg = "Problem reading config file at "+configFileStr;
            MessageManager.publish(configMsg,this);
        }
        if (!savedFlag)
            setDefaultsFromCurrentConfigFile();
    }
    
    protected void setDefaultsFromCurrentConfigFile() {
        
        String threadValue = config.getProperty(NCPUS);
        if (threadValue != null)
            threadNumTF.setText(threadValue);
        else {
            threadNumTF.setText("1");
        }
        
        String cutoffValue = config.getProperty(MIN_PROT_FRAG);
        if (cutoffValue != null)
            aaCutoffTF.setText(cutoffValue);
        else
            aaCutoffTF.setText("15");
        
    }
    
    protected void platformSpecificInit() {
   
        JMenu sequescanMenu = new JMenu("Sequedex");
        menubar.add(sequescanMenu);
        JMenuItem aboutItem = new JMenuItem(ABOUT);
        aboutItem.setActionCommand(ABOUT);
        aboutItem.addActionListener(this);
        JMenuItem exitItem = new JMenuItem(QUIT);
        exitItem.setActionCommand(QUIT);
        exitItem.addActionListener(this);
        sequescanMenu.add(aboutItem);
        sequescanMenu.add(exitItem);  
   
    }
    
    protected void initUIComponents() {       
        // menu
        menubar = new JMenuBar();
        setJMenuBar(menubar);
        
        platformSpecificInit();
        utilitiesMenu = new JMenu(UTILITIES);
        helpMenu = new JMenu(HELP);
        menubar.add(utilitiesMenu); 
        menubar.add(helpMenu);
        
        // items for utility menu
        JMenuItem treeItem = new JMenuItem(VIEW_TREE);
        treeItem.setActionCommand(VIEW_TREE);
        treeItem.addActionListener(this);     
        JMenuItem saveProgressItem = new JMenuItem(SAVE_PROGRESS_PANEL);
        saveProgressItem.setActionCommand(SAVE_PROGRESS_PANEL);
        saveProgressItem.addActionListener(this); 
        utilitiesMenu.add(treeItem);
        utilitiesMenu.add(saveProgressItem);
        
        // items for help menu
        
        JMenuItem buildItem = new JMenuItem(BUILD);
        buildItem.setActionCommand(BUILD);
        buildItem.addActionListener(this);
        JMenuItem runModeHelpItem = new JMenuItem(RUN_MODE_CMD_LINE_HELP);
        runModeHelpItem.setActionCommand(RUN_MODE_CMD_LINE_HELP);
        runModeHelpItem.addActionListener(this);
        
        helpMenu.add(buildItem);
        helpMenu.add(runModeHelpItem);
       
        // Split Panel with Form and Progress Sections
        
        JSplitPane framePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        getContentPane().add(framePane);
        
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BorderLayout());
        runPanel.setBorder(new LineBorder(Color.black));
        JPanel formPanel = genFormPanel();
        formPanel.setBorder(new EmptyBorder(20,20,20,20));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBorder(new EmptyBorder(10,10,10,10));
        runButton = new JButton(SEQUESCAN_BUTTON_LABEL);
        JButton saveButton = new JButton(SAVE);
        JButton resetButton = new JButton(RESET);
        
        runButton.setEnabled(true);  // it will only be enabled when minimum level of fields are completed?
        runButton.setActionCommand(SEQUESCAN_BUTTON_LABEL);
        runButton.addActionListener(this);
        runButton.setOpaque(true);
        runButton.setBackground(Color.GRAY);
        saveButton.setEnabled(true);
        saveButton.setActionCommand(SAVE);
        saveButton.addActionListener(this);
        saveButton.setOpaque(true);
        saveButton.setBackground(Color.GRAY);
        resetButton.setEnabled(true);
        resetButton.setActionCommand(RESET);
        resetButton.addActionListener(this);
        resetButton.setOpaque(true);
        resetButton.setBackground(Color.GRAY);
        
        buttonPanel.setBackground(Color.white);
        buttonPanel.add(runButton); 
        buttonPanel.add(saveButton);

        JScrollPane formScroller = new JScrollPane(formPanel);  
        runPanel.add(formScroller, BorderLayout.CENTER);
        runPanel.add(buttonPanel, BorderLayout.SOUTH);
        framePane.setTopComponent(runPanel);
    
        progressTA = new JTextArea();
        progressTA.setBorder(new EmptyBorder(10,10,10,10));
        progressTA.setLineWrap(true);
        progressTA.setWrapStyleWord(true);
        progressTA.setEditable(false);
      
        JScrollPane progressScroller = new JScrollPane(progressTA);
        LineBorder lborder = new LineBorder(Color.black);       
        TitledBorder border = new TitledBorder(lborder, "Progress");
        border.setTitleJustification(TitledBorder.CENTER);
        progressScroller.setBorder(border);
        framePane.setBottomComponent(progressScroller);
        framePane.setDividerLocation(0.7d);    
        
        addWindowListener(new WindowListener() {
            
            @Override
            public void windowClosing(WindowEvent event) {
                quitAction();
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
        setSize(1000,800);
        
        if (defaultUserPropsMsg != null) {
            ThreadRunnerEvent event = new ThreadRunnerEvent(this, defaultUserPropsMsg, 1);
            observeThreadRunnerEvent(event);              
        }
        
    
    }
    
    protected JPanel genFormPanel() {
        
        genFormComponents();
     
        FormLayout layout = new FormLayout(
                "left:max(60dlu;pref), 5dlu,left:max(60dlu;pref)",
                "");
        
        DefaultFormBuilder builder = new DefaultFormBuilder(layout); 
        builder.append("Data Module",  dataModuleCmB);          
        builder.append("Function Set", functionSetCmB);  
        builder.append("Input Type", inputTypeCmB);
        builder.append("Base Directory for List File", listPanel);
        builder.append("Input", inputPanel); 
        builder.append("Configuration File", configPanel); 
        builder.append("Output Directory", outputPanel);  
        builder.append("Write Sequences", sequenceOutCmB);
        builder.append("Write WhoDoesWhat File", wdwOutCmB);
//        builder.append("Database Top Node", topNodeTF);
        builder.append("Thread Number", threadNumTF);
        builder.append("Protein Fragment Cutoff", aaCutoffTF);
        builder.append("Quiet", quietCmB);                
             
        return builder.getPanel();
    }
    
    protected void genFormComponents() {
        
        // get saved values
        String func = userProps.getProperty(FUNCTION);
        String dataModule = userProps.getProperty(DATA);
        String inputType = userProps.getProperty(INPUT_TYPE);
        String input = userProps.getProperty(INPUT);
        String configPath = userProps.getProperty(CONFIG);
        String output = userProps.getProperty(OUTPUT);
        String seqOut = userProps.getProperty(SEQUENCE_OUTPUT);
        String wdwOut = userProps.getProperty(WDW_OUTPUT);
//        String seqTopNode = userProps.getProperty(SEQUENCE_TOP_NODE);
        String threads = userProps.getProperty(THREAD_NUM);
        String aaCutoff = userProps.getProperty(AACUTOFF);
        String quiet = userProps.getProperty(QUIET);
        String baseDir = userProps.getProperty(BASE_DIR);
        
        Icon fileDetailIcon = UIManager.getIcon("Tree.expandedIcon");
        
        String[] boolArr = new String[2];
        boolArr[0] = "false";
        boolArr[1] = "true";
        quietCmB = new JComboBox<>(boolArr);
        String quietValue;
        if (quiet != null)
            quietValue = quiet;
        else
            quietValue = "false";
        quietCmB.setSelectedItem(quietValue);
        
        String[] seqOutArr = new String[5];
        seqOutArr[0] = SEQ_OPTION_0;
        seqOutArr[1] = SEQ_OPTION_1;
        seqOutArr[2] = SEQ_OPTION_2;
        seqOutArr[3] = SEQ_OPTION_3;
        seqOutArr[4] = SEQ_OPTION_4;
        sequenceOutCmB = new JComboBox<>(seqOutArr);
        String seqOutValue;
        if (seqOut != null)
            seqOutValue = seqOut;
        else {
            seqOutValue = SEQ_OPTION_0;
        }
        if (seqOutValue.equals("true"))
            seqOutValue = SEQ_OPTION_1;
        else if (seqOutValue.equals("false"))
            seqOutValue = SEQ_OPTION_0;
        sequenceOutCmB.setSelectedItem(seqOutValue);
        
        wdwOutCmB = new JComboBox<>(boolArr);
        String wdwOutValue;
        if (wdwOut != null)
            wdwOutValue = wdwOut;
        else {
            wdwOutValue = "false";
        }
        wdwOutCmB.setSelectedItem(wdwOutValue);
            
        // get names (without .jar extension)
        String[] jarFileArr;
        java.util.List<String> moduleNames = Mode.getDataModuleList(this,true);
        if (!moduleNames.isEmpty()) {
            jarFileArr = new String[moduleNames.size()];
            Iterator<String> fileIter = moduleNames.iterator();
            int indx = 0;
            while (fileIter.hasNext()) {
                jarFileArr[indx] = fileIter.next();
                indx++;
            }      
        }
        else {
            jarFileArr = new String[0];
        }
        dataModuleCmB = new JComboBox<>(jarFileArr); 
        dataModuleCmB.addItemListener(this);
        boolean matchFlag = false;
        if (dataModule != null) {
            for (String jarFileArr1 : jarFileArr) {
                if (jarFileArr1.equals(dataModule)) {
                    matchFlag = true;
                    break;
                }
            }
            if (matchFlag)
                dataModuleCmB.setSelectedItem(dataModule);
            else if (jarFileArr.length > 0)
                dataModuleCmB.setSelectedIndex(0);
        }
        else if (jarFileArr.length > 0)
            dataModuleCmB.setSelectedIndex(0);
        
        String selectedModule = dataModuleCmB.getSelectedItem().toString();
        
        createFunctionSetCmB(selectedModule, func);
       
        String configFieldValue = null;
        if (configPath != null) {
            File configfile = new File(configPath);
            if (configfile.exists())
                configFieldValue = configPath;
            else 
                configFieldValue = null;              
        }
        if (configFieldValue == null)
            configFieldValue =  ConfigFile.getDefaultConfigFilePath();
        configFileTF = new JTextField(configFieldValue); 
        configFileTF.setFont(MONOFONT);
        configFileTFDoc = configFileTF.getDocument();
        configFileTFDoc.addDocumentListener(this);
        configPanel = new JPanel();
        configPanel.setBackground(Color.white);
        configPanel.add(configFileTF);
        JButton configFileButton = new JButton(fileDetailIcon);
        configFileButton.setBackground(Color.white);
        configFileButton.setActionCommand(CONFIG_FILE_CHOOSER);
        configFileButton.addActionListener(this);
        configPanel.add(configFileButton);  
        JButton configDefaultButton = new JButton("Default");
        configDefaultButton.setBackground(Color.white);
        configDefaultButton.setActionCommand(DEFAULT_CONFIG);
        configDefaultButton.addActionListener(this);
        configPanel.add(configDefaultButton);
        
        String outputValue;
        if (output != null) {
            File outputDir = new File(output);
            if (outputDir.isDirectory())
                outputValue = output;
            else
                outputValue = INPUT_LOCATION;
        }
        else
            outputValue = INPUT_LOCATION;
//        if (output == null) {
//            String defaultOutputTop = System.getProperty("user.dir");
//            if (defaultOutputTop.equals("/"))
//                defaultOutputTop = SequescanConstants.SEQUEDEX_USER_DIR;
//            File dataOutputDirObj = new File(defaultOutputTop, SequescanConstants.DATA_OUTPUT_NAME);
//            String defaultOutput = dataOutputDirObj.getAbsolutePath();
//            outputValue = defaultOutput;
//        }
//        if (output == null)
//            outputValue = "";
        outputTF = new JTextField(StringOps.rightFill(outputValue,configFieldValue.length(), ' '));
        outputTF.setFont(MONOFONT);
        outputPanel = new JPanel();
        outputPanel.setBackground(Color.white);
        outputPanel.add(outputTF);
        JButton outputDirButton = new JButton(fileDetailIcon);
        outputDirButton.setBackground(Color.white);
        outputDirButton.setActionCommand(OUTPUT_DIR_CHOOSER);
        outputDirButton.addActionListener(this);
        outputPanel.add(outputDirButton); 
        
        String[] inputTypeArr = new String[3];
        inputTypeArr[0] = DIRECTORY_ITEM;   
        inputTypeArr[1] = SEQ_FILE_ITEM; 
        inputTypeArr[2] = LIST_FILE_ITEM;    
        inputTypeCmB = new JComboBox<>(inputTypeArr);
        inputTypeCmB.addItemListener(this);
        String selectedInputType;
        if (inputType != null)
            selectedInputType = inputType;
        else
            selectedInputType = DIRECTORY_ITEM;
        inputTypeCmB.setSelectedItem(selectedInputType);  
        String inputValue;
        if (input != null)
            inputValue = input;
        else inputValue = "";
        inputTF = new JTextField(StringOps.rightFill(inputValue, configFieldValue.length(), ' '));
        inputTF.setFont(MONOFONT);
        inputPanel = new JPanel();
        inputPanel.setBackground(Color.white);
        inputPanel.add(inputTF);
        JButton inputButton = new JButton(fileDetailIcon);
        inputButton.setBackground(Color.white);
        inputButton.setActionCommand(INPUT_CHOOSER);
        inputButton.addActionListener(this);
        inputPanel.add(inputButton);
        
        String baseDirValue = null;
        if (baseDir != null) {
            File baseDirFile = new File(baseDir);
            if (baseDirFile.exists())
                baseDirValue = baseDir;
            else 
                baseDirValue = null;              
        }
        if (baseDirValue == null) {
            if (!selectedInputType.equals(LIST_FILE_ITEM))
                baseDirValue =  NONE;
            
            else
                baseDirValue = "";
        }
        
        baseDirTF = new JTextField(StringOps.rightFill(baseDirValue,configFieldValue.length(),' '));         
        baseDirTF.setFont(MONOFONT);
        listPanel = new JPanel();
        listPanel.setBackground(Color.white);
        listPanel.add(baseDirTF);
        listPanelButton = new JButton(fileDetailIcon);
        listPanelButton.setBackground(Color.white);
        listPanelButton.setActionCommand(BASE_DIR_CHOOSER);
        listPanelButton.addActionListener(this);
        listPanel.add(listPanelButton);  
        
        String threadValue;
        if (threads != null)
            threadValue = threads;
        else {
//            threadValue = (String) currentConfigVarsHM.get(NCPUS);
//            if (threadValue == null)
             threadValue = "1";  //config.getProperty(NCPUS);
        }
        threadNumTF = new JTextField(3);
        threadNumTF.setText(threadValue);
        threadNumTF.setHorizontalAlignment(JTextField.RIGHT);
        
        String cutoffValue;
        if (aaCutoff != null)
            cutoffValue = aaCutoff;
        else {
//            cutoffValue = (String) currentConfigVarsHM.get(MIN_PROT_FRAG);
//            if (cutoffValue == null)
                cutoffValue = "15"; // config.getProperty(MIN_PROT_FRAG);
        }
        aaCutoffTF = new JTextField(3);
        aaCutoffTF.setText(cutoffValue);
        aaCutoffTF.setHorizontalAlignment(JTextField.RIGHT);
            
        
//        String topNodeValue;
//        if (seqTopNode != null)
//            topNodeValue = seqTopNode;
//        else
//            topNodeValue = "0";
//        topNodeTF = new JTextField(5);
//        topNodeTF.setText(topNodeValue);
//        topNodeTF.setHorizontalAlignment(JTextField.RIGHT);
//        topNodeTF.setEditable(false);
//   
    }
    
    protected Set<String> getFunctionSetNames(String moduleName) {
        String jarFilePath = Mode.getJarFilePath(this,moduleName);
        JarModule dataModule = new JarModule(jarFilePath);
        boolean status = dataModule.openJarFile();
        if (!status) {
            MessageManager.publish("Cannot open module file for "+moduleName,this);
            return null;
        }
        else {
            Set<String> functionSetNames = dataModule.getFunctionSetNames();
//            if (functionSetNames != null || )
//                System.out.println("function set names for "+moduleName+": "+functionSetNames.size());
            return functionSetNames;
        }
            
    }
    
    // note:  JComboBox requires either a Vector or an array - no other collections
    protected String[] getFunctionSetArr(String dataModule) {
        
        String funcValue = NO_FUNCTIONS;
        String[] functionSetArr;
        if (dataModule != null) {
            Set<String> functionSetNames = getFunctionSetNames(dataModule);
            int functionSetCnt;
            if (functionSetNames == null || functionSetNames.isEmpty())
                functionSetCnt = 1;
            else
                functionSetCnt = functionSetNames.size() + 1;
            functionSetArr = new String[functionSetCnt];
            functionSetArr[0]= NO_FUNCTIONS;   
            if (functionSetCnt > 1) {
                int ndx = 1;
                Iterator<String> fiter = functionSetNames.iterator();
                while (fiter.hasNext()) {
                    functionSetArr[ndx]= fiter.next();
                    ndx++;
                }
            } 
        }
        else {
            functionSetArr = new String[1];
            functionSetArr[0] = NO_FUNCTIONS;
        }
        return functionSetArr;
        
    }
    
    protected void createFunctionSetCmB(String selectedModule, String savedFunc) {
           
        String[] functionSetArr = getFunctionSetArr(selectedModule);  
        functionSetCmB = new JComboBox<>(functionSetArr);
        int itemCnt = functionSetCmB.getItemCount();
    
        int defaultIndx;
        if (itemCnt == 1)
            defaultIndx = 0;
        else if (itemCnt > 1)
            defaultIndx = 1;
        else
            defaultIndx = 0;
        if (savedFunc == null) {
            functionSetCmB.setSelectedIndex(defaultIndx);          
        }
        else {
            boolean matchFlag = false;
            for (String func : functionSetArr) {
                if (func.equals(savedFunc)) {
                    matchFlag = true;
                    break;
                }               
            }
            if (matchFlag)
                functionSetCmB.setSelectedItem(savedFunc);
            else
                functionSetCmB.setSelectedIndex(defaultIndx);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        String cmd = e.getActionCommand();
        Object src = e.getSource();
    
        if (cmd.equals(QUIT))
            quitAction();
        else if (cmd.equals(SEQUESCAN_BUTTON_LABEL))
            runSequescan();
        else if (cmd.equals(VIEW_TREE))
            viewTree();
        else if (cmd.equals(RESET))
            resetOptions();
        else if (cmd.equals(SAVE)) {
            saveOptions();
        }
        else if (cmd.equals(ABOUT))
            about();
        else if (cmd.equals(BUILD))
            displayBuild();
//        else if (cmd.equals(REQUEST_LICENSE))
//            requestLicense();
//        else if (cmd.equals(INSTALL_LICENSE))
//            installLicense();
        else if (cmd.equals(DISPLAY_LICENSE))
            displayLicense();
        else if (cmd.endsWith("chooser")) {
            File selectedFile = selectFile(cmd);
            if (selectedFile != null) {
                switch (cmd) {
                    case INPUT_CHOOSER:
                        inputTF.setText(selectedFile.getAbsolutePath());
                        break;
                    case OUTPUT_DIR_CHOOSER:
                        String path;
                        // kludge because FileDialog cannot currently be
                        // set to choose only directories in openJDK 1.7
                        if (!selectedFile.isDirectory())
                            path = selectedFile.getParentFile().getAbsolutePath();
                        else
                            path = selectedFile.getAbsolutePath();
                        outputTF.setText(path);
                        break;
                    case CONFIG_FILE_CHOOSER:
                        configFileTF.setText(selectedFile.getAbsolutePath());
                        break;
                    case BASE_DIR_CHOOSER:
                        baseDirTF.setText(selectedFile.getAbsolutePath());
                        break;
                    default:
                        MessageManager.publish("Unknown File Chooser request: "+cmd,this);
                        break;
                }
            }
        }
        else if (cmd.equals(DEFAULT_CONFIG)) {
            setDefaultConfig();
        }
        else if (cmd.equals(RUN_MODE_CMD_LINE_HELP)) {
            displayRunModeHelp();
        }
//        else if (cmd.equals(SEQUINATOR_HELP)) {
//            displaySequinatorHelp();
//        }   
        else if (cmd.equals(SAVE_PROGRESS_PANEL)) {
            saveProgressPanel();
        }
        else
            MessageManager.publish("Unknown action command: "+cmd,this);


        
    } 
    
    protected void setDefaultConfig() {
        String defaultConfigValue =  ConfigFile.getDefaultConfigFilePath();
        configFileTF.setText(defaultConfigValue);
    }
    
    protected void viewTree() {
        
        // get names of data modules (without .jar extension)
        String[] jarFileArr;
        java.util.List<String> moduleNames = Mode.getDataModuleList(this,true);
        if (!moduleNames.isEmpty()) {
            jarFileArr = new String[moduleNames.size()];
            Iterator<String> fileIter = moduleNames.iterator();
            int indx = 0;
            while (fileIter.hasNext()) {
                jarFileArr[indx] = fileIter.next();
                indx++;
            }      
        }
        else {
            jarFileArr = null;
        }
 
        if (jarFileArr == null || jarFileArr.length == 0) {
            DisplayDialog dialog = new DisplayDialog(this,true,
            "There are no data modules installed");
            dialog.setTitle(VIEW_TREE);
            dialog.setVisible(true);
            return;
        }
 
        boolean problemFlag = false;
        String msg = "";
        String selectedValue = (String) JOptionPane.showInputDialog(null,
                "Choose Data Module", "Module", JOptionPane.INFORMATION_MESSAGE, null,
                jarFileArr, jarFileArr[0]);  
        if (selectedValue != null) {
            String configFileName = DEFAULT_ARCHY_CONFIG;  
            String archyConfigPath = ConfigFile.getEtcDir() +
                SEP + "archy" + SEP + configFileName;
            String dataModulePath = ConfigFile.getDefaultDataModuleDir() + SEP + 
                selectedValue + ".jar";
            SignatureModule dataModule = ModuleFactory.getSignatureModule(dataModulePath);
            if (dataModule == null) {
                problemFlag = true;
                msg = "Could not get data module at "+dataModulePath;
            }
            else {
                TreeManager treeMgr = CombinedReport.readTreeFromModule(dataModule);
                if (treeMgr != null) {
                    Phylogeny[] trees = treeMgr.getTrees();  
                    MainFrame treeFrame = Archaeopteryx.createApplication(trees,
                       archyConfigPath, selectedValue);
                       treeFrame.setVisible(true);
                }
                else {
                    problemFlag = true;
                    msg = "Problem reading tree from data module "+selectedValue;
                } 
            }
        }
        else {
            problemFlag = true;
            msg = "No data module has been selected";
        }
        if (problemFlag) {
            DisplayDialog dialog = new DisplayDialog(this,true,msg);
            dialog.setTitle(VIEW_TREE);
            dialog.setVisible(true);          
        }
    }
    
    
    protected void unimplementedOption(String optionName) {
        
        String msg = optionName+" has not yet been implemented";
        JOptionPane.showMessageDialog(this,msg);
        
    }
    
    protected String[] splitArgString(String argString) {
        return StringOps.getTokens(argString, " ");
    }
  
    protected void about() {
        
        Collection<String> lines;
        // construct full path
        String distribDir = ConfigFile.getDistribDir();
        File docDir = new File(distribDir,"doc");
        File aboutFile = new File(docDir, "ABOUT.txt");
        if (aboutFile.exists()) {
            FileObj fileObj = new FileObj(aboutFile);
            lines = fileObj.readLines();
        }
        else {
            lines = new ArrayList<>();
            lines.add(aboutFile.getAbsolutePath()+" is missing");
        }
        DisplayDialog dialog = new DisplayDialog( this, true,
            lines);
        dialog.setVisible(true);
        
    }
    
    protected void displayBuild() {
        
        String buildStr = "Sequescan build "+ BUILDSTRING;
        JOptionPane.showMessageDialog(this,buildStr);
        
    }
    
    protected void resetOptions() {
        
        Collection<String> lines = new ArrayList<>();
        lines.add("Reset Options is not yet implemented");
        DisplayDialog dialog = new DisplayDialog(this, true, lines);
        dialog.setVisible(true);
        
    }
        
    protected void runSequescan() {
        
        String inputStr;
        String rawInputStr = inputTF.getText();
        if (rawInputStr == null)
            inputStr = "";
        else
            inputStr = rawInputStr.trim();
        if (inputStr.length() < 1) {
            progressTA.append("\nEmpty input field");
            return;
        }
        else {
            File inputFile = new File(inputStr);
            if (!inputFile.exists()) {
                String msg = "Input "+inputStr+" does not exist";
                ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
                observeThreadRunnerEvent(event);
                return;
            }
        }
        String quietStr = quietCmB.getSelectedItem().toString().trim();
        boolean quiet;
        quiet = quietStr.equals("true");
        
        String seqOutStr = sequenceOutCmB.getSelectedItem().toString().trim();
        String wdwOutStr = wdwOutCmB.getSelectedItem().toString().trim();
        boolean writeWDWFlag;
        writeWDWFlag = wdwOutStr.equals("true");
        
        String dataStr = dataModuleCmB.getSelectedItem().toString().trim();
        String functionStr = functionSetCmB.getSelectedItem().toString().trim();
        String configStr = configFileTF.getText().trim();
        String outputStr = outputTF.getText().trim();
        String inputTypeStr = inputTypeCmB.getSelectedItem().toString().trim();
        String threadNumStr = threadNumTF.getText().trim();
//        String topNodeStr = topNodeTF.getText().trim();
        String aaCutoffStr = aaCutoffTF.getText().trim();
        
        // note:  will need to add this as ThreadRunnerObserver, ProgressObserver
        runButton.setEnabled(false);
       
        // populate argument list
        ArrayList<String> argList = new ArrayList<>();
        argList.add("run");  // mode
        if (quiet)
            argList.add("-q");
        argList.add("-d");
        argList.add(dataStr);
        if (functionStr.length() > 0  && !functionStr.equals(NO_FUNCTIONS)) {
            argList.add("-s");
            argList.add(functionStr);
        }
        if (configStr != null && configStr.length() > 0) {
            argList.add("-c");
            argList.add(configStr);
        }
        if (outputStr != null && outputStr.length() > 0) {
            argList.add("-o");
            argList.add(outputStr);
        }
        if (!threadNumStr.equals("") /*&& !threadNumStr.equals("config")*/){
            Integer num = StringOps.getInteger(threadNumStr);
            if (num != null) {
                argList.add("-t");
                argList.add(threadNumStr);
            }
        }
        else
            System.out.println("No -t option added");
        if (!seqOutStr.equals("")) {
//            System.out.println("writeSeqFlag "+writeSeqFlag+": "+seqOutStr);
            argList.add("-f");
            String argStr;
            switch (seqOutStr) {
                case SEQ_OPTION_0:
                    argStr = "0";
                    break;
                case SEQ_OPTION_1:
                    argStr = "1";
                    break;
                case SEQ_OPTION_2:
                    argStr = "2";
                    break;
                case SEQ_OPTION_3:
                    argStr = "3";
                    break;
                case SEQ_OPTION_4:
                    argStr = "4";
                    break;
                default:
                    argStr = "0";
                    break;
            }
            argList.add(argStr);
        }
        
        if (!wdwOutStr.equals("")) {
            argList.add("-w");
            if (writeWDWFlag)
                argList.add("1");
            else
                argList.add("0");
        }

        if (!aaCutoffStr.equals("") && !aaCutoffStr.equals("config")) {
            Integer num = StringOps.getInteger(aaCutoffStr);
            if (num != null) {
                argList.add("-a");
                argList.add(aaCutoffStr);
            }
        }
        
            
        if (inputTypeStr.equals(LIST_FILE_ITEM)) {
            argList.add("-l");
            String baseDirValue = baseDirTF.getText().trim();
            if (baseDirValue == null || baseDirValue.equals(""))
                baseDirValue = NONE;
            argList.add(baseDirValue);
        }
        argList.add(inputStr);
        
        // current call to sequescan (within same virtual machine
 
        String[] argArr = argList.toArray(new String[argList.size()]);        
        ModeFactory modeFactory = new ModeFactoryPublic();
        gov.lanl.sequescan.mode.Mode mode = modeFactory.getMode(argArr);
 
        // note:  currently hard-coded to run mode only
        if (mode == null) {
            progressTA.append("\nUnable to create RunMode");
            return;
        }
        // terminating external processes is not currently working - so no point keeping Mode obj in scope
//        else {
//            if (mode.getAutoMaxHeapFlag())
//                extProcModeList.add(mode);
//        }
        
        mode.addProgressObserver(this);
   
        boolean okay = mode.init();
        if (!okay) {
            progressTA.append("\nUnable to initialize RunMode");
            runButton.setEnabled(true);
            return;
        }
        
        if (mode.getAutoMaxHeapFlag()) {
            String logFileName = mode.getLogFileName();
            Mode.publish("AutoMaxHeap execution starting now.  Process output will go to "+logFileName,
                this,Level.INFO); 
            ProgressEvent event = new ProgressEvent(ProgressEvent.BEGIN,logFileName);
            observeProgressEvent(event);
            // note:  above should execute startLogThread;  no need to stopLogThread
            // when exit from GUI since JVM will stop all threads and log thread
            // currently will only write to GUI progress window
            
            // note:  return from doInBackground is a kludge for now
            SwingWorker<Integer,String> worker = new SwingWorker<Integer,String>() {
                
                @Override
                protected Integer doInBackground() throws Exception {
                    mode.runWithAutoMaxHeap();
                    return 1;
                }
                
                @Override
                protected void done() {
//                    extProcModeList.remove(mode);
                    runButton.setEnabled(true);
                }
            
            };
            
            worker.execute();
        }     
        else {
            new Thread(mode).start();
        }
  
    }
    
    protected boolean getUserLicenseAgreement() {
  
        File userPropertiesFile = UserConstants.getUserPropertiesFile();
        if (userPropertiesFile == null)  // i.e. SEQUEDEX_USERDIR does not exist and could not be created
            return false;
        
        // jc:  need to create UserProperties class - but I am in a hurry today
        BaseProperties userProperties = new BaseProperties();
        if (userPropertiesFile.exists()) {
            boolean okay = 
                userProperties.loadPropertiesFromFile(userPropertiesFile.getAbsolutePath());
            if (!okay)
                return false;
        }
       Boolean userLicenseAgreementFlag = userProperties.getFlag(USER_LICENSE_AGREEMENT);
       if (userLicenseAgreementFlag == null)
           return false;
       else return userLicenseAgreementFlag.equals(Boolean.TRUE);    
    }
    
    protected void saveOptions() {
       
        
        Object dataValue = dataModuleCmB.getSelectedItem();
        Object functValue = functionSetCmB.getSelectedItem();
        Object inputTypeValue = inputTypeCmB.getSelectedItem();
        String inputValue = inputTF.getText();
        String configValue = configFileTF.getText(); 
        String outputValue = outputTF.getText();
        String threadValue = threadNumTF.getText();
        Object quietValue = quietCmB.getSelectedItem();
        Object seqOutputValue = sequenceOutCmB.getSelectedItem();
        Object wdwOutputValue = wdwOutCmB.getSelectedItem();
//        String topNodeValue = topNodeTF.getText();
        String aaCutoffValue = aaCutoffTF.getText();
        String baseDirValue = baseDirTF.getText();
        Boolean userLicensePropFlag = userProps.getFlag(USER_LICENSE_AGREEMENT);
        if (userLicensePropFlag == null)
            userLicensePropFlag = Boolean.FALSE;
        
        userProps = new BaseProperties();
        userProps.setProperty(PROP_VERSION, UserConstants.USER_PROPS_VERSION_NUMBER);
        userProps.setFlag(USER_LICENSE_AGREEMENT, userLicensePropFlag);
             
        if (dataValue != null)
            userProps.setProperty(DATA, dataValue.toString());
        if (functValue != null)
            userProps.setProperty(FUNCTION, functValue.toString());
        if (inputTypeValue != null)
            userProps.setProperty(INPUT_TYPE, inputTypeValue.toString());
        if (inputValue != null)
            userProps.setProperty(INPUT, inputValue.trim());
        if (configValue != null)
            userProps.setProperty(CONFIG, configValue.trim());
        if (outputValue != null) 
            userProps.setProperty(OUTPUT, outputValue.trim());
        if (threadValue != null && !threadValue.trim().equals(""))
            userProps.setProperty(THREAD_NUM,threadValue.trim());
        if (quietValue != null)
            userProps.setProperty(QUIET, quietValue.toString());
        if (seqOutputValue != null)
            userProps.setProperty(SEQUENCE_OUTPUT, seqOutputValue.toString());
        else 
            userProps.setProperty(SEQUENCE_OUTPUT, "false");
         if (wdwOutputValue != null)
            userProps.setProperty(WDW_OUTPUT, wdwOutputValue.toString());
        else 
            userProps.setProperty(SEQUENCE_OUTPUT, "false");       
//        if (topNodeValue != null && !topNodeValue.trim().equals("")) {  
//            Integer topNodeInt = StringOps.getInteger(topNodeValue.trim());
//            if (topNodeInt != null) {
//                topNodeValue = topNodeInt.toString();
//                userProps.setProperty(SEQUENCE_TOP_NODE, topNodeValue);
//            }
//        }
        if (aaCutoffValue != null && !aaCutoffValue.trim().equals("")) {
            Integer aaCutoffInt = StringOps.getInteger(aaCutoffValue.trim());
            if (aaCutoffInt != null) {
                aaCutoffValue = aaCutoffInt.toString();
                userProps.setProperty(AACUTOFF, aaCutoffValue);
            }
        }
        
        if (baseDirValue != null && !baseDirValue.trim().equals("")) {
            userProps.setProperty(BASE_DIR, baseDirValue);
        }       
        
        
        File userPropertiesFile = UserConstants.getUserPropertiesFile();
        
        if (userPropertiesFile == null) { //SEQUEDEX_USERDIR does not exist and cannot be created
            String msg = "Could not save properties to file because User Directory could not be created";
            ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
            observeThreadRunnerEvent(event);
            return;
        }

        if (userPropertiesFile.exists())
            userPropertiesFile.delete();
        boolean okay = userProps.writePropertiesToFile(userPropertiesFile.getAbsolutePath());
        if (!okay) {
            String msg = "Problem writing user properties to "+userPropertiesFile.getAbsolutePath();
            ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
            observeThreadRunnerEvent(event);
        }
        else 
            savedFlag = true;
  
    }
        
        
    protected void getSavedOptions() {
        
        File userPropertiesFile = UserConstants.getUserPropertiesFile();
        userProps = new BaseProperties();
        
        if (userPropertiesFile == null) {
            // user dir does not exist and could not be created
            String msg = "Problem loading user properties  user directory does not exist and could not be created";
            ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
            observeThreadRunnerEvent(event); 
            userProps.put(PROP_VERSION, UserConstants.USER_PROPS_VERSION_NUMBER);
        }   
        else if (userPropertiesFile.exists()) {
            boolean okay = userProps.loadPropertiesFromFile(userPropertiesFile.getAbsolutePath());
            if (!okay) {
                String msg = "Problem loading user properties from "+userPropertiesFile.getAbsolutePath()+"; setting options to defaults";
                ThreadRunnerEvent event = new ThreadRunnerEvent(this,msg,1);
                observeThreadRunnerEvent(event);
                userProps = new BaseProperties();
                userProps.put(PROP_VERSION, UserConstants.USER_PROPS_VERSION_NUMBER);
                userPropertiesFile.delete();
            }
            else {
                String propVersion = (String) userProps.get(PROP_VERSION);
                boolean defaultFlag; // = false;
                if (propVersion != null) {
                    Double propVersionNum = StringOps.getDouble(propVersion);
                    Double minPropVersionNum = StringOps.getDouble(UserConstants.MIN_USER_PROPS_VERSION_NUMBER);
                    if (minPropVersionNum == null) {
                        defaultUserPropsMsg = "Missing UserConstants.MIN_USER_PROP_VERSION_NUMBER - setting run options to defaults";
                    }
                    else
                        defaultUserPropsMsg = null;
                        
                    if (propVersionNum != null && minPropVersionNum != null) {
                        defaultFlag = propVersionNum < minPropVersionNum;
                    }
                    else
                        defaultFlag = true;
                }
                else
                    defaultFlag = true;
                if (defaultFlag) {
                    if (defaultUserPropsMsg == null)
                        defaultUserPropsMsg = "User properties file has incorrect version number ("+propVersion+") - setting run options to defaults";
                    Boolean userLicenseAgreementFlag = userProps.getFlag(USER_LICENSE_AGREEMENT);
                    userPropertiesFile.delete();
                    userProps = new BaseProperties();
                    userProps.put(PROP_VERSION, UserConstants.USER_PROPS_VERSION_NUMBER);
                    if (userLicenseAgreementFlag != null)
                        userProps.setFlag(USER_LICENSE_AGREEMENT, userLicenseAgreementFlag);
                    savedFlag = false;
                }
                else
                    savedFlag = true;
            }
            
        } 
        else {
            userProps.put(PROP_VERSION, UserConstants.USER_PROPS_VERSION_NUMBER);
            savedFlag = false;
        }
    }
    
//    protected void terminateExternalProcesses() {
//        if (extProcModeList == null || extProcModeList.isEmpty()) {
//            MessageManager.publish("extProcModeList is empty",this,Level.INFO);
//            return;
//        }
//        Iterator<Mode> iter = extProcModeList.iterator();
//        while (iter.hasNext()) {
//            Mode mode = iter.next();
//            mode.shutdownExecutor();  // does not seem to work ...
//        }
//    }
    
    protected void quitAction() {
        
        ArrayList<String> msgLines = new ArrayList<>();
        msgLines.add("Exiting Sequescan.");
        msgLines.add("This will not stop any runs executing in an external process.");
        msgLines.add("They will continue to run until completion.");
        msgLines.add("Continue to Exit? ");
        QueryDialog dialog = new QueryDialog(this, true, msgLines);
        dialog.setTitle("Exit Program");
        dialog.setVisible(true);
        boolean okay = dialog.getAnswer();
        if (okay) {
     //       terminateExternalProcesses();  // not working ???
             // if exiting next lines are probably not necessary
//            boolean stopOkay = stopLogThread();
//            if (!stopOkay) {
//                
//            }
            stopLogDisplayThread();
            System.exit(0);
        }
        else dialog.dispose();
    
    }
    
    protected void displayRunModeHelp() {
        java.util.List<String> usageLines = RunMode.getUsageList(config,false);
        DisplayDialog dialog = new DisplayDialog(this, false, usageLines);
        dialog.setTitle("Sequescan Run Mode Command Line Help");
        dialog.setButtonText("Close");
        dialog.setVisible(true);
        // dialog.dispose();
    }
    
    protected void displayLicense() {
        Collection<String> lines;
        Collection<String> licenseLines = LicenseAgreementUtilities.getLicenseAgreementLines();
        if (licenseLines == null) {
            lines = new ArrayList<>();
            lines.add("Unable to Display License Agreement File: "+
                LicenseAgreementUtilities.getLicenseAgreementFilePath());
        }
        else {
            lines = licenseLines;
            DisplayDialog dialog = new DisplayDialog(this, true, lines);
            dialog.setSize(750,500);
            dialog.setTitle("License Agreement");
            dialog.setButtonText("Okay");
            dialog.setVisible(true);
            dialog.dispose();
           
        }

    }
        
    
    protected void saveProgressPanel() {
        
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        
        File saveFile = chooser.getSelectedFile();
        
        if (saveFile == null)
            return;
        if (saveFile.exists()) {
 
            String query = "Overwrite existing file?";
            int confirm = JOptionPane.showConfirmDialog(this, "Overwrite existing file?",
                "Existing File",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION)
                saveFile.delete();
            else
                return;       
        }
        
        FileObj saveFileObj = new FileObj(saveFile);
        String progressTxt = progressTA.getText();
        saveFileObj.appendLine(progressTxt);
        
    }
      
    protected File selectFile(String chooserType) {
        
        JFileChooser chooser = null;
     
        switch (chooserType) {
            case BASE_DIR_CHOOSER:
                chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Select Base Directory for Input List File");
                break;
            case INPUT_CHOOSER:
                String inputType = inputTypeCmB.getSelectedItem().toString();
                if (inputType.equals("directory")) {
                    chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setDialogTitle("Select Input Directory");
                }
                else if (inputType.equals("list file")) {
                    chooser = new JFileChooser();
                    chooser.setDialogTitle("Select List File");
                }
                else {
                    chooser = new JFileChooser();
                    if (config != null) {
                        String listStr = (String) config.getProperty(FASTA_EXT_LIST);
                        if (listStr != null) {
                            String[] validExtArr = StringOps.getTokens(listStr, ConfigFile.CONFIG_LIST_DELIM);
                            if (validExtArr != null) {
                                InputFileFilter filter = new InputFileFilter(validExtArr);
                                chooser.addChoosableFileFilter(filter);
                            }
                        }
                    }
                    chooser.setDialogTitle("Select Nucleic Acid Fasta/Fastq File");
                }   break;
            case OUTPUT_DIR_CHOOSER:
                // on Mac, JfileChooser in Java 1.6 does not have new folder
                // button - thus the use of the older FileDialog
                // openJDK 1.7 for Mac does have new folder button but it works a bit wonky
                // FileDialog
                String osName = System.getProperty("os.name");
//            if (osName.equals("Mac OS X")) {
//                FileDialog dialog = new FileDialog(this, "Choose Output Directory (Mac) ");   
//                dialog.setMode(FileDialog.LOAD);
//                System.setProperty("apple.awt.fileDialogForDirectories","true");  // doesn't work in 1.7
//                dialog.setLocation(50, 50);
//                dialog.setVisible(true);
//                String selectedDirStr = dialog.getDirectory();
//                String selectedFileStr = dialog.getFile();
//                if (selectedDirStr == null || selectedFileStr == null)
//                    return null;
//                else
//                    return new File(selectedDirStr, selectedFileStr);          
//            }
//            else {
                chooser = new JFileChooser();
                chooser.setDialogTitle("Choose Output Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);             
//            }
                break;
            case DATA_MODULE_DIR_CHOOSER:
                chooser = new JFileChooser();
                chooser.setDialogTitle("Choose Data Module Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                break;
            case CONFIG_FILE_CHOOSER:
                {
                    chooser = new JFileChooser();
                    FileNameExtensionFilter filter= new FileNameExtensionFilter(
                            "Configuration Files (.conf)", "conf");
                    chooser.setFileFilter(filter);
                    chooser.setDialogTitle("Select Configuration File");
                    break;
                }
//            case LICENSE_FILE_CHOOSER:
//                {
//                    chooser = new JFileChooser();
//                    FileNameExtensionFilter filter= new FileNameExtensionFilter(
//                            "License Files (.lic)", "lic");
//                    chooser.setFileFilter(filter);
//                    chooser.setDialogTitle("Select License File To Install");
//                    break;
//                }
            case DISPLAY_DIR_CHOOSER:
                chooser = new JFileChooser();
                chooser.setDialogTitle("Choose Result Directory for Display");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setSelectedFile(new File(outputTF.getText()));
                break;
        }
             
        if (chooser == null)
            return null;
        else {
            int returnVal = chooser.showDialog(this,"Select");
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            else 
                return null;
        }
 
    }
    
    
  //   observer Methods
    
    @Override
    public void observeProgressEvent(ProgressEvent event) {
        
        boolean okay;
        String msg;
        Date d = event.getEventTime();
        String dateStr = timeFormatter.format(d);
        int action = event.getAction(); 
        
        switch (action) {
            
            case ProgressEvent.PROGRESS_DATA:  
                ProgressEventData progressData = event.getData();
                if (progressData == null)
                    return;
                File fnaFile = new File(progressData.getFnaFileName());
                String fileName = fnaFile.getName();
                String completedPercentStr;
                DecimalFormat form = new DecimalFormat("0.0");
                if (progressData.getFileSize() > 0) {
                    float completedPercent = (float) progressData.getCharProcessed()* (float)100/(float) progressData.getFileSize();
                    Float completedPercentObj = completedPercent;
                    completedPercentStr = " ("+form.format(completedPercentObj)+" percent completed)";
                }
                else if (progressData.processingDone())
                    completedPercentStr = " (100.0 percent completed)";
                else
                    completedPercentStr = "";
                Boolean processingDone;
                if (progressData.processingDone())
                    processingDone = Boolean.TRUE;
                else
                    processingDone = Boolean.FALSE;
                msg = dateStr + "  " + fileName+": "+progressData.getCharProcessed()+
                        " chars processed"+completedPercentStr;
                progressTA.append("\n");
                progressTA.append(msg);
                progressTA.repaint();
                break;
            case ProgressEvent.BEGIN:
                String logFile = event.getLogFile();
                setLogFilesForDisplay(logFile);
                break;
            case ProgressEvent.END:
                stopLogDisplayThread();            
                break;
            default:
                msg = dateStr+" Unknown action for ProgressObserver: "+action;
                progressTA.append("\n");
                progressTA.append(msg);
                progressTA.repaint();
                break;
                
        }
     
    }
    
    protected void startLogDisplayThread() {
        
        if (logDisplayThread != null)
            return;
        
        logDisplay = new LogDisplay(progressTA);
        logDisplayThread = new Thread(logDisplay);
        logDisplayThread.start();
        
    }
    
    protected void setLogFilesForDisplay(String logFileStr) {
        
        File logFile = new File(logFileStr);
        boolean status = logFile.exists();  
        String msg;
        Date d = new Date();
        String dateStr = timeFormatter.format(d);
  
        if (status) {
            File processOutFile = new File(logFileStr + ExternalProcessRunner.PROCESS_OUT_EXT);          
            File processErrFile = new File(logFileStr + ExternalProcessRunner.PROCESS_ERR_EXT);
//            msg = "Setting files for log display thread to "+processOutFile.getAbsolutePath()+" and " +
//                processErrFile.getAbsolutePath();  
            msg = null;
            if (logDisplayThread == null)
                startLogDisplayThread();
            if (logDisplay != null) {
                logDisplay.setLogFiles(processOutFile, processErrFile);
            }
               
        }
        else {
            msg = "Log file does not exist - cannot set files for log display thread";
        }
      
        if (msg != null) {
            progressTA.append("\n");
            progressTA.append(msg);
            progressTA.repaint();
        }
       
    }
    
    protected void closeLogFilesForDisplay() {
        
        String msg;
        if (logDisplay != null) {
            logDisplay.closeOpenLogFiles();
        }
   
    }
    
    protected void stopLogDisplayThread() {
        if (logDisplay != null) {
            logDisplay.stopDisplay();
            logDisplay = null;
        }
        else
            return;

        logDisplayThread = null;  
        progressTA.append("\n");
        progressTA.repaint();

    }
    
    @Override
    public void observeThreadRunnerEvent(final ThreadRunnerEvent event) {
        
        Date d = event.getTime();
        String dateStr = timeFormatter.format(d); 
        String eventMsg = event.getMessage();
        String msg = dateStr + "  " + eventMsg;
        if (progressTA == null) {
            JOptionPane.showMessageDialog(this, eventMsg,"Message", JOptionPane.WARNING_MESSAGE);            
        }
        else {
            progressTA.append("\n");  progressTA.append(msg);
            progressTA.repaint();
            if (eventMsg.toLowerCase().startsWith("end")) {          
                runButton.setEnabled(true);
            }
        }
   
    }  

    @Override
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public void setThreadPoolSize(int size) {
        threadPoolSize = size;
    }
    
    /////////////// end observer methods
      
    @Override
    public void itemStateChanged(ItemEvent e) {
        
        String itemStr = e.getItem().toString();
 
        if (itemStr.equals(DIRECTORY_ITEM) || itemStr.equals(LIST_FILE_ITEM) ||
             itemStr.equals(SEQ_FILE_ITEM)) {
            if (inputTF != null)
                inputTF.setText("");
            if (baseDirTF != null)
                baseDirTF.setText(NONE);
            if (listPanelButton != null) {
                if (itemStr.equals(LIST_FILE_ITEM))
                    listPanelButton.setEnabled(true);
                else
                    listPanelButton.setEnabled(false);
            }
            
        }
        
        else if (functionSetCmB != null && dataModuleCmB != null && 
            itemStr.equals(dataModuleCmB.getSelectedItem())) {
            Set<String> functionSetNames = getFunctionSetNames(itemStr);
            int itemCnt = functionSetCmB.getItemCount();
            if (itemCnt > 0) {
                for (int i=0; i<itemCnt; i++)
                    functionSetCmB.removeItemAt(0);
            }
            functionSetCmB.addItem(NONE);
            Iterator<String> fiter = functionSetNames.iterator();
            while (fiter.hasNext())
                functionSetCmB.addItem(fiter.next());
            itemCnt = functionSetCmB.getItemCount();   // new item count
            if (itemCnt > 1)
                functionSetCmB.setSelectedIndex(1);
            else 
                functionSetCmB.setSelectedIndex(0);          
        }
       
    }
 
    @Override
    public void insertUpdate(DocumentEvent de) {
        if (de.getDocument().equals(configFileTFDoc)) {
            readCurrentConfigFile();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if (de.getDocument().equals(configFileTFDoc)) {
            readCurrentConfigFile();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        if (de.getDocument().equals(configFileTFDoc)) {
            readCurrentConfigFile();
        }
    }
    
   
}
