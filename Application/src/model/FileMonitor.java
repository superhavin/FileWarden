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
     * String for the directory.
     */
    private String myDirectoryString;
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
     * Constructor of the File Monitor.
     * @param theDirectory the directory being monitored.
     */
    public FileMonitor(final String theDirectory){
        myWatchService = null;
        myDirectoryString = "";
        myPath = null;
        isMonitoringActive = false;
        myOldFileEvent = null;
        monitorThread = null;

        try{
            myWatchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Sets the captured directory, then registers it.
     * @param theDirectory the file directory.
     */
    public void captureDirectory(final String theDirectory) {
        //assumes theDirectory is valid

        if(!theDirectory.equals(myDirectoryString)){ //check if the directory has changed
            myPath = Paths.get(theDirectory);
            registerDirectory();
            myDirectoryString = theDirectory;
        }
    }

    /**
     * Fired fileEvent from the captured directory.
     */
    private void fireDirectory(final FileEvent theEvent){
        //add additional firing support
        windowChanges.firePropertyChange("monitorDirectory", myOldFileEvent, theEvent); //check if data is equal, does not fire
        myOldFileEvent = theEvent;
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
                        FileEvent theEvent = new FileEvent(event, myPath);
                        fireDirectory(theEvent);
                    }

                    boolean valid = key.reset();

                    if (!valid) {
                        System.out.println("WatchKey no longer valid. Stopping monitor.");
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("Monitor Thread Stopped");
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
