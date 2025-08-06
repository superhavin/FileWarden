package controller;

import org.sqlite.SQLiteDataSource;

import java.beans.PropertyChangeSupport;

public class SQLController {

    private final PropertyChangeSupport changes;
    static SQLiteDataSource dataSource = null;

    public SQLController(final PropertyChangeSupport changes) {
        this.changes = changes;

        try {
            dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:filewarden.sqlite");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void createTables(){

    }

    public static void insertTables(){

    }

    public void runLookup(final String table){
        String query = "SELECT * FROM " + table;
        //run query then returns results and fires them
        String resultSet = "";
        changes.firePropertyChange("query", null, resultSet);
    }

}
