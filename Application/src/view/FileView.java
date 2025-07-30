package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import controller.ChangeDirectoryController;
import model.FileDirectoryModel;

/**
 * The view of the Application.
 */
public class FileView extends JPanel implements PropertyChangeListener {
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

    public FileView(){
        GridLayout theLayout = new GridLayout(0, 2);
        setLayout(theLayout);

        myStartButton = new JButton("Start Game");
        myStartButton.setMnemonic('n');

        myChangeDirectoryButton = new JButton("Change Directory");

        myDisplayLabel = new JLabel(""); //visualization of system files

        myDirectoryLabel = new JLabel(""); //visualization of monitored directory

        myNewDirectoryField = new JFormattedTextField();

        add(myStartButton);
        add(myChangeDirectoryButton);
        add(myDisplayLabel);
        add(myDirectoryLabel);
        add(myNewDirectoryField);

        addListeners();
    }

    private void addListeners() {

        myStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                FileDirectoryModel fileDirectoryModel = FileDirectoryModel.getInstance();
                fileDirectoryModel.startApp();
                myDisplayLabel.setText(fileDirectoryModel.displayDirectory());

                //[INSERT] initialization of other displays
            }
        });

        myChangeDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent theEvent) {
                FileDirectoryModel fileDirectoryModel = FileDirectoryModel.getInstance();
                if (fileDirectoryModel.getGameActive()){
                    String refinedDirectory = ChangeDirectoryController.refineDirectory(myNewDirectoryField.getText());
                    fileDirectoryModel.setDirectory(refinedDirectory); //need nullException for grabbing textField

                }
                else{
                    JOptionPane.showMessageDialog(null, "Game not active!");
                }

            }
        });
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight){
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final FileView mainPanel = new FileView();

                FileDirectoryModel.getInstance().addPropertyChangeListener(mainPanel);
                final JFrame window = new JFrame("FileWarden");

                window.setSize(new Dimension(theWidth, theHeight));
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
            if ((boolean) evt.getNewValue() == false) {
                myChangeDirectoryButton.setEnabled(false);
                JOptionPane.showMessageDialog(null, "Application not active!");
            }
        }

        if (evt.getPropertyName() == "tick"){
            //[INSERT] periodically checks monitored Directory
            FileDirectoryModel fileDirectoryModel = FileDirectoryModel.getInstance();
            fileDirectoryModel.advance();
        }

        if (evt.getPropertyName() == "monitorDirectory"){
            //[INSERT] the display of the monitored Directory
            myDirectoryLabel.setText(null);
        }
    }
}
