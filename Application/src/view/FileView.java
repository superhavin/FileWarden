package view;

import controller.CSVExportController;
import controller.SQLController;
import model.EmailService;
import model.FileDirectoryModel;
import model.FileEvent;
import model.FileRecord;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractCollection;
import java.util.ArrayDeque;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The view of the Application.
 */
public class FileView extends JFrame implements PropertyChangeListener, Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    public static final String STOP_DIRECTORY = "stopDirectory";
    public static final String CHANGE_DIRECTORY = "changeDirectory";
    public static final String ACTIVE = "active";

    /**
     * Button to start the application.
     */
    private transient final JButton myToggleButton;
    /**
     * Button to start monitoring the grabbed Directory
     */
    private transient final JButton myMonitorButton;
    /**
     * Menu item to start the application.
     */
    private transient final  JMenuItem myStartMenuItem;
    /**
     * Menu item to exit the application.
     */
    private transient final JMenuItem myExitMenuItem;
    /**
     * Menu item to open the about menu.
     */
    private transient final JMenuItem myAboutMenuItem;
    /**
     * The Title Card for the main menu.
     */
    private transient final JPanel myTitleCard;

    /**
     * Static instance of the Model.
     */
    private static FileDirectoryModel fileDirectoryModel;
    /**
     * Static instance of SQL Controller.
     */
    private static SQLController sqlController;
    /**
     * Static instance of a Directory View.
     */
    private static AbstractCollection<FileDirectoryWindow> fileDirectoryWindows;
    /**
     * maximum number of windows a file directory window can have.
     */
    private final static int MAX_WINDOWS = 1;
    /**
     * When game is not active this is present as the toggling button.
     */
    private static final String START = "Start?";
    /**
     * When game is active this is present as the toggling button.
     */
    private static final String STOP = "Stop!";

    /**
     * Constructor of the view.
     */
    public FileView(){
        fileDirectoryModel = FileDirectoryModel.getInstance();
        sqlController = SQLController.getInstance();
        fileDirectoryWindows = new ArrayDeque<>();

        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        myTitleCard = new JPanel(new BorderLayout());
        myTitleCard.setOpaque(false);
        JLabel myTitleLabel = new JLabel("Welcome to File Warden", SwingConstants.CENTER);
        myTitleLabel.setFont(myTitleLabel.getFont().deriveFont(Font.BOLD, 28f));
        myTitleLabel.setForeground(Color.LIGHT_GRAY);
        JLabel mySubtitleLabel = new JLabel("File Monitoring service for file directories", SwingConstants.CENTER);
        mySubtitleLabel.setFont(mySubtitleLabel.getFont().deriveFont(Font.PLAIN, 16f));
        mySubtitleLabel.setForeground(Color.GRAY);
        myTitleCard.add(myTitleLabel, BorderLayout.CENTER);
        myTitleCard.add(mySubtitleLabel, BorderLayout.SOUTH);

        //Menus
        final JMenuBar myMenuBar = new JMenuBar();
        final JMenu myFileMenu = new JMenu("File");
        myStartMenuItem = new JMenuItem(START);
        myStartMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        myStartMenuItem.putClientProperty("JMenuItem.selectionType", "underline");
        myExitMenuItem = new JMenuItem("Exit");
        myExitMenuItem.putClientProperty("JMenuItem.selectionType", "underline");
        myExitMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        myFileMenu.add(myStartMenuItem);
        myFileMenu.addSeparator();
        myFileMenu.add(myExitMenuItem);
        final JMenu myHelpMenu = new JMenu("Help");
        myAboutMenuItem = new JMenuItem("About");
        myAboutMenuItem.putClientProperty("JMenuItem.selectionType", "underline");
        myHelpMenu.add(myAboutMenuItem);
        myMenuBar.add(myHelpMenu);
        myMenuBar.add(myFileMenu);

        //ToolBar
        final JToolBar myToolBar = new JToolBar();
        myToolBar.setFloatable(false);
        myToggleButton = new JButton(START);
        myToggleButton.putClientProperty("JButton.buttonType", "roundRect");
        myToolBar.add(myToggleButton);

        //Directory Panel
        final JPanel myRightPanel = new JPanel();
        myRightPanel.setLayout(new BoxLayout(myRightPanel, BoxLayout.Y_AXIS));
        myRightPanel.setBorder(BorderFactory.createTitledBorder("Directory Controls"));
        myMonitorButton = new JButton("Start Monitoring");
        myMonitorButton.putClientProperty("JButton.buttonType", "roundRect");
        myRightPanel.add(Box.createVerticalStrut(10));
        myRightPanel.add(myMonitorButton);

        setJMenuBar(myMenuBar);
        add(myTitleCard, BorderLayout.CENTER);
        add(myToolBar, BorderLayout.NORTH);
        add(myRightPanel, BorderLayout.EAST);

        addListeners();
        setAllControls(false);
    }

    private void addListeners() {

        final ActionListener togglingEvent = aTogglingEvent -> {
            if(!fileDirectoryModel.isActive()){
                fileDirectoryModel.startApp();
            }else{
                fileDirectoryModel.resetApp();
                //CLOSE ALL Windows
            }
        };

        final ActionListener monitoringEvent = aMonitoringEvent -> {
            String currentFileDirectory = returnDirectory();
            boolean isValidDirectory = (currentFileDirectory != null && !currentFileDirectory.isBlank());

            if(isValidDirectory //checks if an application is active, if max instances have been reached, and if window has already exists
                    && fileDirectoryModel.isActive()
                    && !(fileDirectoryWindows.size() > MAX_WINDOWS)
                    && !(fileDirectoryModel.containsDirectory(currentFileDirectory))
            ){
                FileDirectoryWindow newView = new FileDirectoryWindow(currentFileDirectory);
                fileDirectoryWindows.add(newView);
                fileDirectoryModel.startMonitoring(currentFileDirectory, newView);

                showTitleCard(false);
                revalidate();
                repaint();
            }else{
                JOptionPane.showMessageDialog(null,
                        "Targeted directory is not valid.");
            }
        };

        final ActionListener exitingEvent = aExitingEvent -> {
            fileDirectoryModel.resetApp();
            try {
                sqlController.clearDatabase();
            } catch (SQLException ignored){
                //throw new SQLException("Database not cleared.");
            }
            dispose();
            System.exit(0);
        };

        final ActionListener helpingEvent = aHelpingEvent -> {
            AboutDialog dialogBox = new AboutDialog(getFrame());
            dialogBox.setVisible(true);
        };

        myToggleButton.addActionListener(togglingEvent);
        myStartMenuItem.addActionListener(togglingEvent);
        myExitMenuItem.addActionListener(exitingEvent);

        myMonitorButton.addActionListener(monitoringEvent);

        myAboutMenuItem.addActionListener(helpingEvent);
    }

    /**
     * Allows other package-private classes to access the fileDirectoryModel instance.
     * @return the fileDirectoryModel instance.
     */
    static FileDirectoryModel getFileDirectoryModel(){
        return fileDirectoryModel;
    }

    static SQLController getSqlController(){
        return sqlController;
    }

    JFrame getFrame(){
        return this;
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight){
        javax.swing.SwingUtilities.invokeLater(() -> {
            final FileView mainFrame = new FileView();

            fileDirectoryModel.addPropertyChangeListener(mainFrame);

            mainFrame.setTitle("File Warden");
            mainFrame.setPreferredSize(new Dimension(theWidth, theHeight));
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.pack();
            mainFrame.setVisible(true);
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        switch (theEvent.getPropertyName()) {
            case ACTIVE:
                setAllControls((boolean) theEvent.getNewValue());
                break;

            case CHANGE_DIRECTORY:
                //final String aNewDirectory = ((String) theEvent.getNewValue());
                break;

            case STOP_DIRECTORY:
                //final String aRemovedDirectory = ((String) theEvent.getOldValue());
                break;
        }
    }

    private String returnDirectory(){
        JFileChooser aFileChooser = new JFileChooser();
        aFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int aResolution = aFileChooser.showOpenDialog(this);
        if(aResolution == JFileChooser.APPROVE_OPTION){
            File aFile = aFileChooser.getSelectedFile();
            return aFile.toPath().toString();
        }else{
            return null;
        }
    }

    /**
     * Helper method which disables and enables components of the panel.
     */
    private void setAllControls(final boolean theStatus) {
        myMonitorButton.setEnabled(theStatus);

        myToggleButton.setText(theStatus ? STOP : START);
        myStartMenuItem.setText(theStatus ? STOP : START);

        if(!theStatus){
            closeAllWindows();
        }
    }

    private void showTitleCard(final boolean theStatus){
        myTitleCard.setVisible(theStatus);
    }

    /**
     * Helper method which closes all the windows.
     */
    private void closeAllWindows() {
        for(FileDirectoryWindow window : fileDirectoryWindows){
            window.closeWindow();
        }
        fileDirectoryWindows.clear();

        showTitleCard(true);
        revalidate();
        repaint();
    }
}

/**
 * Windows which display the monitored information of the current directory.
 */
class FileDirectoryWindow extends JPanel implements PropertyChangeListener, Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final FileDirectoryModel myFileDirectoryModel;
    private transient final SQLController mySqlController;
    private transient final AbstractCollection<FileRecord> myFileRecords;
    /**
     * String directory of this window.
     */
    private transient final String myDirectoryString;
    /**
     * Current window.
     */
    private transient final JFrame myWindow;
    /**
     * Menu item to query the database.
     */
    private transient final JMenuItem mySaveMenuItem;
    /**
     * Menu item to query the database.
     */
    private transient final JMenuItem myQueryMenuItem;
    /**
     * Button to export CSV file.
     */
    private transient final JButton myExportCsvButton;
    /**
     * Button to email the database.
     */
    private transient final JButton myEmailDatabaseButton;
    /**
     * Table Model to save the file event summary.
     */
    private transient final DefaultTableModel myTableModel;
    /**
     * Area of the windows.
     */
    private static final int WINDOW_AREA = 600;


    /**
     * Constructor of the view's window.
     * @param theMonitoredDirectory the window's directory.
     */
    FileDirectoryWindow(final String theMonitoredDirectory) {
        myDirectoryString = theMonitoredDirectory;
        myFileRecords = new ArrayDeque<>();
        myFileDirectoryModel = FileView.getFileDirectoryModel();
        mySqlController = FileView.getSqlController();

        setLayout(new GridBagLayout());

        //Menu
        final JMenuBar myMenuBar = new JMenuBar();
        final JMenu myDatabaseMenu = new JMenu("Database");
        mySaveMenuItem = new JMenuItem("Save Events");
        mySaveMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        myQueryMenuItem = new JMenuItem("Query Database");
        myDatabaseMenu.add(mySaveMenuItem);
        myDatabaseMenu.add(myQueryMenuItem);
        myMenuBar.add(myDatabaseMenu);

        //ToolBar
        final JToolBar myExportToolBar = new JToolBar("Export");
        myExportCsvButton = new JButton("Export Database to CSV");
        myEmailDatabaseButton = new JButton("Email Database");
        myExportToolBar.add(myExportCsvButton);
        myExportToolBar.add(myEmailDatabaseButton);

        //TableModel
        myTableModel = new DefaultTableModel(new Object[]{"File Name", "Extension", "Path", "Activity", "DataTime"}, 0);
        final JTable myTable = new JTable(myTableModel);
        JScrollPane myTableScrollPane = new JScrollPane(myTable);

        //GridBagLayout Setup
        GridBagConstraints myGBC = new GridBagConstraints();
        myGBC.gridx = 0; myGBC.gridy = 0; myGBC.weightx = 1.0; myGBC.weighty = 1.0;
        myGBC.insets = new Insets(20, 20, 10,20);
        myGBC.fill = GridBagConstraints.BOTH;
        add(myTableScrollPane, myGBC);

        //Packing
        JPanel myControlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints myControllerGBC = new GridBagConstraints();
        myControllerGBC.gridx = 0;
        myControllerGBC.gridwidth = GridBagConstraints.CENTER;
        myControllerGBC.weightx = 1.0;
        myControllerGBC.insets = new Insets(5, 5, 5, 5);

        myControllerGBC.gridy = 0; myControllerGBC.weighty = 0.0;
        myControllerGBC.fill = GridBagConstraints.HORIZONTAL;
        myControlPanel.add(myMenuBar, myControllerGBC);

        myControllerGBC.gridy = 2; myControllerGBC.weighty = 0.0;
        myControllerGBC.fill = GridBagConstraints.HORIZONTAL;
        myControlPanel.add(myExportToolBar, myControllerGBC);

        myControlPanel.setMinimumSize(new Dimension(WINDOW_AREA / 4, WINDOW_AREA / 7));
        myGBC.gridy = 1; myGBC.weighty = 0.0;
        myGBC.fill = GridBagConstraints.HORIZONTAL;
        add(myControlPanel, myGBC);

        //Final Setup
        myWindow = new JFrame(myDirectoryString);
        incrementTitle(0);
        myWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myWindow.setSize(new Dimension(WINDOW_AREA, WINDOW_AREA));
        myWindow.setContentPane(this);
        myWindow.pack();
        myWindow.setMinimumSize(myWindow.getSize());
        myWindow.setVisible(true);

        addListeners();
    }

    /**
     * A setter for the Window Title.
     * @param theNumberOfActions the int of the current number of actions
     */
    private void incrementTitle(final int theNumberOfActions) {
        myWindow.setTitle(theNumberOfActions + " : " + myDirectoryString);
    }

    /**
     * Closes the window.
     */
    void closeWindow(){
        myWindow.dispose();
    }

    private void addListeners() {
        assert myWindow != null;
        myWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent aEvent){
                myFileDirectoryModel.stopMonitoring(myDirectoryString);
            }
        });

        ActionListener saveDatabaseEvent = aSaveDatabaseEvent -> {
            saveCurrentToDatabase();
        };

        ActionListener queryDatabaseEvent = aQueryDatabaseEvent -> {
            QueryView theView = new QueryView(SwingUtilities.getWindowAncestor(this), FileView.getSqlController());
            theView.setVisible(true);
        };

        ActionListener exportCsvEvent = aExportCsvEvent -> {
            exportCurrentToCsv();
        };

        ActionListener emailingCsvEvent = aEmailingCsvEvent -> {
            sendCurrentToEmail();
        };

        mySaveMenuItem.addActionListener(saveDatabaseEvent);
        myQueryMenuItem.addActionListener(queryDatabaseEvent);
        myExportCsvButton.addActionListener(exportCsvEvent);
        myEmailDatabaseButton.addActionListener(emailingCsvEvent);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent theEvent){
        switch (theEvent.getPropertyName()){
            case "monitorDirectory":
                FileEvent theFile = (FileEvent) theEvent.getNewValue();

                FileRecord theFileRecord = new FileRecord(theFile);

                myFileRecords.add(theFileRecord);

                SwingUtilities.invokeLater(() -> {
                    myTableModel.addRow(theFileRecord.getTableRow());

                    incrementTitle(theFile.getCount());
                });

                break;
        }
    }

    private void saveCurrentToDatabase(){
        if(myFileRecords.isEmpty()){
            JOptionPane.showMessageDialog(this, "No Records to Save.");
            return;
        }

        try{
            mySqlController.insertRecords(myFileRecords);

            JOptionPane.showMessageDialog(this, "Saved " + myFileRecords.size() + " records to database.");
        } catch (Exception theException){
            JOptionPane.showMessageDialog(this, "Save failed: " + theException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCurrentToCsv(){
        if(myFileRecords.isEmpty()){
            JOptionPane.showMessageDialog(this, "No Records to Save.");
            return;
        }

        try {
            JFileChooser aFileChooser = new JFileChooser();
            aFileChooser.setSelectedFile(new File("records.csv"));
            if(aFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                CSVExportController.writeCsv(myFileRecords, aFileChooser.getSelectedFile(), "Export from " + myDirectoryString + " @ " + LocalDateTime.now());
                JOptionPane.showMessageDialog(this, "Saved CSV Records.");
            }
        }catch (Exception theException){
            JOptionPane.showMessageDialog(this, "Save failed: " + theException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendCurrentToEmail(){
        if(myFileRecords.isEmpty()){
            JOptionPane.showMessageDialog(this, "No Records to Save.");
            return;
        }

        try {
            JFileChooser aFileChooser = new JFileChooser();
            aFileChooser.setDialogTitle("Select CSV to Email");
            if(aFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                //email sending service
                File aFile = aFileChooser.getSelectedFile();
                String aEmailDestination = JOptionPane.showInputDialog(this, "Enter destination email.");

                if(aEmailDestination == null || aEmailDestination.trim().isEmpty()){
                    return;
                }

                JTextField aUser = new JTextField();
                JPasswordField aPassword = new JPasswordField();

                Object[] aMessage = {"Your email address:", aUser, "Your email password", aPassword};

                int aConfirmationMenu = JOptionPane.showConfirmDialog(this, aMessage, "Gmail SMTP Login", JOptionPane.OK_CANCEL_OPTION);

                if(aConfirmationMenu == JOptionPane.OK_OPTION){
                    String aSenderEmail = aUser.getText().trim();
                    String aAppPassword = new String(aPassword.getPassword()).trim();

                    if(aSenderEmail.isEmpty() || aAppPassword.isEmpty()){
                        JOptionPane.showMessageDialog(this, "Email and password is required.");
                        return;
                    }

                    EmailService aEmailService = new EmailService(aSenderEmail, aAppPassword);

                    try {
                        aEmailService.sendEmail(aEmailDestination, "File Records", "CSV Records attached", aFile.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Email sent successfully.");
                    }catch (Exception theException){
                        //theException.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Email and password required.");
                    }
                }
            }
        }catch (Exception theException){
            JOptionPane.showMessageDialog(this, "Save failed: " + theException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
