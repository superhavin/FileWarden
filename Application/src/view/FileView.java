package view;

import controller.ChangeDirectoryController;
import model.FileDirectoryModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.io.Serializable;
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
    private JButton myStartButton;
    /**
     * Button to change the file directory.
     */
    private JButton myChangeDirectoryButton;
    /**
     * Label to show basic system information.
     */
    private JLabel myDisplayLabel;
    /**
     * Label to display current directory.
     */
    private JLabel myDirectoryLabel;
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

    public FileView(){
        fileDirectoryModel = FileDirectoryModel.getInstance();

        GridLayout theLayout = new GridLayout(0, 2);
        setLayout(theLayout);

        myStartButton = new JButton("Start");
        myStartButton.setMnemonic('s');

        myChangeDirectoryButton = new JButton("Change Directory");
        myChangeDirectoryButton.setEnabled(false);

        myDisplayLabel = new JLabel(""); //visualization of system files
        myDisplayLabel.setEnabled(false);

        myDirectoryLabel = new JLabel(""); //visualization of monitored directory
        myDirectoryLabel.setEnabled(false);

        myNewDirectoryField = new JFormattedTextField();
        myNewDirectoryField.setEnabled(false);

        myMonitorButton = new JButton("Start Monitoring");
        myMonitorButton.setEnabled(false);

        add(myStartButton);
        add(myChangeDirectoryButton);
        add(myDisplayLabel);
        add(myDirectoryLabel);
        add(myNewDirectoryField);
        add(myMonitorButton);

        addListeners();
    }



    private void addListeners() {

        myStartButton.addActionListener(theEvent -> {
            if(!fileDirectoryModel.getGameActive()){
                fileDirectoryModel.startApp();
                //[INSERT] initialization of other displays
            }else{
                fileDirectoryModel.resetApp();
            }
        });

        myChangeDirectoryButton.addActionListener(theEvent -> {
            if (fileDirectoryModel.getGameActive()){
                String refinedDirectory = ChangeDirectoryController.refineDirectory(myNewDirectoryField.getText());
                fileDirectoryModel.changeDirectory(refinedDirectory); //need nullException for grabbing textField
            }
            else{
                JOptionPane.showMessageDialog(null, "Application not active!");
            }

        });

        myMonitorButton.addActionListener(theEvent -> {
            if(fileDirectoryModel.getGameActive()){
                fileDirectoryModel.displayDirectory();
            }else{
                JOptionPane.showMessageDialog(null, "Application not active!");
            }
        });
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight){
        javax.swing.SwingUtilities.invokeLater(() -> {
            final FileView mainPanel = new FileView();

            fileDirectoryModel.addPropertyChangeListener(mainPanel);

            final JFrame window = new JFrame("FileWarden");

            mainPanel.setPreferredSize(new Dimension(theWidth, theHeight));

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(mainPanel);
            window.pack();
            window.setVisible(true);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "active":
                setAllControls((boolean)evt.getNewValue());
                break;

            case "changeDirectory", "monitorDirectory":
                String theView = ChangeDirectoryController.visualizeDirectory((String) evt.getNewValue());
                if(evt.getPropertyName() == "changeDirectory"){
                    myDisplayLabel.setText(theView);
                }else{
                    myDirectoryLabel.setText(theView);
                }
                break;
        }
    }

    /**
     * Helper method which disables and enables components of the panel
     */
    private void setAllControls(boolean status) {
        myDisplayLabel.setEnabled(status);
        myNewDirectoryField.setEnabled(status);
        myDirectoryLabel.setEnabled(status);

        myChangeDirectoryButton.setEnabled(status);
        myMonitorButton.setEnabled(status);

        if(!status){
            final String BLANK = "";
            myDisplayLabel.setText(BLANK);
            myNewDirectoryField.setText(BLANK);
            myDirectoryLabel.setText(BLANK);
        }
    }
}
