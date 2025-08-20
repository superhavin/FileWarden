package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single file event record.
 */
public class FileRecord {
    private final String myFileName;
    private final String myExtension;
    private final String myPath;
    private final String myEventType; // CREATED, MODIFIED, DELETED, RENAMED
    private final LocalDateTime myDateTime;
    private final String myDateTimeString;

    public FileRecord(final String theFileName, final String theExtension, final String thePath, final String theEventType, final LocalDateTime theDateTime) {
        myFileName = theFileName;
        myExtension = theExtension;
        myPath = thePath;
        myEventType = theEventType;
        myDateTime = theDateTime;
        myDateTimeString = theDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public FileRecord(final FileEvent theEvent){
        myFileName = theEvent.fileTitle();
        myExtension = theEvent.fileExtension();
        myPath = theEvent.filePath().toString();
        myEventType = theEvent.eventType();
        myDateTime = theEvent.getMyTimeStamp();
        myDateTimeString = theEvent.getTime();
    }

    public String getFileName() { return myFileName; }
    public String getExtension() { return myExtension; }
    public String getPath() { return myPath; }
    public String getEventType() { return myEventType; }
    public LocalDateTime getDateTime() { return myDateTime; }
    public String getDateTimeString() { return myDateTimeString; }

    public Object[] getTableRow() { return new Object[]{getFileName(), getExtension(), getPath(), getEventType(), getDateTimeString()}; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s @ %s", myFileName, myExtension, myEventType, getDateTimeString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(myFileName, myExtension, myPath, myEventType, myDateTime);
    }
}