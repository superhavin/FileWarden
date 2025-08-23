package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single file event record.
 * A record stores the file name, extension, path, event type,
 * timestamp, and a formatted timestamp string.
 *
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class FileRecord {
    /** The base name of the file without path information. */
    private final String myFileName;

    /** The extension of the file, or empty string if none exists. */
    private final String myExtension;

    /** The full path of the file as a string. */
    private final String myPath;

    /** The type of event, such as CREATED, MODIFIED, DELETED, or RENAMED. */
    private final String myEventType;

    /** The timestamp of when the event occurred. */
    private final LocalDateTime myDateTime;

    /** The timestamp formatted as a human-readable string. */
    private final String myDateTimeString;

    /**
     * Creates a new file record with the given information.
     *
     * @param theFileName the name of the file
     * @param theExtension the extension of the file
     * @param thePath the path where the file is located
     * @param theEventType the type of event that occurred
     * @param theDateTime the time when the event happened
     */
    public FileRecord(final String theFileName, final String theExtension, final String thePath,
                      final String theEventType, final LocalDateTime theDateTime) {
        myFileName = theFileName;
        myExtension = theExtension;
        myPath = thePath;
        myEventType = theEventType;
        myDateTime = theDateTime;
        myDateTimeString = theDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Creates a new file record from a FileEvent object.
     *
     * @param theEvent the file event used to populate this record
     */
    public FileRecord(final FileEvent theEvent) {
        myFileName = theEvent.fileTitle();
        myExtension = theEvent.fileExtension();
        myPath = theEvent.filePath().toString();
        myEventType = theEvent.eventType();
        myDateTime = theEvent.getMyTimeStamp();
        myDateTimeString = theEvent.getTime();
    }

    /** @return the file name */
    public String getFileName() { return myFileName; }

    /** @return the file extension */
    public String getExtension() { return myExtension; }

    /** @return the file path */
    public String getPath() { return myPath; }

    /** @return the event type */
    public String getEventType() { return myEventType; }

    /** @return the date and time of the event */
    public LocalDateTime getDateTime() { return myDateTime; }

    /** @return the date and time as a formatted string */
    public String getDateTimeString() { return myDateTimeString; }

    /**
     * Returns an array containing all record values in order.
     * Useful for populating a row in a table.
     *
     * @return an array with file name, extension, path, event type, and date-time string
     */
    public Object[] getTableRow() {
        return new Object[]{getFileName(), getExtension(), getPath(), getEventType(), getDateTimeString()};
    }

    /**
     * Returns a string representation of this file record.
     * Format: fileName (extension) - eventType @ timestamp
     *
     * @return a string summary of the file record
     */
    @Override
    public String toString() {
        return String.format("%s (%s) - %s @ %s", myFileName, myExtension, myEventType, getDateTimeString());
    }

    /**
     * Generates a hash code for this file record.
     * The hash is based on file name, extension, path, event type, and timestamp.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(myFileName, myExtension, myPath, myEventType, myDateTime);
    }
}
