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

    private PropertyChangeSupport changes;
    /**
     *
     */
    private WatchService myWatchService = null;
    /**
     *
     */
    private String myDirectoryString;
    /**
     *
     */
    private Path myPath;
    /**
     * boolean which holds if a directory has changed.
     */
    private boolean myDirectoryChanged = false;
    /**
     *
     */
    private String myOldValue;

    FileMonitor(final PropertyChangeSupport thePropertyChange){
        changes = thePropertyChange;
        myDirectoryString = "null";
        myOldValue = null;

        try{
            myWatchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        captureDirectory(ChangeDirectoryController.returnDefaultDirectory());


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
     * immediately captured directory
     */
    private void fireDirectory(final String theNewValue){
        //check if data is null

        changes.firePropertyChange("monitorDirectory", myOldValue, theNewValue);
        myOldValue = theNewValue;
    }

    /**
     * is periodically called view changes and fire changes to view
     */
    public void monitoredDirectory(){
        //[DESIGN] Whenever a Directory has changes do...
        try {
            WatchKey key = myWatchService.take();
            for(WatchEvent<?> event : key.pollEvents()){
                String fileEvent = "Event kind: " + event.kind() + ". File affected: " + event.context();
                fireDirectory(fileEvent);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
