package model;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileEvent{
    private final WatchEvent<?> myFileEvent;
    private final Path myPath;

    private final static Map<String, Integer> myFileCounter = new ConcurrentHashMap<>(); //for thread-safe implementation
    private final String myKey;
    private final LocalDateTime myTimeStamp;

    private final static String CREATED_FILE_DIALOG = "created";
    private final static String MODIFIED_FILE_DIALOG = "modified";
    private final static String DELETED_FILE_DIALOG = "deleted";
    private final static String RENAMED_FILE_DIALOG = "renamed";

    FileEvent(final WatchEvent<?> theFileEvent, final Path thePath){
        if(theFileEvent == null){
            throw new NullPointerException("event is null.");
        }

        myFileEvent = theFileEvent;
        myPath = thePath;
        myKey = eventType() + ":" + fileTitle();
        myTimeStamp = LocalDateTime.now();

        incrementCount();
    }

    /**
     * Increments the file event to increase the count.
     */
    private void incrementCount() {
        myFileCounter.put(myKey, myFileCounter.getOrDefault(myKey, 0) + 1);
    }

    /**
     * Count of actions for file event.
     * @return the count of the file event.
     */
    public int getCount(){
        return myFileCounter.getOrDefault(myKey, 0);
    }

    Path filePath(){
        return myPath.resolve((Path) myFileEvent.context());
    }

    /**
     * File name.
     * @return string file name.
     */
    String fileName(){
        String name = fileTitle();
        int lastDot = name.lastIndexOf('.');
        if(lastDot > 0){
            return name.substring(0, lastDot);
        }
        return name;
    }

    /**
     * File name + extension.
     * @return string of fileName() + fileExtension().
     */
    String fileTitle(){
        return ((Path) myFileEvent.context()).getFileName().toString();
    }

    /**
     * Type of event as String.
     * @return file event type.
     */
    String eventType(){
        return myFileEvent.kind().name();
    }

    /**
     * File extension.
     * @return string file extension.
     */
    String fileExtension(){
        String name = fileTitle(); //((Path) myFileEvent.context()).toString();
        int lastDot = name.lastIndexOf('.');
        if(lastDot > 0 && lastDot < name.length() - 1){
            return name.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Local time stamp of the file event.
     * @return the time stamp.
     */
    LocalDateTime getMyTimeStamp(){return myTimeStamp;}

    String getTime(){
        DateTimeFormatter aFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return getMyTimeStamp().format(aFormat);
    }

    /**
     * formated string of file summary of the file event.
     * @return file event summary.
     */
    public String eventSummary(){
        /*
            format event kind into sentences:
            ENTRY_CREATE => "%fileName% was 'made' in %filepath%. This was done this many %count% times."
         */
        return String.format(
                "%s was %s in %-10s. This action was done %d times. At %s.",
                fileTitle(),
                switch (eventType()) {
                    case "ENTRY_CREATE" -> CREATED_FILE_DIALOG;
                    case "ENTRY_MODIFY" -> MODIFIED_FILE_DIALOG;
                    case "ENTRY_DELETE" -> DELETED_FILE_DIALOG ;
                    case "ENTRY_RENAME" -> RENAMED_FILE_DIALOG;
                    default -> throw new IllegalStateException("Unexpected value: " + myFileEvent.kind());
                },
                filePath(),
                getCount(),
                getTime()
        );
    }

    public String toString(){
        return myKey;
    }
}
