package controller;

import org.sqlite.SQLiteDataSource;

public class SQLController {

    public static void createTables(){
        SQLiteDataSource dataSource = null;

        try {
            dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:filewarden.sqlite");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }


        String query = "CREATE TABLE IF NOT EXISTS Directory(" +
                "Filepath TEXT NOT NULL," + //"Attribute_Name Attribute_Type Constraints"
                "";
    }

    public static void runLookup(){



    }

}
