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
     * String which displays the system information.
     */
    private String myDisplay;
    /**
     * Mutable string which contains the current file directory.
     */
    private String myFileDirectory;

    //properties of Application
    /**
     * Boolean for current active status.
     */
    private boolean isActive;
    /**
     * The current instance of my Model.
     */
    private static FileDirectoryModel myInstance =
            new FileDirectoryModel(ChangeDirectoryController.returnDefaultDirectory());
    /**
     * The active monitor for our the chosen directory
     */
    private FileMonitor myMonitor = new FileMonitor(changes);


    private FileDirectoryModel(final String theStartingDirectory){
        startApp();
        setDirectory(theStartingDirectory);
    }

    public void startApp(){
        setActive(true);
    }

    public void setDirectory(final String theDirectory) {
        String oldDirectory = myFileDirectory;
        myFileDirectory = theDirectory;

        myMonitor.captureDirectory(myFileDirectory);

        changes.firePropertyChange("setDirectory", oldDirectory, myFileDirectory);
    }

    private String getDirectory(){
        return myFileDirectory;
    }

    public static FileDirectoryModel getInstance() {
        return myInstance;
    }

    /**
     * returns directory and fires monitor to update the basic view and monitored directory
     * myFileDirectory -> myDisplayLabel; myMonitor.fireDirectory() -> myDirectoryLabel;
     * @return current file directory
     */
    public String displayDirectory(){
        myMonitor.fireDirectory();
        //[INSERT] Refine the current fileDirectory for myDisplayLabel
        return myFileDirectory;
    }

    public boolean getGameActive() {
        return isActive;
    }

    public void setActive(final boolean theGameActive){
        isActive = theGameActive;
        if(!theGameActive){
            changes.firePropertyChange("active",null,false);
        }
    }

    /**
     * once the application is active, advances time
     */
    public void advance(){
        if(isActive){
            //[INSERT] periodic checks for file watching
            myMonitor.updateDirectory();
        }
    }

    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        changes.addPropertyChangeListener(theListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener theListener){
        changes.removePropertyChangeListener(theListener);
    }
}
