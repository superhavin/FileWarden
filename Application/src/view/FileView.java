package view;

import controller.ChangeDirectoryController;
import model.FileDirectoryModel;
//import model.FileEvent;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.*;

/**
 * The view of the Application.
 */
public class FileView extends JPanel implements PropertyChangeListener, Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    /**
     * Button to start the application.
     */
    private JButton myToggleButton;
    /**
     * Label to show basic system information.
     */
    private JLabel myCurrentDirectoryLabel;
    /**
     * Field to grab new directory.
     */
    private JTextField myNewDirectoryField;
    /**
     * Button to start monitoring the grabbed Directory
     */
    private JButton myMonitorButton;
    /**
     * Static instance of the model.
     */
    private static FileDirectoryModel fileDirectoryModel;

    /**
     * Static instance of a Directory view.
     */
    private static ArrayList<FileDirectoryWindow> fileDirectoryWindows;
    /**
     * maximum number of windows a file directory window can have
     */
    private final static int MAX_WINDOWS = 1;

    /**
     * Constructor of the view.
     */
    public FileView(){
        fileDirectoryModel = FileDirectoryModel.getInstance();
        fileDirectoryWindows = new ArrayList<>();

        GridLayout theLayout = new GridLayout(0, 2);
        setLayout(theLayout);

        myToggleButton = new JButton("Start");
        myToggleButton.setMnemonic('s');

        myCurrentDirectoryLabel = new JLabel(""); //visualization of system files
        myCurrentDirectoryLabel.setEnabled(false);

        myNewDirectoryField = new JFormattedTextField();
        myNewDirectoryField.setEnabled(false);

        myMonitorButton = new JButton("Start Monitoring");
        myMonitorButton.setEnabled(false);

        add(myToggleButton);
        add(myCurrentDirectoryLabel);
        add(myNewDirectoryField);
        add(myMonitorButton);

        addListeners();
    }

    private void addListeners() {

        myToggleButton.addActionListener(theEvent -> {
            if(!fileDirectoryModel.isActive()){
                fileDirectoryModel.startApp();
            }else{
                fileDirectoryModel.resetApp();
                //CLOSE ALL Windows
            }
        });

        myMonitorButton.addActionListener(theEvent -> {

            String currentFileDirectory = ChangeDirectoryController.refineDirectory(myNewDirectoryField.getText());

            //checks if the application is active, if max instances have been reached, and if window has already exists
            if(fileDirectoryModel.isActive()
                    && !(fileDirectoryWindows.size() > MAX_WINDOWS)
                    && !(fileDirectoryModel.containsDirectory(currentFileDirectory))
            ){
                FileDirectoryWindow newView = new FileDirectoryWindow(currentFileDirectory);
                fileDirectoryWindows.add(newView);
                fileDirectoryModel.startMonitoring(currentFileDirectory, newView);
            }else{
                JOptionPane.showMessageDialog(null,
                        "Targeted directory is not valid.");
            }
        });
    }

    /**
     * Allows other package-private classes to access the fileDirectoryModel instance.
     * @return the fileDirectoryModel instance.
     */
    static FileDirectoryModel getFileDirectoryModel(){
        return fileDirectoryModel;
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight){
        javax.swing.SwingUtilities.invokeLater(() -> {
            final FileView mainPanel = new FileView();

            fileDirectoryModel.addPropertyChangeListener(mainPanel);

            final JFrame window = new JFrame("File Warden");

            mainPanel.setPreferredSize(new Dimension(theWidth, theHeight));

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(mainPanel);
            window.pack();
            window.setVisible(true);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent theEvent) {
        switch (theEvent.getPropertyName()) {
            case "active":
                setAllControls((boolean) theEvent.getNewValue());
                break;

            case "changeDirectory":
                String theView = ChangeDirectoryController.visualizeDirectory((String) theEvent.getNewValue());
                myCurrentDirectoryLabel.setText(theView);
                break;

            case "stopDirectory":
                String removedDirectory = ChangeDirectoryController.visualizeDirectory((String) theEvent.getOldValue());
                if (myCurrentDirectoryLabel.getText().equals(removedDirectory)) {
                    myCurrentDirectoryLabel.setText(""); //clears current directory visually, if current visible
                }
                break;
        }
    }


    /**
     * Helper method which disables and enables components of the panel.
     */
    private void setAllControls(boolean status) {
        myCurrentDirectoryLabel.setEnabled(status);
        myNewDirectoryField.setEnabled(status);

        myMonitorButton.setEnabled(status);

        myToggleButton.setText("Stop");

        if(!status){
            final String BLANK = "";
            myCurrentDirectoryLabel.setText(BLANK);
            myNewDirectoryField.setText(BLANK);

            myToggleButton.setText("Start");

            closeAllWindows();
        }
    }

    /**
     * Helper method which closes all the windows.
     */
    private void closeAllWindows() {
        for(FileDirectoryWindow window : fileDirectoryWindows){
            window.closeWindow();
        }
    }
}

/**
 * Windows which display the monitored information of the current directory.
 */
class FileDirectoryWindow extends JPanel implements PropertyChangeListener{
    /**
     * String Log Box of all the active events on the file.
     */
    private final JTextArea myDirectoryBox;
    /**
     * String directory of this window.
     */
    private final String myDirectoryString;
    /**
     * Current window.
     */
    private final JFrame myWindow;

    /**
     * Area of the windows.
     */
    private final static int WINDOW_AREA = 300;

    /**
     * Constructor of the view's window.
     * @param theMonitoredDirectory the window's directory.
     */
    FileDirectoryWindow(final String theMonitoredDirectory) {
        myDirectoryString = theMonitoredDirectory;

        setLayout(new BorderLayout());

        myDirectoryBox = new JTextArea();
        myDirectoryBox.setEnabled(false);
        myDirectoryBox.setText(myDirectoryString);

        JScrollPane scrollPane = new JScrollPane(myDirectoryBox);
        add(scrollPane, BorderLayout.CENTER);

        myWindow = new JFrame("Monitoring: " + myDirectoryString);
        myWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myWindow.setContentPane(this);
        myWindow.setSize(new Dimension(WINDOW_AREA, WINDOW_AREA));
        myWindow.setVisible(true);

        addListeners();
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
            public void windowClosing(WindowEvent theEvent){
                FileView.getFileDirectoryModel().stopMonitoring(myDirectoryString);
            }
        });
    }

    @Override
    public void propertyChange(final PropertyChangeEvent theEvent){
        switch (theEvent.getPropertyName()){
            case "monitorDirectory":
                model.FileEvent theFile = (model.FileEvent) theEvent.getNewValue();
                myDirectoryBox.append("\n" + theFile.eventSummary());
                break;
        }
    }
}
