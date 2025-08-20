package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.*;

/**
 * Class which actively monitors changes of the in the chosen Directory.
 * Lives in the model.
 */
public class FileMonitor {
    /**
     * PropertyChangeSupport for the instantiated windows
     */
    private final PropertyChangeSupport windowChanges = new PropertyChangeSupport(this);
    /**
     * WatchService for the monitor.
     */
    private WatchService myWatchService;
    /**
     * Path of the directory.
     */
    private Path myPath;
    /**
     * Boolean to check if the directory is monitored.
     */
    private volatile boolean isMonitoringActive;
    /**
     * The previous file event.
     */
    private FileEvent myOldFileEvent;
    /**
     * The current thread for the WatchService.
     */
    private Thread monitorThread;
    /**
     *
     */
    private RecentDelete aLastDeleted = null;
    /**
     * 500ms.
     */
    private static final int RENAME_PROCESS_TRIGGER = 500;

    /**
     * Constructor of the File Monitor.
     * @param theDirectory the directory being monitored.
     */
    public FileMonitor(final String theDirectory){
        myWatchService = null;
        myPath = null;
        isMonitoringActive = false;
        myOldFileEvent = null;
        monitorThread = null;

        try{
            myWatchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            System.exit(0);
        }

        captureDirectory(theDirectory);
        monitorDirectory();
    }

    /**
     * Attempts to register the directory.
     */
    private void registerDirectory(){
        try {
            myPath.register(myWatchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                    );
        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * Sets the captured directory, then registers it.
     * @param theDirectory the file directory.
     */
    public void captureDirectory(final String theDirectory) {
        myPath = Paths.get(theDirectory);
        registerDirectory();
    }

    /**
     * Fired fileEvent from the captured directory.
     */
    private void fireDirectory(final FileEvent theEvent){
        windowChanges.firePropertyChange("monitorDirectory", myOldFileEvent, theEvent); //check if data is equal, does not fire
        myOldFileEvent = theEvent;
    }

    private static class RecentDelete{
        final String myFileName;
        final long myTimeStamp;

        RecentDelete(final String theFileName, final long theTimestamp){
            myFileName = theFileName;
            myTimeStamp = theTimestamp;
        }
    }

    /**
     * main method which processes incoming events.
     * @param theEvent the raw event.
     * @param myPath the path of the event.
     */
    private void processEvent(final WatchEvent<?> theEvent, final Path myPath){
        WatchEvent.Kind<?> theKind = theEvent.kind();

        if(theKind == StandardWatchEventKinds.OVERFLOW){
            return;
        }

        Path theContent = (Path) theEvent.context();

        if(theKind == StandardWatchEventKinds.ENTRY_DELETE){
            aLastDeleted = new RecentDelete(theContent.toString(), System.currentTimeMillis());
        }else if(theKind == StandardWatchEventKinds.ENTRY_CREATE) {
            if(aLastDeleted != null
                    && (System.currentTimeMillis() - aLastDeleted.myTimeStamp) < RENAME_PROCESS_TRIGGER
            ){
                FileEvent aRenameEvent = new FileEvent(
                        new SimpleWatchEvent<>("ENTRY_RENAME", theContent),
                        myPath
                );

                fireDirectory(aRenameEvent);

                aLastDeleted = null;
                return;
            }
        }

        FileEvent aFileEvent = new FileEvent(new SimpleWatchEvent<>(theKind.name(), theContent), myPath);
        fireDirectory(aFileEvent);
    }

    /**
     * To start monitoring the directory.
     */
    public void monitorDirectory(){
        if(isMonitoringActive){
            return;
        }

        isMonitoringActive = true;
        monitorThread = new Thread(() -> {
            //[DESIGN] Whenever a Directory has changes do...
            while(isMonitoringActive) {
                try {
                    WatchKey key = myWatchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {

                        processEvent(event, myPath);
                    }

                    boolean valid = key.reset();

                    if (!valid) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        monitorThread.start();
    }

    /**
     * To stop monitoring the directory.
     */
    public void stopMonitoring(){
        isMonitoringActive = false;
        if(monitorThread != null){
            monitorThread.interrupt();
            System.gc(); //ensure the Thread is 'closed'
            monitorThread = null;
        }
    }

    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        windowChanges.addPropertyChangeListener(theListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener theListener){
        windowChanges.removePropertyChangeListener(theListener);
    }
}

/**
 * private helper class which allows us to make custom kind
 * @param <T>
 */
class SimpleWatchEvent<T> implements WatchEvent<T>{
    private final Kind<T> myKind;
    private final T myContext;

    public SimpleWatchEvent(final String theKindName, final T theContext){
        myKind = new Kind<T>() {
            @Override
            public String name() {
                return theKindName;
            }

            @Override
            public Class<T> type() {
                return (Class<T>) theContext.getClass();
            }
        };
        myContext = theContext;
    }


    @Override
    public Kind<T> kind() {
        return myKind;
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public T context() {
        return myContext;
    }
}