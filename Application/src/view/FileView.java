package view;

import controller.ChangeDirectoryController;
import model.FileDirectoryModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class FileView extends JPanel implements PropertyChangeListener {
    private final JButton myStartButton;
    private final JButton myChangeDirectoryButton;
    private final JButton myMonitorButton;
    private final JButton directoryChooserButton;

    private final JLabel myStatusLabel;
    private final JTextField myNewDirectoryField;
    private final JTextArea eventLogArea;
    private final JScrollPane logScrollPane;

    private static FileDirectoryModel fileDirectoryModel;

    public FileView() {
        fileDirectoryModel = FileDirectoryModel.getInstance();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        myStartButton = new JButton("Start / Reset");
        myChangeDirectoryButton = new JButton("Change Directory");
        myMonitorButton = new JButton("Start Monitoring");
        directoryChooserButton = new JButton("Select Directory");
        myNewDirectoryField = new JTextField(30);

        topPanel.add(myStartButton);
        topPanel.add(directoryChooserButton);
        topPanel.add(myNewDirectoryField);
        topPanel.add(myChangeDirectoryButton);
        topPanel.add(myMonitorButton);

        add(topPanel, BorderLayout.NORTH);

        myStatusLabel = new JLabel("Status: Inactive");
        myStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myStatusLabel.setForeground(Color.RED);
        add(myStatusLabel, BorderLayout.SOUTH);

        eventLogArea = new JTextArea(20, 50);
        eventLogArea.setEditable(false);
        logScrollPane = new JScrollPane(eventLogArea);
        add(logScrollPane, BorderLayout.CENTER);

        setAllControlsEnabled(false);
        addListeners();
    }

    private void setAllControlsEnabled(boolean enabled) {
        myChangeDirectoryButton.setEnabled(enabled);
        myMonitorButton.setEnabled(enabled);
        myNewDirectoryField.setEnabled(enabled);
        directoryChooserButton.setEnabled(enabled);
    }

    private void addListeners() {
        myStartButton.addActionListener(e -> {
            if (!fileDirectoryModel.getGameActive()) {
                fileDirectoryModel.startApp();
            } else {
                fileDirectoryModel.resetApp();
            }
        });

        myChangeDirectoryButton.addActionListener(e -> {
            if (fileDirectoryModel.getGameActive()) {
                String refinedDirectory = ChangeDirectoryController.refineDirectory(myNewDirectoryField.getText());
                fileDirectoryModel.changeDirectory(refinedDirectory);
            } else {
                JOptionPane.showMessageDialog(null, "Application not active!");
            }
        });

        myMonitorButton.addActionListener(e -> {
            if (fileDirectoryModel.getGameActive()) {
                fileDirectoryModel.displayDirectory();
            } else {
                JOptionPane.showMessageDialog(null, "Application not active!");
            }
        });

        directoryChooserButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();
                myNewDirectoryField.setText(selectedDir.getAbsolutePath());
            }
        });
    }

    public static void createAndShowGUI(final int theWidth, final int theHeight) {
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
                boolean isActive = (boolean) evt.getNewValue();
                setAllControlsEnabled(isActive);
                myStatusLabel.setText("Status: " + (isActive ? "Active" : "Inactive"));
                myStatusLabel.setForeground(isActive ? Color.GREEN.darker() : Color.RED);
                if (!isActive) {
                    myNewDirectoryField.setText("");
                    eventLogArea.setText("");
                }
                break;

            case "changeDirectory":
                String newDir = (String) evt.getNewValue();
                eventLogArea.append("Changed directory to: " + newDir + "\n");
                break;

            case "monitorDirectory":
                String eventMsg = (String) evt.getNewValue();
                eventLogArea.append(eventMsg + "\n");
                eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength());
                break;
        }
    }
}
