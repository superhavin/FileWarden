package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single file event record.
 */
public class FileRecord {
    private final String fileName;
    private final String extension;
    private final String path;
    private final String eventType; // CREATED, MODIFIED, DELETED, RENAMED
    private final LocalDateTime dateTime;
    private final String dateTimeFormatted;

    public FileRecord(final String theFileName, final String theExtension, final String thePath, final String theEventType, final LocalDateTime theDateTime) {
        fileName = theFileName;
        extension = theExtension;
        path = thePath;
        eventType = theEventType;
        dateTime = theDateTime;
        dateTimeFormatted = theDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public FileRecord(final FileEvent theEvent){
        fileName = theEvent.fileTitle();
        extension = theEvent.fileExtension();
        path = theEvent.filePath().toString();
        eventType = theEvent.eventType();
        dateTime = theEvent.getMyTimeStamp();
        dateTimeFormatted = theEvent.getTime();
    }

    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public String getPath() { return path; }
    public String getEventType() { return eventType; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getDateTimeString() { return dateTimeFormatted; }

    public Object[] getTableRow() { return new Object[]{getFileName(), getExtension(), getPath(), getEventType(), getDateTimeString()}; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s @ %s", fileName, extension, eventType, getDateTimeString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, extension, path, eventType, dateTime);
    }
}