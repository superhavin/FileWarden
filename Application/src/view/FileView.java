package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.io.Serializable;
import javax.swing.*;

import controller.ChangeDirectoryController;
import model.FileDirectoryModel;

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

        myStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                if(!fileDirectoryModel.getGameActive()){
                    fileDirectoryModel.startApp();
                    //[INSERT] initialization of other displays
                }else{
                    fileDirectoryModel.resetApp();
                }
            }
        });

        myChangeDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                if (fileDirectoryModel.getGameActive()){
                    String refinedDirectory = ChangeDirectoryController.refineDirectory(myNewDirectoryField.getText());
                    fileDirectoryModel.changeDirectory(refinedDirectory); //need nullException for grabbing textField
                }
                else{
                    JOptionPane.showMessageDialog(null, "Application not active!");
                }

            }
        });

        myMonitorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileDirectoryModel.getGameActive()){
                    fileDirectoryModel.displayDirectory();
                }else{
                    JOptionPane.showMessageDialog(null, "Application not active!");
                }
            }
        });
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight){
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final FileView mainPanel = new FileView();

                fileDirectoryModel.addPropertyChangeListener(mainPanel);

                final JFrame window = new JFrame("FileWarden");

                mainPanel.setPreferredSize(new Dimension(theWidth, theHeight));

                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setContentPane(mainPanel);
                window.pack();
                window.setVisible(true);
            }
        });
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == "active"){
            if ((boolean)evt.getNewValue() == true) {
                myChangeDirectoryButton.setEnabled(true);
                myDirectoryLabel.setEnabled(true);
                myNewDirectoryField.setEnabled(true);
                myMonitorButton.setEnabled(true);
            }else{
                myChangeDirectoryButton.setEnabled(false);
                myDirectoryLabel.setEnabled(false);
                myNewDirectoryField.setEnabled(false);
                myMonitorButton.setEnabled(false);

                final String BLANK = "";
                myDisplayLabel.setText(BLANK);
                myNewDirectoryField.setText(BLANK);
                myDirectoryLabel.setText(BLANK);
            }
        }

        if (evt.getPropertyName() == "changeDirectory"){
            String theView = ChangeDirectoryController.visualizeDirectory((String) evt.getNewValue());
            myDisplayLabel.setText(theView);
        }

        if (evt.getPropertyName() == "monitorDirectory"){
            String theView = ChangeDirectoryController.visualizeDirectory((String) evt.getNewValue());
            //[INSERT] the display of the monitored Directory, modify the view
            myDirectoryLabel.setText(theView);
        }
    }
}
