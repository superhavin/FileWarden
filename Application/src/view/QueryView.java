package view;

import controller.CSVExportController;
import controller.SQLController;
import model.FileRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.AbstractCollection;

/**
 * Query window where user runs queries, exports CSV, and emails results.
 */
public class QueryView extends JDialog {

    private final DefaultTableModel tableModel;
    private AbstractCollection<FileRecord> lastResults;

    private final SQLController queryController;

    private final JButton runExt;
    private final JButton runAct;
    private final JButton runDir;
    private final JButton runDates;
    private final JButton exportCsv;
    private final JButton clearDb;
    private final JTextField startField;
    private final JTextField endField;
    private final JTextField dirField;
    private final JTextField activityField;
    private final JTextField extField;

    public QueryView(final Window owner, final SQLController queryController) {
        super(owner, "Database Query", ModalityType.APPLICATION_MODAL);
        this.queryController = queryController;

        setLayout(new BorderLayout());
        JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));

        // simple controls for demonstrations
        extField = new JTextField();
        controls.add(new JLabel("Extension (e.g. txt):"));
        controls.add(extField);

        activityField = new JTextField();
        controls.add(new JLabel("Activity (CREATED/MODIFIED/DELETED/RENAMED):"));
        controls.add(activityField);

        dirField = new JTextField();
        controls.add(new JLabel("Directory (prefix search):"));
        controls.add(dirField);

        // yyyy-MM-ddTHH:mm
        startField = new JTextField();
        endField = new JTextField();
        controls.add(new JLabel("Start (yyyy-MM-ddTHH:mm):"));
        controls.add(startField);
        controls.add(new JLabel("End (yyyy-MM-ddTHH:mm):"));
        controls.add(endField);

        runExt = new JButton("Run Extension Query");
        runAct = new JButton("Run Activity Query");


        runDir = new JButton("Run Directory Query");
        runDates = new JButton("Run Date Range Query");

        JPanel btnPanel = new JPanel(new GridLayout(0,1,4,4));
        btnPanel.add(runExt);
        btnPanel.add(runAct);
        btnPanel.add(runDir);
        btnPanel.add(runDates);

        exportCsv = new JButton("Export to CSV");
        clearDb = new JButton("Clear DB");


        btnPanel.add(exportCsv);
        btnPanel.add(clearDb);

        add(controls, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.WEST);

        tableModel = new DefaultTableModel(new Object[]{"File Name", "Ext", "Path", "Activity", "DateTime"}, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> setVisible(false));
        add(close, BorderLayout.SOUTH);

        setSize(900, 500);
        setLocationRelativeTo(owner);
    }

    private void addListeners(){

        clearDb.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear all DB records?") == JOptionPane.YES_OPTION) {
                try {
                    queryController.clearDatabase();
                    JOptionPane.showMessageDialog(this, "Database cleared.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error clearing DB: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exportCsv.addActionListener(e -> {
            if (lastResults == null || lastResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No results to export", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("query_results.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    CSVExportController.writeCsv(lastResults, f, "Exported from QueryView");
                    JOptionPane.showMessageDialog(this, "Saved to " + f.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        runDates.addActionListener(e -> {
            try {
                LocalDateTime s = LocalDateTime.parse(startField.getText().trim());
                LocalDateTime e2 = LocalDateTime.parse(endField.getText().trim());
                lastResults = queryController.queryByDateRange(s, e2);
                populateTable(lastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        runDir.addActionListener(e -> {
            try {
                lastResults = queryController.queryByDirectory(dirField.getText().trim());
                populateTable(lastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        runAct.addActionListener(e -> {
            try {
                lastResults = queryController.queryByActivity(activityField.getText().trim());
                populateTable(lastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        runExt.addActionListener(e -> {
            try {
                lastResults = queryController.queryByExtension(extField.getText().trim());
                populateTable(lastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void populateTable(final AbstractCollection<FileRecord> rows) {
        tableModel.setRowCount(0);
        for (FileRecord r : rows) {
            tableModel.addRow(new Object[]{r.getFileName(), r.getExtension(), r.getPath(), r.getEventType(), r.getDateTimeString()});
        }
    }
}