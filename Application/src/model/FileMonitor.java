package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Class which actively monitors changes of the in the chosen Directory.
 * Lives in the model.
 */
public class FileMonitor {
    private PropertyChangeSupport changes;

    /**
     * Mutable string which contains the current file directory.
     */
    private String myFileDirectory;
    /**
     * boolean which holds if a directory has changed.
     */
    private boolean hasDirectoryChanged;

    FileMonitor(final PropertyChangeSupport thePropertyChange){
        hasDirectoryChanged = false;
        changes = thePropertyChange;
    }

    /**
     * sets the file Directory of
     * @param theDirectory
     */
    public void captureDirectory(String theDirectory) {
        myFileDirectory = theDirectory;
        //[INSERT] Initialization of Directory viewing
    }

    /**
     * immediately captured directory
     */
    public void fireDirectory(){
        changes.firePropertyChange("monitorDirectory",null,null);
    }

    /**
     * is periodically called to check if the Directory has been updated
     */
    public void updateDirectory(){
        //[DESIGN] Whenever a Directory has changes do...
        if(hasDirectoryChanged){

        }
    }

    /**
     * acts on changes to the Directory and fire changes back to view
     */
    private void monitorDirectory(){
        //[DESIGN] Whenever there are new changes do...
        changes.firePropertyChange("monitorDirectory",null,null);
    }

    //Need to [DESIGN] a more specialized view for specific directory
    //This will update independent ally from the rest of the view (needs to fire propertyChange)

}
