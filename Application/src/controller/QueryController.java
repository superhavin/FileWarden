package controller;

import model.FileRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade for query operations used by QueryView.
 */
public class QueryController {
    private final SQLController sqlController;

    public QueryController(SQLController sqlController) {
        this.sqlController = sqlController;
    }

    public List<FileRecord> queryByExtension(String ext) throws SQLException {
        return sqlController.queryByExtension(ext);
    }

    public List<FileRecord> queryByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        return sqlController.queryByDateRange(start, end);
    }

    public List<FileRecord> queryByActivity(String activity) throws SQLException {
        return sqlController.queryByActivity(activity);
    }

    public List<FileRecord> queryByDirectory(String dir) throws SQLException {
        return sqlController.queryByDirectory(dir);
    }

    public void clearDatabase() throws SQLException {
        sqlController.clearDatabase();
    }
}