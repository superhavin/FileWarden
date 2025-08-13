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

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileRecord(String fileName, String extension, String path, String eventType, LocalDateTime dateTime) {
        this.fileName = fileName;
        this.extension = extension;
        this.path = path;
        this.eventType = eventType;
        this.dateTime = dateTime;
    }

    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public String getPath() { return path; }
    public String getEventType() { return eventType; }
    public LocalDateTime getDateTime() { return dateTime; }

    public String getDateTimeString() { return dateTime.format(DISPLAY_FMT); }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s @ %s", fileName, extension, eventType, getDateTimeString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileRecord that = (FileRecord) o;
        return Objects.equals(fileName, that.fileName)
                && Objects.equals(extension, that.extension)
                && Objects.equals(path, that.path)
                && Objects.equals(eventType, that.eventType)
                && Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, extension, path, eventType, dateTime);
    }
}