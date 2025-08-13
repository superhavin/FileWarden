package view;

import controller.ChangeDirectoryController;
import controller.CSVExportController;
import controller.QueryController;
import controller.SQLController;
import model.EmailService;
import model.FileDirectoryModel;
import model.FileMonitor;
import model.FileRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.mail.*;

/**
 * Main application GUI view.
 */
public class FileView extends JFrame {
    private final FileDirectoryModel model;
    private final FileMonitor monitor;
    private final SQLController sqlController;
    private final ChangeDirectoryController changeDirectoryController;
    private final QueryController queryController;

    private final DefaultTableModel tableModel;
    private Path monitoredPath;

    public FileView(FileDirectoryModel model,
                    FileMonitor monitor,
                    SQLController sqlController,
                    ChangeDirectoryController changeDirectoryController,
                    QueryController queryController) {
        super("File Watcher");
        this.model = model;
        this.monitor = monitor;
        this.sqlController = sqlController;
        this.changeDirectoryController = changeDirectoryController;
        this.queryController = queryController;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Menu
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem startItem = new JMenuItem("Start Monitoring");
        startItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        JMenuItem stopItem = new JMenuItem("Stop Monitoring");
        stopItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        fileMenu.add(startItem);
        fileMenu.add(stopItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu dbMenu = new JMenu("Database");
        JMenuItem saveItem = new JMenuItem("Save Current To DB");
        JMenuItem queryItem = new JMenuItem("Query Database");
        JMenuItem clearItem = new JMenuItem("Clear Database");
        dbMenu.add(saveItem);
        dbMenu.add(queryItem);
        dbMenu.add(clearItem);

        JMenu help = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        help.add(aboutItem);

        mb.add(fileMenu);
        mb.add(dbMenu);
        mb.add(help);
        setJMenuBar(mb);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton btnStart = new JButton("Start");
        JButton btnStop = new JButton("Stop");
        JButton btnSave = new JButton("Save to DB");
        JButton btnQuery = new JButton("Query DB");
        toolBar.add(btnStart);
        toolBar.add(btnStop);
        toolBar.add(btnSave);
        toolBar.add(btnQuery);

        add(toolBar, BorderLayout.NORTH);

        // Center table
        tableModel = new DefaultTableModel(new Object[]{"File Name", "Ext", "Path", "Activity", "DateTime"}, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Controls panel on the right
        JPanel right = new JPanel(new GridLayout(0,1,6,6));
        JButton chooseDir = new JButton("Choose Directory...");
        JLabel currentDirLabel = new JLabel("No directory selected");
        JTextField extText = new JTextField("txt,log,csv");
        JButton setExt = new JButton("Set Extensions");
        right.add(chooseDir);
        right.add(currentDirLabel);
        right.add(new JLabel("Watch Extensions (comma separated):"));
        right.add(extText);
        right.add(setExt);

        // Email export controls
        JButton exportCsvBtn = new JButton("Export Table to CSV");
        right.add(exportCsvBtn);
        JButton emailBtn = new JButton("Email CSV...");
        right.add(emailBtn);

        add(right, BorderLayout.EAST);

        // Wire actions
        startItem.addActionListener(e -> startMonitoring(currentDirLabel));
        btnStart.addActionListener(e -> startMonitoring(currentDirLabel));
        stopItem.addActionListener(e -> stopMonitoring());
        btnStop.addActionListener(e -> stopMonitoring());

        chooseDir.addActionListener(e -> {
            Path p = changeDirectoryController.chooseDirectory(this);
            if (p != null) {
                monitoredPath = p;
                currentDirLabel.setText("Dir: " + p.toAbsolutePath().toString());
            }
        });

        setExt.addActionListener(e -> {
            String text = extText.getText();
            String[] parts = text.split(",");
            monitor.setExtensionsToWatch(java.util.Arrays.asList(parts));
            JOptionPane.showMessageDialog(this, "Extensions set.");
        });

        btnSave.addActionListener(e -> saveCurrentToDb());
        saveItem.addActionListener(e -> saveCurrentToDb());

        queryItem.addActionListener(e -> {
            QueryView qv = new QueryView(this, queryController);
            qv.setVisible(true);
        });
        btnQuery.addActionListener(e -> {
            QueryView qv = new QueryView(this, queryController);
            qv.setVisible(true);
        });

        exportCsvBtn.addActionListener(e -> {
            try {
                List<FileRecord> rows = model.getRecords();
                if (rows.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No records to export");
                    return;
                }
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File("records.csv"));
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    CSVExportController.writeCsv(rows, chooser.getSelectedFile(), "Export from FileView " + LocalDateTime.now());
                    JOptionPane.showMessageDialog(this, "CSV saved.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        emailBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select CSV to email");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();

                String to = JOptionPane.showInputDialog(this, "Enter destination email:");
                if (to == null || to.trim().isEmpty()) return;

                // Ask for SMTP login details
                JTextField user = new JTextField();
                JPasswordField pwd = new JPasswordField();
                Object[] msg = {
                        "Your Gmail address:", user,
                        "Your Gmail App Password (not your normal password):", pwd
                };
                int ok = JOptionPane.showConfirmDialog(
                        this,
                        msg,
                        "Gmail SMTP Login",
                        JOptionPane.OK_CANCEL_OPTION
                );

                if (ok == JOptionPane.OK_OPTION) {
                    String senderEmail = user.getText().trim();
                    String appPassword = new String(pwd.getPassword()).trim();

                    if (senderEmail.isEmpty() || appPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Email and password required.");
                        return;
                    }

                    EmailService emailService = new EmailService(senderEmail, appPassword);

                    try {
                        emailService.sendEmail(
                                to, // recipient entered earlier
                                "File Monitor Report",
                                "Attached is the CSV file you requested.",
                                f.getAbsolutePath()
                        );
                        JOptionPane.showMessageDialog(this, "Email sent successfully!");
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Failed to send email: " + ex.getMessage());
                    }
                }
            }
        });


        aboutItem.addActionListener(e -> {
            AboutDialog d = new AboutDialog(this);
            d.setVisible(true);
        });

        clearItem.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear all DB records?") == JOptionPane.YES_OPTION) {
                try {
                    sqlController.clearDatabase();
                    JOptionPane.showMessageDialog(this, "DB cleared");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Clear failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // model listener to update table
        model.addPropertyChangeListener(evt -> {
            SwingUtilities.invokeLater(this::refreshTableFromModel);
        });

        // window close behavior
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (!model.getRecords().isEmpty()) {
                    int res = JOptionPane.showConfirmDialog(FileView.this, "Write current contents to DB before exit?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (res == JOptionPane.CANCEL_OPTION) return;
                    if (res == JOptionPane.YES_OPTION) {
                        saveCurrentToDb();
                    }
                }
                if (monitor.isRunning()) monitor.stop();
                dispose();
                System.exit(0);
            }
        });
    }

    private void startMonitoring(JLabel currentDirLabel) {
        if (monitoredPath == null) {
            JOptionPane.showMessageDialog(this, "Choose a directory first");
            return;
        }
        try {
            changeDirectoryController().startMonitoring(monitoredPath);
            JOptionPane.showMessageDialog(this, "Monitoring started.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to start: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ChangeDirectoryController changeDirectoryController() {
        // reflection not used; we stored the controller as a field in constructor args
        return changeDirectoryController;
    }

    private void stopMonitoring() {
        if (!monitor.isRunning()) {
            JOptionPane.showMessageDialog(this, "Not running");
            return;
        }
        changeDirectoryController.stopMonitoring();
        JOptionPane.showMessageDialog(this, "Stopped monitoring.");
    }

    private void saveCurrentToDb() {
        List<FileRecord> rows = model.getRecords();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to save");
            return;
        }
        try {
            sqlController.insertRecords(rows);
            JOptionPane.showMessageDialog(this, "Saved " + rows.size() + " records to DB");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTableFromModel() {
        tableModel.setRowCount(0);
        for (FileRecord r : model.getRecords()) {
            tableModel.addRow(new Object[]{r.getFileName(), r.getExtension(), r.getPath(), r.getEventType(), r.getDateTimeString()});
        }
    }
}
