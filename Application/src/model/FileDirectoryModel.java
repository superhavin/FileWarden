package model;

import view.FileView;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * The model of the Application.
 */
public class FileDirectoryModel {
    /**
     * PropertyChangeSupport for the view
     */
    private final PropertyChangeSupport viewChanges = new PropertyChangeSupport(this);

    /**
     * The default directory in cases of resetting the application.
     */
    private static final String HOME_DIRECTORY = returnDefaultDirectory();;
    /**
     * Mutable string which contains the current file directory.
     */
    private final Deque<String> myFileDirectories;
    /**
     * The active monitor for our the chosen directory.
     */
    private final Map<String, FileMonitor> myMonitors;
    /**
     * Boolean for current active status.
     */
    private boolean isActive = false;
    /**
     * the current instance
     */
    private static FileDirectoryModel myInstance = null;

    public FileDirectoryModel(){
        myMonitors = new HashMap<>();
        myFileDirectories = new ArrayDeque<>();

        resetApp();
    }

    /**
     * Grabs the current fileDirectoryModel instance.
     * @return current fileDirectoryMode.
     */
    public static FileDirectoryModel getInstance() {
        if(myInstance == null){
            myInstance = new FileDirectoryModel();
        }
        return myInstance;
    }

    /**
     * Start of application.
     */
    public void startApp(){
        boolean oldActive = isActive;
        isActive = true;
        viewChanges.firePropertyChange(FileView.ACTIVE, oldActive, isActive);
    }

    /**
     * Reset condition for application.
     * Clears all the file directories,
     * Then fire a change to the View to reset all its properties,
     * Within the View change also resets the Windows and their Monitors.
     */
    public void resetApp() {
        if(!myFileDirectories.isEmpty()) {
            clearFileDirectory();
        }

        boolean oldActive = isActive;
        isActive = false;
        viewChanges.firePropertyChange(FileView.ACTIVE, oldActive, isActive);
    }

    /**
     * Helper method to add a new directory to the directory collection.
     * Fires into View the new directory.
     * @param theDirectory the new directory.
     */
    private void addDirectory(final String theDirectory) {
        String lastDirectory = myFileDirectories.isEmpty() ? HOME_DIRECTORY : myFileDirectories.getLast();

        myFileDirectories.add(theDirectory);

        final String NEW_DIRECTORY = myFileDirectories.getLast();

        viewChanges.firePropertyChange(FileView.CHANGE_DIRECTORY, lastDirectory, NEW_DIRECTORY);
    }

    /**
     * Helper method to set the clear the directory collection.
     * Fires into View a default directory
     */
    private void clearFileDirectory(){
        final String OLD_DIRECTORY = myFileDirectories.peekLast();
        myFileDirectories.clear();
        viewChanges.firePropertyChange(FileView.CHANGE_DIRECTORY, OLD_DIRECTORY, HOME_DIRECTORY);
    }

    /**
     * Starts the monitor for the directory, and connects the window to the monitor.
     * @param theDirectory the directory being monitored.
     * @param theWindows the directory's windows property change listener.
     */
    public void startMonitoring(final String theDirectory, final PropertyChangeListener theWindows){
        if(!myFileDirectories.contains(theDirectory)){ //!myMonitors.containsKey(theDirectory)
            addDirectory(theDirectory);

            FileMonitor theMonitor = new FileMonitor(theDirectory);
            theMonitor.addPropertyChangeListener(theWindows);
            myMonitors.put(theDirectory, theMonitor);
        }
    }

    /**
     * Stops a monitor then removes it from the Map of monitors.
     * Removes the directory from the collection of fileDirectories.
     * Fire to the view to remove any visuals from the old directory.
     * @param theDirectory the directory which is being removed.
     */
    public void stopMonitoring(String theDirectory) {
        FileMonitor theMonitor = myMonitors.remove(theDirectory);
        myFileDirectories.remove(theDirectory);
        if(theMonitor != null){
            theMonitor.stopMonitoring();
            viewChanges.firePropertyChange("stopDirectory", theDirectory, null);
        }
    }

    /**
     * @return active status of the application.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @return string of the home directory.
     */
    public static String getHomeDirectory(){
        return HOME_DIRECTORY;
    }

    /**
     * @param theDirectory the incoming directory.
     * @return true if the directory is contained in the file directory collection.
     */
    public boolean containsDirectory(final String theDirectory){
        return myFileDirectories.contains(theDirectory);
    }

    /**
     * @return the default home directory of the system.
     */
    private static String returnDefaultDirectory(){
        try{
            String aHomeDirectory = System.getProperty("user.home");
            if(aHomeDirectory != null && !aHomeDirectory.isBlank()){
                return aHomeDirectory;
            }

            String aOs = System.getProperty("os.name").toLowerCase();
            if(aOs.contains("win")){
                return "C:\\";
            } else if (aOs.contains("mac")) {
                return "/Users";
            } else if(aOs.contains("nix") || aOs.contains("nux")){
                return "/home";
            } else{
                return "/";
            }
        } catch (Exception theException) {
            return "/";
        }
    }

    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        viewChanges.addPropertyChangeListener(theListener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener theListener){
        viewChanges.removePropertyChangeListener(theListener);
    }
}
