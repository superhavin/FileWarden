package controller;

import model.FileRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.AbstractCollection;

/**
 * Exports query results to CSV with metadata header.
 */
public class CSVExportController {
    /**
     * Writes given records to the CSV file. Prepend metadata about query (text).
     * @param records rows to write
     * @param csvFile destination file
     * @param queryDescription textual description of the query performed
     */
    public static void writeCsv(AbstractCollection<FileRecord> records, File csvFile, String queryDescription) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            // metadata header
            bw.write("# Query: " + (queryDescription == null ? "" : queryDescription));
            bw.newLine();
            bw.write("# Exported: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            bw.newLine();
            // CSV headers
            bw.write("file_name,extension,path,event_type,date_time");
            bw.newLine();
            for (FileRecord r : records) {
                // escape commas in fields by quoting if necessary
                bw.write(csvEscape(r.getFileName()) + "," +
                        csvEscape(r.getExtension()) + "," +
                        csvEscape(r.getPath()) + "," +
                        csvEscape(r.getEventType()) + "," +
                        csvEscape(r.getDateTimeString()));
                bw.newLine();
            }
            bw.flush();
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        } else return s;
    }
}