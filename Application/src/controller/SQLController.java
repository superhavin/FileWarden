package controller;

import model.FileRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite controller to persist FileRecord entries and query them.
 */
public class SQLController {
    private final String dbUrl;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SQLController(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDb();
    }

    private void initDb() {
        String ddl = "CREATE TABLE IF NOT EXISTS file_events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT NOT NULL," +
                "extension TEXT," +
                "path TEXT NOT NULL," +
                "event_type TEXT NOT NULL," +
                "date_time TEXT NOT NULL" +
                ");";
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement s = c.createStatement()) {
            s.execute(ddl);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create DB/table: " + e.getMessage(), e);
        }
    }

    public void insertRecord(FileRecord r) throws SQLException {
        String sql = "INSERT INTO file_events (file_name, extension, path, event_type, date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getFileName());
            ps.setString(2, r.getExtension());
            ps.setString(3, r.getPath());
            ps.setString(4, r.getEventType());
            ps.setString(5, r.getDateTime().format(DT_FMT));
            ps.executeUpdate();
        }
    }

    public void insertRecords(List<FileRecord> list) throws SQLException {
        String sql = "INSERT INTO file_events (file_name, extension, path, event_type, date_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            c.setAutoCommit(false);
            for (FileRecord r : list) {
                ps.setString(1, r.getFileName());
                ps.setString(2, r.getExtension());
                ps.setString(3, r.getPath());
                ps.setString(4, r.getEventType());
                ps.setString(5, r.getDateTime().format(DT_FMT));
                ps.addBatch();
            }
            ps.executeBatch();
            c.commit();
        }
    }

    public List<FileRecord> queryByExtension(String ext) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE extension = ?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ext == null ? "" : ext);
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    public List<FileRecord> queryByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE date_time BETWEEN ? AND ?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, start.format(DT_FMT));
            ps.setString(2, end.format(DT_FMT));
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    public List<FileRecord> queryByActivity(String activity) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE event_type = ?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, activity);
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    public List<FileRecord> queryByDirectory(String directoryPath) throws SQLException {
        String sql = "SELECT file_name, extension, path, event_type, date_time FROM file_events WHERE path LIKE ?";
        try (Connection c = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, directoryPath.endsWith("%") ? directoryPath : directoryPath + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rsToList(rs);
            }
        }
    }

    public void clearDatabase() throws SQLException {
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM file_events");
        }
    }

    private List<FileRecord> rsToList(ResultSet rs) throws SQLException {
        List<FileRecord> out = new ArrayList<>();
        while (rs.next()) {
            String fn = rs.getString("file_name");
            String ext = rs.getString("extension");
            String path = rs.getString("path");
            String ev = rs.getString("event_type");
            String dt = rs.getString("date_time");
            LocalDateTime dateTime = LocalDateTime.parse(dt, DT_FMT);
            out.add(new FileRecord(fn, ext, path, ev, dateTime));
        }
        return out;
    }
}
