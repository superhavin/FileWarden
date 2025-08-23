package controller;

import model.FileRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractCollection;
import java.util.ArrayList;

/**
 * SQLite controller to persist FileRecord entries and query them.
 * <p>
 * This class provides database access to a SQLite file named file-warden.db by default.
 * It creates and maintains a single table file_events that stores metadata about file activity
 * such as file name, extension, path, event type, and the time of the event.
 * <p>
 * The class uses the singleton pattern via getInstance() to provide
 * a shared controller instance, though it also allows direct instantiation.
 *
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class SQLController {
    private static final String DEFAULT_DATABASE = "file-warden.db";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String myDatabasePath;
    private static SQLController myInstance = null;

    /**
     * Constructs a controller connected to the given SQLite database file.
     *
     * @param theDatabasePath the path to the SQLite database file
     */
    public SQLController(final String theDatabasePath) {
        myDatabasePath = "jdbc:sqlite:" + theDatabasePath;
        initialDatabase();
    }

    /**
     * Constructs a controller connected to the default SQLite database file file-warden.db.
     */
    public SQLController() {
        this(DEFAULT_DATABASE);
    }

    /**
     * Returns the singleton instance of this controller, creating it if necessary.
     *
     * @return the shared SQLController instance
     */
    public static SQLController getInstance() {
        if (myInstance == null) {
            myInstance = new SQLController();
        }
        return myInstance;
    }

    /**
     * Initializes the database by creating the file_events table if it does not already exist.
     */
    private void initialDatabase() {
        String ddl = "CREATE TABLE IF NOT EXISTS file_events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT NOT NULL," +
                "extension TEXT," +
                "path TEXT NOT NULL," +
                "event_type TEXT NOT NULL," +
                "date_time TEXT NOT NULL" +
                ");";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             Statement s = c.createStatement()) {
            s.execute(ddl);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create DB/table: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts a single FileRecord into the database.
     *
     * @param theFileRecord the record to insert
     * @throws SQLException if a database access error occurs
     */
    public void insertRecord(final FileRecord theFileRecord) throws SQLException {
        String sql = "INSERT INTO file_events (file_name, extension, path, event_type, date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theFileRecord.getFileName());
            ps.setString(2, theFileRecord.getExtension());
            ps.setString(3, theFileRecord.getPath());
            ps.setString(4, theFileRecord.getEventType());
            ps.setString(5, theFileRecord.getDateTime().format(DATE_FORMAT));
            ps.executeUpdate();
        }
    }

    /**
     * Inserts a collection of FileRecord objects into the database in batch mode.
     *
     * @param theFileRecords a collection of file records to insert
     * @throws SQLException if a database access error occurs
     */
    public void insertRecords(final AbstractCollection<FileRecord> theFileRecords) throws SQLException {
        String sql = "INSERT INTO file_events (file_name, extension, path, event_type, date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            c.setAutoCommit(false);
            for (FileRecord r : theFileRecords) {
                ps.setString(1, r.getFileName());
                ps.setString(2, r.getExtension());
                ps.setString(3, r.getPath());
                ps.setString(4, r.getEventType());
                ps.setString(5, r.getDateTime().format(DATE_FORMAT));
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
        }
    }

    /**
     * Queries all records with the given file extension.
     *
     * @param theExtension the file extension to filter by (may be null)
     * @return a collection of matching file records
     * @throws SQLException if a database access error occurs
     */
    public AbstractCollection<FileRecord> queryByExtension(final String theExtension) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE extension = ?";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theExtension == null ? "" : theExtension);
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    /**
     * Queries all records within a given date-time range.
     *
     * @param theStart the start of the date range (inclusive)
     * @param theEnd   the end of the date range (inclusive)
     * @return a collection of matching file records
     * @throws SQLException if a database access error occurs
     */
    public AbstractCollection<FileRecord> queryByDateRange(final LocalDateTime theStart, final LocalDateTime theEnd) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE date_time BETWEEN ? AND ?";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theStart.format(DATE_FORMAT));
            ps.setString(2, theEnd.format(DATE_FORMAT));
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    /**
     * Queries all records with the given event type.
     *
     * @param theActivity the event type to filter by (e.g., CREATED, MODIFIED, DELETED)
     * @return a collection of matching file records
     * @throws SQLException if a database access error occurs
     */
    public AbstractCollection<FileRecord> queryByActivity(final String theActivity) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE event_type = ?";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theActivity);
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    /**
     * Queries all records in a given directory path.
     *
     * @param theDirectoryPath the directory path prefix to filter by
     * @return a collection of matching file records
     * @throws SQLException if a database access error occurs
     */
    public AbstractCollection<FileRecord> queryByDirectory(final String theDirectoryPath) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE path LIKE ?";
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, theDirectoryPath.endsWith("%") ? theDirectoryPath : theDirectoryPath + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    /**
     * Clears all records from the file_events table.
     *
     * @throws SQLException if a database access error occurs
     */
    public void clearDatabase() throws SQLException {
        try (Connection c = DriverManager.getConnection(myDatabasePath);
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM file_events");
        }
    }

    /**
     * Converts a ResultSet into a collection of FileRecord objects.
     *
     * @param theResultSet the result set to process
     * @return a collection of file records
     * @throws SQLException if a database access error occurs
     */
    private AbstractCollection<FileRecord> rsToList(final ResultSet theResultSet) throws SQLException {
        final AbstractCollection<FileRecord> out = new ArrayList<>();

        while (theResultSet.next()) {
            String fn = theResultSet.getString("file_name");
            String ext = theResultSet.getString("extension");
            String path = theResultSet.getString("path");
            String ev = theResultSet.getString("event_type");
            String dt = theResultSet.getString("date_time");
            LocalDateTime dateTime = LocalDateTime.parse(dt, DATE_FORMAT);
            out.add(new FileRecord(fn, ext, path, ev, dateTime));
        }
        return out;
    }
}
