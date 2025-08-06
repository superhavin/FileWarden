package model;

import controller.ChangeDirectoryController;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.*;

/**
 * Class which actively monitors changes of the in the chosen Directory.
 * Lives in the model.
 */
public class FileMonitor {

    private final PropertyChangeSupport changes;
    /**
     *
     */
    private WatchService myWatchService;
    /**
     *
     */
    private String myDirectoryString;
    /**
     *
     */
    private Path myPath;
    /**
     * boolean if the directory is monitored.
     */
    private volatile boolean isMonitoringActive;
    /**
     *
     */
    private String myOldValue;
    /**
     *
     */
    private Thread monitorThread;

    public FileMonitor(final PropertyChangeSupport thePropertyChange, final String theDirectory){
        changes = thePropertyChange;
        myWatchService = null;
        myDirectoryString = "";
        myPath = null;
        isMonitoringActive = false;
        myOldValue = "";
        monitorThread = null;

        try{
            myWatchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        captureDirectory(theDirectory);
    }

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
     * sets the file Directory of
     * @param theDirectory
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
     * fired information from the captured directory
     */
    private void fireDirectory(final WatchEvent<?> event){

        String fileEvent = "Event kind: " + event.kind() + ". File affected: " + event.context();
        //add additional firing
        System.out.println(fileEvent);
        changes.firePropertyChange("monitorDirectory", myOldValue, fileEvent); //check if data is equal, does not fire
        myOldValue = fileEvent;
    }

    /**
     * is periodically called view changes and fire changes to view
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
                        fireDirectory(event);
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

    public void stopMonitoring(){
        isMonitoringActive = false;
        if(monitorThread != null){
            monitorThread.interrupt();
            System.gc(); //ensure the Thread is 'closed'
            monitorThread = null;
        }
    }

    private boolean isMonitoring(){
        return isMonitoringActive;
    }
}
