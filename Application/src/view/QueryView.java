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
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class QueryView extends JDialog {
    /** The table model used to display query results. */
    private final DefaultTableModel myTableModel;

    /** Stores the most recent set of results from a query. */
    private AbstractCollection<FileRecord> myLastResults;

    /** Controller used to execute database queries. */
    private final SQLController myQueryController;

    /** Input field for start date and time. */
    private final JTextField myStartField;

    /** Input field for end date and time. */
    private final JTextField myEndField;

    /** Input field for directory path filtering. */
    private final JTextField myDirField;

    /** Input field for file activity type filtering. */
    private final JTextField myActivityField;

    /** Input field for file extension filtering. */
    private final JTextField myExtensionField;

    /**
     * Creates a new query view window where the user can perform database queries,
     * export results to CSV, and clear records.
     *
     * @param theOwner the parent window
     * @param thQueryController the SQL controller that handles database queries
     */
    public QueryView(final Window theOwner, final SQLController thQueryController) {
        super(theOwner, "Database Query", ModalityType.APPLICATION_MODAL);
        this.myQueryController = thQueryController;

        setLayout(new BorderLayout());
        JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));

        // simple controls for demonstrations
        myExtensionField = new JTextField();
        controls.add(new JLabel("Extension (e.g. txt):"));
        controls.add(myExtensionField);

        myActivityField = new JTextField();
        controls.add(new JLabel("Activity (CREATED/MODIFIED/DELETED/RENAMED):"));
        controls.add(myActivityField);

        myDirField = new JTextField();
        controls.add(new JLabel("Directory (prefix search):"));
        controls.add(myDirField);

        // yyyy-MM-ddTHH:mm
        myStartField = new JTextField();
        myEndField = new JTextField();
        controls.add(new JLabel("Start (yyyy-MM-ddTHH:mm):"));
        controls.add(myStartField);
        controls.add(new JLabel("End (yyyy-MM-ddTHH:mm):"));
        controls.add(myEndField);

        JButton myRunExt = new JButton("Run Extension Query");
        JButton myRunAct = new JButton("Run Activity Query");

        JButton myRunDir = new JButton("Run Directory Query");
        JButton myRunDates = new JButton("Run Date Range Query");

        JPanel btnPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        btnPanel.add(myRunExt);
        btnPanel.add(myRunAct);
        btnPanel.add(myRunDir);
        btnPanel.add(myRunDates);

        JButton myExportCsv = new JButton("Export to CSV");
        JButton myClearDb = new JButton("Clear DB");

        btnPanel.add(myExportCsv);
        btnPanel.add(myClearDb);

        add(controls, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.WEST);

        myTableModel = new DefaultTableModel(new Object[]{"File Name", "Ext", "Path", "Activity", "DateTime"}, 0);
        JTable table = new JTable(myTableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> setVisible(false));
        add(close, BorderLayout.SOUTH);

        pack();
        setSize(900, 500);
        setLocationRelativeTo(theOwner);

        myClearDb.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear all DB records?") == JOptionPane.YES_OPTION) {
                try {
                    myQueryController.clearDatabase();
                    JOptionPane.showMessageDialog(this, "Database cleared.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error clearing DB: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        myExportCsv.addActionListener(e -> {
            if (myLastResults == null || myLastResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No results to export", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("query_results.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    CSVExportController.writeCsv(myLastResults, f, "Exported from QueryView");
                    JOptionPane.showMessageDialog(this, "Saved to " + f.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        myRunDates.addActionListener(e -> {
            try {
                LocalDateTime s = LocalDateTime.parse(myStartField.getText().trim());
                LocalDateTime e2 = LocalDateTime.parse(myEndField.getText().trim());
                myLastResults = myQueryController.queryByDateRange(s, e2);
                populateTable(myLastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        myRunDir.addActionListener(e -> {
            try {
                myLastResults = myQueryController.queryByDirectory(myDirField.getText().trim());
                populateTable(myLastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        myRunAct.addActionListener(e -> {
            try {
                myLastResults = myQueryController.queryByActivity(myActivityField.getText().trim());
                populateTable(myLastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        myRunExt.addActionListener(e -> {
            try {
                myLastResults = myQueryController.queryByExtension(myExtensionField.getText().trim());
                populateTable(myLastResults);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Populates the results table with the given collection of file records.
     *
     * @param rows the file records to display in the table
     */
    private void populateTable(final AbstractCollection<FileRecord> rows) {
        myTableModel.setRowCount(0);
        for (FileRecord r : rows) {
            myTableModel.addRow(new Object[]{
                    r.getFileName(),
                    r.getExtension(),
                    r.getPath(),
                    r.getEventType(),
                    r.getDateTimeString()
            });
        }
    }
}
