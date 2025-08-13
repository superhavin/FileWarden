package model;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;

public class FileEvent{
    private final WatchEvent<?> myFileEvent;
    private final Path myPath;

    private final static Map<String, Integer> myFileCounter = new HashMap<>(); //for thread-safe implementation: ConcurrentHashMap
    private final String myKey;

    private final static String CREATED_FILE_DIALOG = "created";
    private final static String MODIFIED_FILE_DIALOG = "modified";
    private final static String DELETED_FILE_DIALOG = "deleted";

    FileEvent(WatchEvent<?> theFileEvent, Path thePath){
        if(theFileEvent == null){
            throw new NullPointerException("event is null.");
        }

        myFileEvent = theFileEvent;
        myPath = thePath;
        myKey = eventType() + ":" + fileName();

        incrementCount(eventType(), fileName());
    }

    private void incrementCount(String theKind, String theFileName) {
        //String key = theKind + ":" + theFileName;
        myFileCounter.put(myKey, myFileCounter.getOrDefault(myKey, 0) + 1);
    }

    int getCount(){
        //String key = theKind + ":" + theFileName;
        return myFileCounter.getOrDefault(myKey, 0);
    }

    Path filePath(){
        return myPath.resolve((Path) myFileEvent.context());
    }

    String fileName(){
        return ((Path) myFileEvent.context()).getFileName().toString();
    }

    String eventType(){
        return myFileEvent.kind().name();
    }

    public String eventSummary(){
        /*
            format event kind into sentences:
            ENTRY_CREATE => "%fileName% was 'made' in %filepath%. This was done this many %count% times."
         */
        return String.format(
                "%s was %s in %-10s. This action was done %d times.",
                fileName(),
                switch (eventType()) {
                    case "ENTRY_CREATE" -> CREATED_FILE_DIALOG;
                    case "ENTRY_MODIFY" -> MODIFIED_FILE_DIALOG;
                    case "ENTRY_DELETE" -> DELETED_FILE_DIALOG ;
                    default -> throw new IllegalStateException("Unexpected value: " + myFileEvent.kind());
                },
                filePath(),
                getCount()
        );
    }

    public String toString(){
        return myKey;
    }
}
