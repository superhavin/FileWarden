package model;

import controller.ChangeDirectoryController;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The model of the Application.
 */
public class FileDirectoryModel {
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    //properties of View
    /**
     * Mutable string which contains the current file directory.
     */
    private String myFileDirectory = "";

    //properties of Application
    /**
     * Boolean for current active status.
     */
    private boolean isActive = false;

    /**
     * The active monitor for our the chosen directory
     */
    private FileMonitor myMonitor;

    /**
     * the current instance
     */
    private static FileDirectoryModel myInstance = null;

    public FileDirectoryModel(){
        startMonitor();
        resetApp();
    }

    private FileDirectoryModel(final String theStartingDirectory){
        this();
        changeDirectory(theStartingDirectory);
    }

    public static FileDirectoryModel getInstance() {
        if(myInstance == null){
            myInstance = new FileDirectoryModel();
        }
        return myInstance;
    }

    /**
     * Helper method to set the initial directory of the monitor
     */
    private void startMonitor(){
        String oldFileDirectory = myFileDirectory;
        myFileDirectory = ChangeDirectoryController.returnDefaultDirectory();

        myMonitor = new FileMonitor(changes, myFileDirectory);

        changes.firePropertyChange("changeDirectory", oldFileDirectory, myFileDirectory);
    }

    public void startApp(){
        boolean oldActive = isActive;
        isActive = true;
        changes.firePropertyChange("active", oldActive, isActive);
    }

    public void resetApp() {
        startMonitor();

        boolean oldActive = isActive;
        isActive = false;
        changes.firePropertyChange("active", oldActive, isActive);
    }

    /**
     * Changes the directory, and stops monitoring
     * Fires into view the new directory
     * @param theDirectory the new directory
     */
    public void changeDirectory(final String theDirectory) {
        String oldDirectory = myFileDirectory;
        myFileDirectory = theDirectory;

        myMonitor.stopMonitoring();
        myMonitor.captureDirectory(myFileDirectory);
        changes.firePropertyChange("changeDirectory", oldDirectory, myFileDirectory);
    }

    private String getDirectory(){
        return myFileDirectory;
    }

    /**
     * returns directory and fires monitor to update the basic view and monitors directory.
     * myFileDirectory -> myDisplayLabel. myMonitor.fireDirectory() -> myDirectoryLabel.
     */
    public void displayDirectory(){
        myMonitor.monitorDirectory(); //monitors the directory fires changes to view
    }

    public boolean getGameActive() {
        return isActive;
    }

    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        changes.addPropertyChangeListener(theListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener theListener){
        changes.removePropertyChangeListener(theListener);
    }
}
